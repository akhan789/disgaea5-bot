/**
 * 
 */
package com.solidwater.disgaea5bot.util;

import java.math.BigInteger;

import com.solidwater.disgaea5bot.util.exception.WindowsAPIException;
import com.solidwater.disgaea5bot.util.winapi.ExtendedAdvapi32;
import com.solidwater.disgaea5bot.util.winapi.ExtendedKernel32;
import com.solidwater.disgaea5bot.util.winapi.ExtendedPsapi;
import com.solidwater.disgaea5bot.util.winapi.ExtendedUser32;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.Psapi.MODULEINFO;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.ptr.IntByReference;

/**
 * @author AK
 *
 */
public class NativeUtils {
	static final ExtendedUser32 USER_32;
	static final ExtendedKernel32 KERNEL_32;
	static final ExtendedAdvapi32 ADVAPI_32;
	static final ExtendedPsapi PSAPI;

	static volatile BigInteger lastDynamicAddress = BigInteger.valueOf(0l);

	static {
		USER_32 = ExtendedUser32.INSTANCE;
		KERNEL_32 = ExtendedKernel32.INSTANCE;
		ADVAPI_32 = ExtendedAdvapi32.INSTANCE;
		PSAPI = ExtendedPsapi.INSTANCE;

		// Enable Debug Privileges.
		HANDLEByReference hToken = new HANDLEByReference();
		boolean success = ADVAPI_32.OpenProcessToken(KERNEL_32.GetCurrentProcess(),
				WinNT.TOKEN_QUERY | WinNT.TOKEN_ADJUST_PRIVILEGES, hToken);
		if (!success) {
			throw new RuntimeException("OpenProcessToken failed. Error " + Native.getLastError() + ": "
					+ Kernel32Util.getLastErrorMessage());
		}

		WinNT.LUID luid = new WinNT.LUID();
		success = ADVAPI_32.LookupPrivilegeValue(null, WinNT.SE_DEBUG_NAME, luid);
		if (!success) {
			throw new RuntimeException("LookupPrivilegeValue failed. Error " + Native.getLastError() + ": "
					+ Kernel32Util.getLastErrorMessage());
		}

		WinNT.TOKEN_PRIVILEGES tkp = new WinNT.TOKEN_PRIVILEGES(1);
		tkp.Privileges[0] = new WinNT.LUID_AND_ATTRIBUTES(luid, new DWORD(WinNT.SE_PRIVILEGE_ENABLED));
		success = ADVAPI_32.AdjustTokenPrivileges(hToken.getValue(), false, tkp, 0, null, null);
		if (!success) {
			throw new RuntimeException("AdjustTokenPrivileges failed. Error " + Native.getLastError() + ": "
					+ Kernel32Util.getLastErrorMessage());
		}
		Kernel32.INSTANCE.CloseHandle(hToken.getValue());
	}

	private NativeUtils() {
	}

	/**
	 * Get the Window Handle for the Window Title provided.
	 * 
	 * @param windowTitle
	 * @return
	 * @throws WindowsAPIException
	 */
	public static HANDLE getWindowHandle(String windowTitle) throws WindowsAPIException {
		final HANDLE[] windowHandles = new HANDLE[1];
		final Object syncObj = new Object();
		new Thread(() -> {
			NativeUtils.USER_32.EnumWindows((hWnd, userData) -> {
				String hWndTitle = NativeUtils.getWindowText(hWnd, NativeUtils.getWindowTextLength(hWnd));
				if (NativeUtils.isWindowVisible(hWnd) && !hWndTitle.isEmpty() && hWndTitle.equals(windowTitle)) {
					windowHandles[0] = hWnd;
					synchronized (syncObj) {
						syncObj.notifyAll();
					}
					return false;
				}
				return true;
			}, null);
		}).start();

		synchronized (syncObj) {
			try {
				syncObj.wait(100);
			} catch (InterruptedException e) {
			}
		}
		return windowHandles[0];
	}

	/**
	 * Open the process for the provided Window Handle with READ/WRITE/OPERATION
	 * privileges.
	 * 
	 * @param windowHandle
	 * @throws WindowsAPIException
	 */
	public static HANDLE openProcess(HANDLE windowHandle) throws WindowsAPIException {
		int processId = NativeUtils.getWindowThreadProcessId((HWND) windowHandle);
		if ((windowHandle = NativeUtils.KERNEL_32.OpenProcess(
				WinNT.PROCESS_VM_OPERATION | WinNT.PROCESS_VM_READ | WinNT.PROCESS_VM_WRITE, true,
				processId)) != null) {
			return windowHandle;
		} else {
			throw new WindowsAPIException(
					"OpenProcess failed. Error " + Native.getLastError() + ": " + Kernel32Util.getLastErrorMessage());
		}
	}

