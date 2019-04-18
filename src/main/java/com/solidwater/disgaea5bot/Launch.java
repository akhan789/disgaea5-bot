/**
 * 
 */
package com.solidwater.disgaea5bot;

import com.solidwater.disgaea5bot.entity.PlayerCharacter;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * @author AK
 *
 */
public class Launch extends Application {
	public static int PROCESS_VM_READ = 0x0010;
	public static int PROCESS_VM_WRITE = 0x0020;
	public static int PROCESS_VM_OPERATION = 0x0008;

	@Override
	public void start(Stage stage) {
		String javaVersion = System.getProperty("java.version");
		String javafxVersion = System.getProperty("javafx.version");
		Label l = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
		Scene scene = new Scene(new StackPane(l), 640, 480);
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		// Application.launch();
		IntByReference pid = new IntByReference(0);
		User32.INSTANCE.GetWindowThreadProcessId(User32.INSTANCE.FindWindow(null, "Solitaire"), pid);
		HANDLE process = Kernel32.INSTANCE.OpenProcess(PROCESS_VM_READ | PROCESS_VM_WRITE | PROCESS_VM_OPERATION, true,
				pid.getValue());
		long dynAddress = findDynAddress(process, PlayerCharacter.CURRENT_X_WAYPOINT_OFFSET,
				PlayerCharacter.CURRENT_POS_BASE_ADDRESS);

		Memory scoreMem = readMemory(process, dynAddress, 4);
		int score = scoreMem.getInt(0);
		System.out.println(score);

		byte[] newScore = new byte[] { 0x22, 0x22, 0x22, 0x22 };
		writeMemory(process, dynAddress, newScore);
	}

	public static long findDynAddress(HANDLE process, long[] offsets, long baseAddress) {
		long pointer = baseAddress;
		int size = 4;
		Memory pTemp = new Memory(size);
		long pointerAddress = 0x0l;
		for (int i = 0; i < offsets.length; i++) {
			if (i == 0) {
				Kernel32.INSTANCE.ReadProcessMemory(process, pointer, pTemp, size, null);
			}
			pointerAddress = ((pTemp.getInt(0) + offsets[i]));
			if (i != offsets.length - 1) {
				Kernel32.INSTANCE.ReadProcessMemory(process, pointerAddress, pTemp.getPointer(0l), size, null);
			}
		}
		return pointerAddress;
	}

	public static Memory readMemory(HANDLE process, Pointer address, int bytesToRead) {
		Memory output = new Memory(bytesToRead);
		Kernel32.INSTANCE.ReadProcessMemory(process, address, output.getPointer(0l), bytesToRead,
				new IntByReference(bytesToRead));
		return output;
	}

	public static void writeMemory(Pointer process, long address, byte[] data) {
		int size = data.length;
		Memory toWrite = new Memory(size);

		for (int i = 0; i < size; i++) {
			toWrite.setByte(i, data[i]);
		}

		boolean b = Kernel32.INSTANCE.WriteProcessMemory(process, address, toWrite, size, null);
	}
}