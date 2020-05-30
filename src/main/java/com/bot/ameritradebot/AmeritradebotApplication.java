package com.bot.ameritradebot;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.bot.ameritradebot.web.TradeBot;

@SpringBootApplication
public class AmeritradebotApplication {

	@PostConstruct
	public void start() {
		String userPrincipalResponse = ""; // Set the user principal here
		new TradeBot().start(userPrincipalResponse);
	}

	public static void main(String[] args) {
		SpringApplication.run(AmeritradebotApplication.class, args);
	}
}
