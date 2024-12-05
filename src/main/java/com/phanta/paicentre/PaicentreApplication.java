package com.phanta.paicentre;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class PaicentreApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaicentreApplication.class, args);
	}

	@GetMapping("/paicenter")
	public String hello(){
		return "It's paicenter";
	}
	@PostMapping("/stripe")
	public Object getPayment(@RequestBody Object stripePay){
		return stripePay;
	}
}
