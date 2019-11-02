package ru.bgcrm.plugin.slack.dao;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ru.bgcrm.model.BGException;
import ru.bgcrm.struts.action.FileAction.FileInfo;
import ru.bgcrm.util.Utils;

public class SlackProto {
	private static final Logger log = Logger.getLogger(SlackProto.class);
	
	private static final ObjectMapper jsonMapper = new ObjectMapper();
	
	private final String token;
	private final String prefixSingle;
	private final String prefix;
	
	public SlackProto(String token, boolean isPrivate) {
		this.token = token;
		this.prefixSingle = isPrivate ? "group" : "channel";
		this.prefix = prefixSingle + "s";
	}
	
	public String getChannelsPrefix() {
		return prefix;
	}

	public String channelCreate(String name) throws Exception {
		URIBuilder url = new URIBuilder("https://slack.com/api/" + prefix + ".create");
		url.addParameter("token", token);
		url.addParameter("name", name);		
		return sendRequest(url).get(prefixSingle).get("id").asText();
	}
	
	public void channelSetTopic(String channel, String topic) throws Exception {
		if (topic.length() > 248)
			topic = topic.substring(0, 248) + "..";		
		URIBuilder url = new URIBuilder("https://slack.com/api/" + prefix + ".setTopic");
		url.addParameter("token", token);
		url.addParameter("channel", channel);
		url.addParameter("topic", topic);
		sendRequest(url);
	}
	
	public void channelSetPurpose(String channel, String purpose) throws Exception {
		URIBuilder url = new URIBuilder("https://slack.com/api/" + prefix + ".setPurpose");
		url.addParameter("token", token);
		url.addParameter("channel", channel);
		url.addParameter("purpose", purpose);
		sendRequest(url);
	}
	
	public void channelArchive(String channel) throws Exception {
		URIBuilder url = new URIBuilder("https://slack.com/api/" + prefix + ".archive");
		url.addParameter("token", token);
		url.addParameter("channel", channel);
		sendRequest(url);
	}
	
	public void channelUnArchive(String channel) throws Exception {
		URIBuilder url = new URIBuilder("https://slack.com/api/" + prefix + ".unarchive");
		url.addParameter("token", token);
		url.addParameter("channel", channel);
		sendRequest(url);
	}
	
	public void channelInviteUsers(String channel, List<String> users) throws Exception {
		for (String user: users) {
			URIBuilder url = new URIBuilder("https://slack.com/api/" + prefix + ".invite");
			url.addParameter("token", token);
			url.addParameter("channel", channel);
			url.addParameter("user", user);
			sendRequest(url);
		}
	}
	
	public JsonNode userList() throws Exception {
		URIBuilder url = new URIBuilder("https://slack.com/api/users.list");
		url.addParameter("token", token);
		return sendRequest(url);
	}

	public JsonNode chatPostMessage(String channel, String text, String username, Map<Integer, FileInfo> attachments) throws Exception {
		URIBuilder url = new URIBuilder("https://slack.com/api/chat.postMessage");
		url.addParameter("token", token);
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
		URIBuilder url = new URIBuilder("https://slack.com/api/" + prefix + ".list");
		url.addParameter("token", token);
		if (excludeArchived)
			url.addParameter("exclude_archived", "1");
		return sendRequest(url);
	}
	
	public JsonNode channelHistory(String channel, String oldest) throws Exception {
		URIBuilder url = new URIBuilder("https://slack.com/api/" + prefix + ".history");
		url.addParameter("token", token);
		url.addParameter("channel", channel);
		if (oldest != null)
			url.addParameter("oldest", oldest);		
		return sendRequest(url);
	}

	private JsonNode sendRequest(URIBuilder url)
			throws Exception {
		Request req = Request.Post(url.build())
		        .connectTimeout(10000)
		        .socketTimeout(10000);
		if (log.isDebugEnabled())
			log.debug("Sending: " + req);

		String result = req.execute().returnContent().asString(Charset.forName("UTF-8"));
		if (log.isDebugEnabled())
			log.debug("Response: " + result);
		
		JsonNode node = jsonMapper.readTree(result);
		if (node.get("ok") == null || !node.get("ok").asBoolean())
			throw new BGException("Incorrect Slack response: " + node.get("error"));
		
		return node;
	}
}
