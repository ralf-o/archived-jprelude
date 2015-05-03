package org.jprelude.experimental.webui.data


import scala.util.Try
import rx.lang.scala.subjects.BehaviorSubject

final class PageableDatasource[T](pageDataFetcher: Function2[Int, Int, PageFetchResult[T]]) {
  require(pageDataFetcher != null)

  private val loadingSubject: BehaviorSubject[LoadingStatus[T]] =
      BehaviorSubject(LoadingStatus.Undefined())
  
  private val positionSubject: BehaviorSubject[PagingPosition] =
      BehaviorSubject(PagingPosition.Undefined())

  val loadingEvents = this.loadingSubject.asJavaSubject.asObservable
  val positionEvents = this.positionSubject.asJavaSubject.asObservable

  def loadPage(pageIdx: Int, pageSize: Int) = {
    require(pageIdx >= 0)
    require(pageSize > 0)
    
    this.loadingSubject.onNext(LoadingStatus.Loading())

    this.pageDataFetcher(pageIdx, pageSize) match {
      case PageFetchResult.Success(data, position) => {
        this.loadingSubject.onNext(LoadingStatus.Success(data, position))
      }
      case PageFetchResult.Failure(error) => {
        this.loadingSubject.onNext(LoadingStatus.Failure(error))
      }
    }
  }

  def reload() = {
    this.positionSubject.asJavaSubject.getValue match {
      case position: PagingPosition.Position => {
          this.loadPage(position.pageIdx, position.pageSize);
      }
    }
  }

  def moveToPage(n: Int) = {
    require(n > 0)

    this.positionSubject.asJavaSubject.getValue match {
      case pos: PagingPosition.Position => {
        if (!pos.isFirstPage) {
          this.loadPage(pos.pageIdx - 1, pos.pageSize);
        }
      }
    }
  }

  def moveToPreviousPage() = {
    val position = this.positionSubject.asJavaSubject.getValue

    position match {
      case pos: PagingPosition.Position => {
        if (!pos.isFirstPage) {
          this.moveToPage(pos.pageIdx - 1)
        }
      }
    }
  }

  def moveToNextPage() = {
    this.positionSubject.asJavaSubject.getValue match {
      case position: PagingPosition.Position => {
        if (!position.isLastPage) {
          this.moveToPage(position.pageIdx + 1)
        }
      }
    }
  }

  def moveToFirstPage() = {
    this.moveToPage(0);
  }

  def moveToLastPage()  = {
    this.positionSubject.asJavaSubject.getValue match {
      case position: PagingPosition.Position => {
        if (!position.isLastPage) {
          this.moveToPage(position.totalPageCount - 1)
        }
      }
    }
  }
}
