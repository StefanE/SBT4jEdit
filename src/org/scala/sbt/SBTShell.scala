package org.scala.sbt

import procshell.ProcessShell.ConsoleState
import org.gjt.sp.util.Log
import procshell.ProcessShell
import projectviewer.ProjectViewer
import errorlist.{DefaultErrorSource, ErrorSource}
import console.Shell.CompletionInfo
import javax.swing.text.AttributeSet
import java.io.{InputStream, File}
import console.{ConsolePane, ConsolePlugin, Console, Output}
import java.awt.Color
import org.gjt.sp.jedit.{jEdit, GUIUtilities, View}

class SBTShell() extends ProcessShell("SBT") {
  private var view: View = null

  protected override def init(state: ConsoleState, str: String) {

    Log.log(Log.DEBUG, this, "Attempting to start Scala process")

    val project = ProjectViewer.getActiveProject(view)
    var file: File = null
    if (project == null) {
      try {
        file = new File(GUIUtilities.input(null, "info.enterProject", null))
      }
      catch {
        case e => {
          Log.log(Log.ERROR, this, e.toString)
          return
        }
      }
    }
    else {
      file = new File(project.getRootPath)
    }
    val pb = new ProcessBuilder(jEdit.getProperty("org.scala.sbt.cmd"))

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
      "test-failed", "test-quick", "test-compile", "test-javap", "test-run",
      "exit", "quit", "reload", "help", "actions",
      "current", "info", "debug", "trace on", "trace nosbt", "trace off", "trace ",
      "warn", "error", "projects", "project", "console-project")
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
  private var rdy = false
  private var parsedOut = ""
  private var outColor = Color.BLACK

  /* Not beautiful:( should be improved
  * This is a crazy method, please do not learn any from this */

  protected override def onRead(state: ConsoleState, str: String, output: Output) {
    rdy = false
    output.commandDone
    if (clearOnNextChange) {
      clearErrors()
      clearOnNextChange = false
    }

    if (str.contains("\n")) {

      tmpString += str
      //Look in clojure plugin for repl style
      Log.log(Log.DEBUG, this, "onRead#" + tmpString.mkString("#", "", "#"))
      val lines = tmpString.split('\n')
      val lastLineOK = if(str.endsWith("\n")) true else false

      val size = if (lastLineOK) lines.size else lines.size -1

      for(counter <- 0 until size) {
        val localString = lines(counter)
        if (localString.startsWith("[error]")) {
          val pattern = """\[error\](\s)(\w):([\\|\w|.]*):(\d*):(\s)([\w*|(\s)|\W]*)""".r
          val list = pattern.unapplySeq(localString).getOrElse(null)
          outColor = Colors.errorColor
          if (list != null && list.size > 5) {
            Log.log(Log.DEBUG, this, "REGISTER#" + pattern.unapplySeq(localString).get.toString)
            errorSource.addError(2, list(1) + ":" + list(2), (list(3).toInt) - 1, 0, 0, list(5))
          }
        }
        else if (localString.startsWith("[warn]")) {
          val pattern = """\[warn\](\s)(\w):([\\|\w|.]*):(\d*):(\s)([\w*|(\s)|\W]*)""".r
          val list = pattern.unapplySeq(localString).getOrElse(null)
          outColor = Colors.warningColor
          if (list != null && list.size > 5) {
            Log.log(Log.DEBUG, this, pattern.unapplySeq(localString).get.toString)
            errorSource.addError(1, list(1) + ":" + list(2), (list(3).toInt) - 1, 0, 0, list(5))
          }
        }
        else if (localString.contains("Waiting for source changes")) {
          outColor = Colors.plainColor
          clearOnNextChange = true

        }
        else if (localString.startsWith("[info]"))
        {
          outColor = Colors.infoColor
          Log.log(Log.ERROR,this,"green"+outColor.getGreen)
        }
        else
          outColor = Colors.plainColor
        //Bad practice should do from actor
        OutputWriter ! (localString+"\n",outColor)
      }

      rdy = true
      parsedOut = tmpString
      //Reset String
      if(lastLineOK)
        tmpString = ""
      else tmpString = lines.last
    }
    else if (str == "> ") {
      outColor = Colors.plainColor
      rdy = true
      parsedOut = str
      tmpString = ""
      OutputWriter ! (str,outColor)
    }
    else {
      tmpString += str
    }
  }



  override def initStreams(console: Console, state: ConsoleState) {
    new SBTProcessReader(console, state, false).start();
    new SBTProcessReader(console, state, true).start();
  }

  class SBTProcessReader(var console: Console, var state: ConsoleState, val error: Boolean) extends Thread {

    import java.awt.Color._

    var output: Output = null
    val in: InputStream = if (error) state.getErrorStream() else state.getInputStream()
    val color: Color = if (error) RED else BLACK
    var col: AttributeSet = ConsolePane.colorAttributes(outColor)

    OutputWriter.start
    OutputWriter.output = console.getOutput
    OutputWriter.col = ConsolePane.colorAttributes(outColor)

    override def run() {
      try {
        val buf = new Array[Byte](4096);
        var read = -1;
        while ( {
          read = in.read(buf);
          read
        } != -1) {
          val str = new String(buf, 0, read);
          /* Setting vars to determine color*/
          output = console.getOutput
          onRead(state, str, output);
          /*
          if (rdy) {
            col = ConsolePane.colorAttributes(outColor)
            output.writeAttrs(col, parsedOut)
          }
          */

        }
      } catch {
        case e: Exception => {
          e.printStackTrace();
          console.print(console.getInfoColor(),
            "\n" + jEdit.getProperty("msg.procshell.stopped"));
        }
      }
      finally {
        state.waiting = false;
      }
    }
  }

}

