package ru.bgcrm.plugin.bgbilling.proto.model.ipn;

import java.util.ArrayList;
import java.util.List;

import ru.bgcrm.model.IdTitle;

public class ResourceCategory
	extends IdTitle
{
	private List<ResourceCategory> subcategoryList = null;

	public ResourceCategory( int id, String title )
	{
		super( id, title );
	}

	public List<ResourceCategory> getSubcategoryList()
	{
		return subcategoryList;
	}

	public void addSubcategory( ResourceCategory item )
	{
		if( subcategoryList == null )
		{
			subcategoryList = new ArrayList<ResourceCategory>();
		}
		subcategoryList.add( item );
	}
}
