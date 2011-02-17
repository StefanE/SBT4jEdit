package org.scala.sbt

import actors.Actor
import javax.swing.text.AttributeSet
import java.awt.Color
import console.{Output, ConsolePane}

object OutputWriter extends Actor {

  var col: AttributeSet = null
  var output: Output = null

  def act {
    loop {
      react {
        case (str: String, color: Color) => {
            col = ConsolePane.colorAttributes(color)
            output.writeAttrs(col, str)
        }
        case other => {
          println("OTHER" + other)
        }
      }
    }
  }
}