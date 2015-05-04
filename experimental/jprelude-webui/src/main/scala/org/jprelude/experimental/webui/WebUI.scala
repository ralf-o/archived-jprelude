package org.jprelude.experimental.webui

;

import com.vaadin.annotations.Theme
import com.vaadin.server.VaadinRequest
import com.vaadin.ui.UI
import org.jprelude.experimental.webui.data.PageFetchResult.Success
import org.jprelude.experimental.webui.data.{PagingPosition, PageableDatasource}
import org.jprelude.experimental.webui.widget.{TableColumn, ColumnGroup, ColumnGroups, DataNavigator}


@Theme("webui")
class WebUI extends UI {
  override def init(request: VaadinRequest) = {
    val ds = new PageableDatasource[String]((pageIdx, pageSize) => {
      new Success[String](Range(0, pageSize).map(n => String.valueOf(n + pageIdx * pageSize + 1)), PagingPosition.Position(pageIdx = pageIdx, totalItemCount = 10000, pageSize = pageSize))
    })

    this.setContent(new DataNavigator(
      datasource = ds,
      columns = ColumnGroups(
        ColumnGroup(
          title = "Meta1",
          columns = List(
            TableColumn[String](
              title = "spalte1",
              render = s => "A" + s),
            TableColumn[String](
              title = "spalte2",
              render = s => "B" + s))),
        ColumnGroup(
          title = "Meta2",
          columns = List(
            TableColumn[String](
              title = "spalte3",
              render = s => "C" + s),
            TableColumn[String](
              title = "spalte4",
              render = s => "D" + s))))
    ).render())
  }
}
 