package org.jprelude.experimental.webui.data

sealed trait LoadingStatus[-T]

object LoadingStatus {
  case class Undefined() extends LoadingStatus[Any]
  case class Loading() extends LoadingStatus[Any]
  case class Failure(error: Throwable) extends LoadingStatus[Any]
  case class Success[T](data: Seq[T], position: PagingPosition) extends LoadingStatus[T]
}


