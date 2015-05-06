package org.jprelude.experimental.webui.widget.filterbox

import com.vaadin.ui.TextField

abstract class TextFilter(
    label: String) extends FilterField(label) {

    def getFilterComponent = {
      val ret = new TextField()
      
      ret
    } 
}