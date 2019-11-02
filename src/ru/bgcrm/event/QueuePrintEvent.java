package ru.bgcrm.event;

import java.util.List;

import ru.bgcrm.model.process.Queue;
import ru.bgcrm.plugin.report.model.PrintQueueConfig.PrintType;
import ru.bgcrm.struts.form.DynActionForm;

public class QueuePrintEvent
	extends UserEvent
{
	private final Queue queue;
	private final List<Object[]> data;
	private final PrintType printType;

	public QueuePrintEvent( DynActionForm form, List<Object[]> searchResult, Queue queue, PrintType printType )
	{
		super( form );

		this.queue = queue;
		this.data = searchResult;
		this.printType = printType;
	}

	public Queue getQueue()
	{
		return queue;
	}

	public List<Object[]> getData()
	{
		return data;
	}

	public PrintType getPrintType()
	{
		return printType;
	}
}
