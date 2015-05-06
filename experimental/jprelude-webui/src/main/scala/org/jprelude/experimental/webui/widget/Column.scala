package org.jprelude.experimental.webui.widget

case class Column[-T](
  title: String,
  render: T => String)
