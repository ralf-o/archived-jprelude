package org.jprelude.experimental.webui.widget


sealed abstract class ColumnType[-T] {
    def columns: Seq[TableColumn[T]]
}

case class Columns[T](val columns: TableColumn[T]*) extends ColumnType[T] {
}

case class ColumnGroups[T](columnGroups: ColumnGroup[T]*) extends ColumnType[T] {
    override def columns: Seq[TableColumn[T]] = columnGroups.flatMap(columnGroup => columnGroup.columns)
}
