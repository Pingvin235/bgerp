package ru.bgcrm.plugin.document.docgen;

import java.sql.Connection;
import java.util.List;

import org.w3c.dom.Element;

import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterValuePair;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.util.XMLUtils;

public abstract class CommonExtensionFunction
	extends ExtensionFunctionDefinition
{
	protected Connection con;
	private String functionName;

	protected CommonExtensionFunction( Connection con, String functionName )
	{
		this.con = con;
		this.functionName = functionName;
	}

	@Override
	public StructuredQName getFunctionQName()
	{
		return new StructuredQName( "bgcrm", "http://bgcrm.ru/saxon-extension", functionName );
	}

	protected void addParams( Element whereAdd, String objectType, int objectId )
		throws Exception
	{
		Element paramsEl = XMLUtils.newElement( whereAdd, "parameters" );

		List<Parameter> paramList;

		switch( objectType )
		{
			case Process.OBJECT_TYPE:
				Process process = new ProcessDAO( con ).getProcess( objectId );
				ProcessType type = ProcessTypeCache.getProcessType( process.getTypeId() );

				paramList = ParameterCache.getParameterList( type.getProperties().getParameterIds() );
				break;

			default:
				paramList = ParameterCache.getObjectTypeParameterList( objectType, -1 );
				break;
		}


		List<ParameterValuePair> parameterValuePairList = new ParamValueDAO( con ).loadParameters( paramList, objectId, true );

		/* for( ParameterValuePair pvp : parameterValuePairList )
		{
			Parameter parameter = pvp.getParameter();
			EventProcessor.processEvent( new ParamListShowListEvent( new DynActionForm(), pvp, objectId, objectType ), parameter.getScript(), new SingleConnectionConnectionSet( con ) );

			if( Process.OBJECT_TYPE.equals( objectType ) )
			{
				Process process = new ProcessDAO( con ).getProcess( objectId );
				ProcessType type = ProcessTypeCache.getProcessType( process.getTypeId() );
				EventProcessor.processEvent( new ParamListShowListEvent( new DynActionForm(), pvp, objectId, objectType ), type.getProperties().getActualScriptName(), new SingleConnectionConnectionSet( con ) );
			}
		} */

		for( ParameterValuePair pair : parameterValuePairList )
		{
			Element param = XMLUtils.newElement( paramsEl, "parameter" );
			param.setAttribute( "id", String.valueOf( pair.getParameter().getId() ) );
			param.setAttribute( "title", pair.getParameter().getTitle() );

			if( pair.getValue() == null )
			{
				param.setAttribute( "value", "" );
				continue;
			}

			param.setAttribute( "value", pair.getValueTitle() );
		}
	}
}
