package ru.bgcrm.plugin.bgbilling.proto.model.inet;

public class IpResourceRange {
	private int resId;
	private String from;
	private String to;

	public IpResourceRange() {}

	public IpResourceRange(int ipResourceId, String addressFrom, String addressTo) {
		this.resId = ipResourceId;
		this.from = addressFrom;
		this.to = addressTo;
	}
	
	public int getResId() {
		return resId;
	}

	public void setResId(int ipResourceId) {
		this.resId = ipResourceId;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String ipFrom) {
		this.from = ipFrom;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String ipTo) {
		this.to = ipTo;
	}
}