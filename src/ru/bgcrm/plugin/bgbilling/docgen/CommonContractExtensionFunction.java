package ru.bgcrm.plugin.bgbilling.docgen;

import java.sql.Connection;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;
import ru.bgcrm.plugin.bgbilling.dao.Tables;
import ru.bgcrm.plugin.bgbilling.model.CommonContract;
import ru.bgcrm.plugin.document.docgen.CommonExtensionFunction;
import ru.bgcrm.util.XMLDatabaseElementSerializer;
import ru.bgcrm.util.XMLUtils;

public class CommonContractExtensionFunction
	extends CommonExtensionFunction
{
	public CommonContractExtensionFunction( Connection con )
    {
	    super( con, "bgbilling-commonContract" );
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
					int commonContractId = (int)((IntegerValue)arguments[0].next()).longValue();
					
					Document doc = XMLUtils.newDocument();
					
					Element data = XMLUtils.newElement( doc, "data" );
					
					XMLDatabaseElementSerializer.addItemsFromRS( con, data, 
					                                             Tables.TABLE_COMMON_CONTRACT.trim(), "id", 
					                                             String.valueOf( commonContractId ), 
					                                             null, false, null, null, "" );
					addParams( data, CommonContract.OBJECT_TYPE, commonContractId );
					
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