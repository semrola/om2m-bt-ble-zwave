package org.eclipse.om2m.binding.bt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.bluetooth.*;

public class DeviceAndServiceSearch implements DiscoveryListener {

	private DiscoveryAgent agent = null;
	
	private ArrayList<int[]> deviceClasses;
	
	
	private ArrayList<RemoteDevice> devices;	// remote devices list
	private final Object inquiryLock = new Object();	// object used for waiting on inquiry completion
	private final Object serviceSearchLock = new Object();	// object used for waiting on service search completion
	private ArrayList<Vector<ServiceRecord>> services = new ArrayList<>();	// services list for every remote device
	
	private LocalDevice localDevice = null;
	
	// set of searched UUIDs
	private ArrayList<UUID> UUIDs;
	
	public DeviceAndServiceSearch() {
		devices = new ArrayList<>();
		deviceClasses = new ArrayList<>();
		try {
			localDevice = LocalDevice.getLocalDevice();
			agent = localDevice.getDiscoveryAgent();
			UUIDs = new ArrayList<>();
			UUIDs.add(ServiceUUIDs.L2CAP.getUUID());	//L2CAP
			//UUIDs.add(ServiceUUIDs.RFCOMM.getUUID());	//L2CAP
						
			startInquiry();
		} catch (BluetoothStateException e) {
			e.printStackTrace();
			System.exit(1);
		}		
	}
	
	void startInquiry() throws BluetoothStateException {
		System.out.println("Device inquiry starting...");
		boolean started = agent.startInquiry(DiscoveryAgent.GIAC, this);
		if(started) {
			try {
				// wait for release
				synchronized(inquiryLock){
					inquiryLock.wait();
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	void startServiceSearch(int[] attributes, UUID[] uuids) {
		System.out.println("Searching for service:");
		uuids = UUIDs.toArray(new UUID[UUIDs.size()]);
		for(UUID uuid : uuids) {
			System.out.println(uuid.toString());
		}
		if(devices.size() > 0) {
			for(RemoteDevice rd: devices) {
				int id;
				try {
					id = agent.searchServices(attributes, uuids, rd, this);
					if(id < 0) {
						System.out.println("Service search failed.");
					}
					else {
						services.add(new Vector<ServiceRecord>());
						System.out.println("Searching services...");
						synchronized(serviceSearchLock) {
							serviceSearchLock.wait();
						}
					}
				} catch (BluetoothStateException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
			}
		} else {
			System.out.println("No devices were found.");
		}
	}
	
	@Override
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		int majorDC = cod.getMajorDeviceClass();
	    int minorDC = cod.getMinorDeviceClass();
	    deviceClasses.add(new int[]{majorDC, minorDC});
		devices.add(btDevice);
		
		try {
			System.out.println("Found device: " + btDevice.getFriendlyName(false));
		} catch (IOException e) {
			System.out.println("Cannot get device friendly name.");
		}
	}

	@Override
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		if(servRecord != null && servRecord.length > 0) {
			for(ServiceRecord sr : servRecord) {
				// adding service records to last vector in services
				Vector<ServiceRecord> vec = services.get(services.size()-1);
				if(sr != null) {
					vec.add(sr);
				}
			}
		}
	}

	@Override
	public void serviceSearchCompleted(int transID, int respCode) {
		switch(respCode) {
		case SERVICE_SEARCH_COMPLETED: System.out.println("Service search completed."); break;
		case SERVICE_SEARCH_DEVICE_NOT_REACHABLE: System.out.println("Device not reachable."); break;
		case SERVICE_SEARCH_ERROR: System.out.println("Service search error."); break;
		case SERVICE_SEARCH_NO_RECORDS: System.out.println("No service records for this device."); break;
		case SERVICE_SEARCH_TERMINATED: System.out.println("Service search terminated."); break;
		default: System.out.println("Service search returned unknown reponse.");
		}
		
		synchronized (serviceSearchLock) {
			serviceSearchLock.notifyAll();
		}
		
	}

	@Override
	public void inquiryCompleted(int discType) {
		System.out.print("Device inquiry completed with status: ");

		switch (discType) {
		case DiscoveryListener.INQUIRY_COMPLETED:
			System.out.println("INQUIRY_COMPLETED");
			break;
		case DiscoveryListener.INQUIRY_TERMINATED:
			System.out.println("INQUIRY_TERMINATED");
			break;
		case DiscoveryListener.INQUIRY_ERROR:
			System.out.println("INQUIRY_ERROR");
			break;
		default:
			System.out.println("Unknown Response Code");
			break;
		}

		// release the lock
		synchronized(inquiryLock){
			inquiryLock.notifyAll();
		}
	}
	
	// getters
	
	public ArrayList<RemoteDevice> getDevices() {
		return this.devices;
	}
	
	public ArrayList<int[]> getDeviceClasses() {
		return this.deviceClasses;
	}
	
	public ArrayList<Vector<ServiceRecord>> getServices() {
		return this.services;
	}
	

}
