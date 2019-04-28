package com.solidwater.disgaea5bot.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class GeneralUtils {
	private GeneralUtils() {
	}

	public static String convertToUnsignedLong(BigInteger value) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.putLong(value.longValue());
		return new BigInteger(1, buffer.array()).toString();
	}

	public static float convertToUnsignedFloat(BigInteger value) {
		return Float.intBitsToFloat(value.intValue());
	}

	public static Byte[] hexObjectToByteArray(Object data) {
		if (data instanceof Float) {
			return GeneralUtils.hexFloatToByteArray((Float) data);
		} else if (data instanceof Double) {
			return GeneralUtils.hexDoubleToByteArray((Double) data);
		} else {
			return GeneralUtils.hexBigIntegerToByteArray((BigInteger) data);
		}
	}

	public static Byte[] hexFloatToByteArray(Float data) {
		return GeneralUtils.hexStringToByteArray(Integer.toHexString(Float.floatToRawIntBits((Float) data)));
	}

	public static Byte[] hexDoubleToByteArray(Double data) {
		return GeneralUtils.hexStringToByteArray(Long.toHexString(Double.doubleToRawLongBits((Double) data)));
	}

	public static Byte[] hexBigIntegerToByteArray(BigInteger data) {
		return GeneralUtils.hexStringToByteArray(new BigInteger(data.toString()).toString(16));
	}

	public static Byte[] hexStringToByteArray(String str) {
		int len = str.length();
		Byte[] data = new Byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = Byte.valueOf(
					(byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16)));
		}
		return data;
	}
}