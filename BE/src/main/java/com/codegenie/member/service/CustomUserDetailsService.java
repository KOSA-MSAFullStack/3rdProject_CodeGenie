package com.codegenie.member.service;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.codegenie.member.dto.CustomUserDetails;
import com.codegenie.member.entity.MemberEntity;
import com.codegenie.member.repository.MemberRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public CustomUserDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<MemberEntity> member = memberRepository.findByEmail(email);
        if (member.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + email);
        }
        return new CustomUserDetails(member.get());
    }
}
