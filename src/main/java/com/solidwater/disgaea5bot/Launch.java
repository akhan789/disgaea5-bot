/**
 * 
 */
package com.solidwater.disgaea5bot;

import java.math.BigInteger;

import com.solidwater.disgaea5bot.util.WindowsUtils;
import com.solidwater.disgaea5bot.util.exceptions.WindowsAPIException;

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
		try {
			WindowsUtils.openProcess("Disgaea 5 Complete");
			//TODO: Use BigInteger for everything since the float value is exceeding size of signed long. 
			System.out.println(WindowsUtils.getFloatValue(BigInteger.valueOf(0x07A6A518l), new BigInteger[] {BigInteger.valueOf(0x8l)}, 8l));
			//System.out.println(WindowsUtils.getIntValue(0x1D1DCB67EC0l, new long[] {0}, 4l));
			//System.out.println(Long.toHexString(Long.parseLong("2217019299680")));
			//System.out.println(WindowsUtils.getLongValue(0x00B66A20l, new long[] {0xD0l, 0x4D8l}, 8l)); // 8 bytes for 64-bit processes.
			//System.out.println(WindowsUtils.getIntValue(0x00B66A20l, new long[] {0x0l}, 4l));
			// WindowsUtils.openProcess("disgaea5.exe");
		} catch (WindowsAPIException e) {
			e.printStackTrace();
		} finally {
			try {
				WindowsUtils.closeProcess();
			} catch (WindowsAPIException e) {
				e.printStackTrace();
			}
		}
	}
}