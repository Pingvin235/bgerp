package ru.bgcrm.plugin.bgbilling.proto.model.inet;

public class InetConnection
{
	public static final int TYPE_NOT_FROM_START = 0x01;
	public static final int TYPE_SPLITTED = 0x02;
	public static final int TYPE_CLOSED_BY_TIMEOUT = 0x04;
	public static final int TYPE_DEVICE_STATE_CHANGED = 0x08;
	public static final int CREATED_BY_LOG_PROCESS = 0x10;
	
	public static final int TYPE_DISABLE_AT_START = 0x20;
	
	public static final int TYPE_FLOW = 0x0100;
	public static final int TYPE_RADIUS = 0x0200;
	public static final int TYPE_DHCPv4 = 0x0400;
	public static final int TYPE_DHCPv6 = 0x0800;
	public static final int TYPE_IP_SUBSCRIPTION = 0x1000;
	
	public static final int STATUS_WAIT = 0;
	public static final int STATUS_ALIVE = 1;
	public static final int STATUS_SUSPENDED = 2;
	public static final int STATUS_CLOSED = 3;
	public static final int STATUS_FINISHED = 4;
}
