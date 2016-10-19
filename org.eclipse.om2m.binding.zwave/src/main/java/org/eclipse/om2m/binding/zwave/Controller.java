package org.eclipse.om2m.binding.zwave;

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
			case ZWAVE_ALL_ON:
				//turn the light on
				ZWaveMain.switchAllOn();
				response.setContent(ObixUtil.getStringRep("Status","All nodes switched on."));
				response.setResponseStatusCode(ResponseStatusCode.OK);
				break;
			case ZWAVE_ALL_OFF:
				//turn the light off
				ZWaveMain.switchAllOff();
				response.setContent(ObixUtil.getStringRep("Status", "All nodes switched off."));
				response.setResponseStatusCode(ResponseStatusCode.OK);
				break;
			case ZWAVE_START_INCLUSION:
				//start including
				ZWaveMain.adding = true;
				new Thread() {
					public void run() {
						ZWaveMain.adding = true;
						ZWaveMain.addingDevices();				
					}
				}.start();
				response.setContent(ObixUtil.getStringRep("Status", "Inclusion started."));
				response.setResponseStatusCode(ResponseStatusCode.OK);
				break;
			case ZWAVE_STOP_INCLUSION:
				//stop including
				ZWaveMain.adding = false;
				response.setContent(ObixUtil.getStringRep("Status", "Inclusion stopped."));
				response.setResponseStatusCode(ResponseStatusCode.OK);
				break;
			case ZWAVE_START_REMOVING_DEVICES:
				ZWaveMain.adding = false;
				new Thread() {
					public void run() {
						ZWaveMain.removing = true;
						ZWaveMain.removingDevices();				
					}
				}.start();
				response.setContent(ObixUtil.getStringRep("Status", "Removing devices started."));
				response.setResponseStatusCode(ResponseStatusCode.OK);
				break;
			case ZWAVE_STOP_REMOVING_DEVICES:
				ZWaveMain.removing = false;
				response.setContent(ObixUtil.getStringRep("Status", "Removing devices stopped."));
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
		return Constants.POA_ZWAVE;
	}

}
