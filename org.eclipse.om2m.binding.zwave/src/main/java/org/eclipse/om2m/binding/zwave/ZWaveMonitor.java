package org.eclipse.om2m.binding.zwave;

import java.math.BigInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.commons.constants.MimeMediaType;
import org.eclipse.om2m.commons.resource.AE;
import org.eclipse.om2m.commons.resource.Container;
import org.eclipse.om2m.commons.resource.ContentInstance;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;
import org.eclipse.om2m.core.service.CseService;

public class ZWaveMonitor {
	private static Log logger = LogFactory.getLog(ZWaveMonitor.class);
	static String CSE_ID = Constants.CSE_ID;
	public static CseService CSE;
	
	public static void start() {
		createAE();
		createZwaveResources();
	}
	
	private static void createAE() {
		logger.info("Creating ZWave AE...");
		AE ae = new AE();
		ae.setRequestReachability(true);
		ae.getPointOfAccess().add(Constants.POA_ZWAVE);
		ae.setAppID(Constants.POA_ZWAVE);
		RequestSender.createAE(ae, Constants.ZWAVE);
	}
	
	private static void createZwaveResources() {
		logger.info("Creating ZWave resources...");
		// ZWAVE container
		String targetId = Constants.CSE_PREFIX + "/" + Constants.ZWAVE;
		//Container cnt = new Container();		
		//cnt.setMaxNrOfInstances(BigInteger.valueOf(10));
		//RequestSender.createContainer(targetId, Constants.ZWAVE, cnt);
		
		//targetId += "/" + Constants.ZWAVE;
		
		// inclusion container
		//String inclCnt = "DESCRIPTOR";
		RequestSender.createContainer(targetId, Constants.DESC, new Container());
		
		// contentInstance, ki vsebuje gumba start in stop including
		ContentInstance cin = new ContentInstance();
		String content = ObixUtil.getZwaveInclusionRep(Constants.CSE_PREFIX + "/" + Constants.POA_ZWAVE);
		cin.setContent(content);
		cin.setContentInfo(MimeMediaType.OBIX);
		RequestSender.createContentInstance(targetId + "/" + Constants.DESC, cin);
		
		// create DATA container
		RequestSender.createContainer(targetId, Constants.DEVICES, new Container());
		
		// start ZWAVE listening
		new Thread() {
			public void run() {
				ZWaveMain.startListening();
			}
		}.start();
	}
	
	public static void addNode(String name) {
		String targetId = Constants.CSE_PREFIX +"/"+Constants.ZWAVE+"/"+Constants.DEVICES;
		RequestSender.createContainer(targetId, name, new Container());
		RequestSender.createContainer(targetId + "/" + name, Constants.DESC, new Container());
		RequestSender.createContainer(targetId + "/" + name, Constants.DATA, new Container());
	}
	
	public static void addNodeValue(String nodeName, String obixRep) {
		String targetId = Constants.CSE_PREFIX + "/" +Constants.ZWAVE+"/"+Constants.DEVICES+"/"+nodeName+"/"+Constants.DATA;
		ContentInstance ci = new ContentInstance();
		ci.setContent(obixRep);
		ci.setContentInfo(MimeMediaType.OBIX);
		RequestSender.createContentInstance(targetId, ci);
	}
	
}
