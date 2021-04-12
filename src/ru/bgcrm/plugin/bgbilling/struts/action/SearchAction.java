package ru.bgcrm.plugin.bgbilling.struts.action;

import java.sql.Connection;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.w3c.dom.Document;

import ru.bgcrm.model.Page;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.plugin.bgbilling.CommonContractConfig;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.dao.BGBillingDAO;
import ru.bgcrm.plugin.bgbilling.dao.CommonContractDAO;
import ru.bgcrm.plugin.bgbilling.model.CommonContract;
import ru.bgcrm.plugin.bgbilling.proto.struts.action.ContractAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class SearchAction
	extends BaseAction
{
	/**
	 * Использовать из {@link ContractAction}. 
	 */
	@Deprecated
	public ActionForward contractSearch( ActionMapping mapping,
										 DynActionForm form,
										 HttpServletRequest request,
										 HttpServletResponse response,
										 ConnectionSet conSet )
		throws Exception
	{
		String searchBy = form.getParam( "searchBy" );
		Set<String> billingIds = form.getSelectedValuesStr( "billing" );

		if( Utils.notBlankString( searchBy ) )
		{
			BGBillingDAO billingDAO = new BGBillingDAO();
			Request req = new Request();
			Page page = new Page();
			page.setPageIndex( 1 );
			// получаем количество строк для вывода из конфига
			// properties
			page.setPageSize( setup.getInt( "bgbilling.contract.search.results.count", 25 ) );
			req.setPage( page );
			req.setModule( "contract" );
			req.setAttribute( "del", setup.getInt( "bgbilling.search.contract.del", 0 ) );
			req.setAttribute( "filter", 0 );
			req.setAttribute( "show_sub", 0 );
			req.setAttribute( "show_closed", "1" );

			if( "address".equals( searchBy ) )
			{
				String[] houseFrac = form.getParam( "house" ).split( "/" );

				req.setAction( "FindContract" );
				req.setAttribute( "type", 2 );
				req.setAttribute( "street", Utils.parseInt( form.getParam( "streetId" ) ) );
				req.setAttribute( "flat", form.getParam( "flat" ) );
				req.setAttribute( "room", form.getParam( "room" ) );

				if( houseFrac.length > 1 )
				{
					req.setAttribute( "house", houseFrac[0] );
					req.setAttribute( "frac", "/" + houseFrac[1] );
				}
				else
				{
					Matcher m = Pattern.compile( "\\d+|[а-я]" ).matcher( houseFrac[0] );
					if( m.find() )
					{
						req.setAttribute( "house", m.group() );
					}

					if( m.find() )
					{
						req.setAttribute( "frac", m.group() );
					}
				}
			}
			else if( "id".equals( searchBy ) )
			{
				req.setAction( "FindContractByID" );
				req.setAttribute( "id", form.getParam( "id" ) );
			}
			else
			{
				req.setAction( "FilterContract" );
				req.setAttribute( "type", -1 );
				if( "title".equals( searchBy ) )
				{
					req.setAttribute( "contractMask", form.getParam( "title" ) );
				}
				else if( "comment".equals( searchBy ) )
				{
					req.setAttribute( "contractComment", form.getParam( "comment" ) );
				}
			}

			Map<String, Document> result = billingDAO.doRequestToBilling( billingIds, form.getUser(), req );
			request.setAttribute( "result", result );
		}

		return html( conSet, mapping, form, "searchContractResult" );
	}

	public ActionForward commonContractSearch( ActionMapping mapping,
											   DynActionForm form,
											   HttpServletRequest request,
											   HttpServletResponse response,
											   Connection con )
		throws Exception
	{
		String searchBy = form.getParam( "searchBy" );

		if( Utils.notBlankString( searchBy ) )
		{
			if( "areaAndNumber".equals( searchBy ) )
			{
				SearchResult<CommonContract> result = new SearchResult<CommonContract>( form );

				CommonContract contract = new CommonContractDAO( con ).getContractByAreaAndNumber( form.getParamInt( "areaId", 0 ),
																								   form.getParamInt( "number", 0 ) );

				if( contract != null )
				{
					result.getList().add( contract );
					result.getPage().setRecordCount( 1 );
				}
				else
				{
					result.getPage().setRecordCount( 0 );
				}
				form.getResponse().setData( "areas", setup.getConfig( CommonContractConfig.class ).getCityAreaMap() );
				request.setAttribute( "data", result );
			}
		}
		return processUserTypedForward( con, mapping, form, response, "searchCommonContractResult" );
	}
}
