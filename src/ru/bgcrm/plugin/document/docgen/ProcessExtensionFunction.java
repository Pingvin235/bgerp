package ru.bgcrm.plugin.document.docgen;

import java.sql.Connection;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.dao.process.Tables;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLDatabaseElementSerializer;
import ru.bgcrm.util.XMLUtils;

public class ProcessExtensionFunction
	extends CommonExtensionFunction
{
	public ProcessExtensionFunction( Connection con )
	{
		super( con, "process" );
	}

	@Override
	public SequenceType[] getArgumentTypes()
	{
		return new SequenceType[] { SequenceType.ANY_SEQUENCE };
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
					int processId = 0;

					Item item = arguments[0].next();

					if( item instanceof IntegerValue )
					{
						processId = (int)((IntegerValue)item).longValue();
					}
					else
					{
						processId = Utils.parseInt( item.getStringValue() );
					}

					Document doc = XMLUtils.newDocument();

					Element data = XMLUtils.newElement( doc, "data" );

					// process
					XMLDatabaseElementSerializer.addItemsFromRS( con, data,
																 Tables.TABLE_PROCESS.trim(), "id",
																 String.valueOf( processId ),
																 null, false, null, null, "" );
					// параметры
					addParams( data, "process", processId );

					// линки
					Element linksEl = XMLUtils.newElement( data, "links" );
					XMLDatabaseElementSerializer.addItemsFromRS( con, linksEl,
																 Tables.TABLE_PROCESS_LINK.trim(), "process_id",
																 String.valueOf( processId ),
																 null, false, null, null, "" );

					// история статусов
					Element statusEL = XMLUtils.newElement( data, "status" );
					XMLDatabaseElementSerializer.addItemsFromRS( con, statusEL,
																 Tables.TABLE_PROCESS_STATUS.trim(), "process_id",
																 String.valueOf( processId ),
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
