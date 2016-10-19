package org.eclipse.om2m.binding.bt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.commons.resource.ContentInstance;

public class ReadData {
	private static Log logger = LogFactory.getLog(ReadData.class);
	
	public static void readSerialData(String url) throws IOException {
		if(url.startsWith("btspp")) {
			StreamConnection streamConnection = (StreamConnection)Connector.open(url);

			//read response 
			InputStream inStream = streamConnection.openInputStream();
			BufferedReader bReader2=new BufferedReader(new InputStreamReader(inStream)); 
			
			String address = getAddress(url);
			logger.info("Connection opened: " + url);
			boolean running = true;
			while(running) {
				String lineRead = bReader2.readLine();
				logger.info("Received: " + lineRead);
				ContentInstance ci = new ContentInstance();
				ci.setContent(ObixUtil.getStringRep("Value",lineRead));
				RequestSender.createContentInstance(Constants.CSE_PREFIX + "/"
						+ Constants.BLUETOOTH + "/" + Constants.DEVICES + "/" + address + "/" + Constants.DATA, ci);
				if(lineRead == null) {
					running = false;
				}			
			}
		}
		else {
			logger.info("Wrong URL format.");
		}
	}
	
	public static void main(String args[]) {
		String line = "btspp://2C8A72563AA3:2;authenticate=false;encrypt=false;master=false";
		System.out.println(getAddress(line));
	}
	
	private static String getAddress(String url) {
		String pattern = "(?<=\\/\\/).*(?=:)";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(url);
		if (m.find( )) {
			return m.group(0);
		}
		return null;
	}
}
