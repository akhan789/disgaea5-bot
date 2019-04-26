/**
 * 
 */
package com.solidwater.disgaea5bot;

import java.math.BigInteger;

import com.solidwater.disgaea5bot.entity.Disgaea5Process;
import com.solidwater.disgaea5bot.entity.PlayerCharacter;
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
			System.out.println("Player Y Pos = " + process.getFloatValue(new BigInteger[] {
					PlayerCharacter.CURRENT_POS_BASE_OFFSET, PlayerCharacter.CURRENT_Y_WAYPOINT_OFFSET }));
			System.out.println("Player Target's HP = "
					+ process.getIntValue(new BigInteger[] { PlayerCharacter.CURRENT_TARGET_HP_BASE_OFFSET,
							PlayerCharacter.CURRENT_TARGET_HP_OFFSET_1, PlayerCharacter.CURRENT_TARGET_HP_OFFSET_2 }));
		} catch (WindowsAPIException e) {
			e.printStackTrace();
		} finally {
			try {
				if (process != null) {
					process.closeProcess();
				}
			} catch (WindowsAPIException e) {
				e.printStackTrace();
			}
		}
	}
}