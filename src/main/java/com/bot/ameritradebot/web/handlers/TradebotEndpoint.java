package com.bot.ameritradebot.web.handlers;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

@ClientEndpoint
public class TradebotEndpoint {

	public static String requestBody;

	Logger logger = Logger.getLogger("TradebotEndpoint");

	@OnOpen
	public void onOpen(Session session) {

		logger.log(Level.INFO, "Connected to web socket.");
		try {
			session.getBasicRemote().sendText(requestBody);
		} catch (IOException ioException) {
			logger.log(Level.SEVERE, "Error sending request to the web socket.");
		}
	}

	@OnMessage
	public void processMessage(String message) {
		logger.log(Level.INFO, String.format("Message: %s", message));
	}

	@OnError
	public void processError(Throwable error) {
		logger.log(Level.SEVERE, String.format("Error: %s", error.getMessage()), error);
	}

	@OnClose
	public void processClosure() {
		logger.log(Level.INFO, "Socket closed.");
	}
}