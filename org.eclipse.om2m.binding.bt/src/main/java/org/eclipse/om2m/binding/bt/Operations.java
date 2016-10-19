package org.eclipse.om2m.binding.bt;

import org.eclipse.om2m.commons.exceptions.BadRequestException;

public enum Operations {
	
	INQUIRY("inquiry"),
	DELETE("delete"),
	BT_OPEN_CONNECTION("open_connection");	
	
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
