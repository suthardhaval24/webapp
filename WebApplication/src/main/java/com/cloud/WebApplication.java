package com.cloud;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebApplication {

	private final static Logger log = LogManager.getLogger(WebApplication.class.getName());
	public static void main(String[] args) {
		log.info("Starting a Web Application");
		SpringApplication.run(WebApplication.class, args);
	}

}
