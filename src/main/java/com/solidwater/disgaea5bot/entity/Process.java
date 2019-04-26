package com.solidwater.disgaea5bot.entity;

import java.math.BigInteger;

import com.solidwater.disgaea5bot.util.exception.WindowsAPIException;

public interface Process {

	public void openProcess();

	public void closeProcess() throws WindowsAPIException;

	public void setValue(BigInteger dynamicAddress, Object data) throws WindowsAPIException;

	public float getFloatValue(BigInteger[] offsets) throws WindowsAPIException;

	public int getIntValue(BigInteger[] offsets) throws WindowsAPIException;

	public long getLongValue(BigInteger[] offsets) throws WindowsAPIException;

	public String getUnsignedLongValue(BigInteger[] offsets) throws WindowsAPIException;

	public BigInteger getBigIntegerValue(BigInteger[] offsets) throws WindowsAPIException;

	public boolean isProcessOpen();

	public void setProcessOpen(boolean processOpen);
}