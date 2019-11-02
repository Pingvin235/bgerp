package ru.bgcrm.plugin.report.event.listener;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.QueuePrintEvent;
import ru.bgcrm.event.listener.EventListener;
import ru.bgcrm.model.BGException;
import ru.bgcrm.plugin.report.dao.JasperReport;
import ru.bgcrm.plugin.report.model.PrintQueueConfig.PrintType;
import ru.bgcrm.util.sql.ConnectionSet;

public class PrintQueueEventListener
{
	private static final Logger log = Logger.getLogger( PrintQueueEventListener.class );

	public PrintQueueEventListener()
	{
		EventProcessor.subscribe( new EventListener<QueuePrintEvent>()
		{
			@Override
			public void notify( QueuePrintEvent e, ConnectionSet connectionSet )
				throws BGException
			{
				QueuePrintEvent event = (QueuePrintEvent)e;

				try
				{
					HttpServletResponse response = event.getForm().getHttpResponse();
					
					PrintType printType = event.getPrintType();

					response.setContentType( "application/octet-stream" );
					response.setHeader( "Content-Disposition", "attachment;filename=" + (printType == null ? "queue.pdf" : printType.getFileName() ) );

					new JasperReport().addPrintQueueDocumentToOutputStream( event.getData(), event.getQueue(), 
					                                                        printType, response.getOutputStream() );
				}
				catch( IOException exp )
				{
					log.error( exp.getMessage() );
				}
				catch( Exception exp )
				{
					log.error( exp.getMessage() );
				}
			}
		}, QueuePrintEvent.class );
	}
}
