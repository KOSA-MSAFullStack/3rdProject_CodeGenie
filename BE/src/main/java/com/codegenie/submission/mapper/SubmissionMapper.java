// SubmissionMapper.java
// Entity-DTO간 '데이터 변환'
/*
 * 설명:
 * - Submission 엔티티와 SubmissionRequestDto, SubmissionResponseDto 간의 매핑 정의
 * - MapStruct 라이브러리를 사용하여 코드 자동 생성
 *
 * 주요 기능:
 * - SubmissionRequestDto를 Submission 엔티티로 변환
 * - Submission 엔티티를 SubmissionResponseDto로 변환
 */
package com.codegenie.submission.mapper;

import com.codegenie.submission.dto.SubmissionRequestDto;
import com.codegenie.submission.dto.SubmissionResponseDto;
import com.codegenie.submission.entity.Submission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

// * author: 김기성
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubmissionMapper {

    // SubmissionMapper 인스턴스
    SubmissionMapper INSTANCE = Mappers.getMapper(SubmissionMapper.class);

    // SubmissionRequestDto를 Submission 엔티티로 변환
    Submission toEntity(SubmissionRequestDto dto);

    // Submission 엔티티를 SubmissionResponseDto로 변환
    @Mapping(source = "member.member_id", target = "memberId")
    @Mapping(source = "codingQuiz.id", target = "quizId")
    SubmissionResponseDto toDto(Submission entity);
}
