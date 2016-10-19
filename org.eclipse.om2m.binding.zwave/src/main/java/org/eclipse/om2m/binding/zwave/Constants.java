package org.eclipse.om2m.binding.zwave;


public class Constants {
		
	private Constants(){}
	
	public static final String POA_BT = "bluetooth";
	public static final String POA_BLE = "ble";
	public static final String POA_ZWAVE= "zwave";
	public static final String POA = "autodetection";
	
	public static final String DATA = "DATA";
	public static final String DESC = "DESCRIPTOR";
	public static final String AE_NAME = "ZWAVE";
	public static final String DEVICES = "DEVICES";
	public static final String SERVICES = "SERVICES";
	public static final String CHARS = "CHARACTERISTICS";
	public static final String DESCRIPTORS = "BLE_DESCRIPTORS";
	
	public static final String BLUETOOTH = "BLUETOOTH";
	public static final String ZWAVE = "ZWAVE";
	public static final String BLE = "BLUETOOTH_LE";
	
	public static final String TYPE = "type";
	
	public static String CSE_ID = "/" + org.eclipse.om2m.commons.constants.Constants.CSE_ID;
	public static String CSE_PREFIX = CSE_ID + "/" + org.eclipse.om2m.commons.constants.Constants.CSE_NAME;
	public static String REQUEST_ENTITY = org.eclipse.om2m.commons.constants.Constants.ADMIN_REQUESTING_ENTITY;
}
