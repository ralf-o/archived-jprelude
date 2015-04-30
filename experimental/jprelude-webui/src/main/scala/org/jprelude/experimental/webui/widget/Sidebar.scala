package org.jprelude.experimental.webui.widget

import org.jprelude.experimental.webui.widget.filterbox.FilterBox
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.themes.ValoTheme
import org.jprelude.experimental.webui.widget.filterbox.FilterSection

class Sidebar extends Widget {
  def render() = {
    val content = new VerticalLayout
    content setWidth "270px"
    content addStyleName ValoTheme.UI_WITH_MENU
        
    content
  }
}
