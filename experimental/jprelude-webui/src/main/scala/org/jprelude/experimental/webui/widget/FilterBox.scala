package org.jprelude.experimental.webui.widget

import com.vaadin.ui.Component
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.TextField
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.DateField
import com.vaadin.ui.Label
import com.vaadin.ui.FormLayout
import com.vaadin.ui.Alignment
import com.vaadin.server.FontAwesome

class FilterBox(
    filters: List[Filter]
  ) extends Widget {

  def render() = {
    val content = new VerticalLayout
    
    for (filter <- this.filters) {
      content.addComponent(filter.getFilterComponent())
    }

    content
  }
}

abstract class Filter(
    val label: String
    ) {

  def getFilterComponent(): Component
}

class TextFilter(
    override val label: String
    ) extends Filter(label) {
  
  override def getFilterComponent(): Component = {
    val ret = new TextField()
    ret setWidth "300px"
    ret.setCaption(label)
    
    ret
  }
}

class DateRangeFilter(
    override val label: String
    ) extends Filter(label) {
  
  override def getFilterComponent(): Component = {
    val ret = new HorizontalLayout
    val datDateFrom = new DateField()
    datDateFrom setWidth "140px"
    val datDateTo =  new DateField()
    datDateTo setWidth "140px"
    
    val labSeparator = new Label("-")
    labSeparator setWidth "20px"
    labSeparator addStyleName "webui-center"
    
    ret addComponent datDateFrom
    ret addComponent labSeparator
    ret setComponentAlignment (labSeparator, Alignment.MIDDLE_CENTER)
    ret addComponent datDateTo
    
    ret.setCaption(this.label)
    return ret
  }
}