name := "jprelude-webui"
version := "0.1" 
scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
   "com.vaadin" % "vaadin-server" % "7.4.5",
   "com.vaadin" % "vaadin-client-compiled" % "7.4.5",
   "com.vaadin" % "vaadin-themes" % "7.4.5",
   "io.reactivex" %% "rxscala" % "0.24.1"
)

jetty(port = 8081)
