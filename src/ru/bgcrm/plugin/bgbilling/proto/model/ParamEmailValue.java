package ru.bgcrm.plugin.bgbilling.proto.model;

import java.util.List;

import ru.bgcrm.model.IdTitleTree;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.util.Utils;

/**
 * Постепенно заменить на {@link ParameterEmailValue}.
 */
@Deprecated
public class ParamEmailValue
{
	private List<String> emails; // Емейлы
	private int eid; //id списка рассылок о_О
	private List<String> subscrs; // Активированные подписки
	private List<IdTitleTree> subscrsTree; // Список существующих рассылок 

	public List<String> getEmails()
	{
		return emails;
	}

	public void setEmails( List<String> emails )
	{
		this.emails = emails;
	}

	public String getEmailsAsString()
	{
		return Utils.toText( emails, "\n" );
	}

	public int getEid()
	{
		return eid;
	}

	public void setEid( int eid )
	{
		this.eid = eid;
	}

	public List<String> getSubscrs()
	{
		return subscrs;
	}

	public void setSubscrs( List<String> subscrs )
	{
		this.subscrs = subscrs;
	}

	public List<IdTitleTree> getSubscrsTree()
	{
		return subscrsTree;
	}

	public void setSubscrsTree( List<IdTitleTree> subscrsTree )
	{
		this.subscrsTree = subscrsTree;
	}
}