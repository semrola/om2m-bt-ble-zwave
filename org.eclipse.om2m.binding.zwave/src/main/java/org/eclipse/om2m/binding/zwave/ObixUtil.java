package org.eclipse.om2m.binding.zwave;

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
	
	public static String getZwaveInclusionRep(String targetId) {
		Obj obj = new Obj();
		
		//start
		Op start = new Op();
		start.setName("Start inclusion of nodes");
		start.setHref(new Uri(targetId + "?op=" + Operations.ZWAVE_START_INCLUSION));
		start.setIs(new Contract("execute"));
		obj.add(start);
		
		Op stop = new Op();
		stop.setName("Stop inclusion of nodes");
		stop.setHref(new Uri(targetId + "?op=" + Operations.ZWAVE_STOP_INCLUSION));
		stop.setIs(new Contract("execute"));
		obj.add(stop);
		
		Op allOn = new Op();
		allOn.setName("Switch all on");
		allOn.setHref(new Uri(targetId + "?op=" + Operations.ZWAVE_ALL_ON));
		allOn.setIs(new Contract("execute"));
		obj.add(allOn);
		
		Op allOff = new Op();
		allOff.setName("Switch all off");
		allOff.setHref(new Uri(targetId + "?op=" + Operations.ZWAVE_ALL_OFF));
		allOff.setIs(new Contract("execute"));
		obj.add(allOff);
		
		String rep = ObixEncoder.toString(obj);
		return rep;
	}
	
	public static String getStringRep(String attr, String str) {
		Obj obj = new Obj();
		Str s = new Str(attr, str);
		obj.add(s);
		String rep = ObixEncoder.toString(obj);
		return rep;
	}
	
	public static String getZwaveValueRep(short commandClassId, short instance, short index,
			String genre, String type, String valueLabel, Object value) {
		Obj obj = new Obj();
		obj.add(new Int("commandClass", commandClassId));
		obj.add(new Int("instance", instance));
		obj.add(new Int("index", index));
		obj.add(new Str("genre", genre));
		obj.add(new Str("type", type));
		obj.add(new Str("label", valueLabel));
		obj.add(new Str("value", value.toString()));
		
		return ObixEncoder.toString(obj);
	}
}