package org.jprelude.experimental.webui.widget

import com.vaadin.ui.Component

abstract class Widget {
  private var maybeComponent: Option[Component] = Option.empty

  final def getComponent(): Component = {
    if (maybeComponent.isEmpty) {
      this.maybeComponent = Option(this.render)
    }

    this.maybeComponent.get
  }

  protected final def isRendered: Boolean = (this.getComponent != null)

  protected def render: Component;
}