package org.jprelude.experimental.webui.widget


import com.vaadin.ui.Grid
import rx.lang.scala.Observable

import scala.collection.mutable.ListBuffer

class DataTable[T] (
      columns: ColumnType[T],
      dataEvents: Observable[Seq[T]]
    ) extends Widget {

  require(columns != null)
  require(dataEvents != null)

  private val grid = new Grid

  dataEvents.subscribe(data => {
    this.refresh(data)
  })

  private def refresh(data: Seq[T]): Unit = {
    this.grid.getContainerDataSource().removeAllItems();

    data.foreach (item => {
      grid.addRow("A" + item, "B" + item, "C" + item, "D" + item)
    })

  }

  override def render = {
    columns match {
      case ColumnGroups(columnGroups @ _*) => {
         val groupingHeader = this.grid.prependHeaderRow();

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
 

    grid.setSizeFull()  
    grid
  }
}
