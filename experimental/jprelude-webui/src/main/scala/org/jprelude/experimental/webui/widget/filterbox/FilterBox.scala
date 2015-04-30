package org.jprelude.experimental.webui.widget.filterbox

import org.jprelude.experimental.webui.widget.Widget
import scala.collection.immutable.Stream
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.Component
import com.vaadin.ui.Label

class FilterBox(
    sections: List[FilterSection]
    ) extends Widget {
  
  def render: Component = {
    val content = new VerticalLayout
    
    for (section <- sections) {
      val labSectionHeader = new Label(section.title)
      labSectionHeader addStyleName "h2"

      content addComponent labSectionHeader
    }
    
    content
  }
}