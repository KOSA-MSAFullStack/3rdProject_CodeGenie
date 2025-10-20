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

    // 회원 정보 수정 (이름 변경 / 비밀번호 변경 분기 처리)
    public void updateMember(String email, MemberUpdateDTO dto) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        System.out.println("=== 회원정보 수정 시도 ===");
        System.out.println("입력 이메일: " + email);
        System.out.println("입력된 현재 비밀번호: " + dto.getCurrentPassword());
        System.out.println("입력된 새 비밀번호: " + dto.getNewPassword());
        System.out.println("DB 저장된 해시: " + member.getPassword());

        // 이름만 변경하려는 경우 (새 비밀번호가 비어있고, 이름만 변경됨)
        boolean wantsToChangePassword = dto.getNewPassword() != null && !dto.getNewPassword().isBlank();
        boolean wantsToChangeName = dto.getUsername() != null && !dto.getUsername().isBlank();

        if (!wantsToChangeName && !wantsToChangePassword) {
            throw new IllegalArgumentException("변경할 항목이 없습니다.");
        }

        // 이름 변경 (비밀번호 없이 가능)
        if (wantsToChangeName && !wantsToChangePassword) {
            member.setUsername(dto.getUsername());
            System.out.println("이름만 변경됨: " + dto.getUsername());
        }

        // 비밀번호 변경 (현재 비밀번호 검증 필요)
        if (wantsToChangePassword) {
            // 현재 비밀번호 입력 여부 확인
            if (dto.getCurrentPassword() == null || dto.getCurrentPassword().isBlank()) {
                throw new IllegalArgumentException("비밀번호를 변경하려면 현재 비밀번호를 입력해야 합니다.");
            }

            // 현재 비밀번호 검증
            if (!passwordEncoder.matches(dto.getCurrentPassword(), member.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }

            // 새 비밀번호가 기존 비밀번호와 동일한 경우
            if (passwordEncoder.matches(dto.getNewPassword(), member.getPassword())) {
                throw new IllegalArgumentException("새 비밀번호는 기존 비밀번호와 달라야 합니다.");
            }

            // 새 비밀번호 변경
            member.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            System.out.println("비밀번호 변경됨");

            // 이름도 같이 변경하려는 경우
            if (wantsToChangeName) {
                member.setUsername(dto.getUsername());
                System.out.println("이름도 함께 변경됨: " + dto.getUsername());
            }
        }

        memberRepository.save(member);
        System.out.println("회원정보 수정 완료");
    }
}
