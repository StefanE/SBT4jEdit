package org.scala.sbt

import org.gjt.sp.jedit.jEdit

object Colors {
  def plainColor = jEdit.getColorProperty("org.scala.sbt.plainColor")
  def infoColor =  jEdit.getColorProperty("org.scala.sbt.infoColor")
  def warningColor = jEdit.getColorProperty("org.scala.sbt.warningColor")
  def errorColor = jEdit.getColorProperty("org.scala.sbt.errorColor")
}