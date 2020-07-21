package ru.bgcrm.plugin.document.docgen;

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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.dao.Tables;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.util.XMLDatabaseElementSerializer;
import ru.bgcrm.util.XMLUtils;

public class CustomerExtensionFunction
	extends CommonExtensionFunction
{
	public CustomerExtensionFunction( Connection con )
    {
	    super( con, "customer" );
    }

	@Override
    public SequenceType[] getArgumentTypes()
    {
		return new SequenceType[] { SequenceType.SINGLE_INTEGER };
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
					int customerId = (int)((IntegerValue)arguments[0].next()).longValue();
					
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
                catch( Exception e )
                {
	                throw new XPathException( e );
                }
			}
		};
	}
}