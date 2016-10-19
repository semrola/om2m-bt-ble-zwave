package org.eclipse.om2m.binding.zwave;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zwave4j.ControllerCallback;
import org.zwave4j.ControllerCommand;
import org.zwave4j.ControllerError;
import org.zwave4j.ControllerState;
import org.zwave4j.Manager;
import org.zwave4j.NativeLibraryLoader;
import org.zwave4j.Notification;
import org.zwave4j.NotificationWatcher;
import org.zwave4j.Options;
import org.zwave4j.ValueId;
import org.zwave4j.ZWave4j;

public class ZWaveMain {

	private static Log logger = LogFactory.getLog(ZWaveMain.class);
	static long homeId;
	static boolean ready = false;
	public static boolean adding = true;
	public static boolean removing = false;
	public static boolean listening = true;
	static final Manager manager;
	
	private static String configPath;
	private static String serialPort;
	
	static {
		//WINDOWS
		//configPath = "C:/Users/user/Desktop/config";
		//serialPort = "//./COM7";
		//LINUX
		configPath = "/home/user/config";
		serialPort = "/dev/ttyACM0";
		Thread.currentThread().setContextClassLoader(NativeLibraryLoader.class.getClassLoader());
		NativeLibraryLoader.loadLibrary(ZWave4j.LIBRARY_NAME, ZWave4j.class);
		final Options options = Options.create(configPath, configPath, "");
		options.addOptionBool("ConsoleOutput", false);
//		options.addOptionInt("SaveLogLevel", 9);
//		options.addOptionInt("QueueLogLevel", 9);
//		options.addOptionInt("DumpTriggerLevel", 9);
		options.lock();
		
		manager = Manager.create();
	}

