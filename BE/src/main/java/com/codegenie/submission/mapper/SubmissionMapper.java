// SubmissionMapper.java
// 역할: Submission 엔티티와 DTO 간 변환
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
//import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

// * author: 김기성
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubmissionMapper {

    // SubmissionMapper 인스턴스
    SubmissionMapper INSTANCE = Mappers.getMapper(SubmissionMapper.class);

    // SubmissionRequestDto를 Submission 엔티티로 변환
    // member 필드는 서비스 계층에서 직접 설정
    Submission toEntity(SubmissionRequestDto dto);
    //@Mapping(target = "submissionId", ignore = true)
    //@Mapping(target = "member", ignore = true)
    //@Mapping(target = "status", ignore = true)
    //@Mapping(target = "stdout", ignore = true)
    //@Mapping(target = "stderr", ignore = true)
    //@Mapping(target = "runTime", ignore = true)
    //@Mapping(target = "memory", ignore = true)
    //@Mapping(target = "submittedAt", ignore = true)

    // Submission 엔티티를 SubmissionResponseDto로 변환
    SubmissionResponseDto toDto(Submission entity);
}