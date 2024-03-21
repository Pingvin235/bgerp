package ru.bgcrm.plugin.bgbilling.proto.model.tariff;

import java.util.Date;

import org.bgerp.model.base.Id;
import ru.bgcrm.model.PeriodSet;

public class ContractPersonalTariff
	extends Id
	implements PeriodSet
{

	private int contractId;
	private int pos;
	private Date date1;
	private Date date2;
	private String title;
	private int treeId;

	private String titleWeb = null;

	private String config = "";

	private boolean useTitleInWeb;

	public String getTitleWeb() {
		return titleWeb;
	}

	public void setTitleWeb(String titleWeb) {
		this.titleWeb = titleWeb;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public boolean isUseTitleInWeb() {
		return useTitleInWeb;
	}

	public void setUseTitleInWeb(boolean useTitleInWeb) {
		this.useTitleInWeb = useTitleInWeb;
	}

	public int getPos()
	{
		return pos;
	}

	public void setPos(int pos)
	{
		this.pos = pos;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle( String title )
	{
		this.title = title;
	}

	public Integer getTreeId()
	{
		return treeId;
	}

	public void setTreeId( Integer treeId )
	{
		this.treeId = treeId;
	}

	public Date getDate1()
	{
		return date1;
	}

	public void setDate1(Date date1)
	{
		this.date1 = date1;
	}

	public Date getDate2()
	{
		return date2;
	}

	public void setDate2(Date date2)
	{
		this.date2 = date2;
	}

	@Override
	public void setDateFrom(Date dateFrom) {
		setDate1(dateFrom);
	}

	@Override
	public void setDateTo(Date dateTo) {
		setDate2(dateTo);
	}

	public int getContractId() {
		return contractId;
	}

	public void setContractId(int contractId) {
		this.contractId = contractId;
	}

}