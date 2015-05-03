package org.jprelude.webui.data;

import java.util.Objects;
import java.util.function.Function;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public final class PageableDataSource<T> {
	private final Function<Integer, T> dataFetcher;
	private final PagingPosition position;
    private final BehaviorSubject<PagingPosition> positionSubject;
    public final Observable<PagingPosition> positionEvents;

    public PageableDataSource(final Function<Integer, T> dataFetcher) {
    	Objects.requireNonNull(dataFetcher);

    	this.dataFetcher = dataFetcher;
    	this.position = new PagingPosition(-1, -1, -1);
    	this.positionSubject = BehaviorSubject.create(this.position);
    	this.positionEvents = this.positionSubject.asObservable();
    }

    private void loadData(final int pageIdx) {
    	final T data = this.dataFetcher.apply(pageIdx);
    }

    public void reload() {
        final int totalItemCount = this.position.getTotalItemCount();
        final int pageSize = this.position.getPageSize();
        final int pageIdx = this.position.getPageIdx();

        if (pageIdx >= 0 && totalItemCount > 0 && pageSize > 0) {
        	final int maxPageIdx = ((totalItemCount - 1) / pageSize);

        	if (pageIdx < maxPageIdx) {
        		this.loadData(pageIdx);
        	}
        }
    }

    public void moveToPage(final int newPageIdx) {
        final int totalItemCount = this.position.getTotalItemCount();
        final int pageSize = this.position.getPageSize();
        final int pageIdx = this.position.getPageIdx();

        if (newPageIdx >= 0 && newPageIdx != pageIdx && totalItemCount > 0 && pageSize > 0) {
        	final int maxPageIdx = ((totalItemCount - 1) / pageSize);

        	if (newPageIdx < maxPageIdx) {
        		this.loadData(newPageIdx);
        	}
        }
    }

    public void moveToFirstPage() {
        this.moveToPage(0);
    }

    public void moveToLastPage() {
        final int totalItemCount = this.position.getTotalItemCount();
        final int pageSize = this.position.getPageSize();
        final int pageIdx = this.position.getPageIdx();

        if (totalItemCount > 0 && pageSize > 0) {
        	final int maxPageIdx = ((totalItemCount - 1) / pageSize);

        	if (pageIdx < maxPageIdx) {
        		this.moveToPage(maxPageIdx);
        	}
        }
    }

    public void moveToNextPage() {
        final int totalItemCount = this.position.getTotalItemCount();
        final int pageSize = this.position.getPageSize();
        final int pageIdx = this.position.getPageIdx();

        if (totalItemCount > 0 && pageSize > 0) {
        	final int maxPageIdx = ((totalItemCount - 1) / pageSize);

        	if (pageIdx < maxPageIdx) {
        		this.moveToPage(pageIdx + 1);
        	}
        }
    }

    public void moveToPreviousPage() {
        final int pageIdx = this.position.getPageIdx();

        if (pageIdx > 0) {
        	this.moveToPage(pageIdx - 1);
        }
    }
}
