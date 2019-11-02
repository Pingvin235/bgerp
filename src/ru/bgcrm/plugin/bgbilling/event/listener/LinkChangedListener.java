package ru.bgcrm.plugin.bgbilling.event.listener;

import java.sql.Connection;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.client.ProcessChangedEvent;
import ru.bgcrm.event.link.LinkAddedEvent;
import ru.bgcrm.event.link.LinkAddingEvent;
import ru.bgcrm.event.listener.EventListener;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Customer;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.plugin.bgbilling.dao.ContractCustomerDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.struts.action.LinkAction;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class LinkChangedListener
{
	private static final Logger log = Logger.getLogger( LinkChangedListener.class );
	
	public LinkChangedListener()
	{
		EventProcessor.subscribe( new EventListener<LinkAddedEvent>()
 	    {
 	    	@Override
 	    	public void notify( LinkAddedEvent e, ConnectionSet connectionSet )
 	    		throws Exception
 	    	{
 	    		linkAdded( e, connectionSet );
 	    	}
 	    }, LinkAddedEvent.class );		                  		
	}
	
	private void linkAdded( LinkAddingEvent event, ConnectionSet connectionSet )
    	throws Exception
    {
    	CommonObjectLink link = event.getLink();
    	
    	// обработка только привязки договора к процессу
    	if( !Process.OBJECT_TYPE.equals( link.getObjectType() ) ||
    		!link.getLinkedObjectType().startsWith( "contract:" ) )
    	{
    		return;
    	}
    	
    	ProcessDAO processDao = new ProcessDAO( connectionSet.getConnection() );
    	
    	int processId = event.getLink().getObjectId();
    	
		Process process = processDao.getProcess( processId );
    	if( process == null )
    	{
    		log.warn( "Process with id: " + processId + " not found."  );
    		return;
    	}
    	
    	ProcessType type = ProcessTypeCache.getProcessType( process.getTypeId() );
    	if( type == null )
    	{
    		log.warn( "Process type with id: " + process.getTypeId() + " not found." );
    		return;
    	}
    	
    	String contractTitle = link.getLinkedObjectTitle();
    	String billingId = StringUtils.substringAfter( link.getLinkedObjectType(), ":" );
    	
    	boolean groupsChanged = false;
    	
    	// переход до первого подходящего правила и установка групп
    	for( ParameterMap pm : type.getProperties().getConfigMap().subIndexed( "bgbilling:processLinkedContract." ).values() )
    	{
    		String titleRegexp = pm.get( "titleRegexp" );
    		Set<String> billingIds = Utils.toSet( pm.get( "billingIds" ) );
    		
    		if( ( Utils.notBlankString( titleRegexp ) && contractTitle.matches( titleRegexp ) ) ||
    			( billingIds.size() > 0 && billingIds.contains( billingId ) ) )
    		{
        		Set<Integer> groupIds = Utils.toIntegerSet( pm.get( "groupIds" ) );
        		if( groupIds.size() == 0 )
        		{
        			continue;
        		}
        		
        		if( log.isDebugEnabled() )
        		{
        			log.debug( "Adding groups: " + groupIds + " for linked contract title: " + contractTitle  );
        		}
        		
        		groupsChanged = process.getGroupIds().addAll( groupIds );
        		
        		break;
    		}
    	}
    	
    	if( groupsChanged )
    	{
    		processDao.updateProcessGroups( process.getProcessGroups(), processId );    		
    		event.getForm().getResponse().addEvent( new ProcessChangedEvent( processId ) );
    	}
    	
    	if( type.getProperties().getConfigMap().getBoolean( "bgbilling:linkCustomerOnContractLink", true ) )
    	{    	
    		Connection con = connectionSet.getConnection();
    		
    		Customer customer = new ContractCustomerDAO( con ).getContractCustomer( new Contract( billingId, link.getLinkedObjectId() ) );
    		if( customer != null )
    		{
    			CommonObjectLink customerLink = new CommonObjectLink();
				customerLink.setObjectId( link.getObjectId() );
				customerLink.setObjectType( link.getObjectType() );
				customerLink.setLinkedObjectId( customer.getId() );
				customerLink.setLinkedObjectTitle( customer.getTitle() );
				customerLink.setLinkedObjectType( Customer.OBJECT_TYPE );
				
				if( !new ProcessLinkDAO( con ).isLinkExists( customerLink ) )
				{
					LinkAction.addLink( event.getForm(), connectionSet.getConnection(), customerLink );
				}
    		}
    	}
    }
}
