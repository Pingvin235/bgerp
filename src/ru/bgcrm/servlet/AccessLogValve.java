package ru.bgcrm.servlet;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

import ru.bgcrm.model.user.User;
import ru.bgcrm.util.Setup;

public class AccessLogValve extends org.apache.catalina.valves.AccessLogValve {
	public static final String PARAM_HEADER_NAME_REMOTE_ADDR = "header.name.remote.addr";

	/**
	* %a - Remote IP address
	* %A - Local IP address
	* %b - Bytes sent, excluding HTTP headers, or '-' if zero
	* %B - Bytes sent, excluding HTTP headers
	* %h - Remote host name (or IP address if resolveHosts is false)
	* %H - Request protocol
	* %l - Remote logical username from identd (always returns '-')
	* %m - Request method (GET, POST, etc.)
	* %p - Local port on which this request was received
	* %q - Query string (prepended with a '?' if it exists)
	* %r - First line of the request (method and request URI)
	* %s - HTTP status code of the response
	* %S - User session ID
	* %t - Date and time, in Common Log Format
	* %u - Remote user that was authenticated (if any), else '-'
	* %U - Requested URL path
	* %v - Local server name
	* %D - Time taken to process the request, in millis
	* %T - Time taken to process the request, in seconds
	* %I - current request thread name (can compare later with stacktraces)
	*/

	public AccessLogValve() {
		setDirectory("log");
		setPrefix("access_log.");
		setRotatable(true);
		setFileDateFormat("yyyy-MM-dd");
		setPattern("%h %S %t %T %s %b \"%r\" ");
		setEnabled(true);
	}

	@Override
	protected AccessLogElement[] createLogElements() {
		AccessLogElement element = new AccessLogElement() {
			@Override
			public void addElement(CharArrayWriter buf, Date date, Request request, Response response, long time)
			{
				String headerNameRemoteAddr = Setup.getSetup().get(PARAM_HEADER_NAME_REMOTE_ADDR);
				if (headerNameRemoteAddr != null) {
					buf.append(headerNameRemoteAddr);
					buf.append("=");
					buf.append(request.getHeader(headerNameRemoteAddr));
					buf.append(" ");
				}

				User user = (User) request.getAttribute("ctxUser");
				if (user != null) {
					buf.append("UID=");
					buf.append(String.valueOf(user.getId()));
					buf.append(" ");
				}

				Map<String, String[]> paramMap = ((HttpServletRequest) request).getParameterMap();
				for (Map.Entry<String, String[]> me : paramMap.entrySet()) {
					String key = me.getKey();
					String[] values = me.getValue();

					buf.append(key);
					buf.append("=");

					boolean first = true;

					for (String value : values) {
						if (!first) {
							buf.append(" ");
						}

						buf.append(value);
						first = false;
					}

					buf.append("&");
				}
			}

		};

		List<AccessLogElement> superList = new ArrayList<AccessLogElement>();
		for (AccessLogElement superEl : super.createLogElements()) {
			superList.add(superEl);
		}
		superList.add(element);

		return superList.toArray(new AccessLogElement[0]);
	}

	@Override
	protected synchronized void open() {
		super.open();

		//менее костыльная перекидовалка старых access логов
		final File file = super.currentLogFile;
		final PrintWriter writer = super.writer;

		super.writer = new PrintWriter(writer) {
			@Override
			public void println(String x) {
				writer.println(x);
			}

			@Override
			public void close() {
				super.close();

				File dir = new File(file.getParentFile(), "access");
				dir.mkdirs();

				file.renameTo(new File(dir, file.getName()));
			}
		};
	}
}