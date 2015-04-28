package org.jprelude.experimental.webui.widget


protected abstract class ColumnType

case class Columns(columns: TableColumn*) extends ColumnType

case class ColumnGroups(columnGroup: ColumnGroup*) extends ColumnType

object ColumnTypeImplicits {
implicit def ListOfColumnGroupsToColumnGroups(columnGroups: List[ColumnGroup])
    = ColumnGroups(columnGroups:_*)
}