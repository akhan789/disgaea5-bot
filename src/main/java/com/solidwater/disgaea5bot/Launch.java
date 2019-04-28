package com.solidwater.disgaea5bot;

import com.solidwater.disgaea5bot.entity.Disgaea5Process;
import com.solidwater.disgaea5bot.entity.PlayerCharacter;
import com.solidwater.disgaea5bot.util.exception.WindowsAPIException;

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
			PlayerCharacter playerCharacter = new PlayerCharacter(process);
			System.out.println("Player Y Pos = " + playerCharacter.getCurrentYPos());
			System.out.println("Player X Pos = " + playerCharacter.getCurrentXPos());
			System.out.println("Player Z Pos = " + playerCharacter.getCurrentZPos());
			System.out.println("Player Target's HP = " + playerCharacter.getCurrentTargetHP());
			System.out.println("Player Money Amount = " + playerCharacter.getMoney());
			// playerCharacter.setCurrentZPos(-150.0f);
			// playerCharacter.setCurrentTargetHP(11111);
			// playerCharacter.setMoney(BigInteger.valueOf(77800359970l));
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