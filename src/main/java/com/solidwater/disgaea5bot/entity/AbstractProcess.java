package com.solidwater.disgaea5bot.entity;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import com.solidwater.disgaea5bot.util.NativeUtils;
import com.solidwater.disgaea5bot.util.exception.WindowsAPIException;
import com.sun.jna.platform.win32.WinNT.HANDLE;

public abstract class AbstractProcess implements Process {
	private HANDLE processWindowHandle;
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
		System.out.println(Long.toHexString(this.baseAddress.longValue()));
	}

	public void closeProcess() throws WindowsAPIException {
		if (isProcessOpen()) {
			NativeUtils.closeProcess(this.processWindowHandle);
		}
	}

	public void setValue(BigInteger dynamicAddress, Object data) throws WindowsAPIException {
		// Allocate 8 bytes for 64 bit.
		System.out.println(data);
		if (data instanceof Float) {
			NativeUtils.setDynamicAddressValue(this.processWindowHandle, dynamicAddress,
					ByteBuffer.allocate(8).putFloat((Float) data).array());
		} else if (data instanceof Integer) {
			NativeUtils.setDynamicAddressValue(this.processWindowHandle, dynamicAddress,
					ByteBuffer.allocate(8).putInt(((Integer) data)).array());
		} else if (data instanceof BigInteger) {
			NativeUtils.setDynamicAddressValue(this.processWindowHandle, dynamicAddress,
					((BigInteger) data).toByteArray());
		}
	}

	public float getFloatValue(BigInteger[] offsets) throws WindowsAPIException {
		return NativeUtils.getFloatValue(this.processWindowHandle, this.baseAddress, offsets);
	}

	public int getIntValue(BigInteger[] offsets) throws WindowsAPIException {
		return NativeUtils.getIntValue(this.processWindowHandle, this.baseAddress, offsets);
	}

	public long getLongValue(BigInteger[] offsets) throws WindowsAPIException {
		return NativeUtils.getLongValue(this.processWindowHandle, this.baseAddress, offsets);
	}

	public String getUnsignedLongValue(BigInteger[] offsets) throws WindowsAPIException {
		return NativeUtils.getUnsignedLongValue(this.processWindowHandle, this.baseAddress, offsets);
	}

	@Override
	public BigInteger getBigIntegerValue(BigInteger[] offsets) throws WindowsAPIException {
		return NativeUtils.getBigIntegerValue(this.processWindowHandle, this.baseAddress, offsets);
	}

	public boolean isProcessOpen() {
		return processOpen;
	}

	public void setProcessOpen(boolean processOpen) {
		this.processOpen = processOpen;
	}

	@Override
	public String toString() {
		// TODO: return process information.
		return super.toString();
	}
}