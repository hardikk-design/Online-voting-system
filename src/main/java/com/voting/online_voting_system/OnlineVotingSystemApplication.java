package com.voting.online_voting_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OnlineVotingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlineVotingSystemApplication.class, args);
	}

}
