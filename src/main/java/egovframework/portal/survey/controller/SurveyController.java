package egovframework.portal.survey.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.github.pagehelper.PageInfo;

import egovframework.common.api.ApiResponse;
import egovframework.portal.survey.domain.PortalSurveyDTO;
import egovframework.portal.survey.domain.SurveyInfoDTO;
import egovframework.portal.survey.domain.SurveyInfoFilterDTO;
import egovframework.portal.survey.domain.SurveyParticipateDTO;
import egovframework.portal.survey.domain.SurveySubmitReqDTO;
import egovframework.portal.survey.service.SurveyService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common/survey")
public class SurveyController {

	private final SurveyService surveyService;
	
	@GetMapping("/list/filter")
    public ResponseEntity<ApiResponse<PageInfo<SurveyInfoDTO>>> selectSurveyListWithFilter(@ModelAttribute SurveyInfoFilterDTO surveyInfoFilterDTO) {
        PageInfo<SurveyInfoDTO> surveyList = surveyService.selectSurveyListWithFilter(surveyInfoFilterDTO);
        return ResponseEntity.ok(ApiResponse.success(surveyList));
    }
	
	@PostMapping("/check")
    public ResponseEntity<ApiResponse<String>> selectSurveyParticipatedYn(@RequestBody @Valid SurveyParticipateDTO surveyParticipateDTO) {
		String surveyInsertResult = surveyService.selectSurveyParticipatedYn(surveyParticipateDTO);
        return ResponseEntity.ok(ApiResponse.success(surveyInsertResult));
    }
	
	@GetMapping("/detail")
    public ResponseEntity<ApiResponse<PortalSurveyDTO>> selectSurveyDetail(@ModelAttribute SurveyInfoFilterDTO SurveyInfoFilterDTO) {
        PortalSurveyDTO surveyDetail = surveyService.selectSurveyDetail(SurveyInfoFilterDTO);
        return ResponseEntity.ok(ApiResponse.success(surveyDetail));
    }
	
	@PostMapping(value = "/insert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Integer>> insertSurveyResponse(
            @RequestPart("data") SurveySubmitReqDTO surveySubmitReqDTO,
            MultipartHttpServletRequest request // 동적 파일 키(file_15 등)를 받기 위한 요청 객체
    ) throws IOException { // 파일 처리 중 발생할 수 있는 예외 선언
        
        // 요청에서 파일들을 추출하여 Map으로 정리
        // Key: "file_12", Value: 파일객체
        Map<String, MultipartFile> fileMap = new HashMap<>();
        Iterator<String> fileNames = request.getFileNames();
        
        while (fileNames.hasNext()) {
            String key = fileNames.next();
            // 프론트엔드에서 보낸 규칙("file_")로 시작하는 파일만 추출
            if (key.startsWith("file_")) {
                MultipartFile file = request.getFile(key);
                if (file != null && !file.isEmpty()) {
                    fileMap.put(key, file);
                }
            }
        }
        Integer surveyInsertResult = surveyService.insertSurveyResponse(surveySubmitReqDTO, fileMap);
        
        return ResponseEntity.ok(ApiResponse.success(surveyInsertResult));
    }	
}
