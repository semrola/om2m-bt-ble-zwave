package org.eclipse.om2m.binding.ble;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReadGatt {

	private static Log logger = LogFactory.getLog(ReadGatt.class);

	/*
	 * CHARACTERISTIC EXAMPLE
	 * handle = 0x0002, char properties = 0x0a, char value handle = 0x0003, uuid = 00002a00-0000-1000-8000-00805f9b34fb
	   handle = 0x0004, char properties = 0x02, char value handle = 0x0005, uuid = 00002a01-0000-1000-8000-00805f9b34fb
	   handle = 0x0006, char properties = 0x0a, char value handle = 0x0007, uuid = 00002a02-0000-1000-8000-00805f9b34fb
	 * 
	 * SERVICE EXAMPLE
	 *  attr handle = 0x0001, end grp handle = 0x000b uuid: 00001800-0000-1000-8000-00805f9b34fb
		attr handle = 0x000c, end grp handle = 0x000f uuid: 00001801-0000-1000-8000-00805f9b34fb
		attr handle = 0x0010, end grp handle = 0x0016 uuid: 0000fee8-0000-1000-8000-00805f9b34fb
		attr handle = 0x0017, end grp handle = 0x001d uuid: 0000fee9-0000-1000-8000-00805f9b34fb

	 * 
	 */
	
	public static final String BLE_DEVICE = "hci1";
	public static final String propertiesPattern = "(?<=properties = 0x).{2}";
	public static final Pattern MAC_PATTERN = Pattern.compile("([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})");
	public static final Pattern DEVICE_PATTERN = Pattern.compile(Pattern.quote("(?<= ).+"));
	public static final Pattern SERVICE_ATTR_HANDLE_PATTERN = Pattern.compile("(?<=attr handle = ).{6}");
	public static final Pattern SERVICE_ENDGRP_HANDLE_PATTERN = Pattern.compile("(?<=end grp handle = ).{6}");
	public static final Pattern SERVICE_UUID_PATTERN = Pattern.compile("(?<=uuid: ).*+");
	public static final Pattern CHAR_HANDLE_PATTERN = Pattern.compile("(?<=handle = ).{6}");
	public static final Pattern CHAR_PROP_PATTERN = Pattern.compile("(?<=char properties = ).{4}");
	public static final Pattern CHAR_VALUE_HANDLE_PATTERN = Pattern.compile("(?<=char value handle = ).{6}");
	public static final Pattern CHAR_UUID_PATTERN = Pattern.compile("(?<=uuid = ).+");
	public static final Pattern CHAR_VAL_DESC_PATTERN = Pattern.compile("(?<=Characteristic value/descriptor: ).+");	//ce beres vrednosti preko handle
	public static final Pattern CHAR_VAL_UUID_PATTERN = Pattern.compile("(?<=value: ).+");	//ce beres vrednosti preko uuid
	
	public static void main(String[] args) throws Exception {
		
		
		
	    
	    
	}
	
	public static HashMap<String, String> oldDevices = new HashMap<>();
	public static HashMap<String, String> newDevices = new HashMap<>();
	public static ArrayList<String[]> charValues = new ArrayList<>();	//macAddress|service|char|value
	public static boolean readingThreadAlive = false;
	
	public static boolean scanning = true;
	public static int scanningInterval = 30;
	public static void startScanning() {
		logger.info("Scanning started.");
		String scanResult;
		boolean first = true;
		while(scanning) {
			scanResult = process("timeout --signal SIGINT 5s hcitool lescan");
			extractAddresses(scanResult);
			addNewDevices();
			synchronized (charValues) {
				searchServices();
			}
			if(first) {
				new Thread() {
					public void run() {
						readingThreadAlive = true;
						refreshValues();
						readingThreadAlive = false;
					}
				}.start();
				first = false;
			}
			newDevices.clear();
			
			try {
				logger.debug("Scan sleeping...");
				Thread.sleep(scanningInterval*1000);
			} catch (InterruptedException e) {
				logger.debug("Scan cannot sleep.");
				e.printStackTrace();
			}
		}
		logger.info("Scanning stopped.");
	}
	
	public static void addNewDevices() {
		for(Map.Entry<String, String> entry:newDevices.entrySet()) {
	        String key = entry.getKey();
	        String value = entry.getValue();
	        BleMonitor.addDevice(key, value);
	        oldDevices.put(key, value);
	    }
	}
	
	public static boolean searchingServices = true;	
	public static void searchServices() {
		for(String address:newDevices.keySet()) {
	        logger.debug("Searching services for " + address);
//	        String value = pair.getValue();
	        String primaryOutput = process(getPrimaryCommand(address));
	        
	        for(String line:primaryOutput.split("\n")) {			
				String primaryUUid="",primaryAttr="",primaryEnd="";
				Matcher m = SERVICE_UUID_PATTERN.matcher(line);
				primaryUUid = m.find() ? m.group(0) : "";
				m = SERVICE_ATTR_HANDLE_PATTERN.matcher(line);
				primaryAttr = m.find() ? m.group(0) : "";
				m = SERVICE_ENDGRP_HANDLE_PATTERN.matcher(line);
				primaryEnd = m.find() ? m.group(0) : "";
				
				//dodamo vsak service posebej
				BleMonitor.addService(address, primaryUUid, ObixUtil.getBleServiceRep(primaryUUid, primaryAttr, primaryEnd));
				
				//dodamo vse karakteristike servisa
				String chars = process(getCharsCommand(address, primaryAttr, primaryEnd));
				for(String characteristic:chars.split("\n")) {
					String charUuid="",charValHnd="",charHnd="",charProp="";
					m = CHAR_UUID_PATTERN.matcher(characteristic);
					charUuid = m.find() ? m.group(0) : "";
					m = CHAR_VALUE_HANDLE_PATTERN.matcher(characteristic);
					charValHnd = m.find() ? m.group(0) : "";
					m = CHAR_PROP_PATTERN.matcher(characteristic);
					charProp = m.find() ? m.group(0) : "";
					m = CHAR_HANDLE_PATTERN.matcher(characteristic);
					charHnd = m.find() ? m.group(0) : "";
					
					String obixCharRep = ObixUtil.getBleCharRep(charHnd, charValHnd, charProp, charUuid);
					BleMonitor.addCharacteristic(address, primaryUUid, charUuid, obixCharRep);
					
					//dodamo vrednosti karakteristik v seznam za spremljanje
					charValues.add(new String[]{address, primaryUUid, charUuid, ""});	//za value damo prazno
					
					String charDescOutput = process(getCharDescCommand(address, charHnd));
					HashMap<String, String> descriptors = getDescriptorHandlesNew(charDescOutput, address);
					for(Map.Entry<String, String> par : descriptors.entrySet()) {
				        String descUuid = par.getKey();
				        String descValue = par.getValue();
				        String obixDescRep = ObixUtil.getBleDescriptorRep(descUuid, descValue);
				        BleMonitor.addDescriptor(address, primaryUUid, charUuid, obixDescRep);
				    }
				}
			}
	    }
	}
	
	public static int refreshTime = 20;	//seconds
	public static boolean refreshing = true;
	public static void refreshValues() {
		logger.info("Reading values started.");
		while(refreshing) {
			synchronized (charValues) {
				for(String[] entry:charValues) {
					String address = entry[0];
					String serviceUuid = entry[1];
					String charUuid = entry[2];
					String value = entry[3];
					
					//najprej dobimo vrednost iz naprave in jo primerjamo s to v tabeli
					String valueOutput = process(getDescriptorValueByUUID(address, charUuid));
					Matcher m = CHAR_VAL_UUID_PATTERN.matcher(valueOutput);
					String realValue = m.find() ? m.group(0).trim() : "null";
					if(!realValue.equals(value)) {
						//podatka nista enaka, zato ga dodamo v om2m in spremenimo v seznamu
						String obixValRep = ObixUtil.getStringRep("Value", realValue);
						logger.debug("Value changed: " + obixValRep);
						BleMonitor.addValue(address, serviceUuid, charUuid, obixValRep);
						charValues.get(charValues.indexOf(entry))[3] = realValue;
					}
				}
			}
			try {
				logger.info("Reading values sleeping...");
				Thread.sleep(refreshTime*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		logger.info("Reading values stopped.");
	}
	
	static final String GATTTOOL = "gatttool -i %s -b %s";
	static final String PRIMARY = " --primary";
	public static String getPrimaryCommand(String address) {
		return String.format(GATTTOOL + PRIMARY, BLE_DEVICE, address);
	}
	
	static final String CHARS = " --characteristics -s %s -e %s";
	public static String getCharsCommand(String address, String start, String end) {
		return String.format(GATTTOOL + CHARS, BLE_DEVICE, address, start, end);
	}
	
	static final String CHAR_DESC = " --char-desc -s %s";
	public static String getCharDescCommand(String address, String start) {
		return String.format(GATTTOOL + CHAR_DESC, BLE_DEVICE, address, start);
	}
	
	static final String DESC_READ = " --char-read -a %s";
	public static String getDescriptorValue(String address, String handle) {
		return String.format(GATTTOOL + DESC_READ, BLE_DEVICE, address, handle);
	}
	
	static final String DESC_READ_UUID = " --char-read -u %s";
	public static String getDescriptorValueByUUID(String address, String uuid) {
		return String.format(GATTTOOL + DESC_READ_UUID, BLE_DEVICE, address, uuid);
	}
	
	
	public static String execCmd(String cmd) throws java.io.IOException {
        Process proc = Runtime.getRuntime().exec(cmd);
        java.io.InputStream is = proc.getInputStream();
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        String val = "";
        if (s.hasNext()) {
            val = s.next();
            System.out.println("HAHA:" + val);
        }
        else {
            val = "";
        }
        is.close();
        return val;
	}
	
	public static String execCmd(String[] cmd) throws java.io.IOException {
        Process proc = Runtime.getRuntime().exec(cmd);
        java.io.InputStream is = proc.getInputStream();
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        String val = "";
        if (s.hasNext()) {
            val = s.next();
            //System.out.println("HAHA:" + val);
        }
        else {
            val = "";
        }
        is.close();
        return val;
	}
	
	public static String process(String command) {
		try {
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(command);
			//output both stdout and stderr data from proc to stdout of this process
			StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream());
			StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream());
			errorGobbler.start();
			outputGobbler.start();
			proc.waitFor();
			return outputGobbler.getResult();
		}
		catch (Exception e) {
			logger.info("Error during executing command. " + e);
		}
		return "";
	}
	
	private static final String unknown = "(unknown)";
	
	public static void extractAddresses(String scanResult) {
		String[] lines = scanResult.split("\n");
		for(String line:lines) {
			Matcher mac = MAC_PATTERN.matcher(line);
			if (mac.find()) {
				String macValue = mac.group(0);
				String deviceName = line.substring(line.indexOf(' '), line.length());
				
				if(!oldDevices.containsKey(macValue) && !newDevices.containsKey(macValue)) {
					newDevices.put(macValue, deviceName);
					logger.debug("New device found: "+macValue);
				}
				
				if(newDevices.containsKey(macValue))
					if(newDevices.get(macValue).contains(unknown))
						if(!deviceName.contains(unknown)) {
							newDevices.remove(macValue);
							newDevices.put(macValue, deviceName);
						}
			}
		}
	}
	
	public static HashMap<String, String> getDescriptorHandles(String charDescOutput, String valhnd, String address) {
		HashMap<String, String> descriptors = new HashMap<>();	//desc uuid => desc value
		String[] lines = charDescOutput.split("\n");
		Matcher m = CHAR_UUID_PATTERN.matcher(lines[0]);
		if(m.find()) {
			//naj bi bil handle od katakteristike 2803
			String uuid = m.group(0);
			if(uuid.startsWith("00002803")) {
				m = CHAR_UUID_PATTERN.matcher(lines[1]);
				if(m.find()) {
					//naj bi bil handle vrednosti karakteristike
					m = CHAR_HANDLE_PATTERN.matcher(lines[1]);
					if(m.find()) {
						if(m.group(0).equals(valhnd)) {
							//value handle se res nahaja v drugi [1] vrstici 
							int st = 2;
							boolean desc = true;	//je deskriptor in ne nova karakteristika
							do {
								m = CHAR_UUID_PATTERN.matcher(lines[st]);
								if(m.find()) {
									//tu naj bi bil deskriptor ali nova karakteristika
									String descUuid = m.group(0);
									if(!descUuid.startsWith("00002803")) {
										//deskriptor
										String hnd = "";
										Matcher hndmatcher = CHAR_HANDLE_PATTERN.matcher(lines[st]);
										if(hndmatcher.find()) {
											hnd = hndmatcher.group(0);
											String readValue = process(getDescriptorValue(address, hnd));
											Matcher val = CHAR_VAL_DESC_PATTERN.matcher(readValue);
											String descValue = "";
											if(val.find()) {
												//descriptor value found
												descValue = val.group(0);
											}
											descriptors.put(descUuid, descValue.trim());
										}
									} else {
										desc = false;
									}
								}
								st++;
							} while (desc); 
						}
					}
				}
			}
		}
		return descriptors;
	}
	
	public static HashMap<String, String> getDescriptorHandlesNew(String charDescOutput, String address) {
		HashMap<String, String> descriptors = new HashMap<>();	//desc uuid => desc value
		String[] lines = charDescOutput.split("\n");
		// lines[0] = 2803, lines[1] = value
		boolean isDesc = true;
		int st = 2;
		do {
			Matcher m = CHAR_UUID_PATTERN.matcher(lines[st]);
			if(m.find()) {
				String uuid = m.group(0);
				//ce ni primary service ali secondary service ali include ali characteristic, je descriptor
				if(!uuid.startsWith("00002800") && !uuid.startsWith("00002801") && !uuid.startsWith("00002802") && !uuid.startsWith("00002803")) {
					//je descriptor
					Matcher hnd = CHAR_HANDLE_PATTERN.matcher(lines[st]);
					String handle = hnd.find() ? hnd.group(0) : "";
					String descOutput = process(getDescriptorValue(address, handle));
					Matcher m1 = CHAR_VAL_DESC_PATTERN.matcher(descOutput);
					String value = m1.find() ? m1.group(0) : "None";
					descriptors.put(uuid, value);
				}
				else {
					isDesc = false;
					break;
				}
			}
			st++;
		} while(isDesc && st < lines.length);
		return descriptors;
	}
}

class StreamGobbler extends Thread {
    InputStream is;
    String result = "";

    // reads everything from is until empty. 
    StreamGobbler(InputStream is) {
        this.is = is;
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ((line = br.readLine()) != null) {
//                System.out.println(line);
                this.result += line+"\n";
            }            
        } catch (IOException ioe) {
            ioe.printStackTrace();  
        }
    }
    
    public String getResult() {
    	return this.result;
    }
}