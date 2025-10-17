package com.codegenie.submission.service;

import com.codegenie.member.entity.MemberEntity;
import com.codegenie.submission.dto.SubmissionDto;

public interface SubmissionService {

    /**
     * 코드 채점 요청 (현재는 간이 채점):
     * - 실행기 없이 규칙적으로 판정(데모용)
     * - 결과를 저장하고 최신 집계(submissions/accepted)를 함께 반환
     * - 나중에 실행 채점기로 교체 시 구현만 바꾸면 됨
     */
    SubmissionDto.SubmitResponse judge(MemberEntity member, SubmissionDto.SubmitRequest req);
}
