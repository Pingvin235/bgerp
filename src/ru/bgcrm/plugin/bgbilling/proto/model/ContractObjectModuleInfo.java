package ru.bgcrm.plugin.bgbilling.proto.model;

import java.util.ArrayList;
import java.util.List;

public class ContractObjectModuleInfo
{
	public class ContractObjectModule
	{
		private Integer id;
		private String name;
		private String packClient;
		private String title;

		public Integer getId()
		{
			return id;
		}

		public void setId( Integer id )
		{
			this.id = id;
		}

		public String getName()
		{
			return name;
		}

		public void setName( String name )
		{
			this.name = name;
		}

		public String getPackClient()
		{
			return packClient;
		}

		public void setPackClient( String packClient )
		{
			this.packClient = packClient;
		}

		public String getTitle()
		{
			return title;
		}

		public void setTitle( String title )
		{
			this.title = title;
		}
	}

	public class ContractObjectModuleData
	{
		private String comment;
		private String data;
		private String module;
		private String period;

		public String getComment()
		{
			return comment;
		}

		public void setComment( String comment )
		{
			this.comment = comment;
		}

		public String getData()
		{
			return data;
		}

		public void setData( String data )
		{
			this.data = data;
		}

		public String getModule()
		{
			return module;
		}

		public void setModule( String module )
		{
			this.module = module;
		}

		public String getPeriod()
		{
			return period;
		}

		public void setPeriod( String period )
		{
			this.period = period;
		}
	}

	private Integer objectId;
	private List<ContractObjectModule> moduleList = new ArrayList<>();
	private List<ContractObjectModuleData> moduleDataList = new ArrayList<>();

	public List<ContractObjectModule> getModuleList()
	{
		return moduleList;
	}

	public void setModuleList( List<ContractObjectModule> moduleList )
	{
		this.moduleList = moduleList;
	}

	public List<ContractObjectModuleData> getModuleDataList()
	{
		return moduleDataList;
	}

	public void setModuleDataList( List<ContractObjectModuleData> moduleDataList )
	{
		this.moduleDataList = moduleDataList;
	}

	public Integer getObjectId()
	{
		return objectId;
	}

	public void setObjectId( Integer objectId )
	{
		this.objectId = objectId;
	}
}
