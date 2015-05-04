package org.jprelude.experimental.webui.widget


sealed abstract class ColumnType[-T]

case class Columns[-T](columns: TableColumn[T]*) extends ColumnType

case class ColumnGroups[-T](columnGroup: ColumnGroup[T]*) extends ColumnType
