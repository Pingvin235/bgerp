package ru.bgcrm.plugin.document.docgen;

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
import org.w3c.dom.Node;
import padeg.lib.FIO;
import padeg.lib.Padeg;
import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.user.Tables;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLDatabaseElementSerializer;
import ru.bgcrm.util.XMLUtils;

import java.sql.Connection;

public class UserExtensionFunction
extends CommonExtensionFunction
{

	protected UserExtensionFunction( Connection con )
	{
		super( con, "user" );
	}

	@Override
	public SequenceType[] getArgumentTypes()
	{
		return new SequenceType[] { SequenceType.SINGLE_ITEM };
	}

	@Override
	public SequenceType getResultType( SequenceType[] suppliedArgumentTypes )
	{
		return SequenceType.SINGLE_NODE;
	}

	private void process( Document doc, Element data, String title )
	{
		Node node = XMLUtils.newElement( data, "fio" );

		FIO fio = new FIO();
		Padeg.getFioParts( title, fio );
		boolean sex = Utils.parseBoolean( String.valueOf( Padeg.getSex( fio.middleName ) ) );

		((Element)node).setAttribute( "sex", String.valueOf( String.valueOf( Padeg.getSex( fio.middleName ) ) ) );

		((Element)node).setAttribute( "im_pad", Padeg.getFIOPadegFSAS( title, 1 ) );
		((Element)node).setAttribute( "rod_pad", Padeg.getFIOPadegFSAS( title, 2 ) );
		((Element)node).setAttribute( "dat_pad", Padeg.getFIOPadegFSAS( title, 3 ) );
		((Element)node).setAttribute( "vin_pad", Padeg.getFIOPadegFSAS( title, 4 ) );
		((Element)node).setAttribute( "tvor_pad", Padeg.getFIOPadegFSAS( title, 5 ) );
		((Element)node).setAttribute( "predl_pad", Padeg.getFIOPadegFSAS( title, 6 ) );

		Node nodeCutFio = XMLUtils.newElement( data, "cut_fio" );

		((Element)nodeCutFio).setAttribute( "im_pad", Padeg.getCutFIOPadegFS( title, sex, 1 ) );
		((Element)nodeCutFio).setAttribute( "rod_pad", Padeg.getCutFIOPadegFS( title, sex, 2 ) );
		((Element)nodeCutFio).setAttribute( "dat_pad", Padeg.getCutFIOPadegFS( title, sex, 3 ) );
		((Element)nodeCutFio).setAttribute( "vin_pad", Padeg.getCutFIOPadegFS( title, sex, 4 ) );
		((Element)nodeCutFio).setAttribute( "tvor_pad", Padeg.getCutFIOPadegFS( title, sex, 5 ) );
		((Element)nodeCutFio).setAttribute( "predl_pad", Padeg.getCutFIOPadegFS( title, sex, 6 ) );

		data.appendChild( nodeCutFio );
		data.appendChild( node );
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
					Item arg = arguments[0].next();
					int userId = 0;

					if( arg instanceof IntegerValue )
					{
						userId = (int)((IntegerValue)arguments[0].next()).longValue();
					}
					else
					{
						userId = Utils.parseInt( arg.getStringValue() );
					}

					Document doc = XMLUtils.newDocument();
					Element data = XMLUtils.newElement( doc, "data" );

					// user
					XMLDatabaseElementSerializer.addItemsFromRS( con, data,
																 Tables.TABLE_USER.trim(), "id",
																 String.valueOf( userId ),
																 null, false, null, null, "" );
					// параметры
					addParams( data, User.OBJECT_TYPE, userId );
					process( doc, data, UserCache.getUser( userId )
												 .getTitle() );

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
