package org.eclipse.om2m.binding.zwave;

import org.eclipse.om2m.commons.exceptions.BadRequestException;

public enum Operations {
	
	ZWAVE_ALL_ON("on"),
	ZWAVE_ALL_OFF("off"),
	ZWAVE_START_INCLUSION("start_inclusion"),
	ZWAVE_STOP_INCLUSION("stop_inclusion"),
	ZWAVE_START_REMOVING_DEVICES("start_removing"),
	ZWAVE_STOP_REMOVING_DEVICES("stop_removing");
	
	private final String value;
	
	private Operations(String operation) {
		this.value = operation;
	}
	
	public String toString() {
		return value;
	}
	
	public String getValue(){
		return value;
	}
	
	/**
	 * Return the operation from the string
	 * @param operation
	 * @return
	 */
	public static Operations getOperationFromString(String operation){
		for(Operations op : values()){
			if(op.getValue().equals(operation)){
				return op;
			}
		}
		throw new BadRequestException("Unknown Operation for Bluetooth");
	}
}
