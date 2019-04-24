package com.solidwater.disgaea5bot.util.exceptions;

import com.sun.jna.platform.win32.WinError;

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

	public static String getWindowsAPIErrorMessage(int errorCode) {
		switch (errorCode) {
		case WinError.ERROR_INVALID_HANDLE:
			return "The handle is invalid.";
		case WinError.ERROR_INVALID_WINDOW_HANDLE:
			return "Invalid window handle.";
		default:
			return "Error code not mapped. Refer to MSDN / WinError.class.";
		}
	}
}