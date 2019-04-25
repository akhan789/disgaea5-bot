/**
 * 
 */
package com.solidwater.disgaea5bot.util;

import com.solidwater.disgaea5bot.util.exceptions.WindowsAPIException;
import com.solidwater.disgaea5bot.util.winapi.ExtendedAdvapi32;
import com.solidwater.disgaea5bot.util.winapi.ExtendedKernel32;
import com.solidwater.disgaea5bot.util.winapi.ExtendedPsapi;
import com.solidwater.disgaea5bot.util.winapi.ExtendedUser32;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.ptr.IntByReference;

/**
 * @author AK
 *
 */
public class WindowsUtils {
	static final ExtendedUser32 USER_32;
	static final ExtendedKernel32 KERNEL_32;
	static final ExtendedAdvapi32 ADVAPI_32;
	static final ExtendedPsapi PSAPI;

	static volatile HANDLE process = null;

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
					+ WindowsAPIException.getWindowsAPIErrorMessage(Native.getLastError()));
		}

		WinNT.LUID luid = new WinNT.LUID();
		success = ADVAPI_32.LookupPrivilegeValue(null, WinNT.SE_DEBUG_NAME, luid);
		if (!success) {
			throw new RuntimeException("LookupPrivilegeValue failed. Error " + Native.getLastError() + ": "
					+ WindowsAPIException.getWindowsAPIErrorMessage(Native.getLastError()));
		}

		WinNT.TOKEN_PRIVILEGES tkp = new WinNT.TOKEN_PRIVILEGES(1);
		tkp.Privileges[0] = new WinNT.LUID_AND_ATTRIBUTES(luid, new DWORD(WinNT.SE_PRIVILEGE_ENABLED));
		success = ADVAPI_32.AdjustTokenPrivileges(hToken.getValue(), false, tkp, 0, null, null);
		if (!success) {
			throw new RuntimeException("AdjustTokenPrivileges failed. Error " + Native.getLastError() + ": "
					+ WindowsAPIException.getWindowsAPIErrorMessage(Native.getLastError()));
		}
		Kernel32.INSTANCE.CloseHandle(hToken.getValue());
	}

	private WindowsUtils() {
	}

	public static void openProcess(final String windowTitle) throws WindowsAPIException {
		final Object synchObj = new Object();
		new Thread(() -> {
			WindowsUtils.USER_32.EnumWindows((hWnd, userData) -> {
				int length = WindowsUtils.USER_32.GetWindowTextLength(hWnd);
				char[] windowText = new char[length];
				WindowsUtils.USER_32.GetWindowText(hWnd, windowText, length + 1);
				String hWndTitle = new String(windowText);
				if (WindowsUtils.USER_32.IsWindowVisible(hWnd) && !hWndTitle.isEmpty()
						&& hWndTitle.equals(windowTitle)) {
					System.out.println("Setting window handle process to: " + windowTitle);
					WindowsUtils.process = hWnd;
					synchronized (synchObj) {
						synchObj.notifyAll();
					}
					return false;
				}
				return true;
			}, null);
		}).start();

		synchronized (synchObj) {
			try {
				synchObj.wait();
			} catch (InterruptedException e) {
			}
		}

		IntByReference processId = new IntByReference();
		WindowsUtils.USER_32.GetWindowThreadProcessId((HWND) WindowsUtils.process, processId);
		if (processId.getValue() != 0) {
			if ((WindowsUtils.process = WindowsUtils.KERNEL_32.OpenProcess(
					WinNT.PROCESS_VM_OPERATION | WinNT.PROCESS_VM_READ | WinNT.PROCESS_VM_WRITE, true,
					processId.getValue())) != null) {
				System.out.println(windowTitle + " process opened successfully");
			} else {
				throw new WindowsAPIException("OpenProcess failed. Error " + Native.getLastError() + ": "
						+ WindowsAPIException.getWindowsAPIErrorMessage(Native.getLastError()));
			}
		} else {
			throw new WindowsAPIException("GetWindowThreadProcessId failed. Error " + Native.getLastError() + ": "
					+ WindowsAPIException.getWindowsAPIErrorMessage(Native.getLastError()));
		}
	}

	public static float getFloatValue(long address, long offset, long bytesToRead) throws WindowsAPIException {
		if(WindowsUtils.process == null)
			throw new WindowsAPIException("Process not initialised correctly");

		IntByReference bytesRead = new IntByReference(0);
		Memory buffer = new Memory(bytesToRead);
		if (WindowsUtils.KERNEL_32.ReadProcessMemory(WindowsUtils.process, new Pointer(address), buffer,
				(int) buffer.size(), bytesRead)) {
			System.out.println("Bytes read: " + bytesRead.getValue());
			return buffer.getFloat(offset);
		} else {
			throw new WindowsAPIException("ReadProcessMemory failed. Error " + Native.getLastError() + ": "
					+ WindowsAPIException.getWindowsAPIErrorMessage(Native.getLastError()));
		}
	}

	public static void setFloatValue(long address, float newValue) {

	}

	public static int getIntValue(long address, long offset, long bytesToRead) throws WindowsAPIException {
		if(WindowsUtils.process == null)
			throw new WindowsAPIException("Process not initialised correctly");

		IntByReference bytesRead = new IntByReference(0);
		Memory buffer = new Memory(bytesToRead);
		if (WindowsUtils.KERNEL_32.ReadProcessMemory(WindowsUtils.process, new Pointer(address), buffer,
				(int) buffer.size(), bytesRead)) {
			System.out.println("Bytes read: " + bytesRead.getValue());
			return buffer.getInt(offset);
		} else {
			throw new WindowsAPIException("ReadProcessMemory failed. Error " + Native.getLastError() + ": "
					+ WindowsAPIException.getWindowsAPIErrorMessage(Native.getLastError()));
		}
	}

	public static void setIntValue(long address, float newValue) {

	}

	public static void closeProcess() throws WindowsAPIException {
		if (WindowsUtils.process != null) {
			if (!WindowsUtils.KERNEL_32.CloseHandle(WindowsUtils.process)) {
				throw new WindowsAPIException("CloseHandle failed. Error " + Native.getLastError() + ": "
						+ WindowsAPIException.getWindowsAPIErrorMessage(Native.getLastError()));
			} else {
				System.out.println("Process closed successfully");
			}
		} else {
			throw new WindowsAPIException("The process was null. Unable to close handle.");
		}
	}

	public static boolean checkRemoteDebuggerPresent() throws WindowsAPIException {
		return false;
	}
}