	public static void startListening() {
		logger.info("Starting Zwave listening...");
//		final NotificationWatcher watcher = new Watcher();
		final NotificationWatcher watcher = new NotificationWatcher() {
			String nodeValueStatus;
			@Override
			public void onNotification(Notification notification, Object context) {
				switch (notification.getType()) {
				case DRIVER_READY:					
					logger.debug(String.format("Driver ready\n" +
							"\thome id: %d",
							notification.getHomeId()
							));
					homeId = notification.getHomeId();
					break;
				case DRIVER_FAILED:
					logger.debug("Driver failed");
					break;
				case DRIVER_RESET:
					logger.debug("Driver reset");
					break;
				case AWAKE_NODES_QUERIED:
					logger.debug("Awake nodes queried");
					break;
				case ALL_NODES_QUERIED:
					logger.debug("All nodes queried");
					manager.writeConfig(homeId);
					ready = true;
					break;
				case ALL_NODES_QUERIED_SOME_DEAD:
					logger.debug("All nodes queried some dead");
					manager.writeConfig(homeId);
					ready = true;
					break;
				case POLLING_ENABLED:
					logger.debug("Polling enabled");
					break;
				case POLLING_DISABLED:
					logger.debug("Polling disabled");
					break;
				case NODE_NEW:
					logger.debug(String.format("Node new\n" +
							"\tnode id: %d",
							notification.getNodeId()
							));
					break;
				case NODE_ADDED:
					logger.debug(String.format("Node added\n" +
							"\tnode id: %d",
							notification.getNodeId()
							));					
					break;
				case NODE_REMOVED:
					logger.debug(String.format("Node removed\n" +
							"\tnode id: %d",
							notification.getNodeId()
							));
					break;
				case ESSENTIAL_NODE_QUERIES_COMPLETE:
					logger.debug(String.format("Node essential queries complete\n" +
							"\tnode id: %d",
							notification.getNodeId()
							));
					break;
				case NODE_QUERIES_COMPLETE:
					logger.debug(String.format("Node queries complete\n" +
							"\tnode id: %d",
							notification.getNodeId()
							));
					break;
				case NODE_EVENT:
					logger.debug(String.format("Node event\n" +
							"\tnode id: %d\n" +
							"\tevent id: %d",
							notification.getNodeId(),
							notification.getEvent()
							));
					break;
				case NODE_NAMING:
					logger.debug(String.format("Node naming\n" +
							"\tnode id: %d",
							notification.getNodeId()
							));
					break;
				case NODE_PROTOCOL_INFO:
					short id = notification.getNodeId();
					String type = manager.getNodeType(notification.getHomeId(), notification.getNodeId());
					logger.debug(String.format("Node protocol info\n" +
							"\tnode id: %d\n" +
							"\ttype: %s",
							id,
							type
							));
					ZWaveMonitor.addNode(getNodeContainerName(id, type));
					break;
				case VALUE_ADDED:
					nodeValueStatus = "added";
				case VALUE_REMOVED:
					nodeValueStatus = "removed";
				case VALUE_CHANGED:
					nodeValueStatus = "changed";
				case VALUE_REFRESHED:
					nodeValueStatus = "refreshed";
					System.out.println(String.format("Value " + nodeValueStatus +"\n" +
							"\tnode id: %d\n" +
							"\tcommand class: %d\n" +
							"\tinstance: %d\n" +
							"\tindex: %d\n" +
							"\tgenre: %s\n" +
							"\ttype: %s\n" +
							"\tlabel: %s\n" +
							"\tvalue: %s",
							notification.getNodeId(),
							notification.getValueId().getCommandClassId(),
							notification.getValueId().getInstance(),
							notification.getValueId().getIndex(),
							notification.getValueId().getGenre().name(),
							notification.getValueId().getType().name(),
							manager.getValueLabel(notification.getValueId()),
							getValue(notification.getValueId())
							));
					String rep = ObixUtil.getZwaveValueRep(
							notification.getValueId().getCommandClassId(),
							notification.getValueId().getInstance(),
							notification.getValueId().getIndex(),
							notification.getValueId().getGenre().name(),
							notification.getValueId().getType().name(),
							manager.getValueLabel(notification.getValueId()),
							getValue(notification.getValueId()));
					ZWaveMonitor.addNodeValue(getNodeContainerName(notification.getNodeId(),
							manager.getNodeType(notification.getHomeId(), notification.getNodeId())), rep);
					break;
				/*case VALUE_ADDED:
					logger.debug(String.format("Value added\n" +
							"\tnode id: %d\n" +
							"\tcommand class: %d\n" +
							"\tinstance: %d\n" +
							"\tindex: %d\n" +
							"\tgenre: %s\n" +
							"\ttype: %s\n" +
							"\tlabel: %s\n" +
							"\tvalue: %s",
							notification.getNodeId(),
							notification.getValueId().getCommandClassId(),
							notification.getValueId().getInstance(),
							notification.getValueId().getIndex(),
							notification.getValueId().getGenre().name(),
							notification.getValueId().getType().name(),
							manager.getValueLabel(notification.getValueId()),
							getValue(notification.getValueId())
							));
					break;
				case VALUE_REMOVED:
					logger.debug(String.format("Value removed\n" +
							"\tnode id: %d\n" +
							"\tcommand class: %d\n" +
							"\tinstance: %d\n" +
							"\tindex: %d",
							notification.getNodeId(),
							notification.getValueId().getCommandClassId(),
							notification.getValueId().getInstance(),
							notification.getValueId().getIndex()
							));
					break;
				case VALUE_CHANGED:
					logger.debug(String.format("Value changed\n" +
							"\tnode id: %d\n" +
							"\tcommand class: %d\n" +
							"\tinstance: %d\n" +
							"\tindex: %d\n" +
							"\tvalue: %s",
							notification.getNodeId(),
							notification.getValueId().getCommandClassId(),
							notification.getValueId().getInstance(),
							notification.getValueId().getIndex(),
							getValue(notification.getValueId())
							));
					break;
				case VALUE_REFRESHED:
					logger.debug(String.format("Value refreshed\n" +
							"\tnode id: %d\n" +
							"\tcommand class: %d\n" +
							"\tinstance: %d\n" +
							"\tindex: %d" +
							"\tvalue: %s",
							notification.getNodeId(),
							notification.getValueId().getCommandClassId(),
							notification.getValueId().getInstance(),
							notification.getValueId().getIndex(),
							getValue(notification.getValueId())
							));
					break;*/
				case GROUP:
					logger.debug(String.format("Group\n" +
							"\tnode id: %d\n" +
							"\tgroup id: %d",
							notification.getNodeId(),
							notification.getGroupIdx()
							));
					break;

				case SCENE_EVENT:
					logger.debug(String.format("Scene event\n" +
							"\tscene id: %d",
							notification.getSceneId()
							));
					break;
				case CREATE_BUTTON:
					logger.debug(String.format("Button create\n" +
							"\tbutton id: %d",
							notification.getButtonId()
							));
					break;
				case DELETE_BUTTON:
					logger.debug(String.format("Button delete\n" +
							"\tbutton id: %d",
							notification.getButtonId()
							));
					break;
				case BUTTON_ON:
					logger.debug(String.format("Button on\n" +
							"\tbutton id: %d",
							notification.getButtonId()
							));
					break;
				case BUTTON_OFF:
					logger.debug(String.format("Button off\n" +
							"\tbutton id: %d",
							notification.getButtonId()
							));
					break;
				case NOTIFICATION:
					logger.debug("Notification");
					break;
				default:
					logger.debug(notification.getType().name());
					break;
				}
			}
		};

		manager.addWatcher(watcher, null);
		manager.addDriver(serialPort);

		while(!ready) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error(e);
			} catch (Exception e) {
				logger.error(e);
			}
		}
		
		logger.info("ZWave Ready");
		while(listening) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		manager.removeDriver(serialPort);
		Manager.destroy();
		Options.destroy();
		logger.info("Zwave listening stopped.");
	}
	
	static Object getValue(ValueId valueId) {
		switch (valueId.getType()) {
		case BOOL:
			AtomicReference<Boolean> b = new AtomicReference<>();
			Manager.get().getValueAsBool(valueId, b);
			return b.get();
		case BYTE:
			AtomicReference<Short> bb = new AtomicReference<>();
			Manager.get().getValueAsByte(valueId, bb);
			return bb.get();
		case DECIMAL:
			AtomicReference<Float> f = new AtomicReference<>();
			Manager.get().getValueAsFloat(valueId, f);
			return f.get();
		case INT:
			AtomicReference<Integer> i = new AtomicReference<>();
			Manager.get().getValueAsInt(valueId, i);
			return i.get();
		case LIST:
			return null;
		case SCHEDULE:
			return null;
		case SHORT:
			AtomicReference<Short> s = new AtomicReference<>();
			Manager.get().getValueAsShort(valueId, s);
			return s.get();
		case STRING:
			AtomicReference<String> ss = new AtomicReference<>();
			Manager.get().getValueAsString(valueId, ss);
			return ss.get();
		case BUTTON:
			return null;
		case RAW:
			AtomicReference<short[]> sss = new AtomicReference<>();
			Manager.get().getValueAsRaw(valueId, sss);
			return sss.get();
		default:
			return null;
		}
	}
	
	public static String getNodeContainerName(short id, String type) {
		if(type.contains(" ")) {
			type = type.replaceAll(" ", "_");
		}
		return type + "_" + id;
	}
	
	public static void addingDevices() {
		while(!ready) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}								
		}
		logger.info("Adding devices started.");
		ControllerCallback cmd = new CmdCallback();
		manager.beginControllerCommand(homeId, ControllerCommand.ADD_DEVICE, cmd);
		while(adding) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		manager.cancelControllerCommand(homeId);
		logger.info("Adding devices stopped.");
	}
	
	public static void removingDevices() {
		while(!ready) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}								
		}
		logger.info("Removing devices started.");
		ControllerCallback cmd = new CmdCallback();
		manager.beginControllerCommand(homeId, ControllerCommand.REMOVE_DEVICE, cmd);
		while(removing) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		manager.cancelControllerCommand(homeId);
		logger.info("Removing devices stopped.");
	}
	
	public static void switchAllOn() {
		manager.switchAllOn(homeId);
	}

	public static void switchAllOff() {
		manager.switchAllOff(homeId);
	}
	
	public static void readProperties() {
		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream("config.properties");

			// load a properties file
			prop.load(input);

			// get the property value and print it out
			serialPort = prop.getProperty("serial_port");
			configPath = prop.getProperty("config_path");

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void readProperties2(){

    	Properties prop = new Properties();
    	InputStream input = null;

    	try {
    		String filename = "config/config.properties";
    		System.out.println(ZWaveMain.class.getClassLoader().getSystemResource(filename));
    		System.out.println(ZWaveMain.class.getResourceAsStream(filename));
    		System.out.println(ZWaveMain.class.getClassLoader().getResourceAsStream(filename));
    		

    		//load a properties file from class path, inside static method
    		prop.load(input);

            serialPort = prop.getProperty("serial_port");
            configPath= prop.getProperty("config_path");

    	} catch (IOException ex) {
    		ex.printStackTrace();
        } finally{
        	if(input!=null){
        		try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        	}
        }

    }
	
	public static void main(String[] args) {

		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream("config.properties");

			// load a properties file
			prop.load(input);

			// get the property value and print it out
			System.out.println(prop.getProperty("database"));
			System.out.println(prop.getProperty("dbuser"));
			System.out.println(prop.getProperty("dbpassword"));

		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	  }
}

