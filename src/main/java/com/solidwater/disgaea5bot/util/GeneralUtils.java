package com.solidwater.disgaea5bot.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class GeneralUtils {
	private GeneralUtils() {
	}

	protected static String convertToUnsignedLong(BigInteger value) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.putLong(value.longValue());
		return new BigInteger(1, buffer.array()).toString();
	}

	protected static float convertToUnsignedFloat(BigInteger value) {
		return Float.intBitsToFloat(value.intValue());
	}
}