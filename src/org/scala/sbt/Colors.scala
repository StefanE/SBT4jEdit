package org.scala.sbt

import org.gjt.sp.jedit.jEdit

object Colors {
  def bgColor = jEdit.getColorProperty("org.scala.sbt.bgColor")
  def plainColor = jEdit.getColorProperty("org.scala.sbt.plainColor")
  def caretColor = jEdit.getColorProperty("org.scala.sbt.caretColor")
  def infoColor =  jEdit.getColorProperty("org.scala.sbt.infoColor")
  def warningColor = jEdit.getColorProperty("org.scala.sbt.warningColor")
  def errorColor = jEdit.getColorProperty("org.scala.sbt.errorColor")
}