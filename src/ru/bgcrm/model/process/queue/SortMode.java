package ru.bgcrm.model.process.queue;

public class SortMode
{
	private int columnPos;
	private String title;
	private boolean desc;
	private String orderExpression;

	public int getColumnPos()
	{
		return columnPos;
	}

	public void setColumnPos( int columnId )
	{
		this.columnPos = columnId;
		orderExpression = String.valueOf( columnId );
		if( desc )
		{
			orderExpression += " DESC";
		}
	}

	public void setOrderExpression( String orderExpression )
	{
		this.orderExpression = orderExpression;

		if( desc )
		{
			this.orderExpression += " DESC";
		}
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle( String title )
	{
		this.title = title;
	}

	public boolean isDesc()
	{
		return desc;
	}

	public void setDesc( boolean desc )
	{
		this.desc = desc;
	}

	public String getOrderExpression()
	{
		return orderExpression;
	}
}
