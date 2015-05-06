package org.jprelude.experimental.webui.widget

case class ColumnGroup[T](
    title: String,
    columns: Columns[T]
)