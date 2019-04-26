/**
 * 
 */
package com.solidwater.disgaea5bot.entity;

import java.math.BigInteger;

/**
 * @author AK
 *
 */
public class PlayerCharacter {
	// "disgaea5.exe"+07A6A518 +8 offset
	public static final BigInteger CURRENT_POS_BASE_OFFSET = BigInteger.valueOf(0x07A6A518l);
	public static final BigInteger CURRENT_Y_WAYPOINT_OFFSET = BigInteger.valueOf(0x8l);
	public static final BigInteger CURRENT_X_WAYPOINT_OFFSET = BigInteger.valueOf(0x10l);
	public static final BigInteger CURRENT_Z_WAYPOINT_OFFSET = BigInteger.valueOf(0xCl);

	public static final BigInteger CURRENT_TARGET_HP_BASE_OFFSET = BigInteger.valueOf(0x00B66A20);
	public static final BigInteger CURRENT_TARGET_HP_OFFSET_1 = BigInteger.valueOf(0x98);
	public static final BigInteger CURRENT_TARGET_HP_OFFSET_2 = BigInteger.valueOf(0x4D8);
}