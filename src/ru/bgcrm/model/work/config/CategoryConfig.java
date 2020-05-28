package ru.bgcrm.model.work.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import ru.bgcrm.model.IdTitle;
import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

public class CategoryConfig
	extends Config
{
	private List<Category> list = new ArrayList<Category>();

	public CategoryConfig( ParameterMap setup )
	{
		super( setup );
		
		for( Map.Entry<Integer, ParameterMap> me : setup.subIndexed( "callboard.worktype.category." ).entrySet() )
		{
			ParameterMap params = me.getValue();		
			list.add( new Category( me.getKey(), params.get( "title", "" ), params.getBoolean( "public", false ) ) );
		}
	}
	
	public List<Category> getCategoryList( Set<Integer> allowOnly )
	{
		List<Category> result = new ArrayList<Category>();
		
		for( Category cat : this.list )
		{
			if( CollectionUtils.isEmpty( allowOnly ) || allowOnly.contains( cat.getId() ) || cat.isForAll() )
			{
				result.add( cat );
			}
		}
		
		return result;
	}
	
	public Set<Integer> getCategoryIds( Set<Integer> allowOnly )
	{
		return Utils.getObjectIdsSet( getCategoryList( allowOnly ) );
	}

	public static class Category
		extends IdTitle
	{
		private final boolean forAll;
		
		public Category( int id, String title, boolean forAll )
		{
			super( id, title );
			this.forAll = forAll;
		}

		public boolean isForAll()
		{
			return forAll;
		}
	}	
}
