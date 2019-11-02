package ru.bgcrm.plugin.bgbilling.proto.model.bill;

public class Invoice
	extends Document
{
	private boolean showOnWeb;

	public boolean isShowOnWeb()
	{
		return showOnWeb;
	}

	public void setShowOnWeb( boolean showOnWeb )
	{
		this.showOnWeb = showOnWeb;
	}
}
