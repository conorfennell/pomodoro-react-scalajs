enablePlugins(ScalaJSPlugin)

scalaJSUseMainModuleInitializer := true
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % "1.1.1"
libraryDependencies += "io.suzaku" %%% "diode" % "1.1.2"

jsDependencies ++= Seq(

  "org.webjars.bower" % "react" % "15.6.1"
    /        "react-with-addons.js"
    minified "react-with-addons.min.js"
    commonJSName "React",

  "org.webjars.bower" % "react" % "15.6.1"
    /         "react-dom.js"
    minified  "react-dom.min.js"
    dependsOn "react-with-addons.js"
    commonJSName "ReactDOM",

  "org.webjars.bower" % "react" % "15.6.1"
    /         "react-dom-server.js"
    minified  "react-dom-server.min.js"
    dependsOn "react-dom.js"
    commonJSName "ReactDOMServer")