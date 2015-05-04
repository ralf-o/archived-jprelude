package org.jprelude.experimental.webui.widget

import com.vaadin.ui.{Alignment, Component, HorizontalLayout, VerticalLayout}
import org.jprelude.experimental.webui.data.{PageableDatasource, PagingPosition}
import rx.lang.scala.Observable

class DataNavigator[T](datasource: PageableDatasource[T], columns: ColumnType[T]) extends Widget {
  override def render(): Component = {
    val ret = new VerticalLayout


    val masterContent = new HorizontalLayout
    ret addComponent masterContent
    masterContent.setSizeFull()
    masterContent addStyleName "masterview"

    val content = new VerticalLayout

    masterContent addComponent ((new Sidebar).render)
    masterContent addComponent content
    content.setSizeFull()
    content.setSpacing(true)
    content.setMargin(true)
    masterContent.setExpandRatio(content, 1)

    val dataTable = new DataTable[T](
      columns = columns,
      dataEvents = datasource.dataEvents
    )

    val dataTableComponent = dataTable.render
    val toolBar = new ToolBar
    content addComponent (toolBar.render)
    content addComponent dataTableComponent
    content addComponent this.renderNavigationBar()
    content.setExpandRatio(dataTableComponent, 1)


    ret
  }

  private def renderNavigationBar(): Component = {
    val ret = new HorizontalLayout
    ret setWidth "100%"


    datasource.loadPage(0, 25)

    val positionEvents: Observable[PagingPosition] = datasource.positionEvents
positionEvents.subscribe(println(_))
    val paginator = new Pagination(
      viewType = Pagination.ViewType.Paginator,
      positionEvents = positionEvents,
      onMoveToPageRequest = n => {println ("=> " + n)
        datasource.moveToPage(n)
      }
    )

    val pageSizeSelector = new Pagination(
      viewType = Pagination.ViewType.PageSizeSelector,
      positionEvents = positionEvents,
      onPageSizeChangeRequest = pageSize => datasource.loadPage(0, pageSize)
    )

    val pageInfo = new Pagination(
      viewType = Pagination.ViewType.PageInfo,
      positionEvents = positionEvents
    )

    ret addComponent paginator.getComponent
    ret addComponent pageSizeSelector.getComponent
    ret addComponent pageInfo.getComponent

    ret.setComponentAlignment(ret.getComponent(0), Alignment.MIDDLE_LEFT)
    ret.setComponentAlignment(ret.getComponent(1), Alignment.MIDDLE_CENTER)
    ret.setComponentAlignment(ret.getComponent(2), Alignment.MIDDLE_RIGHT)

    ret
  }
}
