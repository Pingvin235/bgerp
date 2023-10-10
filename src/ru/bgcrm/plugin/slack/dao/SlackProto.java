package ru.bgcrm.plugin.slack.dao;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.bgerp.model.file.FileInfo;
import org.bgerp.util.Log;

import ru.bgcrm.model.BGException;
import ru.bgcrm.util.Utils;

/**
 * Slack API: https://api.slack.com/web
 *
 * @author Shamil Vakhitov
 */
public class SlackProto {
    private static final Log log = Log.getLog();

    private static final String API_URL = "https://slack.com/api/";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String tokenHeader;

    public SlackProto(String token) {
        this.tokenHeader = "Bearer " + token;
    }

    public String channelCreate(String name, boolean isPrivate) throws Exception {
        var url = new URIBuilder(API_URL + "conversations.create");
        url.addParameter("name", name);
        url.addParameter("is_private", String.valueOf(isPrivate));
        return sendRequest(url).get("channel").get("id").asText();
    }

    public void channelSetTopic(String channel, String topic) throws Exception {
        if (topic.length() > 248)
            topic = topic.substring(0, 248) + "..";
        var url = new URIBuilder(API_URL + "conversations.setTopic");
        url.addParameter("channel", channel);
        url.addParameter("topic", topic);
        sendRequest(url);
    }

    public void channelSetPurpose(String channel, String purpose) throws Exception {
        var url = new URIBuilder(API_URL + "conversations.setPurpose");
        url.addParameter("channel", channel);
        url.addParameter("purpose", purpose);
        sendRequest(url);
    }

    public void channelArchive(String channel) throws Exception {
        var url = new URIBuilder(API_URL + "conversations.archive");
        url.addParameter("channel", channel);
        sendRequest(url);
    }

    public void channelUnArchive(String channel) throws Exception {
        var url = new URIBuilder(API_URL + "conversations.unarchive");
        url.addParameter("channel", channel);
        sendRequest(url);
    }

    public void channelInviteUsers(String channel, List<String> users) throws Exception {
        for (String user: users) {
            var url = new URIBuilder(API_URL + "conversations.invite");
            url.addParameter("channel", channel);
            url.addParameter("user", user);
            sendRequest(url);
        }
    }

    public JsonNode userList() throws Exception {
        var url = new URIBuilder("https://slack.com/api/users.list");
        return sendRequest(url);
    }

    public JsonNode chatPostMessage(String channel, String text, String username, Map<Integer, FileInfo> attachments) throws Exception {
        var url = new URIBuilder("https://slack.com/api/chat.postMessage");
        url.addParameter("channel", channel);
        url.addParameter("text", text);
        if (Utils.notBlankString(username)) {
            url.addParameter("as_user", "false");
            url.addParameter("username", username);
        }
        if (attachments != null) {
            /*ArrayNode attachmentsNode = JsonNodeFactory.instance.arrayNode();
            for (FileInfo file : attachments.values()) {
                JsonNode attachment = JsonNodeFactory.instance.objectNode();
                attachmentsNode.add(attachment);
            }*/
        }
        return sendRequest(url);
    }

    public JsonNode channelList(boolean excludeArchived) throws Exception {
        var url = new URIBuilder(API_URL + "conversations.list");
        url.addParameter("exclude_archived", String.valueOf(excludeArchived));
        return sendRequest(url).get("channels");
    }

    public JsonNode channelHistory(String channel, String oldest) throws Exception {
        var url = new URIBuilder(API_URL + "conversations.history");
        url.addParameter("channel", channel);
        if (oldest != null)
            url.addParameter("oldest", oldest);
        return sendRequest(url);
    }

    private JsonNode sendRequest(URIBuilder url) throws Exception {
        var req = Request.Post(url.build())
            .setHeader("Authorization", tokenHeader)
            .setHeader("Content-Type", "application/x-www-form-urlencoded")
            .connectTimeout(10000)
            .socketTimeout(10000);

        log.debug("Sending: {}", req);

        var result = req.execute().returnContent().asString(Charset.forName("UTF-8"));

        log.debug("Response: {}", result);

        var node = MAPPER.readTree(result);
        if (node.get("ok") == null || !node.get("ok").asBoolean())
            throw new BGException("Incorrect Slack response: " + node.get("error"));

        return node;
    }
}
