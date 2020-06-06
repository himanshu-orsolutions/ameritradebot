package com.bot.ameritradebot.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.WebSocketContainer;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.stereotype.Component;

import com.bot.ameritradebot.web.handlers.TradebotEndpoint;

@Component
public class TradeBot {

	private Logger logger = Logger.getLogger("TradeBot");

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
			logger.log(Level.SEVERE, "Unsupported encoding: UTF-8", unsupportedEncodingException);
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
			logger.log(Level.SEVERE, String.format("Error parsing the date string: %s", dateString), parseException);
		}
		return new Date().getTime();
	}

	@SuppressWarnings("unchecked")
	public void start(Document userPrincipal) {
		try {
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

				TradebotEndpoint.requests = Arrays.asList(
						new Document()
								.append("requests",
										Arrays.asList(
												new Document().append("service", "ADMIN").append("command", "LOGIN")
														.append("requestid", 0)
														.append("account",
																account.get("accountId"))
														.append("source", streamerInfo.get("appId"))
														.append("parameters", new Document()
																.append("credential", prepareQueryString(credentials))
																.append("token", streamerInfo.get("token"))
																.append("version", "1.0").append("qoslevel", 0))))
								.toJson(),
						new Document()
								.append("requests", Arrays.asList(new Document().append("service", "CHART_EQUITY")
										.append("requestid", 1).append("command", "SUBS")
										.append("account", account.get("accountId"))
										.append("source", streamerInfo.get("appId")).append("parameters", new Document()
												.append("keys", "AAPL").append("fields", "0,1,2,3,4,5,6,7,8"))))
								.toJson());
				WebSocketContainer container = ContainerProvider.getWebSocketContainer();
				container.connectToServer(TradebotEndpoint.class,
						URI.create(StringUtils.join("wss://", streamerInfo.getString("streamerSocketUrl"), "/ws")));
			} else {
				logger.log(Level.SEVERE, "Invalid user principal.");
			}
		} catch (IOException | DeploymentException exception) {
			logger.log(Level.SEVERE, "Error connecting to the web socket.", exception);
		}
	}
}
