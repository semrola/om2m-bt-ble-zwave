package org.eclipse.om2m.binding.bt;

import java.util.ArrayList;
import java.util.Vector;

import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.commons.constants.MimeMediaType;
import org.eclipse.om2m.commons.resource.AE;
import org.eclipse.om2m.commons.resource.Container;
import org.eclipse.om2m.commons.resource.ContentInstance;
import org.eclipse.om2m.core.service.CseService;

public class BluetoothMonitor {
	
	static String CSE_ID = Constants.CSE_ID;
	public static CseService CSE;
	
	ArrayList<Vector<ServiceRecord>> services;
	ArrayList<RemoteDevice> devices;
	ArrayList<int[]> classes;
	
	private static Log logger = LogFactory.getLog(BluetoothMonitor.class);
		
	public static void start() {
		createAE();
		createBluetoothResources();				
	}
	
	private static void createAE() {
		logger.info("Creating Bluetooth AE...");
		AE ae = new AE();
		ae.setRequestReachability(true);
		ae.getPointOfAccess().add(Constants.POA_BT);
		ae.setAppID(Constants.POA_BT);
		RequestSender.createAE(ae, Constants.BLUETOOTH);
	}
	
	public static void createBluetoothResources(){
		// Create the BLUETOOTH container 
		String targetId = Constants.CSE_PREFIX + "/" + Constants.BLUETOOTH;
		//Container cnt = new Container();		
		//cnt.setMaxNrOfInstances(BigInteger.valueOf(10));
		//RequestSender.createContainer(targetId, Constants.BLUETOOTH, cnt);
 
		//create DESCRIPTOR container inside BLUETOOTH container
		//targetId += "/" + Constants.BLUETOOTH;
		RequestSender.createContainer(targetId, Constants.DESC, new Container());
		
		// Create the BLUETOOTH DESCRIPTOR contentInstance
		String content = ObixUtil.getBluetoothDescriptorRep(Constants.CSE_PREFIX + "/" + Constants.BLUETOOTH);
		ContentInstance cin = new ContentInstance();
		cin.setContent(content);
		cin.setContentInfo(MimeMediaType.OBIX);
		RequestSender.createContentInstance(targetId + "/" + Constants.DESC, cin);
		
//		logger.info("RESPONSE: " + resp);
//		logger.info("CSE-ID: " + Constants.CSE_ID);
//		logger.info("CSE-PREFIX: " + Constants.CSE_PREFIX);
//		logger.info("#1: " + resp.getLocation()); //isto kot #5, npr: /mn-cse/cin-97343144
//		logger.info("#2: " + resp.getRequestIdentifier());	
//		logger.info("#3: " + resp.getTo());
//		logger.info("#4: " + resp.getContentType());
//		logger.info("#5: " + ((ContentInstance)resp.getContent()).getResourceID());
//		//logger.info("#5: " + ((ContentInstance)resp.getContent()).get);
//		Controller.ciId = resp.getLocation();
		
		// create DEVICES container
		RequestSender.createContainer(targetId, Constants.DEVICES, new Container());
		
		new Thread() {
			public void run() {
				Controller.inquiryActive = true;
				Client.startClient();
				Controller.inquiryActive = false;
			}
		}.start();
	}
	
	public static void addDevice(String address, String obixDescRep) {
		String targetId = Constants.CSE_PREFIX +"/"+Constants.BLUETOOTH+"/"+Constants.DEVICES;
		RequestSender.createContainer(targetId, address, new Container());	//naredi vsebnik naprave
		RequestSender.createContainer(targetId + "/" + address, Constants.DESC, new Container());	//naredi DESC naprave
		RequestSender.createContainer(targetId + "/" + address, Constants.DATA, new Container());	//naredi DATA naprave
		ContentInstance ci = new ContentInstance();
		ci.setContent(obixDescRep);
		RequestSender.createContentInstance(targetId+"/"+address+"/"+Constants.DESC, ci);	//dodaj opis naprave
		RequestSender.createContainer(targetId+"/"+address+"/"+Constants.DESC, Constants.SERVICES, new Container());	//dodaj servise naprave
	}
	
	public static void addService(String address, String obixServiceRep) {
		String targetId = Constants.CSE_PREFIX + "/" + Constants.BLUETOOTH + "/"
				+ Constants.DEVICES + "/" + address + "/" + Constants.DESC + "/" + Constants.SERVICES;
		ContentInstance ci = new ContentInstance();
		ci.setContent(obixServiceRep);
		RequestSender.createContentInstance(targetId, ci);	//dodaj opis naprave
	}
}
