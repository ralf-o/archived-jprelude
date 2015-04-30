package org.jprelude.experimental.webui.widget

import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Component

class Flyout(content: Component) extends Widget {
  
  
  override def render(): Component = {
    val ret = new HorizontalLayout
    ret addStyleName "webui-flyout"
    ret addComponent content
    ret
  }
}