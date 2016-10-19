package org.eclipse.om2m.binding.ble;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.commons.resource.AE;
import org.eclipse.om2m.commons.resource.Container;
import org.eclipse.om2m.commons.resource.ContentInstance;

public class BleMonitor {
	
	private static Log logger = LogFactory.getLog(BleMonitor.class);
	public static boolean threadAlive = false;
	
	public static void start() {
		createAE();
		createBleResources();
		
		new Thread() {
			public void run() { 
				threadAlive = true;
				logger.info("Starting BLE...");
				ReadGatt.startScanning();
				threadAlive = false;
			}
		}.start();
	}
	
	private static void createAE() {
		logger.info("Creating BLE AE...");
		AE ae = new AE();
		ae.setRequestReachability(true);
		ae.getPointOfAccess().add(Constants.POA_BLE);
		ae.setAppID(Constants.POA_BLE);
		RequestSender.createAE(ae, Constants.BLE);
	}
	
	public static void createBleResources() {
		String targetId = Constants.CSE_PREFIX + "/" + Constants.BLE;
		//RequestSender.createContainer(targetId, Constants.BLE, new Container());
		
		RequestSender.createContainer(targetId, Constants.DESC, new Container());
		ContentInstance desc = new ContentInstance();
		desc.setContent(ObixUtil.getBleDescRep(targetId));
		RequestSender.createContentInstance(targetId+"/"+Constants.DESC, desc);
		
		RequestSender.createContainer(targetId, Constants.DEVICES, new Container());
	}
	
	static String devicesTargetId = Constants.CSE_PREFIX + "/" + Constants.BLE + "/"
			+ Constants.DEVICES;
	
	public static void addDevice(String address, String name) {
		address = correctAddress(address);
		RequestSender.createContainer(devicesTargetId, address, new Container());
		
		String targetId = devicesTargetId+"/"+address;
		RequestSender.createContainer(targetId, Constants.DESC, new Container());
		ContentInstance desc = new ContentInstance();
		desc.setContent(ObixUtil.getBleDeviceRep(address, name));
		RequestSender.createContentInstance(targetId+"/"+Constants.DESC, desc);
		
		RequestSender.createContainer(targetId, Constants.SERVICES, new Container());
	}
	
	public static void addService(String address, String uuid, String obixServiceRep) {
		logger.info("Adding service " + uuid);
		address = correctAddress(address);
		String targetId = devicesTargetId+"/"+address+"/"+Constants.SERVICES;
		RequestSender.createContainer(targetId, uuid, new Container());
		
		RequestSender.createContainer(targetId+"/"+uuid, Constants.DESC, new Container());
		ContentInstance desc = new ContentInstance();
		desc.setContent(obixServiceRep);
		RequestSender.createContentInstance(targetId+"/"+uuid+"/"+Constants.DESC, desc);
		
		RequestSender.createContainer(targetId+"/"+uuid, Constants.CHARS, new Container());
		
	}
	
	public static void addCharacteristic(String address, String serviceUuid, String charUuid, String obixCharRep) {
		logger.info("Adding char. " + charUuid);
		address = correctAddress(address);
		String targetId = devicesTargetId+"/"+address+"/"+Constants.SERVICES+"/"+serviceUuid+"/"+Constants.CHARS;
		RequestSender.createContainer(targetId, charUuid, new Container());
		
		RequestSender.createContainer(targetId+"/"+charUuid, Constants.DESC, new Container());
		ContentInstance desc = new ContentInstance();
		desc.setContent(obixCharRep);
		RequestSender.createContentInstance(targetId+"/"+charUuid+"/"+Constants.DESC, desc);		
		
		RequestSender.createContainer(targetId+"/"+charUuid, Constants.DATA, new Container());
		RequestSender.createContainer(targetId+"/"+charUuid, Constants.DESCRIPTORS, new Container());
	}
	
	public static void addDescriptor(String address, String serviceUuid, String charUuid, String obixDescRep) {
		logger.info("Adding descriptor ");
		address = correctAddress(address);
		String targetId = devicesTargetId + "/" + address + "/" + Constants.SERVICES + "/" + serviceUuid + "/"
				+ Constants.CHARS + "/" + charUuid + "/" + Constants.DESCRIPTORS;
		ContentInstance descriptor = new ContentInstance();
		descriptor.setContent(obixDescRep);
		RequestSender.createContentInstance(targetId, descriptor);	
	}
	
	public static void addValue(String address, String sU, String cU, String obixValRep) {
		logger.info("Adding value");
		address = correctAddress(address);
		String targetId = devicesTargetId+"/"+address+"/"+Constants.SERVICES+"/"+sU+"/"+Constants.CHARS+"/"+cU+"/"+Constants.DATA;
		ContentInstance ci = new ContentInstance();
		ci.setContent(obixValRep);
		RequestSender.createContentInstance(targetId, ci);
	}
	
	public static String correctAddress(String address) {
		return address.replaceAll(":", "-");
	}
}
