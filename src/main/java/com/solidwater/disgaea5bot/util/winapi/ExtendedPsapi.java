/**
 * 
 */
package com.solidwater.disgaea5bot.util.winapi;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

/**
 * @author AK
 *
 */
public interface ExtendedPsapi extends Psapi {
	ExtendedPsapi INSTANCE = Native.load("psapi", ExtendedPsapi.class, W32APIOptions.DEFAULT_OPTIONS);

	boolean EnumProcesses(IntByReference[] lpidProcess, DWORD cb, IntByReference[] lpcbNeeded);
}