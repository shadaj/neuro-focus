seq(appbundle.settings: _*)

name := "NeuroFocus"

organization := "me.shadaj.neuro"

version := "0.1-SNAPSHOT"

libraryDependencies += "me.shadaj.neuro" %% "thinkgear" % "0.1"

libraryDependencies += "com.netflix.rxjava" % "rxjava-scala" % "0.17.1"

appbundle.icon := Some(file("icon.icns"))