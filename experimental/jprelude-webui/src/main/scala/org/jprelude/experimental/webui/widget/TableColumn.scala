package org.jprelude.experimental.webui.widget

case class TableColumn[-T](
  title: String,
  render: T => String
)
