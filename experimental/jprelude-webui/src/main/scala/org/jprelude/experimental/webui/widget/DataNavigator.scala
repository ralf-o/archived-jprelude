package org.jprelude.experimental.webui.widget

import com.vaadin.ui.{Alignment, Component, HorizontalLayout, VerticalLayout}
import org.jprelude.experimental.webui.data.PagingPosition

class DataNavigator extends Widget {
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

    val paginator = new Pagination {
      position = PagingPosition.Position(29, 1500, 50)
    }


    val pageSizeSelector = new Pagination {
      viewType = Pagination.ViewType.PageSizeSelector
      position = PagingPosition.Position(29, 1500, 50)
    }

    val pageInfo = new Pagination {
      viewType = Pagination.ViewType.PageInfo
      position = PagingPosition.Position(29, 1500, 50)
    }

    ret addComponent paginator.getComponent
    ret addComponent pageSizeSelector.getComponent
    ret addComponent pageInfo.getComponent

    ret.setComponentAlignment(ret.getComponent(0), Alignment.MIDDLE_LEFT)
    ret.setComponentAlignment(ret.getComponent(1), Alignment.MIDDLE_CENTER)
    ret.setComponentAlignment(ret.getComponent(2), Alignment.MIDDLE_RIGHT)

    ret
  }
}
