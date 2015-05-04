package org.jprelude.experimental.webui;

import com.vaadin.annotations.Theme
import com.vaadin.server.VaadinRequest
import com.vaadin.ui.UI
import org.jprelude.experimental.webui.data.PageFetchResult.Success
import org.jprelude.experimental.webui.data.{PagingPosition, PageableDatasource}
import org.jprelude.experimental.webui.widget.DataNavigator


@Theme("webui")
class WebUI extends UI {
  override def init(request: VaadinRequest) = {
    val ds = new PageableDatasource[String]((pageIdx, pageSize) => {
      new Success[String](Range(0, pageSize).map(n => String.valueOf(n + pageIdx *  pageSize + 1)), PagingPosition.Position(pageIdx = pageIdx, totalItemCount = 10000, pageSize = pageSize))
    })


    this.setContent(new DataNavigator(datasource = ds).render())
  }
}
 