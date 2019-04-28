package com.solidwater.disgaea5bot.util.winapi;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

public interface ExtendedKernel32 extends Kernel32 {
	ExtendedKernel32 INSTANCE = Native.load("kernel32", ExtendedKernel32.class, W32APIOptions.DEFAULT_OPTIONS);

	/**
	 * Changes the protection on a region of committed pages in the virtual address
	 * space of a specified process.
	 * 
	 * @param hProcess      A handle to the process whose memory protection is to be
	 *                      changed. The handle must have the PROCESS_VM_OPERATION
	 *                      access right. For more information, see Process Security
	 *                      and Access Rights.
	 * @param lpAddress     A pointer to the base address of the region of pages
	 *                      whose access protection attributes are to be changed.
	 * 
	 *                      All pages in the specified region must be within the
	 *                      same reserved region allocated when calling the
	 *                      VirtualAlloc or VirtualAllocEx function using
	 *                      MEM_RESERVE. The pages cannot span adjacent reserved
	 *                      regions that were allocated by separate calls to
	 *                      VirtualAlloc or VirtualAllocEx using MEM_RESERVE.
	 * @param dwSize        The size of the region whose access protection
	 *                      attributes are changed, in bytes. The region of affected
	 *                      pages includes all pages containing one or more bytes in
	 *                      the range from the lpAddress parameter to
	 *                      (lpAddress+dwSize). This means that a 2-byte range
	 *                      straddling a page boundary causes the protection
	 *                      attributes of both pages to be changed.
	 * @param flNewProtect  The memory protection option. This parameter can be one
	 *                      of the memory protection constants. For mapped views,
	 *                      this value must be compatible with the access protection
	 *                      specified when the view was mapped (see MapViewOfFile,
	 *                      MapViewOfFileEx, and MapViewOfFileExNuma).
	 * @param lpfOldProtect A pointer to a variable that receives the previous
	 *                      access protection of the first page in the specified
	 *                      region of pages. If this parameter is NULL or does not
	 *                      point to a valid variable, the function fails.
	 * @return If the function succeeds, the return value is nonzero.
	 * 
	 *         If the function fails, the return value is zero. To get extended
	 *         error information, call GetLastError.
	 */
	boolean VirtualProtectEx(HANDLE hProcess, LPVOID lpAddress, SIZE_T dwSize, DWORD flNewProtect,
			IntByReference lpfOldProtect);
}