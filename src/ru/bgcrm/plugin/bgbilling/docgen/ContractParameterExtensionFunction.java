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

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractParamDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractParameter;
import ru.bgcrm.plugin.bgbilling.proto.model.ParamAddressValue;
import ru.bgcrm.plugin.bgbilling.proto.model.ParameterType;
import ru.bgcrm.plugin.document.docgen.CommonExtensionFunction;
import ru.bgcrm.util.XMLUtils;

public class ContractParameterExtensionFunction
	extends CommonExtensionFunction
{
	public ContractParameterExtensionFunction( Connection con )
	{
		super( con, "bgbilling-parameter" );
	}

	@Override
	public SequenceType[] getArgumentTypes()
	{
		return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING, SequenceType.SINGLE_INTEGER, SequenceType.SINGLE_INTEGER };
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
					String loginAndPswd = ((StringValue)arguments[1].next()).getStringValue();
					int contractId = (int)((IntegerValue)arguments[2].next()).longValue();
					int paramId = (int)((IntegerValue)arguments[3].next()).longValue();

					String[] login_pswd = loginAndPswd.split( ":" );

					ContractParamDAO contractParamDAO = new ContractParamDAO( new User( login_pswd[0], login_pswd[1] ), billingId );
					ContractParameter parameter = contractParamDAO.getParameter( contractId, paramId );

					Document doc = XMLUtils.newDocument();
					Element eventEl = XMLUtils.newElement( doc, "parameter" );
					
					if( parameter != null )
					{
						switch( parameter.getParamType() )
						{
							case ParameterType.ContractType.TYPE_ADDRESS:
								ParamAddressValue billingAddress = contractParamDAO.getAddressParam( contractId, paramId );
								eventEl.setAttribute( "value", contractParamDAO.toCrmObject( billingAddress, con ).getValue() );
								break;
						}
					}

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
