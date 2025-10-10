// SubmissionDTO.java
// 문제 제출 데이터 전송 객체 (DTO)
/*
 * 설명:
 * - 클라이언트와 서버 간, 혹은 각 계층(Layer) 간 데이터 전송에 사용되는 객체
 * - 문제 제출과 관련된 데이터 담고 있으며, 화면에 필요한 정보 전달 시 주로 사용
 */

package com.codegenie.submission.dto;

import lombok.Getter;
import lombok.ToString;
import lombok.Setter;

// * author: 김기성
@Getter
@Setter
@ToString
public class SubmissionDTO {
    private int submissionId;       // 문제제출 ID (PK)
    private int quizId;             // 문제 ID (FK)
    private String answer;          // 답
}
