package com.solidwater.disgaea5bot.util.exception;

public class WindowsAPIException extends Exception {
	private static final long serialVersionUID = 1L;

	public WindowsAPIException() {
		super();
	}

	public WindowsAPIException(String message) {
		super(message);
	}

	public WindowsAPIException(Throwable throwable) {
		super(throwable);
	}

	public WindowsAPIException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public WindowsAPIException(String message, Throwable throwable, boolean enableSuppression,
			boolean writeableStackTrace) {
		super(message, throwable, enableSuppression, writeableStackTrace);
	}
}