package ru.bgcrm.plugin.bgbilling.proto.model;

public class ParameterType
{
	public class ContractType
	{
		public final static int TYPE_TEXT = 1;
		public final static int TYPE_DATE = 6;
		public final static int TYPE_EMAIL = 3;
		public final static int TYPE_LIST = 7;
		public final static int TYPE_PHONE = 9;
		public final static int TYPE_ADDRESS = 2;
		public final static int TYPE_FLAG = 5;
	}
	
	public class ContractObjectType
	{
		public final static int TYPE_TEXT = 1;
		public final static int TYPE_LIST = 2;
		public final static int TYPE_DATE = 3;
		public final static int TYPE_ADDRESS = 4;
	}
}
