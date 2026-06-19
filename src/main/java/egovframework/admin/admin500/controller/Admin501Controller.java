package egovframework.admin.admin500.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.PageInfo;

import egovframework.admin.admin500.domain.Admin501SurveyDTO;
import egovframework.admin.admin500.domain.Admin501VO;
import egovframework.admin.admin500.domain.SurveySaveDTO;
import egovframework.admin.admin500.service.Admin501Service;
import egovframework.common.annotation.CheckMenuPermission;
import egovframework.common.annotation.LogParam;
import egovframework.common.api.ApiResponse;
import egovframework.common.component.EgovMapComponent;
import egovframework.common.enums.PermissionType;
import egovframework.common.excel.domain.ExcelExportResult;
import egovframework.portal.survey.domain.PortalSurveyDTO;
import egovframework.portal.survey.domain.SurveyInfoFilterDTO;
import egovframework.portal.survey.service.SurveyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j // log
@RestController // json api
@RequiredArgsConstructor
@RequestMapping("/api/admin/admin501/survey")
public class Admin501Controller {
	private final Admin501Service admin501Service;
	private final SurveyService surveyService;
	private final EgovMapComponent egovMapComponent;
	
	// 나중에 스웨거 추가할 수 도 있음.
	@GetMapping("/list") // 간단한 조회
	@CheckMenuPermission(permission = PermissionType.READ)
    public ResponseEntity<ApiResponse<PageInfo<Admin501VO>>> selectAdmin501List(@LogParam @RequestParam Map<String, Object> params) { // DTO로 받을 시 @ModelAttribute 및 해당 객체에 bean validation 적용할 것. 
		EgovMap egovMap = egovMapComponent.convertToEgovMap(params);
		PageInfo<Admin501VO> admin501List = admin501Service.selectAdmin501List(egovMap); // 상황에 따라 map 등 다른 형식 사용 가능
		return new ResponseEntity<>(ApiResponse.success(admin501List), HttpStatus.OK);
    }
	
	@GetMapping("/detail") // 간단한 조회
	@CheckMenuPermission(permission = PermissionType.READ)
    public ResponseEntity<ApiResponse<Admin501SurveyDTO>> selectAdmin501Detail(@LogParam @RequestParam Map<String, Object> params) { // DTO로 받을 시 @ModelAttribute 및 해당 객체에 bean validation 적용할 것. 
		EgovMap egovMap = egovMapComponent.convertToEgovMap(params);
		Admin501SurveyDTO admin501Detail = admin501Service.selectAdmin501Detail(egovMap); // 상황에 따라 map 등 다른 형식 사용 가능
		return new ResponseEntity<>(ApiResponse.success(admin501Detail), HttpStatus.OK);
    }	
	
	@GetMapping("/preview")
	@CheckMenuPermission(permission = PermissionType.READ)
    public ResponseEntity<ApiResponse<PortalSurveyDTO>> selectUserSurveyDetail(@ModelAttribute SurveyInfoFilterDTO surveyInfoFilterDTO) {
		PortalSurveyDTO surveyDetail = surveyService.selectSurveyDetail(surveyInfoFilterDTO);
        return ResponseEntity.ok(ApiResponse.success(surveyDetail));
    }		
	
	// 설문 저장 시 본문(JSON)과 문항 첨부파일을 동시에 받기 위해 multipart로 변경
	@PutMapping(value = "/insert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@CheckMenuPermission(permission = PermissionType.WRITE)
	public ResponseEntity<ApiResponse<Integer>> insertAdmin501Survey(
			@LogParam @RequestPart("survey") SurveySaveDTO params,
			@RequestPart(value = "questionFiles", required = false) List<MultipartFile> questionFiles,
			@RequestPart(value = "optionFiles", required = false) List<MultipartFile> optionFiles) throws IOException {
		int result = admin501Service.insertAdmin501Survey(params, questionFiles, optionFiles);
		return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
	}
	
	@PostMapping(value = "/upsert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@CheckMenuPermission(permission = PermissionType.WRITE)
    public ResponseEntity<ApiResponse<Integer>> upsertAdmin501List(
    		@LogParam @RequestPart("survey") SurveySaveDTO params,
    		@RequestPart(value = "questionFiles", required = false) List<MultipartFile> questionFiles,
    		@RequestPart(value = "optionFiles", required = false) List<MultipartFile> optionFiles) throws IOException {
		int result = admin501Service.upsertAdmin501(params, questionFiles, optionFiles);

		return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
    }
	
	@PostMapping("/excel/export")
    @CheckMenuPermission(permission = PermissionType.EXCEL)
    public ResponseEntity<byte[]> admin501ExportExcel(@LogParam @RequestBody EgovMap params) throws IOException {
            EgovMap cond = egovMapComponent.convertToEgovMap(params);
			if(cond.get("reason") == null || cond.get("reason").toString().trim().length() < 2) {
				throw new IllegalArgumentException("사유는 2자 이상 입력해주세요.");
			}

            ExcelExportResult result = admin501Service.admin501ExportExcel(cond);
            
            String fileName = result.getFileName();
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            String contentDisposition = "attachment; filename*=UTF-8''" + encodedFileName;
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                    		contentDisposition)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(result.getBytes());
    }
    
    @PutMapping("/copy")
    @CheckMenuPermission(permission = PermissionType.WRITE)
	public ResponseEntity<ApiResponse<Integer>> insertAdmin501SurveyCopy(@RequestBody Admin501VO params) {
		int result = admin501Service.insertAdmin501SurveyCopy(params);
		return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
	}
    
    @PatchMapping("/delete")
    @CheckMenuPermission(permission = PermissionType.DELETE)
	public ResponseEntity<ApiResponse<Integer>> updateAdmin501Stat(@RequestBody Admin501VO params) {
		int result = admin501Service.updateAdmin501Stat(params);
		return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
	}
    
    
}
