package ru.bgcrm.plugin.bgbilling.creator;

/**
import static ru.bgcrm.model.param.Parameter.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import ru.bgcrm.model.BGException;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;


 * Поле - набор параметров контрагента, участвующий в импорте.
 * Набор вместо одного параметра в основном из-за адресного параметра, чтобы перечислять все возможные значения.
 
class Field
{
	private static final Logger log = Logger.getLogger( Field.class );
	
	private static final Set<String> ALLOWED_PARAM_TYPES = new HashSet<String>( Arrays.asList( TYPE_ADDRESS, TYPE_TEXT, TYPE_PHONE, TYPE_DATE ) );
	
	// тип параметра
	public final String type;
	// код параметра
	public final int id;

	public Field( ParameterMap params )
	    throws BGException
	{
		this.type = params.get( "type" );
		this.id =  Utils.parseInt( params.get( "id" ) );
		
		if( !ALLOWED_PARAM_TYPES.contains( type ) )
		{
			throw new BGException( "Unsupported key param type:" + type );
		}
		if( id <= 0 )
		{
			throw new BGException( "Not defined id." );
		}
		
		log.info( "Field type: " + type + "; id: " + id );
		
		//if( idList.size() > 0 && type.equals( TYPE_ADDRESS ) )			
	}
}
*/