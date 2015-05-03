package org.jprelude.experimental.webui.data

sealed trait PagingPosition

object PagingPosition {
  case class Undefined() extends PagingPosition
  
  case class Position(pageIdx: Int, totalItemCount: Int, pageSize: Int) extends PagingPosition {
    require(pageIdx > 0)
    require(pageSize > 0)
    require(totalItemCount > pageSize * pageIdx)
    
    def totalPageCount = (totalItemCount / pageSize) + 1
    def offset = pageIdx * pageSize
    def isFirstPage = this.pageIdx == 0
    def isLastPage = this.pageIdx == this.totalPageCount
  }
}
