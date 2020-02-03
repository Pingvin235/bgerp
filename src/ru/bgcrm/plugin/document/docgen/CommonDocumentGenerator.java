package ru.bgcrm.plugin.document.docgen;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.util.StreamUtils;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import net.sf.saxon.s9api.DOMDestination;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.event.Event;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.listener.DynamicEventListener;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterValuePair;
import ru.bgcrm.plugin.bgbilling.event.RegisterExtensionFunctionsEvent;
import ru.bgcrm.plugin.document.PadegExtensionFunction;
import ru.bgcrm.plugin.document.dao.DocumentDAO;
import ru.bgcrm.plugin.document.event.DocumentGenerateEvent;
import ru.bgcrm.plugin.document.model.Pattern;
import ru.bgcrm.servlet.CustomHttpServletResponse;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.RegexpStringUtils;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;
import ru.bgcrm.util.sql.ConnectionSet;

public class CommonDocumentGenerator
	extends DynamicEventListener
{
	private static final Logger log = Logger.getLogger( CommonDocumentGenerator.class );

	protected ConnectionSet conSet;
	protected DocumentGenerateEvent event;

	public CommonDocumentGenerator()
	{}

	public CommonDocumentGenerator( Event e, ConnectionSet conSet )
	{
		this.event = (DocumentGenerateEvent)e;
		this.conSet = conSet;
	}

	private static final void addParamElement( Element requestEl, String name, String val )
	{
		Element paramEl = XMLUtils.newElement( requestEl, "param" );
		paramEl.setAttribute( "name", name );
		paramEl.setAttribute( "value", val );
	}

	@Override
	public void notify( Event e, ConnectionSet conSet )
		throws BGException
	{
		this.event = (DocumentGenerateEvent)e;
		this.conSet = conSet;

		try
		{
			Pattern pattern = event.getPattern();

			ByteArrayOutputStream result = new ByteArrayOutputStream( 1000000 );

			// документ содержит результат XSLT преобразования, если шаблон есть
			final int type = pattern.getType();
			if( event.isDebug() ) 
			{
				OutputStreamWriter writer = new OutputStreamWriter( result, Utils.UTF8 );
				writer.write( "Генерация отчёта в режиме отладки.\n\n" );
				
				writer.write( "Событие, переданное в скрипт:\n" );
				writer.write( event.toString() );
				writer.write( "\n\n" );
				
				writer.write( "Шаблон: " );
				writer.write( pattern.toString() );
								
				if (type == Pattern.TYPE_JSP_HTML || type == Pattern.TYPE_XSLT_HTML) 
				{
					writer.write( "\n\nДля данных типов отладка может производится в результат." );
					writer.flush();				
				}
				else  
				{
					int objectId = event.getObjectId();
					
					writer.write( "\n\n" );
					writer.write( "Код объекта: " );
					writer.write( String.valueOf( objectId ) );
					writer.write( "\n\n" );
					
    				try
    				{
    					Pair<Document, String> processResult = processToDocument(objectId);
    					
    					writer.write( "Подготовленные данные:\n" );
    					
    					writer.flush();
    					XMLUtils.serialize( processResult.getFirst(), result, null, true );
    					
    					if (processResult.getSecond() != null) 
    					{
        					writer.write( "\n\n" );    
        					writer.write( "Отладка:\n" );
        					
        					writer.flush();
        					writer.write( processResult.getSecond() );
    					}
    				}
    				catch( Exception ex )
    				{
    					writer.write( "Ошибка выполнения:\n" );
    					writer.flush();    
    					ex.printStackTrace( new PrintStream( result, true, Utils.UTF8.name() ) );
    
    					log.error( ex.getMessage(), ex );
    				}
				}
				
				writer.flush();
				
				/*// для HTML результата возможен вывод конечного результата
				if (type == Pattern.TYPE_XSLT_HTML)
				{
					if (Utils.notBlankString( pattern.getExpression())) 
					{
						writer.write( "\n\n" );
						writer.write( "Полный XML документ:\n" );
						
						writer.flush();
    					XMLUtils.serialize( getFullDocument( results ), new StreamResult(result), null, true );
					}
					
					writer.write( "\n\n" );
					writer.write( "Результат HTML:\n" );
					writer.flush();
					
					xmlToHtml( results, pattern, result );
				}*/
				
			}
			else if( type == Pattern.TYPE_PDF_FORM )
			{
				Document docData = processToDocument(event.getObjectId()).getFirst();

				final BaseFont bf = BaseFont.createFont( "jar:" + this.getClass()
																	  .getResource( "/ru/bitel/fonts/arial.ttf" )
																	  .getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED );

				PdfReader reader = new PdfReader( pattern.getFile() );
				PdfStamper stamper = new PdfStamper( reader, result );

				AcroFields fields = stamper.getAcroFields();
				fields.addSubstitutionFont( bf );

				for( String key : fields.getFields().keySet() )
				{
					if( docData != null && docData.getDocumentElement() != null )
					{
						String value = XMLUtils.selectText( docData.getDocumentElement(), "/data/field[@name='" + key + "']/text()" );
						if( Utils.notBlankString( value ) )
						{
							fields.setField( key, value );
						}
					}
				}

				if( pattern.getParams().getBoolean( "flattening", false ) )
				{
					stamper.setFormFlattening( true );
				}

				stamper.close();
			}
			else if( type == Pattern.TYPE_DOCX_FORM || type == Pattern.TYPE_ODT_FORM )
			{
				Document docData = processToDocument(event.getObjectId()).getFirst();

				ZipInputStream zis = new ZipInputStream( new FileInputStream( pattern.getFile() ) );
				ZipOutputStream zos = new ZipOutputStream( result );

				Set<String> replacedFileSet = new HashSet<>();
				if( type == Pattern.TYPE_DOCX_FORM )
				{
					replacedFileSet.add( "word/document.xml" );
				}
				else
				// TYPE_ODT_FORM
				{
					replacedFileSet.add( "content.xml" );
					replacedFileSet.add( "styles.xml" );
				}

				ZipEntry ze = null;
				while( (ze = zis.getNextEntry()) != null )
				{
					if( !replacedFileSet.contains( ze.getName() ) )
					{
						zos.putNextEntry( new ZipEntry( ze.getName() ) );
						StreamUtils.copy( zis, zos, 1024 );
					}
					else
					{
						String replaced = new String( StreamUtils.getBytes( zis ), Utils.UTF8 );

						//поиск шаблонов вида ${macros}
						List<String> variables = RegexpStringUtils.findMatchesByTemplate( replaced, "\\$\\{[a-zA-Zа-яА-Я0-9_.]+\\}" );
						for( String var : variables )
						{
							var = var.substring( 2, var.length() - 1 );
							String value = "";
							value = XMLUtils.selectText( docData.getDocumentElement(), "/data/field[@name='" + var + "']/text()", "" );
							replaced = replaced.replaceAll( "\\$\\{" + var + "\\}", value );
						}

						zos.putNextEntry( new ZipEntry( ze.getName() ) );
						zos.write( replaced.getBytes( Utils.UTF8 ) );
					}
				}

				zos.close();
				zis.close();
			}
			else if( type == Pattern.TYPE_XSLT_HTML )
			{
				// иначе - необходимо склеить полученные HTML документы
				// не совсем корректный, но обратно совместимый путь
				for (int objectId : event.getObjectIds())
				{
					XMLUtils.serialize( processToDocument( objectId ).getFirst(), 
					                    result, null, true );
				}
			}
			else if (type == Pattern.TYPE_JSP_HTML)
			{
				// шаблон сам должен разобраться с objectIds
				CustomHttpServletResponse resp = new CustomHttpServletResponse( event.getForm().getHttpResponse(), result );
				
				HttpServletRequest req = event.getForm().getHttpRequest();
				
				req.setAttribute( "event", event );
				req.setAttribute( "conSlave", conSet.getSlaveConnection() );
				
				req.getRequestDispatcher( pattern.getJsp() ).include( req, resp );
				resp.flush();
			}
			
			// создание документа, если нужно
			if( !DynActionForm.RESPONSE_TYPE_STREAM.equals( event.getForm().getResponseType() ) )
			{
				DocumentDAO docDao = new DocumentDAO( conSet.getConnection() );
				OutputStream out = docDao.createDocumentFile( event.setResultDocument(), pattern.getDocumentTitle() );

				out.write( result.toByteArray() );
				out.flush();
				out.close();
			}
			else
			{
				event.setResultBytes( result.toByteArray() );
			}
		}
		catch( Exception ex )
		{
			throw new BGException( ex );
		}
	}

	/*private void xmlToHtml( List<Pair<Integer, Document>> results, Pattern pattern, ByteArrayOutputStream result )
		throws TransformerException
	{
		// если было JEXL выражение - то документы склеить и на прогнать через XSLT оформления
		if (Utils.notBlankString( pattern.getExpression())) {
			
			Document fullDoc = getFullDocument( results );
			
			XMLUtils.transform( new DOMSource( fullDoc ), 
			                    new StreamSource(pattern.getXslt()), 
			                    new StreamResult( result ), Utils.UTF8.name() );
			
		} 
		// иначе - необходимо склеить полученные HTML документы
		// не совсем корректный, но обратно совместимый путь
		else 
		{
			for (Pair<Integer, Document> pair : results)
			{
				XMLUtils.serialize( pair.getSecond(), new StreamResult(result), null, true );
			}
		}
	}

	private Document getFullDocument( List<Pair<Integer, Document>> results )
	{
		Document fullDoc = XMLUtils.newDocument();
		Element dataNode = XMLUtils.newElement(fullDoc, "data");
		
		for (Pair<Integer, Document> pair : results) 
		{
			final Element docEl = pair.getSecond().getDocumentElement();
			docEl.setAttribute( "objectId", String.valueOf( pair.getFirst() ) );
			dataNode.appendChild( fullDoc.importNode( docEl, true) );						
		}
		return fullDoc;
	}*/

	private Pair<Document, String> processToDocument(int objectId)
		throws Exception
	{
		Pair<Document, String> result = new Pair<>();
		
		result.setFirst( XMLUtils.newDocument());

		Pattern pattern = event.getPattern();
			
		// JSP шаблон используется для подготовки данных, данные формируются объектом field
		// а вывод шаблона используется для отладки
		if (Utils.notBlankString( pattern.getJsp()))
		{
			Element dataElement = XMLUtils.newElement( result.getFirst(), "data" );
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream( 200 );
			
			CustomHttpServletResponse resp = new CustomHttpServletResponse( event.getForm().getHttpResponse(), bos );
			
			HttpServletRequest req = event.getForm().getHttpRequest();
			
			req.setAttribute( "event", event );
			req.setAttribute( "objectId", objectId );
			req.setAttribute( "conSlave", conSet.getSlaveConnection() );
			req.setAttribute( "field", new FieldSetter( dataElement ) );
			
			req.getRequestDispatcher( pattern.getJsp() ).include( req, resp );
			resp.flush();
			
			result.setSecond(new String(bos.toByteArray(), Utils.UTF8));
		}
		else
		{
			processXslt( objectId, new DOMDestination( result.getFirst() ) );
		}

		return result;
	}
	
	public static final class FieldSetter 
	{
		private final Element dataNode;
		
		private FieldSetter(Element rootNode)
		{
			this.dataNode = rootNode;
		}
		
		public void set(String field, String value)
		{
			Element fieldEl = XMLUtils.newElement( dataNode, "field" );
			fieldEl.setAttribute( "name", field );
			XMLUtils.createTextNode( fieldEl, value );
		}
	}

	private void processXslt( int objectId, Destination result )
		throws Exception
	{
		Processor proc = new Processor( false );
		proc.registerExtensionFunction( new UserExtensionFunction( conSet.getConnection() ) );
		proc.registerExtensionFunction( new CustomerExtensionFunction( conSet.getConnection() ) );
		proc.registerExtensionFunction( new ProcessExtensionFunction( conSet.getConnection() ) );
		proc.registerExtensionFunction( new ParameterExtensionFunction( conSet.getConnection() ) );
		proc.registerExtensionFunction( new NumberToTextExstensionFunction() );
		proc.registerExtensionFunction( new PadegExtensionFunction() );
		proc.registerExtensionFunction( new MathBitSetExtensionFunction() );

		registerExtensionFunctions( conSet, proc );

		// событие в плагины для регистрации своих функций
		EventProcessor.processEvent( new RegisterExtensionFunctionsEvent( proc, event.getForm() ), conSet );

		XsltCompiler comp = proc.newXsltCompiler();
		XsltExecutable templates = comp.compile( new StreamSource( new File( event.getPattern()
																				  .getXslt() ) ) );
		XsltTransformer transformer = templates.load();

		Document data = XMLUtils.newDocument();

		Element eventEl = XMLUtils.newElement( data, "event" );
		eventEl.setAttribute( "objectType", event.getObjectType() );
		eventEl.setAttribute( "objectId", String.valueOf( objectId ) );
		eventEl.setAttribute( "userId", String.valueOf( event.getUser().getId() ) );

		// передача в XSLT шаблон параметров HTTP запроса
		Element requestEl = XMLUtils.newElement( eventEl, "request" );
		for( Map.Entry<String, Object> me : event.getForm().getParam().entrySet() )
		{
			String name = me.getKey();
			Object value = me.getValue();

			if( value instanceof String[] )
			{
				for( String val : (String[])value )
				{
					addParamElement( requestEl, name, val );
				}
			}
			else if( value instanceof String )
			{
				addParamElement( requestEl, name, (String)value );
			}
		}

		prepareData( conSet, data );

		transformer.setSource( new DOMSource( data ) );
		transformer.setDestination( result );

		transformer.transform();
	}

	// функцию можно переопределить для регистрации произвольных функций XSLT
	protected void registerExtensionFunctions( ConnectionSet conSet, Processor proc )
		throws BGException
	{}

	// функцию можно переопределить для передачи дополнительных данных в XML документ
	protected void prepareData( ConnectionSet conSet, Document data )
		throws BGException
	{}

	//TODO: Весь этот "язык" ниже удалить после перехода на XSLT обработчик.
	private Map<Integer, String> paramValueCache = new HashMap<Integer, String>();

	@Deprecated
	private String processMacros( String macros )
		throws BGException
	{
		String currentValue = "";

		if( log.isDebugEnabled() )
		{
			log.debug( "Macros: " + macros );
		}

		macros = macros.replaceAll( "\\|\\|", "\u0000" );

		String[] tokens = macros.split( "\\s*\\|\\s*" );
		for( String token : tokens )
		{
			token = token.replace( "\u0000", "|" );

			currentValue = processMacros( currentValue, token );

			if( log.isDebugEnabled() )
			{
				log.debug( "Token: " + token + " => " + currentValue );
			}

			// если любая из функций возвращает пустую строку - прерываем цепочку
			if( Utils.isBlankString( currentValue ) )
			{
				break;
			}
		}

		if( log.isDebugEnabled() )
		{
			log.debug( " => " + currentValue );
		}

		return currentValue;
	}

	@Deprecated
	private String getParamStringValue( int paramId )
		throws BGException
	{
		Parameter param = ParameterCache.getParameter( paramId );
		if( param == null )
		{
			return "";
		}

		String value = paramValueCache.get( paramId );
		if( value == null )
		{
			ParameterValuePair paramValue = new ParameterValuePair( param );
			new ParamValueDAO( conSet.getConnection() ).loadParameterValue( paramValue, event.getObjectId(), true );

			value = paramValue.getValueTitle();
			paramValueCache.put( paramId, value );
		}

		return value;
	}

	/**
	 * Старая технология работы с макросами.
	 */
	@Deprecated
	public String processMacros( String incomingValue, String macros )
		throws BGException
	{
		if( macros.startsWith( "param:" ) )
		{
			int paramId = Utils.parseInt( StringUtils.substringAfter( macros, ":" ) );
			if( paramId <= 0 )
			{
				return incomingValue;
			}

			return getParamStringValue( paramId );
		}
		else if( macros.startsWith( "params:" ) )
		{
			StringBuilder result = new StringBuilder();

			List<Integer> paramIds = Utils.toIntegerList( StringUtils.substringAfter( macros, ":" ) );
			for( int paramId : paramIds )
			{
				String value = getParamStringValue( paramId );
				if( Utils.notBlankString( value ) )
				{
					Utils.addCommaSeparated( result, value );
				}
			}

			return result.toString();
		}
		else if( macros.startsWith( "token" ) )
		{
			String[] vars = macros.split( ":" );
			if( vars.length != 3 )
			{
				throw new BGException( "Incorrect token macros: " + macros );
			}

			int pos = Utils.parseInt( vars[2] );

			String[] tokens = incomingValue.split( vars[1] );
			if( pos >= 0 && pos < tokens.length )
			{
				return tokens[pos];
			}
		}
		else if( macros.startsWith( "replace" ) )
		{
			String[] vars = macros.split( ":" );
			if( vars.length != 2 && vars.length != 3 )
			{
				throw new BGException( "Incorrect token macros: " + macros );
			}

			String replaceTo = "";
			if( vars.length == 3 )
			{
				replaceTo = vars[2];
			}

			return incomingValue.replaceAll( vars[1], replaceTo );
		}
		else if( macros.startsWith( "substr" ) )
		{
			String[] vars = macros.split( ":" );
			if( vars.length < 2 )
			{
				throw new BGException( "Incorrect token macros: " + macros );
			}

			int posFrom = Utils.parseInt( vars[1] );
			int posTo = incomingValue.length();
			if( vars.length > 2 )
			{
				posTo = Utils.parseInt( vars[2] );
			}

			if( 0 <= posFrom && posFrom < posTo && posTo <= incomingValue.length() )
			{
				return incomingValue.substring( posFrom, posTo );
			}
			else
			{
				throw new BGException( "Incorrect substr positions: " + posFrom + ", " + posTo );
			}
		}
		else if( macros.equals( "trim" ) )
		{
			return incomingValue.trim();
		}
		else if( macros.startsWith( "dateParse" ) )
		{
			String format = StringUtils.substringAfter( macros, ":" );
			if( Utils.isBlankString( format ) )
			{
				throw new BGException( "Can't find date format in: " + macros );
			}

			Date date = TimeUtils.parse( incomingValue, format );
			if( date != null )
			{
				return String.valueOf( date.getTime() );
			}
			return "";
		}
		else if( macros.startsWith( "dateFormat" ) )
		{
			String format = StringUtils.substringAfter( macros, ":" );
			if( Utils.isBlankString( format ) )
			{
				throw new BGException( "Can't find date format in: " + macros );
			}

			return new SimpleDateFormat( format ).format( new Date( Utils.parseLong( incomingValue ) ) );
		}
		else if( macros.startsWith( "ifEq" ) )
		{
			String[] vars = macros.split( ":" );
			if( vars.length != 3 )
			{
				throw new BGException( "Incorrect token macros: " + macros );
			}

			if( incomingValue.equals( vars[1] ) )
			{
				return vars[2];
			}
			return "";
		}
		else if( macros.startsWith( "ifNotEq" ) )
		{
			String[] vars = macros.split( ":" );
			if( vars.length != 3 )
			{
				throw new BGException( "Incorrect token macros: " + macros );
			}

			if( !incomingValue.equals( vars[1] ) )
			{
				return vars[2];
			}
			return "";
		}
		else if( macros.startsWith( "indexOf" ) )
		{
			String[] vars = macros.split( ":" );
			if( vars.length != 2 )
			{
				throw new BGException( "Incorrect token macros: " + macros );
			}

			return String.valueOf( incomingValue.indexOf( vars[1] ) );
		}
		else if( macros.startsWith( "ifInSet" ) )
		{
			String[] vars = macros.split( ":" );
			if( vars.length != 3 )
			{
				throw new BGException( "Incorrect token macros: " + macros );
			}

			if( Utils.toSet( incomingValue )
					 .contains( vars[1] ) )
			{
				return vars[2];
			}
			return "";
		}
		else if( macros.equals( "switch" ) )
		{
			String[] vars = macros.split( ":" );
			if( vars.length != 3 )
			{
				throw new BGException( "Incorrect token macros: " + macros );
			}

			List<String> vars1 = Utils.toList( vars[1] );
			List<String> vars2 = Utils.toList( vars[2] );

			int length = Math.min( vars1.size(), vars2.size() );

			for( int i = 0; i < length; i++ )
			{
				if( incomingValue.equals( vars1.get( i ) ) )
				{
					return vars2.get( i );
				}
			}
			return "";
		}
		else if( macros.startsWith( "xpathSelect" ) )
		{
			String expression = StringUtils.substringAfter( macros, ":" );
			if( Utils.isBlankString( expression ) )
			{
				throw new BGException( "Empty expression for macros: " + macros );
			}

			StringBuilder result = new StringBuilder();
			try
			{
				Document doc = XMLUtils.parseDocument( new InputSource( new StringReader( incomingValue ) ) );
				NodeList nodeList = XPathAPI.selectNodeList( doc, expression );

				final int size = nodeList.getLength();
				for( int i = 0; i < size; i++ )
				{
					if( result.length() != 0 )
					{
						result.append( "," );
					}
					result.append( nodeList.item( i )
										   .getNodeValue() );
				}
			}
			catch( Exception e )
			{
				throw new BGException( e );
			}

			return result.toString();
		}

		return incomingValue;
	}
}