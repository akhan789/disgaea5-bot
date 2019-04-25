/**
 * 
 */
package com.solidwater.disgaea5bot;

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
			//System.out.println(WindowsUtils.getFloatValue(0x07A6A518l, new long[] {0x00000008l}, 4l));
			//System.out.println(WindowsUtils.getFloatValue(0x07A6A518l, new long[] { 0x00000008l }, 16l));
			//System.out.println(WindowsUtils.getIntValue(0x1D1DCB67EC0l, new long[] {0}, 4l));
			System.out.println(WindowsUtils.getIntValue(0x00B66A20l, new long[] {0x98l, 0x4D8l}, 4l));
			//System.out.println(Long.toHexString(Long.parseLong("3664289952")));
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