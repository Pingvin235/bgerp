package ru.bgcrm.plugin.document.model;

import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

public class Pattern
	extends IdTitle
{
	private static final Logger log = Logger.getLogger( Pattern.class );
	
	public static final int TYPE_PDF_FORM = 1;
	public static final int TYPE_DOCX_FORM = 2;
	public static final int TYPE_ODT_FORM = 3;
	public static final int TYPE_XSLT_HTML = 4;
	
	/**
	 * Задумывалось для FOP генерации, но неизвестно, будет ли реализовано.
	 */
	@Deprecated
	public static final int TYPE_XSLT_PDF = 5;
	public static final int TYPE_XSLX = 6;
	public static final int TYPE_JSP_HTML = 7;

	private final ParameterMap params;

	private final String file;
	private String documentTitle;
	private final String script;
	private final String scope;
	private final String scopeType;
	private final int type;
	private final boolean resultSave;
	private final boolean resultStream;
	private java.util.regex.Pattern titlePattern;
	
	// XSLT шаблон, изымающий и подготавливающий значения под позиции, 
	// либо FO/HTML документ в перспективе 
	private final String xslt;
	
	// JSP шаблон, генерирует HTML вывод, либо подготавливает данные
	private final String jsp;
	
	// макросы позиций, устаревший метод
	@Deprecated
	private ParameterMap positionMap;

	public Pattern( int id, ParameterMap params )
		throws BGException
	{
		super( id, params.get( "title" ) );

		this.params = params;
		this.script = params.get( "script" );
		this.scope = params.get( "scope" );
		this.scopeType = params.get( "scopeType" );
		this.file = params.get( "file" );
		this.documentTitle = params.get( "documentTitle" );
		this.xslt = params.get( "xslt" );
		this.jsp = params.get( "jsp" );
		
		String type = params.get( "type" );

		if( id <= 0 ||
			Utils.isBlankString( scope ) ||
			Utils.isBlankString( script ) ||
			Utils.isBlankString( type ) )
		{
			throw new BGException( "Error pattern load: " + id );
		}

		String titlePattern = params.get( "titleRegexp" );
		if( Utils.notBlankString( titlePattern ) )
		{
			try
			{
				this.titlePattern = java.util.regex.Pattern.compile( titlePattern );
			}
			catch( Exception e )
			{
				throw new BGException( "Error on regexp parse: " + titlePattern );
			}
		}

		Set<String> resultSet = Utils.toSet( params.get( "result", "save" ) );
		resultSave = resultSet.contains( "save" );
		resultStream = resultSet.contains( "stream" );
		
		if( "pdfForm".equals( type ) )
		{
			this.type = TYPE_PDF_FORM;
		}
		else if( "docxForm".equals( type ) )
		{
			this.type = TYPE_DOCX_FORM;
		}
		else if( "odtForm".equals( type ) )
		{
			this.type = TYPE_ODT_FORM;
		}
		else if( "xsltHtml".equals( type ) )
		{
			this.type = TYPE_XSLT_HTML;
		}
		else if( "xsltPdf".equals( type ) )
		{
			this.type = TYPE_XSLT_PDF;
		}
		else if( "xlsxForm".equals( type ) )
		{
			this.type = TYPE_XSLX;
		}
		else if ("jspHtml".equals( type ))
		{
			this.type = TYPE_JSP_HTML;
		}
		else
		{
			throw new BGException( "Unsupported document pattern type: " + type + "; pattern: " + id );
		}
		
		positionMap = params.sub( "position." );
		
		if (log.isDebugEnabled()) 
		{
			log.debug( "Pattern loaded: " + ToStringBuilder.reflectionToString( this ) );
		}
	}

	public ParameterMap getParams()
	{
		return params;
	}

	public String getScript()
	{
		return script;
	}

	public String getScope()
	{
		return scope;
	}

	public int getType()
	{
		return type;
	}

	public String getFile()
	{
		return file;
	}

	public void setDocumentTitle( String documentTitle )
	{
		this.documentTitle = documentTitle;
	}

	public String getDocumentTitle()
	{
		return documentTitle;
	}

	public String getXslt()
	{
		return xslt;
	}
	
	public String getJsp()
	{
		return jsp;
	}

	public boolean isResultSave()
	{
		return resultSave;
	}

	public boolean isResultStream()
	{
		return resultStream;
	}
	
	/**
	 * Запрос макроса позиции - устаревший метод.
	 */
	@Deprecated
	public String getPositionMacros( String positionName )
	{
		return positionMap.get( positionName );
	}

	public boolean checkTitle( String objectTitle )
	{
		if( objectTitle != null && titlePattern != null )
		{
			return titlePattern.matcher( objectTitle ).matches();
		}
		return true;
	}

	public boolean checkType( String objectType )
	{
		if( objectType != null && scopeType != null )
		{
			return scopeType.equals( objectType );
		}
		return true;
	}
}