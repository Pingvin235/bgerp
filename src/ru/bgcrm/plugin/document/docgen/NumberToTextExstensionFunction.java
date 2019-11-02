package ru.bgcrm.plugin.document.docgen;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import ru.bgcrm.util.NumberToText;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

public class NumberToTextExstensionFunction
extends ExtensionFunctionDefinition
{
	protected NumberToTextExstensionFunction()
	{
		super( );
	}

	@Override public StructuredQName getFunctionQName()
	{
		return new StructuredQName( "bgcrm", "http://bgcrm.ru/saxon-extension", "numberToText" );
	}

	@Override public SequenceType[] getArgumentTypes()
	{
		return new SequenceType[] { SequenceType.SINGLE_ITEM };
	}

	@Override public SequenceType getResultType( SequenceType[] sequenceTypes )
	{
		return SequenceType.SINGLE_NODE;
	}

	@Override public ExtensionFunctionCall makeCallExpression()
	{
		return new ExtensionFunctionCall()
		{
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public SequenceIterator call( SequenceIterator[] arguments, XPathContext ctx )
			throws XPathException
			{
				try
				{
					Item arg = arguments[0].next();
					long number = 0;

					if( arg instanceof IntegerValue )
					{
						number = ((IntegerValue)arguments[0].next()).longValue();
					}
					else
					{
						number = Utils.parseLong( arg.getStringValue() );
					}

					Document doc = XMLUtils.newDocument();
					Element data = XMLUtils.newElement( doc, "data" );

					Node node = XMLUtils.newElement( data, "number" );
					((Element)node).setAttribute( "value", String.valueOf( number ) );
					((Element)node).setAttribute( "text", NumberToText.numberToString( number ) );
					data.appendChild( node );

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
