package org.jprelude.experimental.webui;

import java.util.Date
import org.jprelude.experimental.webui.widget.ColumnGroup
import org.jprelude.experimental.webui.widget.ColumnGroups
import org.jprelude.experimental.webui.widget.DataTable
import org.jprelude.experimental.webui.widget.Paginator
import org.jprelude.experimental.webui.widget.TableColumn
import com.vaadin.annotations.Theme
import com.vaadin.server.VaadinRequest
import com.vaadin.ui.Button
import com.vaadin.ui.Button.ClickEvent
import com.vaadin.ui.Button.ClickListener
import com.vaadin.ui.Label
import com.vaadin.ui.Notification
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout
import org.jprelude.experimental.webui.widget.ColumnTypeImplicits._
import com.vaadin.ui.TextField
import com.vaadin.ui.themes.ValoTheme
import com.vaadin.server.FontAwesome
import com.vaadin.ui.Alignment
import org.jprelude.experimental.webui.widget.ToolBar
import org.jprelude.experimental.webui.widget.ToolBar


@Theme("webui")
class WebUI extends UI {
  override def init(request: VaadinRequest) = {
    val content: VerticalLayout = new VerticalLayout
    this.setContent(content)
    content.setSizeFull()
    content.setSpacing(true)
    content.setMargin(true)

   

    
    val dataTable = DataTable(
        columns = ColumnGroups(
            ColumnGroup(
                title = "Meta1",
                columns = List(
                  TableColumn(
                      title = "spalte1"),
                  TableColumn(
                      title = "spalte2"))),
            ColumnGroup(
               title = "Meta2",
               columns = List(
                 TableColumn(
                   title = "spalte3"),
                 TableColumn(
                   title = "spalte4")))))
    
     val dataTableComponent = dataTable.render
     val paginator = new Paginator
     val toolBar = new ToolBar
        content addComponent (toolBar.render)
    content addComponent dataTableComponent
    content addComponent paginator.render
  content.setExpandRatio(dataTableComponent, 1)
    }
}
 