package org.scala.sbt

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.Arrays
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JColorChooser
import javax.swing.JComboBox
import javax.swing.JTextField
import org.gjt.sp.jedit.AbstractOptionPane
import org.gjt.sp.jedit.MiscUtilities
import org.gjt.sp.jedit.jEdit
import org.gjt.sp.jedit.gui.FontSelector
import console.gui.Label
import java.awt.{Component, Color}

class OptionsPane extends AbstractOptionPane("org.scala.sbt.general") {


  protected override def _init: Unit = {
    var encodings: Array[String] = MiscUtilities.getEncodings(true)
    Arrays.sort(encodings.asInstanceOf[Array[AnyRef]], new MiscUtilities.StringICaseCompare)
    plainColor = createColorButton("org.scala.sbt.plainColor")
    addComponent(jEdit.getProperty("options.org.scala.sbt.general.plainColor"),plainColor )
    infoColor = createColorButton("org.scala.sbt.infoColor")
    addComponent(jEdit.getProperty("options.org.scala.sbt.general.infoColor"),infoColor )
    warningColor = createColorButton("org.scala.sbt.warningColor")
    addComponent(jEdit.getProperty("options.org.scala.sbt.general.warningColor"),warningColor )
    errorColor = createColorButton("org.scala.sbt.errorColor")
    addComponent(jEdit.getProperty("options.org.scala.sbt.general.errorColor"),errorColor )
  }

  protected override def _save: Unit = {
    jEdit.setColorProperty("org.scala.sbt.plainColor", plainColor.getBackground)
    jEdit.setColorProperty("org.scala.sbt.infoColor", infoColor.getBackground)
    jEdit.setColorProperty("org.scala.sbt.warningColor", warningColor.getBackground)
    jEdit.setColorProperty("org.scala.sbt.errorColor", errorColor.getBackground)
  }

  private def createColorButton(property: String): JButton = {
    val b: JButton = new JButton(" ")
    b.setBackground(jEdit.getColorProperty(property))
    b.addActionListener(new ActionListener {
      def actionPerformed(evt: ActionEvent): Unit = {
        var c: Color = JColorChooser.showDialog(OptionsPane.this, jEdit.getProperty("colorChooser.title"), b.getBackground)
        if (c != null) b.setBackground(c)
      }
    })
    b.setRequestFocusEnabled(false)
    b
  }

  private var plainColor: JButton = null
  private var infoColor: JButton = null
  private var warningColor: JButton = null
  private var errorColor: JButton = null
}