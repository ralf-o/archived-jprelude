package org.jprelude.experimental.webui.widget


import com.vaadin.event.SelectionEvent
import com.vaadin.event.SelectionEvent.SelectionListener
import com.vaadin.ui.Grid
import rx.lang.scala.Observable

import scala.collection.mutable.ListBuffer

class DataTable[T] (
      columns: ColumnType[T],
      selectionMode: SelectionMode.Value = SelectionMode.None,
      dataEvents: Observable[Seq[T]],
      onSelection: (Seq[T]) => Unit = (_: Seq[T]) => {}
    ) extends Widget {

  require(columns != null)
  require(dataEvents != null)

  private var currentData = Seq[T]()

  private val grid = new Grid

  selectionMode match {
    case SelectionMode.Multi => grid.setSelectionMode(com.vaadin.ui.Grid.SelectionMode.MULTI)
    case SelectionMode.Single => grid.setSelectionMode(com.vaadin.ui.Grid.SelectionMode.SINGLE)
    case SelectionMode.None => grid.setSelectionMode(com.vaadin.ui.Grid.SelectionMode.NONE)
  }

  grid.addSelectionListener(new SelectionListener() {
    override def select(selectionEvent: SelectionEvent): Unit = {
      val selection = selectionEvent.getSelected.toArray.map(n => currentData(n.asInstanceOf[Int])).toList
      onSelection(selection)
    }
  })


  dataEvents.subscribe(data => {
    this.currentData = data
    this.refresh(data)
  })

  private def refresh(data: Seq[T]): Unit = {
    this.grid.getContainerDataSource().removeAllItems();

    data.foreach (item => {
      grid.addRow(
        columns.columns.map(col => col.render(item)):_*
      )
    })

  }

  override def render = {
    columns match {
      case ColumnGroups(columnGroups @ _*) => {
         val groupingHeader = this.grid.prependHeaderRow();

        columnGroups.foreach (columnGroup => {
          var group = ListBuffer[String]()
          
          columnGroup.columns.columns.foreach (column => {
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
