package ru.bgcrm.util.soap;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

public class HeaderHandler
    implements SOAPHandler<SOAPMessageContext>
{
	private String username;
	private String password;
	
	public HeaderHandler(String username, String password)
	{
		this.username = username;
		this.password = password;
	}

	public boolean handleMessage( SOAPMessageContext smc )
	{
		Boolean outboundProperty = (Boolean)smc.get( MessageContext.MESSAGE_OUTBOUND_PROPERTY );

		if( outboundProperty.booleanValue() )
		{
			SOAPMessage message = smc.getMessage();

			try
			{
				SOAPEnvelope envelope = smc.getMessage().getSOAPPart().getEnvelope();
				if (envelope.getHeader() != null) 
				{
					envelope.getHeader().detachNode();
				}
				
				SOAPHeader header = envelope.addHeader();

				SOAPFactory soapFactory = SOAPFactory.newInstance();
				SOAPElement auth = soapFactory.createElement( "auth", "", "http://" + "ws" + ".base" + ".kernel.bgbilling.bitel.ru/" );
				auth.setAttribute( "user", username );
				auth.setAttribute( "pswd", password );
				
				header.addChildElement( auth );
				
				message.writeTo( System.out );
				System.out.println( "" );

			}
			catch( Exception e )
			{
				e.printStackTrace();
			}

		}
		else
		{
			try
			{
				SOAPMessage message = smc.getMessage();
				message.writeTo( System.out );
				System.out.println( "" );

			}
			catch( Exception ex )
			{
				ex.printStackTrace();
			}
		}

		return outboundProperty;
	}

	public Set<QName> getHeaders()
	{
		return null;
	}

	public boolean handleFault( SOAPMessageContext context )
	{
		return true;
	}

	public void close( MessageContext context )
	{
	}
}

