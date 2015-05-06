package org.jprelude.experimental.webui.widget


sealed abstract class ColumnType[-T] {
    def columns: Seq[Column[T]]
}

case class Columns[T](val columns: Column[T]*) extends ColumnType[T] {
}

case class ColumnGroups[T](columnGroups: ColumnGroup[T]*) extends ColumnType[T] {
    override def columns: Seq[Column[T]] = columnGroups.flatMap(columnGroup => columnGroup.columns.columns)
}
