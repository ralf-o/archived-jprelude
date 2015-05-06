package org.jprelude.experimental.webui.widget.filterbox

import com.vaadin.ui.Component

abstract class FilterField (
    val label: String) {
 
  def getAllowedOperators
  def getFilterComponent: Component
}