	/**
	 * Close the process for the provided Window Handle.
	 * 
	 * @param windowHandle
	 * @throws WindowsAPIException
	 */
	public static void closeProcess(HANDLE windowHandle) throws WindowsAPIException {
		if (!NativeUtils.KERNEL_32.CloseHandle(windowHandle)) {
			throw new WindowsAPIException(
					"CloseHandle failed. Error " + Native.getLastError() + ": " + Kernel32Util.getLastErrorMessage());
		}
	}

	/**
	 * Get the base address for the provided Window Handle and its associated
	 * filename.
	 * 
	 * @param handle
	 * @param processFilename
	 * @return
	 * @throws WindowsAPIException
	 */
	public static BigInteger getBaseAddress(HANDLE windowHandle, String processFilename) throws WindowsAPIException {
		return NativeUtils.getBaseAddress(windowHandle, 1024, processFilename);
	}

	// TODO: make sure to add the static offset to list of offsets.
	/**
	 * Get a float value at the dynamic address located by the addition of the
	 * offsets to the process' base address. Include base offset as the first offset
	 * in the offsets array.
	 * 
	 * @param windowHandle
	 * @param processBaseAddress
	 * @param offsets
	 * @return
	 * @throws WindowsAPIException
	 */
	public static float getFloatValue(HANDLE windowHandle, BigInteger processBaseAddress, BigInteger[] offsets)
			throws WindowsAPIException {
		return GeneralUtils.convertToUnsignedFloat(NativeUtils.getValue(windowHandle, processBaseAddress, offsets));
	}

	public static void setFloatValue(BigInteger baseAddress, BigInteger newValue) {
		// TODO:
	}

	/**
	 * Get an int value at the dynamic address located by the addition of the
	 * offsets to the process' base address. Include base offset as the first offset
	 * in the offsets array.
	 * 
	 * @param windowHandle
	 * @param processBaseAddress
	 * @param offsets
	 * @return
	 * @throws WindowsAPIException
	 */
	public static int getIntValue(HANDLE windowHandle, BigInteger processBaseAddress, BigInteger[] offsets)
			throws WindowsAPIException {
		return NativeUtils.getValue(windowHandle, processBaseAddress, offsets).intValue();
	}

	public static void setIntValue(BigInteger baseAddress, BigInteger newValue) {
		// TODO:
	}

	/**
	 * Get a long value at the dynamic address located by the addition of the
	 * offsets to the process' base address. Include base offset as the first offset
	 * in the offsets array.
	 * 
	 * @param windowHandle
	 * @param processBaseAddress
	 * @param offsets
	 * @return
	 * @throws WindowsAPIException
	 */
	public static long getLongValue(HANDLE windowHandle, BigInteger processBaseAddress, BigInteger[] offsets)
			throws WindowsAPIException {
		return NativeUtils.getValue(windowHandle, processBaseAddress, offsets).longValue();
	}

	public static void setLongValue(BigInteger baseAddress, BigInteger newValue) {
		// TODO:
	}

	/**
	 * Get an unsigned long value at the dynamic address located by the addition of
	 * the offsets to the process' base address. Include base offset as the first
	 * offset in the offsets array.
	 * 
	 * @param windowHandle
	 * @param processBaseAddress
	 * @param offsets
	 * @return
	 * @throws WindowsAPIException
	 */
	public static String getUnsignedLongValue(HANDLE windowHandle, BigInteger processBaseAddress, BigInteger[] offsets)
			throws WindowsAPIException {
		return GeneralUtils.convertToUnsignedLong(NativeUtils.getValue(windowHandle, processBaseAddress, offsets));
	}

	public static void setUnsignedLongValue(BigInteger baseAddress, BigInteger newValue) {
		// TODO:
	}

	public static BigInteger getBigIntegerValue(HANDLE windowHandle, BigInteger processBaseAddress,
			BigInteger[] offsets) throws WindowsAPIException {
		return NativeUtils.getValue(windowHandle, processBaseAddress, offsets);
	}

	public static void setBigIntegerValue(BigInteger baseAddress, BigInteger newValue) {
		// TODO:
	}

	public static BigInteger getLastDynamicAddress() {
		return NativeUtils.lastDynamicAddress;
	}

//	public static float byteArrayToFloat(byte[] bytes) {
//	    int intBits = 
//	      bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
//	    return Float.intBitsToFloat(intBits);  
//	}
	public static boolean setDynamicAddressValue(HANDLE handle, BigInteger dynamicAddress, byte[] data)
			throws WindowsAPIException {
		// System.out.println("converted to: " + byteArrayToFloat(data));
		// System.out.println("converted to: " + Integer.pa(data));
		long size = data.length;
		Memory buffer = new Memory(size);
		if (!NativeUtils.KERNEL_32.WriteProcessMemory(handle, new Pointer(dynamicAddress.longValue()), buffer,
				(int) buffer.size(), new IntByReference(0))) {
			throw new WindowsAPIException("WriteProcessMemory failed. Error " + Native.getLastError() + ": "
					+ Kernel32Util.getLastErrorMessage());
		}
		return true;
	}

