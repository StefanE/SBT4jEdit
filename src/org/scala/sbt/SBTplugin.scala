package org.scala.sbt

import org.gjt.sp.jedit.{View, EBMessage, EBPlugin}

object SBTPlugin {

  val NAME = "SBTPlugin"

}

class SBTPlugin extends EBPlugin {

  override def handleMessage(message: EBMessage) {
     //hmm
  }

  override def start {
    //Dunno
  }

  override def stop {
    //Should stop sbt
  }
}