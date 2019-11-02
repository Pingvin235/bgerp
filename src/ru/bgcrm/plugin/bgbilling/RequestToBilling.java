package ru.bgcrm.plugin.bgbilling;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.user.User;

public class RequestToBilling
    implements Runnable
{
	private static final Logger log = Logger.getLogger( RequestToBilling.class );

	private String key;
	private Request request;
	private User user;

	// параметры асинхронного режима
	private Map<String, Document> resultDocs;
	private TransferData transferData;
	private AtomicInteger taskCount;

	/**
	 * Конструктор запроса в синхронном режиме, запрос к одному биллингу.
	 * @param transferData
	 * @param user
	 * @param req
	 */
	public RequestToBilling( TransferData transferData, User user, Request req )
	{
		this.transferData = transferData;
		this.user = user;
		this.request = req;
	}

	/**
	 * Конструктор запроса в асинхронном режиме, одновременный опрос нескольких биллингов.
	 * @param taskCount
	 * @param transferData
	 * @param dbKey
	 * @param user
	 * @param req
	 * @param resultDocs
	 */
	public RequestToBilling( AtomicInteger taskCount, TransferData transferData, String dbKey, User user, Request req, Map<String, Document> resultDocs )
	{
		this( transferData, user, req );
		this.resultDocs = resultDocs;
		this.taskCount = taskCount;
		this.key = dbKey;
	}

	public String getKey()
	{
		return key;
	}

	@Override
	public void run()
	{
		Document document = null;
		try
		{
			document = transferData.postData( request, user );
		}
		catch( BGException exception )
		{
			log.error( exception );
		}

		resultDocs.put( key, document );

		if( taskCount != null )
		{
			taskCount.decrementAndGet();
		}
	}
}
