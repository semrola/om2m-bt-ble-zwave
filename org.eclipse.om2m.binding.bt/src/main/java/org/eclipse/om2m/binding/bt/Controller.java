package org.eclipse.om2m.binding.bt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.commons.constants.ResponseStatusCode;
import org.eclipse.om2m.commons.exceptions.BadRequestException;
import org.eclipse.om2m.commons.resource.RequestPrimitive;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;
import org.eclipse.om2m.interworking.service.InterworkingService;

public class Controller implements InterworkingService {
	
	private static Log logger = LogFactory.getLog(Controller.class);
	public static String ciId;
	public static String inclusionCntId;
	public static boolean inquiryActive = false;
	
	@Override
	public ResponsePrimitive doExecute(RequestPrimitive request) {
		ResponsePrimitive response = new ResponsePrimitive(request);
		
		if(request.getQueryStrings().containsKey("op")) {
			String valueOp = request.getQueryStrings().get("op").get(0);
			logger.info("Value OP: " + valueOp);
			Operations op = Operations.getOperationFromString(valueOp);
			switch(op) {
			case INQUIRY:
				if(inquiryActive) {
					response.setContent(ObixUtil.getStringRep("Status", "Inquiry curently active."));
				} else {
					new Thread() {
						public void run() {
							inquiryActive = true;
							Client.startClient();
							inquiryActive = false;
						}
					}.start();
					response.setContent(ObixUtil.getStringRep("Status", "Inquiry started."));
				}
				response.setResponseStatusCode(ResponseStatusCode.OK);
				break;
			case BT_OPEN_CONNECTION:
				if(request.getQueryStrings().containsKey("href")) {
					String url="";
					try {
						url = URLDecoder.decode(request.getQueryStrings().get("href").get(0), "UTF-8");
					} catch (UnsupportedEncodingException e1) {
						e1.printStackTrace();
					}
					final String url1 = url;
					new Thread() {
						public void run() {
							try {
								ReadData.readSerialData(url1);
							} catch (IOException e) {
								logger.error(e.getMessage());
								e.printStackTrace();
							}
						}
					}.start();
				} else {
					logger.info("No connection URL specified.");
				}
				response.setContent(ObixUtil.getStringRep("Status", "Connection opened."));
				response.setResponseStatusCode(ResponseStatusCode.OK);
				break;
			default:
				throw new BadRequestException();
			}
		}
		
		return response;
	}

	@Override
	public String getAPOCPath() {
		// TODO Auto-generated method stub
		return Constants.POA_BT;
	}

}
