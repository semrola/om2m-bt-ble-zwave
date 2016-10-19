package org.eclipse.om2m.binding.ble;

import java.util.HashMap;
import java.util.UUID;

import com.movisens.smartgattlib.GattUtils;

public class Numbers {
	public static final UUID CHAR_DECLARATION = new UUID((0x2803L << 32) | 0x1000, GattUtils.leastSigBits);
	public static final UUID PRIMARY_SERVICE = new UUID((0x2800L << 32) | 0x1000, GattUtils.leastSigBits);
	public static final UUID SECONDARY_SERVICE = new UUID((0x2801L << 32) | 0x1000, GattUtils.leastSigBits);
	public static final UUID INCLUDE = new UUID((0x2802L << 32) | 0x1000, GattUtils.leastSigBits);
	
	private static HashMap<UUID, String> attributes = new HashMap<UUID, String>();
	
	static {
		attributes.put(CHAR_DECLARATION, "Characteristic declaration attribute");
	}
	
	public static String lookup(UUID uuid, String defaultName) {
		String name = attributes.get(uuid);
		return name == null ? defaultName : name;
	}
	
	// bit 0 = broadcast, bit 7 = Extended Props; ni treba obracati stringa in gremo od leve proti desni
	public static final String[] properties = {"Extended Properties",
			"Authenticated Signed Writes", "Indicate", "Notify", "Write", "Write Without Response",
			"Read", "Broadcast"};
	
	public static String getCharacteristicProperties(byte property) {
		String binary = String.format("%8s", Integer.toBinaryString(property & 0xFF)).replace(' ', '0');
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<binary.length();i++) {
			if(String.valueOf(binary.charAt(i)).equals("1")) {
				sb.append(properties[i] + ", ");
			}
		}
		return sb.toString();		
	}
	
	public static String getCharacteristicProperties(String hexProperty, boolean zeroXIncluded) {
		if(zeroXIncluded)
			hexProperty = hexProperty.substring(2);
		int in = Integer.parseInt(hexProperty, 16);
//		byte b = Byte.parseByte(hexProperty);
	    return getCharacteristicProperties((byte)in);
	}
	
	public static void main(String args[]) {
		byte prop = 0x22;
		String prop1 = "0x10";
		System.out.println(getCharacteristicProperties(prop1, true));		
		System.out.println(getCharacteristicProperties(prop));
	}	
}
