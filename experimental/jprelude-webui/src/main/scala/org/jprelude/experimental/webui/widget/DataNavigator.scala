package org.jprelude.experimental.webui.widget

import com.vaadin.ui._
import org.jprelude.experimental.webui.data.{PageableDatasource, PagingPosition}
import rx.lang.scala.Observable
import rx.lang.scala.subjects.BehaviorSubject

class DataNavigator[T](
      dataSource: PageableDatasource[T],
      columns: ColumnType[T],
      actions: ActionsDef[T] = Actions[T]()
    ) extends Widget {

  private val selectionMode: SelectionMode.Value = {
    def determineSelectionMode(actions: Seq[Action[T]]) = {
      actions
        .flatMap(_.visibleActions)
        .map(_ match {
          case action: SingleSelectAction[T] => SelectionMode.Single
          case action: MultiSelectAction[T] => SelectionMode.Multi
          case _ => SelectionMode.None
        })
        //.takeWhile(_ != SelectionMode.Multi)
        .foldLeft(SelectionMode.None)((mode1, mode2) => {println(mode1, mode2);if (mode1.id > mode2.id) mode1 else mode2})// TODO ?
    }

    determineSelectionMode(actions.visibleActions)
  }

  override def render(): Component = {
    val ret = new VerticalLayout


    val masterContent = new HorizontalLayout
    ret addComponent masterContent
    masterContent.setSizeFull()
    masterContent addStyleName "masterview"

    val content = new VerticalLayout

    masterContent addComponent content
    content.setSizeFull()
    content.setSpacing(true)
    content.setMargin(true)
    masterContent.setExpandRatio(content, 1)

    val selectionSubject = BehaviorSubject[Seq[T]](Seq.empty)

    val dataTable = new DataTable[T](
      columns = columns,
      selectionMode = this.selectionMode,
      dataEvents = dataSource.dataEvents,
      onSelection = selection => selectionSubject.onNext(selection)

    )

    val dataTableComponent = dataTable.render

    content addComponent new ActionBar(
      actions = this.actions,
      size = Size.Small,
      selectionEvents = selectionSubject).component

    content addComponent dataTableComponent
    content addComponent this.renderNavigationBar
    content.setExpandRatio(dataTableComponent, 1)

    ret
  }

  private def renderNavigationBar(): Component = {
    val ret = new HorizontalLayout
    ret setWidth "100%"


    dataSource.loadPage(0, 25)

    val positionEvents: Observable[PagingPosition] = dataSource.positionEvents

    val paginator = new Pagination(
      viewType = Pagination.ViewType.Paginator,
      positionEvents = positionEvents,
      onMoveToPageRequest = n => {println ("=> " + n)
        dataSource.moveToPage(n)
      }
    )

    val pageSizeSelector = new Pagination(
      viewType = Pagination.ViewType.PageSizeSelector,
      positionEvents = positionEvents,
      onPageSizeChangeRequest = pageSize => dataSource.loadPage(0, pageSize)
    )

    val pageInfo = new Pagination(
      viewType = Pagination.ViewType.PageInfo,
      positionEvents = positionEvents
    )

    ret addComponent paginator.component
    ret addComponent pageSizeSelector.component
    ret addComponent pageInfo.component

    ret.setComponentAlignment(ret.getComponent(0), Alignment.MIDDLE_LEFT)
    ret.setComponentAlignment(ret.getComponent(1), Alignment.MIDDLE_CENTER)
    ret.setComponentAlignment(ret.getComponent(2), Alignment.MIDDLE_RIGHT)

    ret
  }
}
