package org.scala.sbt

import procshell.ProcessShell.ConsoleState
import org.gjt.sp.util.Log
import procshell.ProcessShell
import projectviewer.ProjectViewer
import java.io.File
import errorlist.{DefaultErrorSource, ErrorSource}
import org.gjt.sp.jedit.{GUIUtilities, View}
import console.Shell.CompletionInfo
import console.{ConsolePlugin, Console, Output}

class SBTShell() extends ProcessShell("SBT") {
  private var view: View = null


  /*
  def completion() = {
    val console = ConsolePlugin.getConsole(view)
    val text = console.getConsolePane

  }
  */

  protected override def init(state: ConsoleState, str: String) {

    Log.log(Log.DEBUG, this, "Attempting to start Scala process")
    //this.consoleStateMap.entrySet.toArray.foreach(x => println(x))
    //this.getCompletions()

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

  override def getCompletions(console: Console, command: String): CompletionInfo = {

    /* Temporary until this is redirected to SBT, this is not a complete list*/
    val completion = new CompletionInfo
    completion.completions = Array("compile", "console",
      "clean", "clean-cache", "clean-lib", "clean-plugins",
      "console-quick",
      "doc", "doc-test", "doc-all",
      "jetty", "jetty-run", "jetty-stop",
      "update", "prepare-webap", "help", "test", "console", "console-quick",
      "exec", "graph-src", "graph-pkg",
      "javap", "test-javap",
      "package", "package-test", "package-docs", "package-all", "package-project",
      "package-all", "package-project", "package-src", "package-test-src",
      "run", "sh",
      "test-failed", "test-quick", "test-compile", "test-javap","test-run",
      "exit","quit","reload","help","actions",
      "current","info","debug", "trace on","trace nosbt","trace off","trace ",
      "warn","error","projects","project","console-project")
    completion.completions = completion.completions.filter(str => str.startsWith(command))
    completion
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