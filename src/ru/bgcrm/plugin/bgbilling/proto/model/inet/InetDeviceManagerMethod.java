package ru.bgcrm.plugin.bgbilling.proto.model.inet;

import java.util.Set;

public class InetDeviceManagerMethod {
	public static enum DeviceManagerMethodType {
		DEVICE, ACCOUNT, CONNECTION;
	}

	private Set<DeviceManagerMethodType> types;
	private String method;
	private String title;

	public Set<DeviceManagerMethodType> getTypes() {
		return types;
	}

	public void setTypes(Set<DeviceManagerMethodType> types) {
		this.types = types;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
