package org.jprelude.experimental.webui.widget.form

import com.vaadin.ui._
import org.jprelude.experimental.webui.widget.{ToolBar, Widget}

class DataForm(
    title: String = "",
    toolbar: Option[ToolBar] = Option.empty,
    sections: Sections,
    margin: Boolean = true) extends Widget {

  override protected def render: Component = {
    val ret = new VerticalLayout

    val header = new HorizontalLayout
    header setSpacing true
    ret addComponent header

    val titleLabel = new Label(title)
    titleLabel.addStyleName("h3 large")
    header addComponent titleLabel
    header.setComponentAlignment(titleLabel, Alignment.MIDDLE_LEFT)

    if (this.toolbar.isDefined) {
      val component = toolbar.get.component
      header addComponent component
      header.setComponentAlignment(component, Alignment.MIDDLE_LEFT)
    }

    ret.setMargin(this.margin)

    sections.sections.foreach(section => {
      ret.addComponent(section.component)
    })

    ret
  }
}

abstract class Control(
                        caption: String) extends Widget

case class Controls(controls: Control*)


case class TextInput(caption: String) extends Control(caption) {
  def render(): Component = {
    val ret = new TextField(caption)
    ret
  }
}


sealed trait Section extends Widget

final case class Sections(sections: Section*)


final case class FieldSet(
    title: String = "",
    controls: Controls = Controls()) extends Section {

  override def render(): Component = {
    val ret = new FormLayout

    if (this.title != null && !this.title.trim().isEmpty) {
      ret.setCaption(this.title)
    }

    ret.setSizeUndefined();

    for (control <- controls.controls) {
      ret.addComponent(control.component)
    }

    return ret
  }
}

final case class FieldSets(sections: FieldSet*)

final case class TabPage(
    title: String,
    sections: Sections = Sections())

final case class TabPages(pages: TabPage*)

final case class TabBox(
    pages: TabPages) extends Section {

  override def render(): Component = {
    val ret = new TabSheet

    for (page <- this.pages.pages) {
      val vbox = new VerticalLayout
      vbox.setCaption(page.title)
      vbox.setMargin(true)

      for (section <- page.sections.sections) {
        vbox addComponent section.component
      }

      ret addComponent vbox
    }

    return ret
  }
}
