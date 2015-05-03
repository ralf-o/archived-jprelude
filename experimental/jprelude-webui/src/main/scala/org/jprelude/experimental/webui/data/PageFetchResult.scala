package org.jprelude.experimental.webui.data

sealed trait PageFetchResult[-T];

object PageFetchResult {
  case class Success[T](data: Seq[T], position: PagingPosition) extends PageFetchResult[T]
  case class Failure(error: Throwable) extends PageFetchResult[Any]
}