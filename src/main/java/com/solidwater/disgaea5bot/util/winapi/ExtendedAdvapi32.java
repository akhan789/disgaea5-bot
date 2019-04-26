package com.solidwater.disgaea5bot.util.winapi;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.win32.W32APIOptions;

public interface ExtendedAdvapi32 extends Advapi32 {
	ExtendedAdvapi32 INSTANCE = Native.load("Advapi32", ExtendedAdvapi32.class, W32APIOptions.DEFAULT_OPTIONS);
}