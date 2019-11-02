package ru.bgcrm.plugin.document.docgen;

import java.sql.Connection;
import java.util.Map;
import java.util.SortedMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

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
import net.sf.saxon.value.StringValue;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.AddressDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

public class ParameterExtensionFunction
	extends CommonExtensionFunction
{

	public ParameterExtensionFunction( Connection con )
	{
		super( con, "param" );
	}

	@Override
	public SequenceType[] getArgumentTypes()
	{
		return new SequenceType[] { SequenceType.SINGLE_INTEGER, SequenceType.SINGLE_ITEM };
	}

	@Override
	public SequenceType getResultType( SequenceType[] suppliedArgumentTypes )
	{
		return SequenceType.SINGLE_NODE;
	}

	@SuppressWarnings("rawtypes")
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
					int id = parseInt( arguments[0].next() );
					int paramId = parseInt( arguments[1].next() );

					Document doc = XMLUtils.newDocument();
					Element data = XMLUtils.newElement( doc, "data" );

					ParamValueDAO paramValueDAO = new ParamValueDAO( con );
					Parameter param = ParameterCache.getParameter( paramId );

					AddressDAO addressDAO = new AddressDAO( con );

					if( Parameter.TYPE_ADDRESS.equals( param.getType() ) )
					{
						JAXBContext paramAddressValueContext = JAXBContext.newInstance( ParameterAddressValue.class );
						Marshaller paramAddressValueMarshaller = paramAddressValueContext.createMarshaller();
						JAXBContext addressHouseContext = JAXBContext.newInstance( AddressHouse.class );
						Marshaller addressHouseMarshaller = addressHouseContext.createMarshaller();

						SortedMap<Integer, ParameterAddressValue> val = paramValueDAO.getParamAddress( id, paramId );
						for( Map.Entry<Integer, ParameterAddressValue> entry : val.entrySet() )
						{
							JAXBElement<ParameterAddressValue> valueElement = new JAXBElement<ParameterAddressValue>( new QName( "param" ), ParameterAddressValue.class, entry.getValue() );
							paramAddressValueMarshaller.marshal( valueElement, data );

							AddressHouse addressHouse = addressDAO.getAddressHouse( entry.getValue().getHouseId(), true, true, true );
							JAXBElement<AddressHouse> addressElement = new JAXBElement<AddressHouse>( new QName( "address" ), AddressHouse.class, addressHouse );
							addressHouseMarshaller.marshal( addressElement, data );
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
