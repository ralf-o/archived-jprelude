package org.jprelude.webui.data;

public class LoadingState {
	private static enum Status {
		UNDEFINED,
		LOADING,
		LOADED_SUCESSFULLY,
		LOADING_FAILED;
	}
}
