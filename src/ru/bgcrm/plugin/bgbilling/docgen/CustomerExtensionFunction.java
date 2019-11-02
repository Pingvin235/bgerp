package ru.bgcrm.plugin.bgbilling.docgen;

import java.sql.Connection;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.dao.CustomerLinkDAO;
import ru.bgcrm.dao.Tables;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Customer;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.document.docgen.CommonExtensionFunction;
import ru.bgcrm.util.XMLDatabaseElementSerializer;
import ru.bgcrm.util.XMLUtils;

public class CustomerExtensionFunction
	extends CommonExtensionFunction
{
	private User user;

	public CustomerExtensionFunction( Connection con, User user )
	{
		super( con, "bgbilling-customerByContractId" );
		this.user = user;
	}

	@Override
	public SequenceType[] getArgumentTypes()
	{
		return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.SINGLE_INTEGER };
	}

	@Override
	public SequenceType getResultType( SequenceType[] suppliedArgumentTypes )
	{
		return SequenceType.SINGLE_NODE;
	}

	@SuppressWarnings("serial")
	@Override
	public ExtensionFunctionCall makeCallExpression()
	{
		return new ExtensionFunctionCall()
		{
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public SequenceIterator call( SequenceIterator[] arguments, XPathContext ctx )
				throws XPathException
			{
				try
				{
					String billingId = ((StringValue)arguments[0].next()).getStringValue();
					int contractId = (int)((IntegerValue)arguments[1].next()).longValue();

					// контрагент
					CommonObjectLink link = new CommonObjectLink();
					link.setLinkedObjectType( "contract:" + billingId );
					link.setLinkedObjectId( contractId );

					SearchResult<Customer> customerSearch = new SearchResult<Customer>();
					new CustomerLinkDAO( con ).searchCustomerByLink( customerSearch, link );

					if( customerSearch.getList().size() > 0 )
					{
						Integer customerId = customerSearch.getList().get( 0 ).getId();

						Document doc = XMLUtils.newDocument();

						Element data = XMLUtils.newElement( doc, "data" );

						// customer
						XMLDatabaseElementSerializer.addItemsFromRS( con, data,
																	 Tables.TABLE_CUSTOMER.trim(), "id",
																	 String.valueOf( customerId ),
																	 null, false, null, null, "" );
						// параметры
						addParams( data, Customer.OBJECT_TYPE, customerId );

						// линки
						Element linksEl = XMLUtils.newElement( data, "links" );
						XMLDatabaseElementSerializer.addItemsFromRS( con, linksEl,
																	 Tables.TABLE_CUSTOMER_LINK.trim(), "customer_id",
																	 String.valueOf( customerId ),
																	 null, false, null, null, "" );

						return SingletonIterator.makeIterator( new DocumentWrapper( doc, "", new Configuration() ) );
					}
					
					return null;
				}
				catch( Exception e )
				{
					throw new XPathException( e );
				}
			}
		};
	}
}
