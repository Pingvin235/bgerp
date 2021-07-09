package ru.bgcrm.plugin.bgbilling.event.listener;

import org.apache.commons.lang.StringUtils;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.listener.EventListener;
import ru.bgcrm.event.process.ProcessDoActionEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractObjectParamDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractParamDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractParameter;
import ru.bgcrm.plugin.bgbilling.proto.model.ParamAddressValue;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class ProcessDoActionListener
{
	public ProcessDoActionListener()
	{
		EventProcessor.subscribe( new EventListener<ProcessDoActionEvent>()
 	    {
 	    	@Override
 	    	public void notify( ProcessDoActionEvent e, ConnectionSet connectionSet )
 	    		throws Exception
 	    	{
 	    		doCommand( e, connectionSet );
 	    	}
 	    }, ProcessDoActionEvent.class );
	}
	
	private void doCommand( ProcessDoActionEvent e, ConnectionSet conSet )
		throws Exception
	{
		final String prefix = "bgbilling:";
		
		String command = StringUtils.substringAfter( e.getActionName(), prefix );
		if( Utils.isBlankString( command ) )
		{
			return;
		}
		
		Process process = e.getProcess();
		
		if( command.startsWith( "getLinkedContractAddressParam" ) )
		{
			command = StringUtils.substringAfter( command, ":" );
			
			String billingId = StringUtils.substringBefore( command, ":" );
			int billingParamId = Utils.parseInt( StringUtils.substringBetween( command, ":" ) );
			int crmParamId = Utils.parseInt( StringUtils.substringAfterLast( command, ":" ) );
			 
			CommonObjectLink link = Utils.getFirst( new ProcessLinkDAO( conSet.getConnection() ).getObjectLinksWithType( process.getId(), 
			                                                                                                             Contract.OBJECT_TYPE + ":" + billingId ) );
			if( link == null )
			{
				return;
			}
			
			ContractParamDAO paramDAO = new ContractParamDAO( e.getForm().getUser(), billingId );
			ParamAddressValue billingAddress = paramDAO.getAddressParam( link.getLinkedObjectId(), billingParamId );
			if( billingAddress == null )
			{
				return;
			}
			
			ParameterAddressValue address = ContractObjectParamDAO.toCrmObject( billingAddress, conSet.getConnection() );

			if( address.getHouseId() != 0 || Utils.notBlankString( address.getValue() ) )
			{
				new ParamValueDAO( conSet.getConnection() ).updateParamAddress( process.getId(), crmParamId, 0, address );
			}
		}
		// текстовое представление параметра в параметр ЦРМки текстовый же
		else if( command.startsWith( "getLinkedContractParam" ) )
		{ 
			command = StringUtils.substringAfter( command, ":" );
			
			String billingId = StringUtils.substringBefore( command, ":" );
			int billingParamId = Utils.parseInt( StringUtils.substringBetween( command, ":" ) );
			int crmParamId = Utils.parseInt( StringUtils.substringAfterLast( command, ":" ) );
			
			String paramValue = getLinkedContractParamText( e, conSet, process, billingId, billingParamId, false, null );
			updateTextParam( conSet, process, crmParamId, paramValue );
		}
		else if( command.startsWith( "linkedContractParamToDescription" ) )
		{
			final boolean before = command.startsWith( "linkedContractParamToDescriptionBefore" );
			
			command = StringUtils.substringAfter( command, ":" );
			
			String[] tokens = command.split( ":" );
			if( tokens.length < 2 )
			{
				throw new BGException( "Incorrect tokens: " + command );
			}
			
			String billingId = tokens[0];
			int billingParamId = Utils.parseInt( tokens[1] );
			String paramPrefix  = tokens.length > 2 ? tokens[2] : null;
						
			String textForAdd = getLinkedContractParamText( e, conSet, process, billingId, billingParamId, true, paramPrefix );
			addToDescription( conSet, process, textForAdd, before );
		}
		else if( command.startsWith( "linkedContractCommentToDescription" ) )
		{
			final boolean before = command.startsWith( "linkedContractCommentToDescriptionBefore" );
			
			command = StringUtils.substringAfter( command, ":" );
			
			String[] tokens = command.split( ":" );
			if( tokens.length < 1 )
			{
				throw new BGException( "Incorrect tokens: " + command );
			}
			
			String billingId = tokens[0];
			String commentPrefix  = tokens.length > 1 ? tokens[1] : null;
						
			CommonObjectLink link = Utils.getFirst( new ProcessLinkDAO( conSet.getConnection() ).getObjectLinksWithType( process.getId(), 
			                                                                                                             Contract.OBJECT_TYPE + ":" + billingId ) );
			if( link == null )
			{
				return;
			}
			
			Contract contract = new ContractDAO( e.getForm().getUser(), billingId ).getContractById( link.getLinkedObjectId() );
			if( contract != null )
			{
				String textForAdd = "";
				if( Utils.notBlankString( commentPrefix ) )
				{
					textForAdd += commentPrefix + ": ";
				}
				textForAdd += contract.getComment();
				
				addToDescription( conSet, process, textForAdd, before );
			}
		}
		else if( command.startsWith( "linkedContractCommentToParam" ) )
		{
			command = StringUtils.substringAfter( command, ":" );
			
			String[] tokens = command.split( ":" );
			if( tokens.length < 2 )
			{
				throw new BGException( "Incorrect tokens: " + command );
			}
			
			String billingId = tokens[0];
			int crmParamId = Utils.parseInt( tokens[1] );
						
			CommonObjectLink link = Utils.getFirst( new ProcessLinkDAO( conSet.getConnection() ).getObjectLinksWithType( process.getId(), 
			                                                                                                             Contract.OBJECT_TYPE + ":" + billingId ) );
			if( link == null )
			{
				return;
			}
			
			Contract contract = new ContractDAO( e.getForm().getUser(), billingId ).getContractById( link.getLinkedObjectId() );
			if( contract != null )
			{
				updateTextParam( conSet, process, crmParamId, contract.getComment() );
			}
		}
	}

	private void updateTextParam( ConnectionSet conSet, Process process, int crmParamId, String paramValue )
		throws Exception
	{
		if( Utils.notBlankString( paramValue ) )
		{
			Parameter param = ParameterCache.getParameter( crmParamId );
			if( param == null || !Parameter.TYPE_TEXT.equals( param.getType() ) )
			{
				throw new BGException( "В макросе getLinkedContractParam указан несуществующий либо не текстовый параметр с кодом " + crmParamId );
			}
			
			new ParamValueDAO( conSet.getConnection() ).updateParamText( process.getId(), crmParamId, paramValue );
		}
	}

	private String getLinkedContractParamText( ProcessDoActionEvent e, ConnectionSet conSet, Process process, 
	                                       	String billingId, int billingParamId, boolean addPrefix, String paramPrefix )
		throws BGException
	{
		CommonObjectLink link = Utils.getFirst( new ProcessLinkDAO( conSet.getConnection() ).getObjectLinksWithType( process.getId(), 
		                                                                                                             Contract.OBJECT_TYPE + ":" + billingId ) );
		if( link == null )
		{
			return  null;
		}
		
		String textForAdd = null;
		
		ContractParamDAO paramDAO = new ContractParamDAO( e.getForm().getUser(), billingId );
		for( ContractParameter param : paramDAO.getParameterList( link.getLinkedObjectId() ) )
		{
			if( param.getParamId() == billingParamId )
			{
				if( addPrefix )
				{
    				if( paramPrefix == null )
    				{
    					paramPrefix = param.getTitle();
    				}					
    				textForAdd = paramPrefix + ": " + param.getValue();
				}
				else
				{
					textForAdd = param.getValue();
				}
				break;
			}
		}
		
		return textForAdd;
	}
	
	private void addToDescription( ConnectionSet conSet, Process process, String textForAdd, boolean before )
		throws Exception
	{
		String description = process.getDescription();
		
		if( Utils.notBlankString( textForAdd ) && 
			!description.contains( textForAdd ) )
		{
			if( Utils.notBlankString( description ) )
			{
				if( before )
				{
					description = "\n" + description;
				}
				else
				{
					description += "\n";
				}
			}
			
			if( before )
			{
				process.setDescription( textForAdd + description );
			}
			else
			{
				process.setDescription( description + textForAdd );
			}
			new ProcessDAO( conSet.getConnection() ).updateProcess( process );
		}
	}
}
