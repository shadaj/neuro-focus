package me.shadaj.neuro

import me.shadaj.neuro.thinkgear.{ESense, NeuroObserver, PoorSignalLevel, EEG}

import sys.process.stringToProcess

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import javax.swing.ImageIcon
import java.net.URL
import java.awt.{PopupMenu, TrayIcon, SystemTray, MenuItem}
import java.awt.event.{ActionListener, ActionEvent}
import java.applet.Applet

object Focus extends App {
  var recording = false
  val observer = NeuroObserver()

  var previousValues = Seq.fill(10)(100)

  var lastMessage = 0L

  val BAD_THRESHOLD = 33
  val OKAY_THRESHOLD = 66

  val glass = Applet.newAudioClip(new URL("file:///System/Library/Sounds/Glass.aiff"))
  val popup = new PopupMenu
  val greenImage = new ImageIcon(getClass.getResource("/green.png"), "").getImage
  val yellowImage = new ImageIcon(getClass.getResource("/yellow.png"), "").getImage
  val redImage = new ImageIcon(getClass.getResource("/red.png"), "").getImage
  val blackImage = new ImageIcon(getClass.getResource("/black.png"), "").getImage
  val trayIcon = new TrayIcon(blackImage)
  val tray = SystemTray.getSystemTray

  val startFocus = new MenuItem("Start Focusing")

  startFocus.addActionListener {
    new ActionListener {
      override def actionPerformed(e: ActionEvent) {
        if (startFocus.getLabel == "Start Focusing") {
          startFocus.setLabel("Stop Focusing")
          recording = true
        } else {
          trayIcon.setImage(blackImage)
          averageAttention.setLabel("Average Attention: Unknown")
          signalQuality.setLabel("Signal Quality: Unknown")
          startFocus.setLabel("Start Focusing")
          recording = false
        }
      }
    }
  }

  val averageAttention = new MenuItem("Average Attention: Unknown")
  averageAttention.setEnabled(false)
  
  val signalQuality = new MenuItem("Signal Quality: Unknown")
  signalQuality.setEnabled(false)
  
  val quit = new MenuItem("Quit")
  quit.addActionListener {
    new ActionListener {
      override def actionPerformed(e: ActionEvent) {
        System.exit(0)
      }
    }
  }
  popup.add(startFocus)
  popup.add(averageAttention)
  popup.add(signalQuality)
  popup.add(quit)

  trayIcon.setPopupMenu(popup)
  tray.add(trayIcon)

  val messageGap = 10000

  observer.subscribe { data =>
    if (recording) {
      val currentTime = System.currentTimeMillis
      data match {
        case PoorSignalLevel(level) => {
          signalQuality.setLabel(s"Signal Quality: $level")
          trayIcon.setImage(blackImage)
          if ((currentTime - lastMessage) >= messageGap) {
            Future("say Please check your headset".!)
            lastMessage = currentTime
          }
        }

        case EEG(ESense(attention, _), _, PoorSignalLevel(level)) => {
          signalQuality.setLabel(s"Signal Quality: $level")
          if (level > 0) {
            trayIcon.setImage(blackImage)
            if ((currentTime - lastMessage) >= messageGap) {
              Future("say Please check your headset".!)
              lastMessage = currentTime
            }
          } else {
            previousValues = previousValues.tail :+ attention
            val average = previousValues.sum / previousValues.length
            averageAttention.setLabel(s"Average Attention: $average")

            if (average <= BAD_THRESHOLD) {
              trayIcon.setImage(redImage)
              if ((currentTime - lastMessage) >= messageGap) {
                Future(glass.play())
                lastMessage = currentTime
              }
            } else if (average <= OKAY_THRESHOLD) {
              trayIcon.setImage(yellowImage)
              lastMessage = currentTime
            } else {
              trayIcon.setImage(greenImage)
            }
          }
        }

        case _ =>
      }
    }
  }
}