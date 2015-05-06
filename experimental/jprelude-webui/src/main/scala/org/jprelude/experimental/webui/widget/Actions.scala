package org.jprelude.experimental.webui.widget

import com.vaadin.server.Resource


sealed trait Action[-T] {
  def caption: String
  def icon: Option[Resource]
  def visible: Boolean
  def visibleActions: Seq[Action[T]]
}

case class SingleSelectAction[-T](
    caption: String,
    icon: Option[Resource] = Option.empty,
    command: T => Unit = (item: T) => {},
    visible: Boolean = true) extends Action[T] {

  def visibleActions = if (this.visible) Seq(this) else Seq.empty

  assert(icon != null)
  assert(command !=  null)
}

case class MultiSelectAction[-T](
    caption: String,
    icon: Option[Resource] = Option.empty,
    command: Seq[T] => Unit = (item: Seq[T]) => {},
    visible: Boolean = true) extends Action[T] {

  def visibleActions = if (this.visible) Seq(this) else Seq.empty

  assert(icon != null)
  assert(command !=  null)
}

case class GeneralAction(
    caption: String,
    icon: Option[Resource] = Option.empty,
    command: () => Unit = () => {},
    visible: Boolean = true) extends Action[Any] {

  def visibleActions = if (this.visible) Seq(this) else Seq.empty

  assert(icon != null)
  assert(command !=  null)
}

case class ActionMenu[-T](
    caption: String,
    icon: Option[Resource] = Option.empty,
    actions: Actions[T],
    visible: Boolean = true) extends Action[T] {

  def visibleActions: Seq[Action[T]] = actions.actions.flatMap(_.visibleActions)

  assert(icon != null)
}

sealed trait ActionsDef[-T] {
  def visibleActions: Seq[Action[T]]
}


case class Actions[-T](actions: Action[T]*) extends  ActionsDef[T] {
  require(actions != null)

  override def visibleActions = actions.filter(_.visible)
}

case class GroupedActions[-T](groups: Actions[T]*) extends ActionsDef[T] {
  require(groups != null)

  override def visibleActions = groups.flatMap(_.actions).filter(_.visible)
}
