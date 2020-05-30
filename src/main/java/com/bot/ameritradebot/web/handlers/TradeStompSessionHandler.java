package com.bot.ameritradebot.web.handlers;

import java.lang.reflect.Type;

import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

public class TradeStompSessionHandler extends StompSessionHandlerAdapter {

	private String request;

	public TradeStompSessionHandler(String request) {
		this.request = request;
	}

	@Override
	public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
		session.send("", request);
	}

	@Override
	public Type getPayloadType(StompHeaders headers) {
		return String.class;
	}

	@Override
	public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload,
			Throwable exception) {
		System.out.println(String.format("Error connecting to web-socket. Reason: %s", exception.getMessage()));
	}

	@Override
	public void handleFrame(StompHeaders headers, Object payload) {
		System.out.println(String.format("Message: %s", payload));
	}
}
