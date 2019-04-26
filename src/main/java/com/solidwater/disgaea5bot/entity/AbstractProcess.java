package com.solidwater.disgaea5bot.entity;

import java.math.BigInteger;

import com.solidwater.disgaea5bot.util.NativeUtils;
import com.solidwater.disgaea5bot.util.exception.WindowsAPIException;
import com.sun.jna.platform.win32.WinDef.HWND;

public abstract class AbstractProcess extends HWND {
	protected BigInteger baseAddress = BigInteger.valueOf(0x0l);

	public AbstractProcess() {
	}

	public AbstractProcess(String windowTitle, String processFilename) throws WindowsAPIException {
		NativeUtils.setWindowHandleDetails(this, windowTitle);
		NativeUtils.openProcess(this);
		this.baseAddress = NativeUtils.getBaseAddress(this, processFilename);
	}

	public void closeProcess() throws WindowsAPIException {
		NativeUtils.closeProcess(this);
	}
}