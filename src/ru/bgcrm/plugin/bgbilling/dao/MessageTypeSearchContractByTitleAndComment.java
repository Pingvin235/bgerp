package ru.bgcrm.plugin.bgbilling.dao;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.DBInfoManager;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO.SearchOptions;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class MessageTypeSearchContractByTitleAndComment
	extends MessageTypeSearchBilling
{
	private static final Logger log = Logger.getLogger( MessageTypeSearchContractByTitleAndComment.class );
	
	public MessageTypeSearchContractByTitleAndComment( ParameterMap config )
		throws BGException
	{
		super( config );
	}
	
	@Override
	public String getJsp()
	{
		return "/WEB-INF/jspf/user/plugin/bgbilling/message_search_contract_title_comment.jsp";
	}

	@Override
	public void search( DynActionForm form, ConnectionSet conSet, Message message, Set<CommonObjectLink> result )
		throws BGException
	{
		DBInfo dbInfo = DBInfoManager.getDbInfo( billingId );
		if( dbInfo == null )
		{
			log.warn( "Billing not found: " + billingId );			
			return;
		}
		
		String title = form.getParam( "title" );
		String comment = form.getParam( "comment" );
		
		if( (Utils.isBlankString( title ) && Utils.isBlankString( comment ) ) || 
		    (Utils.notBlankString( title ) && title.length() < 3) ||
			(Utils.notBlankString( comment ) && comment.length() < 3 ) )
		{
			return;
		}
		
		SearchResult<IdTitle> searchResult = new SearchResult<IdTitle>();
		new ContractDAO( form.getUser(), billingId ).searchContractByTitleComment( searchResult, title, comment, 
		                                                                           new SearchOptions( false, false, false ) );
		
		for( IdTitle object : searchResult.getList() )
		{
			result.add( new CommonObjectLink( 0, Contract.OBJECT_TYPE + ":" + billingId, object.getId(), 
			                                  StringUtils.substringBeforeLast( object.getTitle(), "[" ).trim(), 
			                                  StringUtils.substringBetween( object.getTitle(), "[", "]" ).trim() ) );
		}
	}	
}
