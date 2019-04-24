package com.solidwater.disgaea5bot.util.winapi;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.OaIdl.VARIANT_BOOLByReference;
import com.sun.jna.win32.W32APIOptions;

public interface ExtendedKernel32 extends Kernel32 {
	ExtendedKernel32 INSTANCE = Native.load("kernel32", ExtendedKernel32.class, W32APIOptions.DEFAULT_OPTIONS);

	boolean CheckRemoteDebuggerPresent(HANDLE hProcess, VARIANT_BOOLByReference pbDebuggerPresent);
}