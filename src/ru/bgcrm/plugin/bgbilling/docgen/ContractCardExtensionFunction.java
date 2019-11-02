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

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO;
import ru.bgcrm.plugin.document.docgen.CommonExtensionFunction;

public class ContractCardExtensionFunction
	extends CommonExtensionFunction
{
	private User user;
	
	public ContractCardExtensionFunction( Connection con, User user )
    {
	    super( con, "bgbilling-contractCard" );
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
					
					ContractDAO contractDAO = new ContractDAO( user, billingId );
					Document doc = contractDAO.getContractCardDoc( contractId );
					
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
