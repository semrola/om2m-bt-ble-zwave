package org.eclipse.om2m.binding.ble;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.commons.obix.Contract;
import org.eclipse.om2m.commons.obix.Obj;
import org.eclipse.om2m.commons.obix.Op;
import org.eclipse.om2m.commons.obix.Str;
import org.eclipse.om2m.commons.obix.Uri;
import org.eclipse.om2m.commons.obix.io.ObixEncoder;

import com.movisens.smartgattlib.Characteristic;
import com.movisens.smartgattlib.Descriptor;
import com.movisens.smartgattlib.GattUtils;
import com.movisens.smartgattlib.Service;

public class ObixUtil {
	
	private static Log logger = LogFactory.getLog(ObixUtil.class);
	
	public static String getStringRep(String attr, String str) {
		Obj obj = new Obj();
		Str s = new Str(attr, str);
		obj.add(s);
		String rep = ObixEncoder.toString(obj);
		return rep;
	}
	
	public static String getBleDeviceRep(String address, String name) {
		Obj obj = new Obj();
		obj.add(new Str("Device MAC address", address));
		obj.add(new Str("Device name", name));
		return ObixEncoder.toString(obj);
	}
	
	public static String getBleServiceRep(String uuid, String attr, String end) {
		Obj obj = new Obj();
		obj.add(new Str("Service UUID", uuid));
		obj.add(new Str("Attr handle", attr));
		obj.add(new Str("End grp handle", end));
		String name = Service.lookup(GattUtils.toUuid(uuid), "Service name not found");
		obj.add(new Str("Service name", name));
		return ObixEncoder.toString(obj);
	}
	
	public static String getBleCharRep(String hnd, String valhnd, String prop, String uuid) {
		Obj obj = new Obj();
		obj.add(new Str("Characteristic UUID", uuid));
		obj.add(new Str("Handle", hnd));
		obj.add(new Str("Value handle", valhnd));
		String name = Characteristic.lookup(GattUtils.toUuid(uuid), "Characteristic name not found");
		obj.add(new Str("Characteristic name", name));
		obj.add(new Str("Properties", Numbers.getCharacteristicProperties(prop, true)));
		return ObixEncoder.toString(obj);
	}
	
	public static String getBleDescriptorRep(String uuid, String value) {
		Obj obj = new Obj();
		obj.add(new Str("Descriptor UUID", uuid));
		obj.add(new Str("Descriptor value", value));
		obj.add(new Str("Descriptor name", Descriptor.lookup(GattUtils.toUuid(uuid), "Descriptor name not found")));
		return ObixEncoder.toString(obj);
	}
	
	public static String getBleDescRep(String target) {
		Obj obj = new Obj();
		Op op = new Op();
		op.setName("Start BLE scanning");
		op.setHref(new Uri(target + "?op=" + Operations.BLE_START_SCAN));
		op.setIs(new Contract("execute"));
		obj.add(op);
		
		Op op1 = new Op();
		op1.setName("Stop BLE scanning");
		op1.setHref(new Uri(target + "?op=" + Operations.BLE_STOP_SCAN));
		op1.setIs(new Contract("execute"));
		obj.add(op1);
		
		Op op2 = new Op();
		op2.setName("Start BLE reading");
		op2.setHref(new Uri(target + "?op=" + Operations.BLE_START_READING));
		op2.setIs(new Contract("execute"));
		obj.add(op2);
		
		Op op3 = new Op();
		op3.setName("Stop BLE reading");
		op3.setHref(new Uri(target + "?op=" + Operations.BLE_STOP_READING));
		op3.setIs(new Contract("execute"));
		obj.add(op3);
		
		return ObixEncoder.toString(obj);
	}
}