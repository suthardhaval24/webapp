package com.cloud;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebApplication {

	private final static Logger log = Logger.getLogger(WebApplication.class.getName());
	public static void main(String[] args) {
		log.info("Starting a Web Application");
		SpringApplication.run(WebApplication.class, args);
	}

}
