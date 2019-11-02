package ru.bgcrm.plugin.document;

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
import net.sf.saxon.value.StringValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import padeg.lib.FIO;
import padeg.lib.Padeg;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

public class PadegExtensionFunction
	extends ExtensionFunctionDefinition
{
	private static final int FIO_FLAG = 1;
	private static final int APPOINTMENT_FLAG = 2;
	private static final int OFFICE_FLAG = 3;

	@Override
	public StructuredQName getFunctionQName()
	{
		return new StructuredQName( "bgcrm", "http://bgcrm.ru/saxon-extension", "padeg" );
	}

	@Override
	public SequenceType[] getArgumentTypes()
	{
		return new SequenceType[] { SequenceType.SINGLE_ITEM, SequenceType.SINGLE_ITEM };
	}

	@Override
	public SequenceType getResultType( SequenceType[] sequenceTypes )
	{
		return SequenceType.SINGLE_NODE;
	}

	private int parseInt( Item item )
		throws XPathException
	{
		int value = 0;
		if( item instanceof StringValue )
		{
			value = Utils.parseInt( item.getStringValue() );
		}
		else if( item instanceof IntegerValue )
		{
			value = (int)((IntegerValue)item).longValue();
		}
		else
		{
			value = (int)((IntegerValue)item).longValue();
		}
		return value;
	}

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
					String text = arguments[0].next().getStringValue();
					int flag = parseInt( arguments[1].next() );

					Document doc = XMLUtils.newDocument();
					Element data = XMLUtils.newElement( doc, "data" );

					process( data, text, flag );

					return SingletonIterator.makeIterator( new DocumentWrapper( doc, "", new Configuration() ) );
				}
				catch( Exception e )
				{
					throw new XPathException( e );
				}
			}
		};
	}

	private void process( Element data, String text, int flag )
	{
		Node node = XMLUtils.newElement( data, "text" );

		switch( flag )
		{
			case FIO_FLAG:

				FIO fio = new FIO();
				Padeg.getFioParts( text, fio );

				text = text.replaceAll( "\"", "" );

				((Element)node).setAttribute( "sex", String.valueOf( Padeg.getSex( fio.middleName ) ) );

				((Element)node).setAttribute( "im_pad", Padeg.getFIOPadegFSAS( text, 1 ) );
				((Element)node).setAttribute( "rod_pad", Padeg.getFIOPadegFSAS( text, 2 ) );
				((Element)node).setAttribute( "dat_pad", Padeg.getFIOPadegFSAS( text, 3 ) );
				((Element)node).setAttribute( "vin_pad", Padeg.getFIOPadegFSAS( text, 4 ) );
				((Element)node).setAttribute( "tvor_pad", Padeg.getFIOPadegFSAS( text, 5 ) );
				((Element)node).setAttribute( "predl_pad", Padeg.getFIOPadegFSAS( text, 6 ) );

				Node nodeCutFio = XMLUtils.newElement( (Element)node, "cut_fio" );
				boolean sex = Utils.parseBoolean( String.valueOf( Padeg.getSex( fio.middleName ) ) );

				((Element)nodeCutFio).setAttribute( "im_pad", Padeg.getCutFIOPadegFS( text, sex, 1 ) );
				((Element)nodeCutFio).setAttribute( "rod_pad", Padeg.getCutFIOPadegFS( text, sex, 2 ) );
				((Element)nodeCutFio).setAttribute( "dat_pad", Padeg.getCutFIOPadegFS( text, sex, 3 ) );
				((Element)nodeCutFio).setAttribute( "vin_pad", Padeg.getCutFIOPadegFS( text, sex, 4 ) );
				((Element)nodeCutFio).setAttribute( "tvor_pad", Padeg.getCutFIOPadegFS( text, sex, 5 ) );
				((Element)nodeCutFio).setAttribute( "predl_pad", Padeg.getCutFIOPadegFS( text, sex, 6 ) );

				node.appendChild( nodeCutFio );

				break;

			case APPOINTMENT_FLAG:

				((Element)node).setAttribute( "im_pad", Padeg.getAppointmentPadeg( text, 1 ) );
				((Element)node).setAttribute( "rod_pad", Padeg.getAppointmentPadeg( text, 2 ) );
				((Element)node).setAttribute( "dat_pad", Padeg.getAppointmentPadeg( text, 3 ) );
				((Element)node).setAttribute( "vin_pad", Padeg.getAppointmentPadeg( text, 4 ) );
				((Element)node).setAttribute( "tvor_pad", Padeg.getAppointmentPadeg( text, 5 ) );
				((Element)node).setAttribute( "predl_pad", Padeg.getAppointmentPadeg( text, 6 ) );

				break;

			case OFFICE_FLAG:

				((Element)node).setAttribute( "im_pad", Padeg.getOfficePadeg( text, 1 ) );
				((Element)node).setAttribute( "rod_pad", Padeg.getOfficePadeg( text, 2 ) );
				((Element)node).setAttribute( "dat_pad", Padeg.getOfficePadeg( text, 3 ) );
				((Element)node).setAttribute( "vin_pad", Padeg.getOfficePadeg( text, 4 ) );
				((Element)node).setAttribute( "tvor_pad", Padeg.getOfficePadeg( text, 5 ) );
				((Element)node).setAttribute( "predl_pad", Padeg.getOfficePadeg( text, 6 ) );

				break;
		}
	}
}
