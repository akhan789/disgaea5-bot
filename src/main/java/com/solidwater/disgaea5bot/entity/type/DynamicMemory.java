package com.solidwater.disgaea5bot.entity.type;

import java.math.BigInteger;

public class DynamicMemory<T> {
	private T value;
	private BigInteger dynamicAddress = BigInteger.valueOf(0l);

	public DynamicMemory() {
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public BigInteger getDynamicAddress() {
		return dynamicAddress;
	}

	public void setDynamicAddress(BigInteger dynamicAddress) {
		this.dynamicAddress = dynamicAddress;
	}
}