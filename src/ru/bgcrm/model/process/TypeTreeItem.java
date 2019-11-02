package ru.bgcrm.model.process;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import ru.bgcrm.model.IdTitleTreeItem;

public class TypeTreeItem
	extends IdTitleTreeItem<TypeTreeItem>
{
	/**
	 * Рекурсивно копирует дерево с ограничением по выбранным типам.
	 * Если узел выбран в наборе - попадает он и все его дочерние узлы.
	 * Если выбран дочерний узел - попадает они в все его родительские узлы.
	 * 
	 * @param typeSet
	 * @return
	 */
	public TypeTreeItem clone( Set<Integer> typeSet )
	{
		return clone( typeSet, false );
	}

	public TypeTreeItem clone( Set<Integer> typeSet, boolean onlyTypesInSet )
	{
		TypeTreeItem treeItem = new TypeTreeItem();
		treeItem.setId( id );
		treeItem.setTitle( title );
		treeItem.setChildren( this.children );

		if( typeSet.contains( id ) )
		{
			return treeItem;
		}
		else
		{
			List<TypeTreeItem> childs = new ArrayList<TypeTreeItem>();
			for( TypeTreeItem item : this.children )
			{
				TypeTreeItem tempItem = item.clone( typeSet, onlyTypesInSet );

				if( tempItem != null )
				{
					if( onlyTypesInSet )
					{
						Set<Integer> childIds = tempItem.getAllChildIds();
						childIds.remove( tempItem.getId() );
						if( !childIds.isEmpty() && CollectionUtils.intersection( typeSet, childIds ).isEmpty() )
						{
							continue;
						}

						List<TypeTreeItem> acceptedChildList = new ArrayList<TypeTreeItem>();
						for( TypeTreeItem child : tempItem.getChildren() )
						{
							if( !CollectionUtils.intersection( typeSet, child.getAllChildIds() ).isEmpty() )
							{
								acceptedChildList.add( child );
							}
						}

						treeItem.setChildren( acceptedChildList );
					}
					childs.add( tempItem );
				}
			}
			treeItem.setChildren( childs );
			if( childs.isEmpty() )
			{
				return null;
			}
			return treeItem;
		}
	}

	/**
	 * Возвращает код узла и коды всех узлов-потомков данного узла.
	 * 
	 * @return
	 */
	public Set<Integer> getAllChildIds()
	{
		Set<Integer> result = new HashSet<Integer>();

		result.add( id );
		for( TypeTreeItem childItem : children )
		{
			result.addAll( childItem.getAllChildIds() );
		}

		return result;
	}

	/**
	 * Возвращает коды типов процессов с фильтром по выбранным типам.
	 * Если узел выбран в наборе - добавляются все его дочерние узлы.
	 * 
	 * @return
	 */
	public Set<Integer> getSelectedChildIds( Set<Integer> typeSet )
	{
		Set<Integer> result = new HashSet<Integer>( typeSet.size() );

		for( TypeTreeItem childItem : children )
		{
			// если узел есть в результате - есть там и уже все его потомки
			if( result.contains( childItem.getId() ) )
			{
				continue;
			}

			if( typeSet.contains( childItem.getId() ) )
			{
				result.addAll( childItem.getAllChildIds() );
			}
			else
			{
				result.addAll( childItem.getSelectedChildIds( typeSet ) );
			}
		}

		return result;
	}
}