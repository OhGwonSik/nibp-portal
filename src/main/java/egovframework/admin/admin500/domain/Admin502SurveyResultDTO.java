package egovframework.admin.admin500.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Admin502SurveyResultDTO {

	// 1. 설문 기본 정보
    private String srvyTtl;
    private String surveyPeriod;
    private int targetCount;
    private int participantCount;
    private double participationRate;

    // 2. 질문 목록
    private List<Admin502QuestionDTO> questions = new ArrayList<>();
}
