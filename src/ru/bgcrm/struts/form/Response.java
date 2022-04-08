package ru.bgcrm.struts.form;

import ru.bgcrm.event.client.ClientEvent;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.SearchResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP response object, may be JSON serialized.
 * @author Shamil Vakhitov
 */
public class Response {
    public static final String STATUS_OK = "ok";
    public static final String STATUS_ERROR = "error";
    public static final String STATUS_MESSAGE = "message";

    private String status = STATUS_OK;
    private String message = "";

    private final Map<String, Object> data = new HashMap<>();
    private final List<ClientEvent> eventList = new ArrayList<>();

    /**
     * Status of request's execution, may be: {@link STATUS_OK}, {@link STATUS_ERROR}, {@link STATUS_MESSAGE}
     * @return
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set status of request's execution.
     * @param status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Message for {@link STATUS_ERROR} or {@link STATUS_MESSAGE}.
     * @return
     */
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Events for processing of frontend.
     * @return
     */
    public List<ClientEvent> getEventList() {
        return eventList;
    }

    /**
     * Add an event for frontend.
     * @param event
     */
    public void addEvent(ClientEvent event) {
        if (event != null) {
            eventList.add(event);
        }
    }

    /**
     * Response data. May be serialized to JSON, or available on JSP page.
     * @return
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Set response data object.
     * @param key
     * @param data
     */
    public void setData(String key, Object data) {
        this.data.put(key, data);
    }

    /**
     * Adds search result to the response data, two objects with keys: 'page' and 'list'
     * @param result
     */
    public void addSearchResult(SearchResult<?> result) {
        Page page = result.getPage();
        if (page.isPaginationEnabled()) {
            this.data.put("page", page);
        }
        this.data.put("list", result.getList());
    }
}
