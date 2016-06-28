lazy val dataclasses = project in file(".")

organization in Global := "com.dwijnand"
     version in Global := "1.0.0-SNAPSHOT"
    licences in Global := Seq(Apache2)
   startYear in Global := Some(2016)
 description in Global := "A code generation library and sbt autoplugin growable and bincompat-friendly datatypes"
  developers in Global := List(Developer("dwijnand", "Dale Wijnand", "dale wijnand gmail com", url("https://dwijnand.com")))
     scmInfo in Global := Some(ScmInfo(url("https://github.com/dwijnand/dataclasses"), "scm:git:git@github.com:dwijnand/dataclasses.git"))

scala211 in Global := "2.11.8"

      scalaVersion := scala211.value
crossScalaVersions := Seq(scala211.value)

       maxErrors := 15
triggeredMessage := Watched.clearWhenTriggered

scalacOptions ++= "-encoding utf8"
scalacOptions ++= "-deprecation -feature -unchecked -Xlint"
scalacOptions  += "-language:experimental.macros"
scalacOptions  += "-language:higherKinds"
scalacOptions  += "-language:implicitConversions"
scalacOptions  += "-language:postfixOps"
scalacOptions  += "-Xfuture"
scalacOptions  += "-Yinline-warnings"
scalacOptions  += "-Yno-adapted-args"
scalacOptions  += "-Ywarn-dead-code"
scalacOptions  += "-Ywarn-numeric-widen"
scalacOptions  += "-Ywarn-unused"
scalacOptions  += "-Ywarn-unused-import"
scalacOptions  += "-Ywarn-value-discard"

scalacOptions in (Compile, console) -= "-Ywarn-unused-import"
scalacOptions in (Test,    console) -= "-Ywarn-unused-import"

parallelExecution in Test := false // so printlns don't interwine

         fork in run := true
cancelable in Global := true

libraryDependencies += "org.scalameta" %% "scalameta" % "1.0.0"

noDocs
noArtifacts // for now
