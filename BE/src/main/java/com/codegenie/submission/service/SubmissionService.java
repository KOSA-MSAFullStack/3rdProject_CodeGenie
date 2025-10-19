// SubmissionService.java
// 문제 제출 '비즈니스 로직 인터페이스'
/*
 * 설명:
 * - 문제 제출 관련 비즈니스 로직 정의하는 인터페이스
 * 
 * 주요 기능:
 * - 답안 제출 및 채점 요청 기능 명세
 */

package com.codegenie.submission.service;

import com.codegenie.member.entity.MemberEntity;
import com.codegenie.submission.dto.SubmissionDTO;

// * author: 김기성
public interface SubmissionService {

    /**
     * 답안 제출 후, Judge0 통해 채점 요청
     * @param member 현재 로그인한 사용자 엔티티
     * @param req 제출 요청 데이터 (코드, 언어, 문제 ID)
     * @return 채점 결과 데이터
     */
    SubmissionDTO.SubmitResponse judge(MemberEntity member, SubmissionDTO.SubmitRequest req);
}
