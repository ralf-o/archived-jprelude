package org.jprelude.webui.data;


public class PageableFetchResult<T> extends PagingPosition {
	private final T data;

	public PageableFetchResult(
			final T data,
			final int pageIdx,
			final int pageSize,
			final int totalRecCount) {

		super(pageIdx, pageSize, totalRecCount);
		this.data = data;
	}

	public T getData() {
		return this.data;
	}
}
