package ru.bgcrm.plugin.bgbilling.proto.model.balance;

import java.math.BigDecimal;

public class ContractBalanceGeneral
{
	private BigDecimal account;
	private BigDecimal charge;
	private BigDecimal inputBalance;
	private String month;
	private BigDecimal outputBalance;
	private BigDecimal payment;

	public BigDecimal getAccount()
	{
		return account;
	}

	public void setAccount( BigDecimal account )
	{
		this.account = account;
	}

	public BigDecimal getCharge()
	{
		return charge;
	}

	public void setCharge( BigDecimal charge )
	{
		this.charge = charge;
	}

	public BigDecimal getInputBalance()
	{
		return inputBalance;
	}

	public void setInputBalance( BigDecimal inputBalance )
	{
		this.inputBalance = inputBalance;
	}

	public String getMonth()
	{
		return month;
	}

	public void setMonth( String month )
	{
		this.month = month;
	}

	public BigDecimal getOutputBalance()
	{
		return outputBalance;
	}

	public void setOutputBalance( BigDecimal outputBalance )
	{
		this.outputBalance = outputBalance;
	}

	public BigDecimal getPayment()
	{
		return payment;
	}

	public void setPayment( BigDecimal payment )
	{
		this.payment = payment;
	}
}
