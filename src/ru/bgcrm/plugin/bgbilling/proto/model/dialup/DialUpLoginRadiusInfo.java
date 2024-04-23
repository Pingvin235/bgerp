package ru.bgcrm.plugin.bgbilling.proto.model.dialup;

import java.util.ArrayList;
import java.util.List;

public class DialUpLoginRadiusInfo
{
	public static final int ATTR_MODE_GLOBAL_AND_LOCAL = 0;
	public static final int ATTR_MODE_ONLY_LOCAL = 1;

	private String realmGroup;
	private int attributeMode;
	private final List<DialUpLoginAttrSet> attrSetList = new ArrayList<>();
	private final List<DialUpLoginAttr> attrList = new ArrayList<>();

	public String getRealmGroup()
	{
		return realmGroup;
	}

	public void setRealmGroup( String realmGroup )
	{
		this.realmGroup = realmGroup;
	}

	public int getAttributeMode()
	{
		return attributeMode;
	}

	public void setAttributeMode( int attributeMode )
	{
		this.attributeMode = attributeMode;
	}

	public List<DialUpLoginAttrSet> getAttrSetList()
	{
		return attrSetList;
	}

	public void addAttrSet( DialUpLoginAttrSet attrSet )
	{
		attrSetList.add( attrSet );
	}

	public List<DialUpLoginAttr> getAttrList()
	{
		return attrList;
	}

	public void addAttr( DialUpLoginAttr attr )
	{
		attrList.add( attr );
	}
}