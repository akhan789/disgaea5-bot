package com.solidwater.disgaea5bot.entity;

import com.solidwater.disgaea5bot.util.exception.WindowsAPIException;

public class Disgaea5Process extends AbstractProcess {
	public static final String WINDOW_TITLE = "Disgaea 5 Complete";
	public static final String PROCESS_FILENAME = "disgaea5.exe";

	public Disgaea5Process() throws WindowsAPIException {
		super(Disgaea5Process.WINDOW_TITLE, Disgaea5Process.PROCESS_FILENAME);
	}
}