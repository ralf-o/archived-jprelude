package org.jprelude.experimental.webui.widget

import com.vaadin.ui._

trait Tool extends Widget

case class Tools(tools: Tool*) extends Tool {
  override protected def render: Component = {
    val ret = new CssLayout
    ret addStyleName "v-component-group"

    for (tool <- tools) {
      ret addComponent tool.component
    }

    ret
  }
}



case class ToolButton(caption: String, menu: ToolMenuItems = ToolMenuItems()) extends Tool {
  override protected def render: Component = {
    if (menu.items.isEmpty) {
      val button = new Button(this.caption)
      button
    } else {
      val menuBar = new MenuBar
      val dropdown = menuBar.addItem(this.caption, null)

      def addActionItem(vaadinMenuItem: MenuBar#MenuItem, menuItem: ToolMenuItem): Unit = {
        menuItem match {
          case menu: ToolMenu => {
            val vaadinSubMenu = vaadinMenuItem.addItem(menu.caption, null)

            for (item <- menu.items.items) {
              addActionItem(vaadinSubMenu, item)
            }
          }

          case command: ToolMenuCommand => vaadinMenuItem.addItem(command.caption, null)
        }
      }

      for (item <- menu.items) {
        addActionItem(dropdown, item)
      }

      menuBar
    }
  }
}

sealed trait ToolMenuItem

case class ToolMenu(caption: String, items: ToolMenuItems = ToolMenuItems()) extends ToolMenuItem

case class ToolMenuItems(items: ToolMenuItem*)

case class ToolMenuCommand(caption: String) extends ToolMenuItem



case class ToolBar(tools: Tools) extends Widget {
  override protected def render: Component = {
    val ret = new HorizontalLayout
    ret setSpacing true

    for (tool <- this.tools.tools) {
        val component = tool.component
        ret addComponent component
        ret setComponentAlignment(component, Alignment.MIDDLE_CENTER)
    }

    ret
  }
}
