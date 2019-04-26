package com.solidwater.disgaea5bot.entity;

import java.math.BigInteger;

import com.solidwater.disgaea5bot.util.NativeUtils;
import com.solidwater.disgaea5bot.util.exception.WindowsAPIException;
import com.sun.jna.platform.win32.WinNT.HANDLE;

public abstract class AbstractProcess {
	private volatile HANDLE processWindowHandle;
	private BigInteger baseAddress = BigInteger.valueOf(0x0l);
	private boolean processOpen = false;

	public AbstractProcess() {
	}

	public AbstractProcess(String windowTitle, String processFilename) throws WindowsAPIException {
		openProcess(windowTitle, processFilename);
	}

	public abstract void openProcess();

	protected void openProcess(String windowTitle, String processFilename) throws WindowsAPIException {
		this.processWindowHandle = NativeUtils.getWindowHandle(windowTitle);
		this.processWindowHandle = NativeUtils.openProcess(this.processWindowHandle);
		this.baseAddress = NativeUtils.getBaseAddress(this.processWindowHandle, processFilename);
	}

	public void closeProcess() throws WindowsAPIException {
		if (isProcessOpen()) {
			NativeUtils.closeProcess(this.processWindowHandle);
		}
	}

	public boolean isProcessOpen() {
		return processOpen;
	}

	public void setProcessOpen(boolean processOpen) {
		this.processOpen = processOpen;
	}
}