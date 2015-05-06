package org.jprelude.experimental.webui.widget

import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import com.vaadin.ui._
import rx.lang.scala.subjects.BehaviorSubject
import rx.lang.scala.{Observable, Subject}

class ActionBar[T](
    actions: ActionsDef[T],
    selectionEvents: Observable[Seq[T]],
    size: Size.Value = Size.Normal,
    buttonStyle: ButtonStyle.Value = ButtonStyle.Normal) extends Widget {

  require(actions != null)
  require(selectionEvents != null)

  private val selectionSubject: Subject[Seq[T]] = BehaviorSubject(Seq.empty)
  private var selection: Seq[T] = Seq.empty

  selectionEvents.subscribe(selectionSubject)
  selectionEvents.subscribe(selection = _)

  override protected def render: Component = {
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

          if (this.buttonStyle == ButtonStyle.Normal) {
            layout addStyleName "v-component-group";
          }

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
      case action: GeneralAction => {
        val button = new Button(action.caption)

        button.addClickListener(new ClickListener() {
          override def buttonClick(clickEvent: ClickEvent): Unit = action.command()
        })

        button
      }

      case action: SingleSelectAction[T] => {
        val button = new Button(action.caption)

        this.selectionSubject.subscribe(_.length match {
          case 1 => button.setEnabled(true)
          case _ => button.setEnabled(false)
        })

        button.addClickListener(new ClickListener() {
          override def buttonClick(clickEvent: ClickEvent): Unit = action.command(selection(0))
        })

        button
      }

      case action: MultiSelectAction[T] => {
        val button = new Button(action.caption)

        this.selectionSubject.subscribe(_.length match {
          case 0 => button.setEnabled(false)
          case _ => button.setEnabled(true)
        })

        button.addClickListener(new ClickListener() {
          override def buttonClick(clickEvent: ClickEvent): Unit = action.command(selection)
        })

        button
      }

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
      case action: Action[T] => {
        if (action.icon.isDefined) {
          ret setIcon action.icon.get
        }
      }
    }

    if (this.buttonStyle == ButtonStyle.Borderless || (this.buttonStyle == ButtonStyle.Link && action.isInstanceOf[ActionMenu[T]])) {
      ret setStyleName "borderless"
    } else if (this.buttonStyle == ButtonStyle.Link) {
      ret setStyleName "link"
    }

    this.size match {
      case Size.Large => ret setStyleName "large"
      case Size.Small => ret setStyleName "small"
      case _ => {}
    }

    ret
  }
}

