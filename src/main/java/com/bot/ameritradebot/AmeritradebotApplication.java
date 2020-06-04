package com.bot.ameritradebot;

import javax.annotation.PostConstruct;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.bot.ameritradebot.web.TradeBot;

@SpringBootApplication
public class AmeritradebotApplication {

	@Autowired
	TradeBot tradeBot;

	@PostConstruct
	public void start() {
		String userPrincipalResponse = ""; // Set the user principal here
		tradeBot.start(Document.parse(userPrincipalResponse));
	}

	public static void main(String[] args) {
		SpringApplication.run(AmeritradebotApplication.class, args);
	}
}
