package com.voting.online_voting_system.service;

import com.voting.online_voting_system.entity.Election;
import com.voting.online_voting_system.entity.User;
import com.voting.online_voting_system.entity.VotingChallenge;
import com.voting.online_voting_system.repository.VotingChallengeRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {

    private final VotingChallengeRepository votingChallengeRepository;
    private final PasswordEncoder passwordEncoder;

    public OtpService(
            VotingChallengeRepository votingChallengeRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.votingChallengeRepository = votingChallengeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String generateAndSendOtp(
            User user,
            Election election
    ) {

        // Generate 6-digit OTP
        String otp = String.format(
                "%06d",
                new Random().nextInt(1_000_000)
        );

        // Hash OTP before storing it
        String otpHash =
                passwordEncoder.encode(otp);

        VotingChallenge challenge =
                new VotingChallenge();

        challenge.setUser(user);
        challenge.setElection(election);
        challenge.setOtpHash(otpHash);

        // OTP valid for 5 minutes
        challenge.setExpiresAt(
                LocalDateTime.now().plusMinutes(5)
        );

        challenge.setAttempts(0);
        challenge.setVerified(false);
        challenge.setUsed(false);
        challenge.setCreatedAt(
                LocalDateTime.now()
        );

        votingChallengeRepository.save(challenge);

        // TEMPORARY DEVELOPMENT MODE
        // Email sending is disabled for now.
        System.out.println(
                "================================="
        );
        System.out.println(
                "DEVELOPMENT OTP"
        );
        System.out.println(
                "User = " + user.getEmail()
        );
        System.out.println(
                "Election ID = " + election.getId()
        );
        System.out.println(
                "OTP = " + otp
        );
        System.out.println(
                "OTP expires at = "
                        + challenge.getExpiresAt()
        );
        System.out.println(
                "================================="
        );

        // Return OTP temporarily for Postman testing
        return otp;
    }
}