package org.eclipse.om2m.binding.bt;

import javax.bluetooth.UUID;

public enum ServiceUUIDs {
	
	SDP(0x0001, "SDP"), RFCOMM(0x0003, "RFCOMM"), OBEX(0x0008, "OBEX"), HTTP(0x000C, "HTTP"),
	L2CAP(0x0100, "L2CAP"), SERIAL_PORT(0x1101, "SERIAL_PORT");
	
	private long uuidlong;
	private UUID uuid;
	private String name;
	
	ServiceUUIDs(long uuid, String name) {
		this.uuidlong = uuid;
		this.uuid = new UUID(uuid);
		this.name = name;
	}
	
	public long getUUIDlong() {
		return this.uuidlong;
	}
	
	public UUID getUUID() {
		return this.uuid;
	}
	
	@Override
	public String toString() {
		return this.name + this.uuid.toString();
	}
	
	/*
	public final static long SDP = 0x0001;
	public final static long RFCOMM = 0x0003;
	public final static long OBEX = 0x0008;
	public final static long HTTP = 0x000C;
	public final static long L2CAP = 0x0100;
	public final static long SERIAL_PORT = 0x1101;
	
	
	UUIDs.add(new UUID(0x0001));	//
	UUIDs.add(new UUID());	//RFCOMM
	UUIDs.add(new UUID(0x0008));	//OBEX
	UUIDs.add(new UUID(0x000C));	//HTTP
	UUIDs.add(new UUID(0x0100));	//L2CAP
	UUIDs.add(new UUID(0x1000));	//ServiceDiscoveryServerServiceClassID
	UUIDs.add(new UUID(0x1001));	//BrowseGroupDescriptorServiceClassID
	UUIDs.add(new UUID(0x1101));	//Serial Port
	UUIDs.add(new UUID(0x1116));	//Network Access Point
	*/
}
