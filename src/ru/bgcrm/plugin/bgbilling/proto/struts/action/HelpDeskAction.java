package ru.bgcrm.plugin.bgbilling.proto.struts.action;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.message.MessageType;
import ru.bgcrm.dao.message.config.MessageTypeConfig;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.plugin.bgbilling.dao.MessageTypeHelpDesk;
import ru.bgcrm.plugin.bgbilling.proto.dao.HelpDeskDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.helpdesk.HdTopic;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class HelpDeskAction
	extends BaseAction
{
	public ActionForward getAttach( ActionMapping mapping,
	                                DynActionForm form,
	                                HttpServletRequest request,
	                                HttpServletResponse response,
	                                ConnectionSet conSet )
		throws Exception
	{
		int processId = form.getParamInt( "processId" );
		String billingId = form.getParam( "billingId" );
		int attachId = form.getParamInt( "id" );
		String title = form.getParam( "title" );
				
		MessageTypeHelpDesk mt = null;
		
		MessageTypeConfig config = setup.getConfig( MessageTypeConfig.class );
		for( MessageType type : config.getTypeMap().values() )
		{
			if( type instanceof MessageTypeHelpDesk &&
				((MessageTypeHelpDesk)type).getBillingId().equals( billingId ) )
			{
				mt = (MessageTypeHelpDesk)type;
				break;
			}
		}
		
		if( mt != null )
		{	
			HelpDeskDAO hdDao = new HelpDeskDAO( mt.getUser(), mt.getDbInfo() );
			
			CommonObjectLink link = Utils.getFirst( new ProcessLinkDAO( conSet.getConnection() ).getObjectLinksWithType( processId, mt.getObjectType() ) );
			if( link == null )
			{
				throw new ru.bgcrm.model.BGException( "К процессу не привязан топик HelpDesk." );
			}
			
			SearchResult<HdTopic> topicSearch = new SearchResult<HdTopic>();
			hdDao.seachTopicList( topicSearch, null, null, false, link.getLinkedObjectId() );
			
			HdTopic topic = Utils.getFirst( topicSearch.getList() );
			if( topic == null )
			{
				throw new BGException( "Не найден топик HelpDesk с кодом: " + link.getLinkedObjectId() );
			}
			
			byte[] attach = hdDao.getAttach( topic.getContractId(), attachId );
			
			Utils.setFileNameHeades( response, title );

			OutputStream out = response.getOutputStream();

			IOUtils.copy( new ByteArrayInputStream( attach ), out );

			out.flush();
		}
		
		return null;
	}
	
	public ActionForward markMessageRead( ActionMapping mapping,
	                                      DynActionForm form,
	                                      HttpServletRequest request,
	                                      HttpServletResponse response,
	                                      ConnectionSet conSet )
		throws Exception
	{
		int messageId = form.getParamInt( "messageId" );
		if( messageId <= 0 )
		{
			throw new BGIllegalArgumentException();
		}
		
		MessageDAO messageDao = new MessageDAO( conSet.getConnection() );
		
		Message message = messageDao.getMessageById( messageId );
		if( message == null )
		{
			throw new BGException( "Сообщение не найдено" );			
		}
		
		MessageTypeConfig config = setup.getConfig( MessageTypeConfig.class );
		
		MessageType mt = config.getTypeMap().get( message.getTypeId() );
		if( mt == null || !(mt instanceof MessageTypeHelpDesk) )
		{
			throw new BGException( "Не найден тип сообщения либо это не HelpDesk сообщение" );
		}
		
		MessageTypeHelpDesk mtHd = (MessageTypeHelpDesk)mt;
		
		HelpDeskDAO hdDao = new HelpDeskDAO( form.getUser(), mtHd.getDbInfo() );
		hdDao.markMessageRead( Utils.parseInt( message.getSystemId() ) );
		
		message.setToTime( new Date() );
		message.setUserId( form.getUserId() );
		
		messageDao.updateMessageProcess( message );
		
		return processJsonForward( conSet, form, response );
	}
}