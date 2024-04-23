package ru.bgcrm.plugin.bgbilling.proto.model.ipn;

import java.util.ArrayList;
import java.util.List;

public class ContractGateInfo
{
	private int statusId;
	private List<ContractGate> gateList = new ArrayList<>();
	private List<ContractGateLogItem> statusLog = new ArrayList<>();

	public void setStatusId( int statusId )
	{
		this.statusId = statusId;
	}

	public int getStatusId()
	{
		return statusId;
	}

	public List<ContractGate> getGateList()
	{
		return gateList;
	}

	public List<ContractGateLogItem> getStatusLog()
	{
		return statusLog;
	}
}