	private static BigInteger getValue(HANDLE windowHandle, BigInteger processBaseAddress, BigInteger[] offsets)
			throws WindowsAPIException {
		BigInteger dynamicAddress = BigInteger.valueOf(0l);
		for (int i = 0; i < offsets.length; i++) {
			// 8 bytes to read for 64 bit.
			// TODO: Maybe check if we're 32 bit or 64 bit process in future?
			NativeUtils.lastDynamicAddress = dynamicAddress.add(offsets[i]);
			if (i == 0) {
				dynamicAddress = NativeUtils.getDynamicAddressValue(windowHandle, processBaseAddress.add(offsets[i]),
						8l);
			} else {
				dynamicAddress = NativeUtils.getDynamicAddressValue(windowHandle, dynamicAddress.add(offsets[i]), 8l);
			}
		}
		return dynamicAddress;
	}

	private static BigInteger getBaseAddress(HANDLE windowHandle, int modulesLength, String processFilename)
			throws WindowsAPIException {
		HMODULE[] lphModules = NativeUtils.getProcessModules(windowHandle, modulesLength);
		for (HMODULE module : lphModules) {
			if (NativeUtils.getModuleFilename(windowHandle, module).contains(processFilename)) {
				MODULEINFO moduleInfo = new MODULEINFO();
				if (NativeUtils.PSAPI.GetModuleInformation(windowHandle, module, moduleInfo, moduleInfo.size())) {
					return BigInteger
							.valueOf(Long.valueOf(String.valueOf(Pointer.nativeValue(moduleInfo.lpBaseOfDll))));
				} else {
					throw new WindowsAPIException("GetModuleInformation failed. Error " + Native.getLastError() + ": "
							+ Kernel32Util.getLastErrorMessage());
				}
			}
		}
		return BigInteger.valueOf(0l);
	}

	private static int getWindowTextLength(HANDLE handle) {
		return NativeUtils.USER_32.GetWindowTextLength((HWND) handle);
	}

	private static String getWindowText(HANDLE handle, int bufferSize) {
		char[] windowText = new char[bufferSize];
		NativeUtils.USER_32.GetWindowText((HWND) handle, windowText, bufferSize + 1);
		return new String(windowText);
	}

	private static boolean isWindowVisible(HANDLE handle) {
		return NativeUtils.USER_32.IsWindowVisible((HWND) handle);
	}

	private static int getWindowThreadProcessId(HANDLE handle) throws WindowsAPIException {
		IntByReference processId = new IntByReference();
		if (NativeUtils.USER_32.GetWindowThreadProcessId((HWND) handle, processId) == 0) {
			throw new WindowsAPIException("GetWindowThreadProcessId failed. Error " + Native.getLastError() + ": "
					+ Kernel32Util.getLastErrorMessage());
		}
		return processId.getValue();
	}

	private static HMODULE[] getProcessModules(HANDLE handle, int modulesLength) throws WindowsAPIException {
		HMODULE[] lphModules = new HMODULE[modulesLength];
		IntByReference lpcbNeeded = new IntByReference(0);
		if (NativeUtils.PSAPI.EnumProcessModules(handle, lphModules, lphModules.length, lpcbNeeded)) {
			if (lphModules.length < lpcbNeeded.getValue()) {
				return NativeUtils.getProcessModules(handle, lpcbNeeded.getValue());
			} else {
				return lphModules;
			}
		} else {
			throw new WindowsAPIException("EnumProcessModules failed. Error " + Native.getLastError() + ": "
					+ Kernel32Util.getLastErrorMessage());
		}
	}

	private static String getModuleFilename(HANDLE handle, HMODULE module) throws WindowsAPIException {
		byte[] moduleFileName = new byte[1024];
		if (NativeUtils.PSAPI.GetModuleFileNameExA(handle, module, moduleFileName, moduleFileName.length) != 0) {
			return Native.toString(moduleFileName);
		} else {
			throw new WindowsAPIException("GetModuleFileNameExA failed. Error " + Native.getLastError() + ": "
					+ Kernel32Util.getLastErrorMessage());
		}
	}

	private static BigInteger getDynamicAddressValue(HANDLE handle, BigInteger processBaseAddress, long bytesToRead)
			throws WindowsAPIException {
		IntByReference bytesRead = new IntByReference(0);
		Memory buffer = new Memory(bytesToRead);
		if (NativeUtils.KERNEL_32.ReadProcessMemory(handle, new Pointer(processBaseAddress.longValue()), buffer,
				(int) buffer.size(), bytesRead)) {
			return BigInteger.valueOf(buffer.getLong(0l));
		} else {
			throw new WindowsAPIException("ReadProcessMemory failed. Error " + Native.getLastError() + ": "
					+ Kernel32Util.getLastErrorMessage());
		}
	}
}