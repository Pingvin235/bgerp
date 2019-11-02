package ru.bgcrm.plugin.mtsc;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;

import ru.bgcrm.util.Utils;

public class MessageSelector {

	private static final Logger log = Logger.getLogger(MessageSelector.class);

	public static void main(String[] args) {
		String urlString = "http://mcommunicator.ru/M2M/m2m_api.asmx/GetMessages";
		String login = "79148544920";
		String password = Utils.getDigest("467927");

		try {
			URIBuilder url = new URIBuilder(urlString);
			url.addParameter("login", login);
			url.addParameter("password", password);
			url.addParameter("messageType", "MO");
			url.addParameter("subscriberMsids", "79148544920");
			url.addParameter("DateFrom", "2016-05-01");
			url.addParameter("DateTo", "2016-05-02");
			/*url.addParameter("msid", numberTo);
			url.addParameter("message", text);*/

			Request req = Request.Get(url.build());
			if (log.isDebugEnabled()) {
				log.debug("Sending: " + req);
			}
			String response = req.execute().returnContent().asString(Utils.UTF8);
			if (log.isDebugEnabled()) {
				log.debug("=> " + response);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
