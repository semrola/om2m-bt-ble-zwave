package org.eclipse.om2m.binding.bt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.bluetooth.DataElement;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Client {
	private static Log logger = LogFactory.getLog(Client.class);
	
	public static ArrayList<Vector<ServiceRecord>> services;
	public static ArrayList<RemoteDevice> devices;
	public static ArrayList<int[]> classes;
	

	public ArrayList<Vector<ServiceRecord>> getServices() {
		return services;
	}

	public static void startClient() {
		UUID[] uuids = new UUID[]{
				//ServiceUUIDs.HTTP.getUUID(),
				/*ServiceUUIDs.L2CAP.getUUID(),
				ServiceUUIDs.RFCOMM.getUUID(),
				ServiceUUIDs.OBEX.getUUID(),
				ServiceUUIDs.SDP.getUUID(),
				ServiceUUIDs.SERIAL_PORT.getUUID(),*/
				//new UUID(0x1800),
				//new UUID(0x1801),
				//new UUID(0x2800),
				//new UUID(0x2801),
				//new UUID(Service.ALERT_NOTIFICATION_SERVICE.toString().replaceAll("-", ""), false),
				
		};
		
		int[] attributes = new int[] {
				0x0100	// service name
		};
		
		DeviceAndServiceSearch search = new DeviceAndServiceSearch();
		search.startServiceSearch(attributes, uuids);
		
		services = search.getServices();
		devices = search.getDevices();
		classes = search.getDeviceClasses();
		
		System.out.println("Services size: " + services.size());
		System.out.println("Devices size: " + devices.size());	
		
		for(int i = 0; i < devices.size(); i++) {
			RemoteDevice device = devices.get(i);
//			logger.debug("Address: " + device.getBluetoothAddress());
			String deviceAddress = device.getBluetoothAddress();
			String deviceName = "Device name could not be retrieved.";
			try {
//				logger.debug("Name: " + device.getFriendlyName(false));
				deviceName = device.getFriendlyName(false);
			} catch (IOException e) {
				logger.error(e);
			}
			
			int[] deviceClass = classes.get(i);
//			System.out.println("Device class major: " + deviceClass[0]);
//			System.out.println("Device class minor: " + deviceClass[1]);			

			Vector<ServiceRecord> deviceServices = services.get(i);
			String obixDescRep = ObixUtil.getBluetoothDeviceDescRep(deviceName, deviceClass[0], deviceClass[1], deviceServices.size());
//			System.out.println(obixDescRep);
			BluetoothMonitor.addDevice(deviceAddress, obixDescRep);
			
			System.out.println("Device: "+deviceName+" has " + deviceServices.size() + " services");
			for(ServiceRecord sr : deviceServices) {
				if(sr != null) {
					System.out.println(sr.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false));
					DataElement de = sr.getAttributeValue(0x0100);
					if(de != null) {
						System.out.println(de.getValue());	//name
					}
					else {
						System.out.println("No value");
					}
					System.out.println(Long.toHexString(sr.getAttributeValue(0x0000).getLong()));	//record handle
				}
				String obixServiceRep = ObixUtil.getBluetoothServiceRep(sr);
//				System.out.println(obixServiceRep);
				BluetoothMonitor.addService(deviceAddress, obixServiceRep);
			}
		}
	}
	
	public static void main(String[] args) {
		startClient();		
	}	

}
