package ru.bgcrm.plugin.bgbilling.proto.model.crm.task;

/**
 * Задача из плагина CRM биллинга. 
 * @deprecated для исключения случайного использования.  
 * @author Shamil
 */
@Deprecated
public class Task
{
	public class Log
	{
		private String open;
		private String accept;
		private String close;
		private String lastModify;

		public String getOpen()
		{
			return open;
		}

		public void setOpen( String open )
		{
			this.open = open;
		}

		public String getAccept()
		{
			return accept;
		}

		public void setAccept( String accept )
		{
			this.accept = accept;
		}

		public String getClose()
		{
			return close;
		}

		public void setClose( String close )
		{
			this.close = close;
		}

		public String getLastModify()
		{
			return lastModify;
		}

		public void setLastModify( String lastModify )
		{
			this.lastModify = lastModify;
		}
	}
	/*aObjectId="0"
	problemId="0
	target_date_and_time="20.06.2011 18:00"*/

	public static final int STATUS_OPEN = 0;
	public static final int STATUS_ACCEPTED = 1;
	public static final int STATUS_CLOSED = 2;

	private String address;
	private int addressPId;
	private String comment;
	private String contract;
	private int contractId;
	private int objectId;
	private String contractComment;
	private String currentDate;
	private String executeDate;
	private String executors;
	private String fio;
	private int groupId;
	private String groupTitle;
	private int id;
	private String openDate;
	private String openUser;
	private String phones;
	private String resolution;
	private String status;
	private int statusCode;
	private String targetDateTime;
	private int typeId;

	public String getTypeTitle()
	{
		return typeTitle;
	}

	public void setTypeTitle( String typeTitle )
	{
		this.typeTitle = typeTitle;
	}

	private String typeTitle;
	private Log log = new Log();

	public String getAddress()
	{
		return address;
	}

	public void setAddress( String address )
	{
		this.address = address;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment( String comment )
	{
		this.comment = comment;
	}

	public String getGroupTitle()
	{
		return groupTitle;
	}

	public void setGroupTitle( String groupTitle )
	{
		this.groupTitle = groupTitle;
	}

	public String getContract()
	{
		return contract;
	}

	public void setContract( String contract )
	{
		this.contract = contract;
	}

	public int getContractId()
	{
		return contractId;
	}

	public void setContractId( int contractId )
	{
		this.contractId = contractId;
	}

	public int getObjectId()
	{
		return objectId;
	}

	public void setObjectId( int objectId )
	{
		this.objectId = objectId;
	}

	public String getContractComment()
	{
		return contractComment;
	}

	public void setContractComment( String contractComment )
	{
		this.contractComment = contractComment;
	}

	public String getCurrentDate()
	{
		return currentDate;
	}

	public void setCurrentDate( String currentDate )
	{
		this.currentDate = currentDate;
	}

	public String getExecuteDate()
	{
		return executeDate;
	}

	public void setExecuteDate( String executeDate )
	{
		this.executeDate = executeDate;
	}

	public String getExecutors()
	{
		return executors;
	}

	public void setExecutors( String executors )
	{
		this.executors = executors;
	}

	public String getFio()
	{
		return fio;
	}

	public void setFio( String fio )
	{
		this.fio = fio;
	}

	public int getGroupId()
	{
		return groupId;
	}

	public void setGroupId( int groupId )
	{
		this.groupId = groupId;
	}

	public int getId()
	{
		return id;
	}

	public void setId( int id )
	{
		this.id = id;
	}

	public String getOpenDate()
	{
		return openDate;
	}

	public void setOpenDate( String openDate )
	{
		this.openDate = openDate;
	}

	public String getOpenUser()
	{
		return openUser;
	}

	public void setOpenUser( String openUser )
	{
		this.openUser = openUser;
	}

	public String getPhones()
	{
		return phones;
	}

	public void setPhones( String phones )
	{
		this.phones = phones;
	}

	public String getResolution()
	{
		return resolution;
	}

	public void setResolution( String resolution )
	{
		this.resolution = resolution;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus( String status )
	{
		this.status = status;
	}

	public int getStatusCode()
	{
		return statusCode;
	}

	public void setStatusCode( int statusCode )
	{
		this.statusCode = statusCode;
	}

	public String getTargetDateTime()
	{
		return targetDateTime;
	}

	public void setTargetDateTime( String targetDateTime )
	{
		this.targetDateTime = targetDateTime;
	}

	public int getTypeId()
	{
		return typeId;
	}

	public void setTypeId( int typeId )
	{
		this.typeId = typeId;
	}

	public Log getLog()
	{
		return log;
	}

	public void setLog( Log log )
	{
		this.log = log;
	}

	public int getAddressPId()
	{
		return addressPId;
	}

	public void setAddressPId( int addressPId )
	{
		this.addressPId = addressPId;
	}
}
