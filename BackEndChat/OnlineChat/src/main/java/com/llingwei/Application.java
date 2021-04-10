package com.llingwei;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
// scan path of mybatis mapper
@MapperScan(basePackages = "com.llingwei.mapper")
// scan all needed packages, including some tool classed used by this project
@ComponentScan(basePackages= {"com.llingwei","org.n3r.idworker"})
public class Application {

	@Bean
	public SpringUtil getSpringUtil(){
		return new SpringUtil();
	}
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
