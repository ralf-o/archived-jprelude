package org.jprelude.webui.data;

public class PagingPosition {
	private final int pageIdx;
	private final int pageSize;
	private final int totalItemCount;

	public PagingPosition(
			final int pageIdx,
			final int pageSize,
			final int totalItemCount) {

		this.pageIdx = pageIdx;
		this.pageSize = pageSize;
		this.totalItemCount = totalItemCount;
	}

	public int getPageIdx() {
		return this.pageIdx;
	}

	public int getPageSize() {
		return this.pageSize;
	}

	public int getTotalItemCount() {
		return this.totalItemCount;
	}
}
