package com.solidwater.disgaea5bot.entity;

import java.math.BigInteger;

import com.solidwater.disgaea5bot.entity.type.DynamicMemory;
import com.solidwater.disgaea5bot.util.NativeUtils;
import com.solidwater.disgaea5bot.util.exception.WindowsAPIException;

public class PlayerCharacter {
	// POS base offset was achieved by "disgaea5.exe"+7A6A518. disgaea5.exe
	// corresponds to the process's base address.
	public static final BigInteger CURRENT_POS_BASE_OFFSET = BigInteger.valueOf(0x7A6A518l);
	public static final BigInteger CURRENT_Y_WAYPOINT_OFFSET = BigInteger.valueOf(0x8l);
	public static final BigInteger CURRENT_X_WAYPOINT_OFFSET = BigInteger.valueOf(0x10l);
	public static final BigInteger CURRENT_Z_WAYPOINT_OFFSET = BigInteger.valueOf(0xCl);

	public static final BigInteger CURRENT_TARGET_HP_BASE_OFFSET = BigInteger.valueOf(0xB66A20l);
	public static final BigInteger CURRENT_TARGET_HP_OFFSET_1 = BigInteger.valueOf(0x98l);
	public static final BigInteger CURRENT_TARGET_HP_OFFSET_2 = BigInteger.valueOf(0x4D8l);

	public static final BigInteger MONEY_BASE_OFFSET = BigInteger.valueOf(0x4FCD518l);

	protected Process process = null;

	private DynamicMemory<Float> currentYPos;
	private DynamicMemory<Float> currentXPos;
	private DynamicMemory<Float> currentZPos;
	private DynamicMemory<Integer> currentTargetHP;
	private DynamicMemory<BigInteger> money;

	public PlayerCharacter(Process process) throws WindowsAPIException {
		this.process = process;

		populateEntity();
	}

	protected void populateEntity() throws WindowsAPIException {
		this.currentYPos = new DynamicMemory<>();
		this.currentXPos = new DynamicMemory<>();
		this.currentZPos = new DynamicMemory<>();
		this.currentTargetHP = new DynamicMemory<>();
		this.money = new DynamicMemory<>();
		this.currentYPos.setValue(this.process.getFloatValue(new BigInteger[] { PlayerCharacter.CURRENT_POS_BASE_OFFSET,
				PlayerCharacter.CURRENT_Y_WAYPOINT_OFFSET }));
		this.currentYPos.setDynamicAddress(NativeUtils.getLastDynamicAddress());
		this.currentXPos.setValue(this.process.getFloatValue(new BigInteger[] { PlayerCharacter.CURRENT_POS_BASE_OFFSET,
				PlayerCharacter.CURRENT_X_WAYPOINT_OFFSET }));
		this.currentXPos.setDynamicAddress(NativeUtils.getLastDynamicAddress());
		this.currentZPos.setValue(this.process.getFloatValue(new BigInteger[] { PlayerCharacter.CURRENT_POS_BASE_OFFSET,
				PlayerCharacter.CURRENT_Z_WAYPOINT_OFFSET }));
		this.currentZPos.setDynamicAddress(NativeUtils.getLastDynamicAddress());
		this.currentTargetHP
				.setValue(this.process.getIntValue(new BigInteger[] { PlayerCharacter.CURRENT_TARGET_HP_BASE_OFFSET,
						PlayerCharacter.CURRENT_TARGET_HP_OFFSET_1, PlayerCharacter.CURRENT_TARGET_HP_OFFSET_2 }));
		this.currentTargetHP.setDynamicAddress(NativeUtils.getLastDynamicAddress());
		this.money.setValue(this.process.getBigIntegerValue(new BigInteger[] { PlayerCharacter.MONEY_BASE_OFFSET }));
		this.money.setDynamicAddress(NativeUtils.getLastDynamicAddress());
	}

	public float getCurrentYPos() throws WindowsAPIException {
		populateEntity();
		return this.currentYPos.getValue().floatValue();
	}

	public void setCurrentYPos(float newCurrentYPos) throws WindowsAPIException {
		this.process.setValue(this.currentYPos.getDynamicAddress(), Float.valueOf(newCurrentYPos));
	}

	public float getCurrentXPos() throws WindowsAPIException {
		populateEntity();
		return this.currentXPos.getValue().floatValue();
	}

	public void setCurrentXPos(float newCurrentXPos) throws WindowsAPIException {
		this.process.setValue(this.currentXPos.getDynamicAddress(), Float.valueOf(newCurrentXPos));
	}

	public float getCurrentZPos() throws WindowsAPIException {
		populateEntity();
		return this.currentZPos.getValue().floatValue();
	}

	public void setCurrentZPos(float newCurrentZPos) throws WindowsAPIException {
		this.process.setValue(this.currentZPos.getDynamicAddress(), Float.valueOf(newCurrentZPos));
	}

	public int getCurrentTargetHP() throws WindowsAPIException {
		populateEntity();
		return this.currentTargetHP.getValue().intValue();
	}

	public void setCurrentTargetHP(int newCurrentTargetHP) throws WindowsAPIException {
		this.process.setValue(this.currentTargetHP.getDynamicAddress(), Integer.valueOf(newCurrentTargetHP));
	}

	public BigInteger getMoney() throws WindowsAPIException {
		populateEntity();
		return this.money.getValue();
	}

	public void setMoney(BigInteger newMoney) throws WindowsAPIException {
		this.process.setValue(this.money.getDynamicAddress(), newMoney);
	}
}