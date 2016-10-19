package org.eclipse.om2m.binding.bt;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.bluetooth.DataElement;
import javax.bluetooth.ServiceRecord;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.commons.obix.Contract;
import org.eclipse.om2m.commons.obix.Int;
import org.eclipse.om2m.commons.obix.Obj;
import org.eclipse.om2m.commons.obix.Op;
import org.eclipse.om2m.commons.obix.Str;
import org.eclipse.om2m.commons.obix.Uri;
import org.eclipse.om2m.commons.obix.io.ObixEncoder;

public class ObixUtil {
	
	private static Log logger = LogFactory.getLog(ObixUtil.class);
	
	public static String getBluetoothDescriptorRep(String targetId) {
		
		Obj obj = new Obj();
		
		// get latest data
		Op opGet = new Op();
		opGet.setName("Start Bluetooth Inquiry");
		opGet.setHref(new Uri(targetId + "?op=" + Operations.INQUIRY));
		opGet.setIs(new Contract("execute"));
		//opGet.setIn(new Contract("obix:Nil"));
		//opGet.setOut(new Contract("obix:Nil"));
		obj.add(opGet);
		String rep = ObixEncoder.toString(obj);
		logger.debug("Description Representation " + rep);
		return rep; 
	}
	
	
	
	public static String getStringRep(String attr, String str) {
		Obj obj = new Obj();
		Str s = new Str(attr, str);
		obj.add(s);
		String rep = ObixEncoder.toString(obj);
		return rep;
	}
	
	public static String getBluetoothDeviceDescRep(String deviceName, int major, int minor, int numServices) {
		Obj obj = new Obj();
		obj.add(new Str("Device name", deviceName));
		obj.add(new Int("Major class", major));
		obj.add(new Int("Minor class", minor));	
		obj.add(new Int("Number of services", numServices));	
		
		return ObixEncoder.toString(obj);
	}
	
	public static String getBluetoothServiceRep(ServiceRecord sr) {
		Obj obj = new Obj();
		if(sr != null) {			
			DataElement de = sr.getAttributeValue(0x0100);	//name
			String serviceName = "No service name";
			if(de != null) {
				serviceName = de.getValue().toString().trim();
			}
						
			DataElement rh = sr.getAttributeValue(0x0000);	//record handle
			String recordHandle = "No record handle";
			if(rh != null) {
				recordHandle = Long.toHexString(rh.getLong());
			}
			String conUrl = sr.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
			
			obj.add(new Str("Service name", serviceName));
			obj.add(new Str("Record handle", recordHandle));
			obj.add(new Str("Connection URL", conUrl));
			if(conUrl != null && conUrl.startsWith("btspp")) {
				String targetId = Constants.CSE_PREFIX + "/" + Constants.BLUETOOTH;
				Op open = new Op();
				open.setName("Open connection");
				try {
					open.setHref(new Uri(targetId + "?op=" + Operations.BT_OPEN_CONNECTION + "&href="+URLEncoder.encode(conUrl, "UTF-8")));
				} catch (UnsupportedEncodingException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
				open.setIs(new Contract("execute"));
				obj.add(open);
			}
		}
		return ObixEncoder.toString(obj);
	}
}