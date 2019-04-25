/**
 * 
 */
package com.solidwater.disgaea5bot.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import com.solidwater.disgaea5bot.util.exceptions.WindowsAPIException;
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
public class WindowsUtils {
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
						+ Kernel32Util.getLastErrorMessage());
			}
		} else {
			throw new WindowsAPIException("GetWindowThreadProcessId failed. Error " + Native.getLastError() + ": "
					+ Kernel32Util.getLastErrorMessage());
		}
	}

	public static float getFloatValue(BigInteger staticPointerAddress, BigInteger[] offsets, long bytesToRead)
			throws WindowsAPIException {
		if (WindowsUtils.process == null) {
			throw new WindowsAPIException("Process not initialised correctly");
		}

		BigInteger processBaseAddress = WindowsUtils.getBaseAddressForProcess(1024);
		System.out.println("Process base address: " + Long.toHexString(processBaseAddress.longValue()));
		BigInteger address = processBaseAddress.add(staticPointerAddress);
		System.out.println("New base address: " + Long.toHexString(address.longValue()));
		address = WindowsUtils.getDynamicAddress(address, bytesToRead);
		System.out.println("New dynamic address: " + Long.toHexString(address.longValue()));
		for (BigInteger offset : offsets) {
			address = address.add(offset);
			System.out.println("Testing address: " + Long.toHexString(address.longValue()));
			address = WindowsUtils.getDynamicAddress(address, bytesToRead);
			System.out.println("Address value: " + Long.toHexString(address.longValue()));
			System.out.println(WindowsUtils.getUnsignedLong(address).intValue());
		}
		try {
			return Float.intBitsToFloat(WindowsUtils.getUnsignedLong(address).intValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0l;
	}

	public static void setFloatValue(BigInteger baseAddress, float newValue) {

	}

	public static BigInteger getLongValue(BigInteger staticPointerAddress, BigInteger[] offsets, long bytesToRead)
			throws WindowsAPIException {
		if (WindowsUtils.process == null) {
			throw new WindowsAPIException("Process not initialised correctly");
		}

		BigInteger processBaseAddress = WindowsUtils.getBaseAddressForProcess(1024);
		System.out.println("Process base address: " + Long.toHexString(processBaseAddress.longValue()));
		BigInteger address = processBaseAddress.add(staticPointerAddress);
		System.out.println("New base address: " + Long.toHexString(address.longValue()));
		address = WindowsUtils.getDynamicAddress(address, bytesToRead);
		System.out.println("New dynamic address: " + Long.toHexString(address.longValue()));
		for (BigInteger offset : offsets) {
			address = address.add(offset);
			System.out.println("Testing address: " + Long.toHexString(address.longValue()));
			address = WindowsUtils.getDynamicAddress(address, bytesToRead);
			System.out.println("Address value: " + Long.toHexString(address.longValue()));
		}
		return address;
	}

	public static void setIntValue(BigInteger baseAddress, float newValue) {

	}

	public static void closeProcess() throws WindowsAPIException {
		if (WindowsUtils.process != null) {
			if (!WindowsUtils.KERNEL_32.CloseHandle(WindowsUtils.process)) {
				throw new WindowsAPIException("CloseHandle failed. Error " + Native.getLastError() + ": "
						+ Kernel32Util.getLastErrorMessage());
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

	protected static BigInteger getDynamicAddress(BigInteger processBaseAddress, long bytesToRead)
			throws WindowsAPIException {
		IntByReference bytesRead = new IntByReference(0);
		Memory buffer = new Memory(bytesToRead);
		if (WindowsUtils.KERNEL_32.ReadProcessMemory(WindowsUtils.process, new Pointer(processBaseAddress.longValue()),
				buffer, (int) buffer.size(), bytesRead)) {
			System.out.println("Bytes read: " + bytesRead.getValue());
			return BigInteger.valueOf(buffer.getLong(0l));
		} else {
			throw new WindowsAPIException("ReadProcessMemory failed. Error " + Native.getLastError() + ": "
					+ Kernel32Util.getLastErrorMessage());
		}
	}

	protected static BigInteger getBaseAddressForProcess(int modulesLength) throws WindowsAPIException {
		HMODULE[] lphModules = new HMODULE[modulesLength];
		IntByReference lpcbNeeded = new IntByReference(0);
		if (WindowsUtils.PSAPI.EnumProcessModules(WindowsUtils.process, lphModules, lphModules.length, lpcbNeeded)) {
			if (lphModules.length < lpcbNeeded.getValue()) {
				return WindowsUtils.getBaseAddressForProcess(lpcbNeeded.getValue());
			} else {
				byte[] moduleFileName = new byte[1024];
				for (HMODULE module : lphModules) {
					if (WindowsUtils.PSAPI.GetModuleFileNameExA(WindowsUtils.process, module, moduleFileName,
							moduleFileName.length) != 0) {
						String fileName = Native.toString(moduleFileName);
						System.out.println("Module found: " + fileName);
						if (fileName.contains("disgaea5.exe")) {
							System.out.println("Found disgaea5.exe module");
							MODULEINFO moduleInfo = new MODULEINFO();
							if (WindowsUtils.PSAPI.GetModuleInformation(WindowsUtils.process, module, moduleInfo,
									moduleInfo.size())) {
								return BigInteger.valueOf(
										Long.valueOf(String.valueOf(Pointer.nativeValue(moduleInfo.lpBaseOfDll))));
							} else {
								throw new WindowsAPIException("GetModuleInformation failed. Error "
										+ Native.getLastError() + ": " + Kernel32Util.getLastErrorMessage());
							}
						}
					} else {
						throw new WindowsAPIException("GetModuleFileNameExA failed. Error " + Native.getLastError()
								+ ": " + Kernel32Util.getLastErrorMessage());
					}
				}
			}
		} else {
			throw new WindowsAPIException("EnumProcessModules failed. Error " + Native.getLastError() + ": "
					+ Kernel32Util.getLastErrorMessage());
		}
		return BigInteger.valueOf(0l);
	}

	protected static BigInteger getUnsignedLong(BigInteger value) {
		return new BigInteger(1, WindowsUtils.longToBytes(value.longValue()));
	}

	protected static byte[] longToBytes(long x) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.putLong(x);
		return buffer.array();
	}

	protected static float toSingle(byte[] bytes, int index) throws Exception {
		if (bytes.length < 4)
			throw new Exception("The length of the byte array must be at least 4 bytes long.");
		return Float.intBitsToFloat(WindowsUtils.toInt32(bytes, index));
	}

	protected static int toInt32(byte[] bytes, int index) throws Exception {
		if (bytes.length < 4)
			throw new Exception("The length of the byte array must be at least 4 bytes long.");
		return (int) ((int) (0xff & bytes[index]) << 56 | (int) (0xff & bytes[index + 1]) << 48
				| (int) (0xff & bytes[index + 2]) << 40 | (int) (0xff & bytes[index + 3]) << 32);
	}
}