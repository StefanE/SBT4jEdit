package org.scala.sbt

import procshell.ProcessShell.ConsoleState
import org.gjt.sp.util.Log
import procshell.ProcessShell
import projectviewer.ProjectViewer
import java.io.File
import console.Output
import errorlist.{DefaultErrorSource, ErrorSource}
import org.gjt.sp.jedit.{GUIUtilities, View}

class SBTShell() extends ProcessShell("SBT") {
  private var view: View = _

  protected override def init(state: ConsoleState, str: String) {
    Log.log(Log.DEBUG, this, "Attempting to start Scala process")

    val project = ProjectViewer.getActiveProject(view)
    var file: File = null
    if (project == null) {
      file = new File(GUIUtilities.input(null, "info.enterProject", null))
      /* TODO: Error handling if no active project*/
    }
    else {
      file = new File(project.getRootPath)
    }
    val pb = new ProcessBuilder("sbt.bat")

    pb.directory(file)

    state.p = pb.start();
    Log.log(Log.DEBUG, this, "Scala started.");
  }

  private def getScalaJars() = {
    val path = System.getProperty("user.home") + "\\.jedit\\jars\\"
    val lib = path + "scala-library.jar"
    val compiler = path + "scala-compiler.jar"
    lib + ";" + compiler
  }

  def setView(newView: View) {
    view = newView
  }

  protected override def onWrite(state: ConsoleState, str: String) = {
    clearErrors
    str
  }

  private def clearErrors() {
    Log.log(Log.ERROR, this, "ClearCalled")
    val arr = ErrorSource.getErrorSources
    arr.foreach(e => {
      ErrorSource.unregisterErrorSource(e)
    })
    errorSource = new DefaultErrorSource("SBT")
    ErrorSource.registerErrorSource(errorSource)
  }

  private var errorSource = new DefaultErrorSource("SBT")
  private var tmpString = ""
  private var clearOnNextChange = false
  /* Not beautiful:( shoud be improved*/
  protected override def onRead(state: ConsoleState, str: String, output: Output) {
    if (clearOnNextChange) {
      clearErrors()
      clearOnNextChange = false
    }
    //Look in clojure plugin for repl style
    Log.log(Log.DEBUG, this, "onRead#" + str.mkString("#", "", "#"))
    if (str.contains("\n")) {
      tmpString += str
      val lines = tmpString.split('\n')
      lines.foreach(line => {
        val localString = line.dropWhile(ch => ch == '\n')
        if (localString.startsWith("[error]")) {
          val pattern = """\[error\](\s)(\w):([\\|\w|.]*):(\d*):(\s)([\w*|(\s)|\W]*)""".r
          val list = pattern.unapplySeq(localString).getOrElse(null)
          if (list != null && list.size > 5) {
            Log.log(Log.DEBUG, this, "REGISTER#" + pattern.unapplySeq(localString).get.toString)
            errorSource.addError(2, list(1) + ":" + list(2), (list(3).toInt) - 1, 0, 0, list(5))
          }
        }
        else if (localString.startsWith("[warn]")) {
          val pattern = """\[warn\](\s)(\w):([\\|\w|.]*):(\d*):(\s)([\w*|(\s)|\W]*)""".r
          val list = pattern.unapplySeq(localString).getOrElse(null)
          if (list != null && list.size > 5) {
            Log.log(Log.DEBUG, this, pattern.unapplySeq(localString).get.toString)
            errorSource.addError(1, list(1) + ":" + list(2), (list(3).toInt) - 1, 0, 0, list(5))
          }
        }
        else if (localString.contains("Waiting for source changes")) {
          clearOnNextChange = true

        }
      })
      //Reset String
      tmpString = ""
    }
    else {
      tmpString += str
    }
  }
}