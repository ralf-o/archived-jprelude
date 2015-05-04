package org.jprelude.experimental.webui.data

sealed trait PagingPosition

object PagingPosition {
  case class Undefined() extends PagingPosition
  
  case class Position(val pageIdx: Int, val totalItemCount: Int, val pageSize: Int) extends PagingPosition {
    require(pageIdx >= 0)
    require(pageSize > 0)
    require(totalItemCount > pageSize * pageIdx)
    
    def totalPageCount = ((totalItemCount - 1) / pageSize) + 1
    def offset = pageIdx * pageSize
    def isFirstPage = this.pageIdx == 0
    def isLastPage = this.pageIdx == this.totalPageCount - 1
  }
}