final class CmdCallback implements ControllerCallback {
	private static Log logger = LogFactory.getLog(CmdCallback.class);
	@Override
	public void onCallback(ControllerState state, ControllerError err, Object context) {
		switch(state) {
		case CANCEL:
			logger.debug("CMD cancel");
			break;
		case COMPLETED:
			logger.debug("CMD completed");
			break;
		case ERROR:
			logger.debug("CMD error");
			break;
		case FAILED:
			logger.debug("CMD failed");
			break;
		case IN_PROGRESS:
			logger.debug("CMD progress");
			break;
		case NODE_FAILED:
			logger.debug("CMD failed");
			break;
		case NODE_OK:
			logger.debug("CMD node ok");
			break;
		case NORMAL:
			logger.debug("CMD normal");
			break;
		case SLEEPING:
			logger.debug("CMD sleeping");
			break;
		case STARTING:
			logger.debug("CMD starting");
			break;
		case WAITING:
			logger.debug("CMD waiting");
			break;
		default:
			break;

		}

		switch(err) {
		case BUSY:
			logger.debug("CMD err busy");
			break;
		case BUTTON_NOT_FOUND:
			logger.debug("CMD err btn not found");
			break;
		case DISABLED:
			logger.debug("CMD err disabled");
			break;
		case FAILED:
			logger.debug("CMD err failed");
			break;
		case IS_PRIMARY:
			logger.debug("CMD err is primary");
			break;
		case NODE_NOT_FOUND:
			logger.debug("CMD err node not found");
			break;
		case NONE:
			logger.debug("CMD err none");
			break;
		case NOT_BRIDGE:
			logger.debug("CMD err not bridge");
			break;
		case NOT_FOUND:
			logger.debug("CMD err not found");
			break;
		case NOT_PRIMARY:
			logger.debug("CMD err not primary");
			break;
		case NOT_SECONDARY:
			logger.debug("CMD err not secondary");
			break;
		case NOT_SUC:
			logger.debug("CMD err not suc");
			break;
		case OVERFLOW:
			logger.debug("CMD err overflow");
			break;
		default:
			break;

		}
	}


}

