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

	static volatile HANDLE process = null;
	static volatile BigInteger processBaseAddress = BigInteger.valueOf(0l);

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
	 * @return the HWND
	 * @throws WindowsAPIException
	 */
	public static void setWindowHandleDetails(HWND windowHandle, String windowTitle) throws WindowsAPIException {
		final Object syncObj = new Object();
		new Thread(() -> {
			NativeUtils.USER_32.EnumWindows((hWnd, userData) -> {
				String hWndTitle = NativeUtils.getWindowText(hWnd, NativeUtils.getWindowTextLength(hWnd));
				if (NativeUtils.isWindowVisible(hWnd) && !hWndTitle.isEmpty() && hWndTitle.equals(windowTitle)) {
					NativeUtils.process = hWnd;
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
				syncObj.wait();
			} catch (InterruptedException e) {
			}
		}
		windowHandle = (HWND) NativeUtils.process;
	}

	/**
	 * Open the process for the provided Window Handle with READ/WRITE/OPERATION
	 * privileges.
	 * 
	 * @param windowHandle
	 * @throws WindowsAPIException
	 */
	public synchronized static void openProcess(HWND windowHandle) throws WindowsAPIException {
		int processId = NativeUtils.getWindowThreadProcessId(windowHandle);
		if ((windowHandle = (HWND) NativeUtils.KERNEL_32.OpenProcess(
				WinNT.PROCESS_VM_OPERATION | WinNT.PROCESS_VM_READ | WinNT.PROCESS_VM_WRITE, true,
				processId)) == null) {
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
	public static void closeProcess(HWND windowHandle) throws WindowsAPIException {
		if (!NativeUtils.KERNEL_32.CloseHandle(windowHandle))
			throw new WindowsAPIException(
					"CloseHandle failed. Error " + Native.getLastError() + ": " + Kernel32Util.getLastErrorMessage());
	}

	/**
	 * Get the base address for the provided Window Handle and its associated
	 * filename.
	 * 
	 * @param hWnd
	 * @param processFilename
	 * @return
	 * @throws WindowsAPIException
	 */
	public static BigInteger getBaseAddress(HWND windowHandle, String processFilename) throws WindowsAPIException {
		return NativeUtils.getBaseAddress(windowHandle, 1024, processFilename);
	}

	private static BigInteger getBaseAddress(HWND windowHandle, int modulesLength, String processFilename)
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

	private static int getWindowTextLength(HWND hWnd) {
		return NativeUtils.USER_32.GetWindowTextLength(hWnd);
	}

	private static String getWindowText(HWND hWnd, int bufferSize) {
		char[] windowText = new char[bufferSize];
		NativeUtils.USER_32.GetWindowText(hWnd, windowText, bufferSize + 1);
		return new String(windowText);
	}

	private static boolean isWindowVisible(HWND hWnd) {
		return NativeUtils.USER_32.IsWindowVisible(hWnd);
	}

	private static int getWindowThreadProcessId(HWND hWnd) throws WindowsAPIException {
		IntByReference processId = new IntByReference();
		if (NativeUtils.USER_32.GetWindowThreadProcessId(hWnd, processId) == 0) {
			throw new WindowsAPIException("GetWindowThreadProcessId failed. Error " + Native.getLastError() + ": "
					+ Kernel32Util.getLastErrorMessage());
		}
		return processId.getValue();
	}

	private static HMODULE[] getProcessModules(HWND hWnd, int modulesLength) throws WindowsAPIException {
		HMODULE[] lphModules = new HMODULE[modulesLength];
		IntByReference lpcbNeeded = new IntByReference(0);
		if (NativeUtils.PSAPI.EnumProcessModules(hWnd, lphModules, lphModules.length, lpcbNeeded)) {
			if (lphModules.length < lpcbNeeded.getValue()) {
				return NativeUtils.getProcessModules(hWnd, lpcbNeeded.getValue());
			} else {
				return lphModules;
			}
		} else {
			throw new WindowsAPIException("EnumProcessModules failed. Error " + Native.getLastError() + ": "
					+ Kernel32Util.getLastErrorMessage());
		}
	}

	private static String getModuleFilename(HWND hWnd, HMODULE module) throws WindowsAPIException {
		byte[] moduleFileName = new byte[1024];
		if (NativeUtils.PSAPI.GetModuleFileNameExA(hWnd, module, moduleFileName, moduleFileName.length) != 0) {
			return Native.toString(moduleFileName);
		} else {
			throw new WindowsAPIException("GetModuleFileNameExA failed. Error " + Native.getLastError() + ": "
					+ Kernel32Util.getLastErrorMessage());
		}
	}

	private static BigInteger getDynamicAddress(BigInteger processBaseAddress, long bytesToRead)
			throws WindowsAPIException {
		IntByReference bytesRead = new IntByReference(0);
		Memory buffer = new Memory(bytesToRead);
		if (NativeUtils.KERNEL_32.ReadProcessMemory(NativeUtils.process, new Pointer(processBaseAddress.longValue()),
				buffer, (int) buffer.size(), bytesRead)) {
			System.out.println("Bytes read: " + bytesRead.getValue());
			return BigInteger.valueOf(buffer.getLong(0l));
		} else {
			throw new WindowsAPIException("ReadProcessMemory failed. Error " + Native.getLastError() + ": "
					+ Kernel32Util.getLastErrorMessage());
		}
	}

	// ---- OLD CODE ----
	public static float getFloatValue(BigInteger staticPointerAddress, BigInteger[] offsets)
			throws WindowsAPIException {
		if (NativeUtils.process == null) {
			throw new WindowsAPIException("Process not initialised correctly");
		}

		BigInteger processBaseAddress = NativeUtils.getBaseAddress(1024);
		System.out.println("Process base address: " + Long.toHexString(processBaseAddress.longValue()));
		BigInteger address = processBaseAddress.add(staticPointerAddress);
		System.out.println("New base address: " + Long.toHexString(address.longValue()));
		address = NativeUtils.getDynamicAddress(address, 8l);
		System.out.println("New dynamic address: " + Long.toHexString(address.longValue()));
		for (BigInteger offset : offsets) {
			address = address.add(offset);
			System.out.println("Testing address: " + Long.toHexString(address.longValue()));
			address = NativeUtils.getDynamicAddress(address, 8l);
			System.out.println("Address value: " + Long.toHexString(address.longValue()));
		}
		return GeneralUtils.convertToUnsignedFloat(address);
	}

	public static void setFloatValue(BigInteger baseAddress, BigInteger newValue) {

	}

	public static BigInteger getLongValue(BigInteger staticPointerAddress, BigInteger[] offsets)
			throws WindowsAPIException {
		if (NativeUtils.process == null) {
			throw new WindowsAPIException("Process not initialised correctly");
		}

		BigInteger processBaseAddress = NativeUtils.getBaseAddress(1024);
		System.out.println("Process base address: " + Long.toHexString(processBaseAddress.longValue()));
		BigInteger address = processBaseAddress.add(staticPointerAddress);
		System.out.println("New base address: " + Long.toHexString(address.longValue()));
		address = NativeUtils.getDynamicAddress(address, 8l);
		System.out.println("New dynamic address: " + Long.toHexString(address.longValue()));
		for (BigInteger offset : offsets) {
			address = address.add(offset);
			System.out.println("Testing address: " + Long.toHexString(address.longValue()));
			address = NativeUtils.getDynamicAddress(address, 8l);
			System.out.println("Address value: " + Long.toHexString(address.longValue()));
		}
		return address;
	}

	public static void setLongValue(BigInteger baseAddress, BigInteger newValue) {

	}
}