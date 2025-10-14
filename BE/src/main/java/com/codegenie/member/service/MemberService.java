package com.codegenie.member.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.codegenie.member.dto.MemberUpdateDTO;
import com.codegenie.member.dto.MemberInfoDTO;
import com.codegenie.member.entity.MemberEntity;
import com.codegenie.member.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, BCryptPasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원 정보 조회
    public MemberInfoDTO getMemberInfo(String email) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return new MemberInfoDTO(member.getEmail(), member.getUsername());
    }

    // 회원 정보 수정 (비밀번호 검증 포함)
    public void updateMember(String email, MemberUpdateDTO dto) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        System.out.println("=== 회원정보 수정 시도 ===");
        System.out.println("입력 이메일: " + email);
        System.out.println("입력된 현재 비밀번호: " + dto.getCurrentPassword());
        System.out.println("입력된 새 비밀번호: " + dto.getNewPassword());
        System.out.println("DB 저장된 해시: " + member.getPassword());

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(dto.getCurrentPassword(), member.getPassword())) {
            System.out.println("❌ 현재 비밀번호가 일치하지 않습니다.");
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호가 기존 비밀번호와 동일한 경우
        if (passwordEncoder.matches(dto.getNewPassword(), member.getPassword())) {
            System.out.println("❌ 새 비밀번호가 기존 비밀번호와 동일합니다.");
            throw new IllegalArgumentException("새 비밀번호는 기존 비밀번호와 달라야 합니다.");
        }

        // 이름 변경 (입력 시만)
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            member.setUsername(dto.getUsername());
            System.out.println("이름 변경됨: " + dto.getUsername());
        }

        // 비밀번호 변경
        member.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        memberRepository.save(member);

        System.out.println("회원정보 수정 완료");
    }
}
