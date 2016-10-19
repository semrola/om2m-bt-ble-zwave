package org.eclipse.om2m.binding.ble;

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
	
	@Override
	public ResponsePrimitive doExecute(RequestPrimitive request) {
		ResponsePrimitive response = new ResponsePrimitive(request);
		
		if(request.getQueryStrings().containsKey("op")) {
			String valueOp = request.getQueryStrings().get("op").get(0);
			logger.info("Value OP: " + valueOp);
			Operations op = Operations.getOperationFromString(valueOp);
			switch(op) {
			case BLE_START_SCAN:
				if(BleMonitor.threadAlive) {
					response.setContent(ObixUtil.getStringRep("Response", "Scanning thread is alive."));
				} else {
					ReadGatt.scanning = true;
					new Thread() {
						public void run() { 
							BleMonitor.threadAlive = true;
							logger.info("Starting BLE scanning...");
							ReadGatt.startScanning();
							BleMonitor.threadAlive = false;
						}
					}.start();
					response.setContent(ObixUtil.getStringRep("Response", "Scanning thread started."));
				}
				response.setResponseStatusCode(ResponseStatusCode.OK);
				break;
			case BLE_STOP_SCAN:
				if(BleMonitor.threadAlive) {
					ReadGatt.scanning = false;
					response.setContent(ObixUtil.getStringRep("Response", "Scanning thread will stop shortly."));
				} else {
					response.setContent(ObixUtil.getStringRep("Response", "Scanning thread is not running."));
				}
				response.setResponseStatusCode(ResponseStatusCode.OK);
				break;
			case BLE_START_READING:
				if(ReadGatt.readingThreadAlive) {
					response.setContent(ObixUtil.getStringRep("Response", "Reading thread is alive."));
				} else {
					ReadGatt.refreshing = true;
					new Thread() {
						public void run() { 
							ReadGatt.readingThreadAlive = true;
							logger.info("Starting BLE value reading...");
							ReadGatt.refreshValues();
							ReadGatt.readingThreadAlive = false;
						}
					}.start();
					response.setContent(ObixUtil.getStringRep("Response", "Reading values thread started."));
				}
				response.setResponseStatusCode(ResponseStatusCode.OK);
				break;
			case BLE_STOP_READING:
				if(ReadGatt.readingThreadAlive) {
					ReadGatt.refreshing = false;
					response.setContent(ObixUtil.getStringRep("Response", "Reading values thread will stop shortly."));
				} else {
					response.setContent(ObixUtil.getStringRep("Response", "Reading values thread is not running."));
				}
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
		return Constants.POA_BLE;
	}

}
