package org.jprelude.experimental.webui.widget

import com.vaadin.server.Resource

case class Column[-T](
  title: String,
  icon: Option[Resource] = Option.empty,
  render: T => String)
