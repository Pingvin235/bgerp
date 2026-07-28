package ru.bgcrm.plugin.bgbilling;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.bgerp.app.exception.BGException;
import org.bgerp.util.Log;
import org.w3c.dom.Document;

import ru.bgcrm.model.user.User;

public class RequestToBilling implements Runnable {
    private static final Log log = Log.getLog();

    private String key;
    private Request request;
    private User user;

    // asynchronous mode parameters
    private Map<String, Document> resultDocs;
    private TransferData transferData;
    private AtomicInteger taskCount;

    /**
     * Constructs a request in synchronous mode, a request to a single billing
     * @param transferData the transfer data
     * @param user the user
     * @param req the request
     */
    public RequestToBilling(TransferData transferData, User user, Request req) {
        this.transferData = transferData;
        this.user = user;
        this.request = req;
    }

    /**
     * Constructs a request in asynchronous mode, simultaneous polling of multiple billings
     * @param taskCount the task counter
     * @param transferData the transfer data
     * @param dbKey the billing key
     * @param user the user
     * @param req the request
     * @param resultDocs the result document map
     */
    public RequestToBilling(AtomicInteger taskCount, TransferData transferData, String dbKey, User user, Request req,
            Map<String, Document> resultDocs) {
        this(transferData, user, req);
        this.resultDocs = resultDocs;
        this.taskCount = taskCount;
        this.key = dbKey;
    }

    public String getKey() {
        return key;
    }

    @Override
    public void run() {
        Document document = null;
        try {
            document = transferData.postData(request, user);
        } catch (BGException exception) {
            log.error(exception);
        }

        resultDocs.put(key, document);

        if (taskCount != null) {
            taskCount.decrementAndGet();
        }
    }
}
