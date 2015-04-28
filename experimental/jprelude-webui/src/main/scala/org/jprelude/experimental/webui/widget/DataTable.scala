package org.jprelude.experimental.webui.widget


import com.vaadin.ui.Grid
import scala.collection.mutable.ListBuffer
import com.vaadin.shared.ui.grid.HeightMode
import com.vaadin.ui.themes.ValoTheme

case class DataTable (
  columns: ColumnType
) extends Widget {
  override def render = {
    val grid = new Grid
    
    
    columns match {
      case ColumnGroups(columnGroups @ _*) => {
         val groupingHeader = grid.prependHeaderRow();

        columnGroups.foreach (columnGroup => {
          var group = ListBuffer[String]()
          
          columnGroup.columns.foreach (column => {
            group += column.title
            grid.addColumn(column.title, classOf[String])
          })
          println(group)
           groupingHeader.join(group:_*).setText(columnGroup.title)
          
        })
        
        
       // groupingHeader.join(groupingHeader.getCell("spalte3"), groupingHeader.getCell("spalte4"));
      }
      
      case Columns(columns  @ _*) => {
        columns.foreach (column => 
          grid.addColumn(column.title, classOf[String]))
      }
    }
    grid addStyleName "small compact tiny"
 
    Range(1, 500).foreach (n => {
      grid.addRow("A" + n, "B" + n, "C" + n, "D" + n);
    })
    
    grid.setSizeFull()  
    grid
  }
}
