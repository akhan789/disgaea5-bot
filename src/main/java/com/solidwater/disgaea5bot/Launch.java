/**
 * 
 */
package com.solidwater.disgaea5bot;

import com.solidwater.disgaea5bot.entity.Disgaea5Process;
import com.solidwater.disgaea5bot.util.exception.WindowsAPIException;

/**
 * @author AK
 *
 */
public class Launch /* extends Application */ {
	/*
	 * @Override public void start(Stage stage) { String javaVersion =
	 * System.getProperty("java.version"); String javafxVersion =
	 * System.getProperty("javafx.version"); Label l = new Label("Hello, JavaFX " +
	 * javafxVersion + ", running on Java " + javaVersion + "."); Scene scene = new
	 * Scene(new StackPane(l), 640, 480); stage.setScene(scene); stage.show(); }
	 */
	public static void main(String[] args) {
		Disgaea5Process process = null;
		try {
			process = new Disgaea5Process();
			//TODO: Use BigInteger for everything since the float value is exceeding size of signed long. 
			//System.out.println(NativeUtils.getFloatValue(BigInteger.valueOf(PlayerCharacter.CURRENT_POS_BASE_ADDRESS), new BigInteger[] {BigInteger.valueOf(PlayerCharacter.CURRENT_Y_WAYPOINT_OFFSET)}));
			//System.out.println(WindowsUtils.getIntValue(0x1D1DCB67EC0l, new long[] {0}, 4l));
			//System.out.println(Long.toHexString(Long.parseLong("2217019299680")));
			//System.out.println(WindowsUtils.getLongValue(0x00B66A20l, new long[] {0xD0l, 0x4D8l}, 8l)); // 8 bytes for 64-bit processes.
			//System.out.println(WindowsUtils.getIntValue(0x00B66A20l, new long[] {0x0l}, 4l));
			// WindowsUtils.openProcess("disgaea5.exe");
		} catch (WindowsAPIException e) {
			e.printStackTrace();
		} finally {
			try {
				if(process != null)
					process.closeProcess();
			} catch (WindowsAPIException e) {
				e.printStackTrace();
			}
		}
	}
}