package org.jprelude.experimental.webui.widget

import com.vaadin.ui._
import org.jprelude.experimental.webui.data.{PageableDatasource, PagingPosition}
import rx.lang.scala.Observable

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

    val dataTable = new DataTable[T](
      columns = columns,
      selectionMode = this.selectionMode,
      dataEvents = dataSource.dataEvents,
      onSelection = println(_)

    )

    val dataTableComponent = dataTable.render
    //val toolBar = new ToolBar
    //content addComponent (toolBar.render)
    content addComponent this.renderToolBar

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

    ret addComponent paginator.getComponent
    ret addComponent pageSizeSelector.getComponent
    ret addComponent pageInfo.getComponent

    ret.setComponentAlignment(ret.getComponent(0), Alignment.MIDDLE_LEFT)
    ret.setComponentAlignment(ret.getComponent(1), Alignment.MIDDLE_CENTER)
    ret.setComponentAlignment(ret.getComponent(2), Alignment.MIDDLE_RIGHT)

    ret
  }

  private def renderToolBar: Component = {
    val ret = new HorizontalLayout
    ret setSpacing true

    val groups: Seq[Actions[T]]  = this.actions match {
      case groups: GroupedActions[T] => groups.groups
      case actions: Actions[T] => Seq(actions)
    }

    for (group <- groups) {
      val actions = group.visibleActions.toList

      actions.length match {
        case 0 => {
        }

        case 1 => {
          ret addComponent this.renderActionComponent(actions(0))
        }

        case _ => {
          val layout = new CssLayout
          layout addStyleName "v-component-group";
          ret addComponent layout

          for (action <- group.actions) {
            layout addComponent this.renderActionComponent(action)
          }
        }
      }
    }

    ret
  }

  private def renderActionComponent(action: Action[T]): Component = {
    assert(action != null)

    val ret = action match {
      case action: GeneralAction => new Button(action.caption)
      case action: SingleSelectAction[T] => new Button(action.caption)
      case action: MultiSelectAction[T] => new Button(action.caption)

      case action: ActionMenu[T] => {
        val menu = new MenuBar
        val dropdown = menu.addItem(action.caption, null)

        if (action.icon.isDefined) {
          dropdown setIcon action.icon.get
        }

        def addActionItem(item: MenuBar#MenuItem, action: Action[T]): Unit = {
          action match {
            case actionMenu: ActionMenu[T] => {
              val subMenu = item.addItem(actionMenu.caption, null);

              if (action.icon.isDefined) {
                subMenu setIcon action.icon.get
              }

              for (subAction <- actionMenu.actions.actions) {
                addActionItem(subMenu, subAction)
              }
            }

            case _ => item.addItem(action.caption, null)
          }
        }

        for (subAction <- action.actions.actions) {
          addActionItem(dropdown, subAction)
        }

        menu
      }
    }

    action match {
      case action: ActionMenu[T] => {
      }
      case _ => {
        if (action.icon.isDefined) {
          ret setIcon action.icon.get
        }
      }
    }

    ret
  }
}
