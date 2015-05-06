package org.jprelude.experimental.webui.data

import rx.lang.scala.Observable
import rx.lang.scala.subjects.BehaviorSubject

final class PageableDatasource[T](pageDataFetcher: (Int, Int) => PageFetchResult[T]) {
  require(pageDataFetcher != null)

  private val loadingSubject: BehaviorSubject[LoadingStatus[T]] =
      BehaviorSubject(LoadingStatus.Undefined())
  
  private val positionSubject: BehaviorSubject[PagingPosition] =
      BehaviorSubject(PagingPosition.Undefined())

  private val dataSubject: BehaviorSubject[Seq[T]] = BehaviorSubject(Seq.empty)

  val loadingEvents: Observable[LoadingStatus[T]] = this.loadingSubject
  val positionEvents: Observable[PagingPosition] = this.positionSubject
  val dataEvents: Observable[Seq[T]] = this.dataSubject

  def loadPage(pageIdx: Int, pageSize: Int) = {
    require(pageIdx >= 0)
    require(pageSize > 0)
    
    this.loadingSubject.onNext(LoadingStatus.Loading())

    this.pageDataFetcher(pageIdx, pageSize) match {
      case result: PageFetchResult.Success[T] => {
        this.loadingSubject.onNext(LoadingStatus.Success[T](result.data, result.position))
        this.positionSubject.onNext(result.position)
        this.dataSubject.onNext(result.data)
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
      case PagingPosition.Undefined() => {}
    }
  }

  def moveToPage(idx: Int) = {
    require(idx >= 0)

    this.positionSubject.asJavaSubject.getValue match {
      case pos: PagingPosition.Position => {
        println("Datasource moving to page " + idx)
        this.loadPage(idx, pos.pageSize);
      }
      case PagingPosition.Undefined() => {}
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
      case PagingPosition.Undefined() => {}
    }
  }

  def moveToNextPage() = {
    this.positionSubject.asJavaSubject.getValue match {
      case position: PagingPosition.Position => {
        if (!position.isLastPage) {
          this.moveToPage(position.pageIdx + 1)
        }
      }
      case PagingPosition.Undefined() => {}
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
      case PagingPosition.Undefined() => {}
    }
  }
}
