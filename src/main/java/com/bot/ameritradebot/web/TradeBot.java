package com.bot.ameritradebot.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.bot.ameritradebot.web.handlers.TradeStompSessionHandler;

public class TradeBot {

	/**
	 * Prepares a URL query string from the keys present in the document
	 * 
	 * @param document The JSON document
	 * @return The query string
	 * @throws UnsupportedEncodingException
	 */
	private String prepareQueryString(Document document) {

		try {
			return URLEncoder.encode(StringUtils.join(document.keySet().stream()
					.map(key -> StringUtils.join(key, "=", document.get(key))).collect(Collectors.toList()), "&"),
					"UTF-8");
		} catch (UnsupportedEncodingException unsupportedEncodingException) {
			System.out.println("Unsupported encoding: UTF-8");
		}
		return StringUtils.EMPTY;
	}

	/**
	 * Converts date string to milliseconds
	 * 
	 * @param dateString The date string
	 * @return The milliseconds
	 */
	private Long convertToMillis(String dateString) {

		try {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(dateString).getTime();
		} catch (ParseException parseException) {
			System.out.println(String.format("Error parsing the date string: %s", dateString));
		}
		return new Date().getTime();
	}

	@SuppressWarnings("unchecked")
	public void start(String userPrincipalResponse) {

		Document userPrincipal = Document.parse(userPrincipalResponse);
		Document streamerInfo = (Document) userPrincipal.get("streamerInfo");
		List<Document> accounts = (List<Document>) userPrincipal.get("accounts");

		if (streamerInfo != null && accounts != null && !accounts.isEmpty()) {
			Document account = accounts.get(0);

			// Preparing the credentials object
			Document credentials = new Document().append("userid", account.get("accountId"))
					.append("token", streamerInfo.get("token")).append("company", account.get("company"))
					.append("segment", account.get("segment")).append("cddomain", account.get("accountCdDomainId"))
					.append("usergroup", streamerInfo.get("userGroup"))
					.append("accesslevel", streamerInfo.get("accessLevel")).append("authorized", "Y")
					.append("timestamp", convertToMillis(streamerInfo.getString("tokenTimestamp")))
					.append("appid", streamerInfo.get("appId")).append("acl", streamerInfo.get("acl"));

			Document request = new Document().append("requests", Arrays.asList(
					new Document().append("service", "ADMIN").append("command", "LOGIN").append("requestid", 0)
							.append("account", account.get("accountId")).append("source", streamerInfo.get("appId"))
							.append("parameters",
									new Document().append("credential", prepareQueryString(credentials))
											.append("token", streamerInfo.get("token")).append("version", "1.0")),
					new Document().append("service", "CHART_EQUITY").append("requestid", 1).append("command", "SUBS")
							.append("account", account.get("accountId")).append("source", streamerInfo.get("appId"))
							.append("parameters",
									new Document().append("keys", "AAPL").append("fields", "0,1,2,3,4,5,6,7,8"))));

			WebSocketClient client = new StandardWebSocketClient();
			WebSocketStompClient stompClient = new WebSocketStompClient(client);
			stompClient.setMessageConverter(new MappingJackson2MessageConverter());
			StompSessionHandler sessionHandler = new TradeStompSessionHandler(request.toJson());
			stompClient.connect(StringUtils.join("wss://", streamerInfo.getString("streamerSocketUrl"), "/ws"),
					sessionHandler);
		} else {
			System.out.println("Invalid user principal.");
		}
	}
}
