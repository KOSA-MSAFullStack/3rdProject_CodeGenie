// CodeGenieApplication.java

package com.codegenie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CodeGenieApplication {
	private static final Logger log = LoggerFactory.getLogger(CodeGenieApplication.class);

	public static void main(String[] args) {
		log.info("프로그램 시작");
		
		try {
			SpringApplication.run(CodeGenieApplication.class, args);
		} catch (Exception e) {
			log.error("\n\n !!!예외 발생: {}", e.getMessage(), e);
			throw e;
		}
		log.info("프로그램 종료");
	}

}
