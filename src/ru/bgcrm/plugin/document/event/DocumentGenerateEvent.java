package ru.bgcrm.plugin.document.event;

import java.util.List;

import org.bgerp.event.base.UserEvent;

import ru.bgcrm.plugin.document.model.Document;
import ru.bgcrm.plugin.document.model.Pattern;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

public class DocumentGenerateEvent
	extends UserEvent
{
	private final Pattern pattern;
	private final String objectType;
	private final List<Integer> objectIds;

	private Document resultDocument;
	private byte[] resultBytes;

	public DocumentGenerateEvent( DynActionForm form, Pattern pattern,
	                              String objectType, List<Integer> objectIds)
	{
		super( form );
		this.pattern = pattern;
		this.objectType = objectType;
		this.objectIds = objectIds;
	}

	public Document setResultDocument()
	{
		resultDocument = new Document();
		resultDocument.setObjectId( Utils.getFirst( objectIds ) );
		resultDocument.setObjectType( objectType );
		return resultDocument;
	}

	public Document getResultDocument()
	{
		return resultDocument;
	}

	public byte[] getResultBytes()
	{
		return resultBytes;
	}

	public void setResultBytes( byte[] resultStream )
	{
		this.resultBytes = resultStream;
	}

	public Pattern getPattern()
	{
		return pattern;
	}

	public String getObjectType()
	{
		return objectType;
	}

	public int getObjectId()
	{
		return Utils.getFirst( objectIds );
	}

	public List<Integer> getObjectIds()
	{
		return objectIds;
	}

	public boolean isDebug()
	{
		return isDebug(form);
	}

	public static boolean isDebug(DynActionForm form)
	{
		return form.getParamBoolean( "debug", false );
	}
}