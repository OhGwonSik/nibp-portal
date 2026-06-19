package egovframework.admin.admin500.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import egovframework.admin.admin500.domain.Admin502ExcelDTO;
import egovframework.admin.admin500.domain.Admin502RespondentDTO;

import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;

import egovframework.admin.admin500.domain.Admin502DTO;
import egovframework.admin.admin500.domain.Admin502SurveyResultDTO;
import egovframework.admin.admin500.domain.Admin502VO;
import egovframework.admin.admin500.service.Admin502Service;
import egovframework.common.annotation.CheckMenuPermission;
import egovframework.common.annotation.LogParam;
import egovframework.common.api.ApiResponse;
import egovframework.common.component.EgovMapComponent;
import egovframework.common.enums.PermissionType;
import egovframework.common.excel.domain.ExcelExportResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j // log
@RestController // json api
@RequiredArgsConstructor
@RequestMapping("/api/admin/admin502")
public class Admin502Controller {
	private final Admin502Service admin502Service;
	private final EgovMapComponent egovMapComponent;
	
	/**
	 * 조사결과 상세 조회
	 *
	 * @param admin502DTO       Admin502DTO
	 * @return ResponseEntity<ApiResponse<PageInfo<Admin810VO>>>
	 */
	@PostMapping("/survey/detail")
	@CheckMenuPermission(permission = PermissionType.READ)
	public ResponseEntity<ApiResponse<Admin502SurveyResultDTO>> selectAdmin502ResultList(@LogParam @RequestBody @Valid Admin502DTO admin502DTO) {
		Admin502SurveyResultDTO resultList = admin502Service.selectAdmin502ResultList(admin502DTO);
		return new ResponseEntity<>(ApiResponse.success(resultList), HttpStatus.OK);
	}

	@GetMapping("/survey/list")
	@CheckMenuPermission(permission = PermissionType.READ)
    public ResponseEntity<ApiResponse<PageInfo<Admin502VO>>> selectAdmin502List(@LogParam @RequestParam Map<String, Object> params) {
		EgovMap egovMap = egovMapComponent.convertToEgovMap(params);
		PageInfo<Admin502VO> admin502List = admin502Service.selectAdmin502List(egovMap);
		return new ResponseEntity<>(ApiResponse.success(admin502List), HttpStatus.OK);
    }
	
	@PostMapping("/survey/excel/export")
    @CheckMenuPermission(permission = PermissionType.EXCEL)
    public ResponseEntity<byte[]> admin502ExportExcel(@LogParam @RequestBody @Valid Admin502ExcelDTO params) throws IOException {
		ExcelExportResult result = admin502Service.admin502ExcelDownload(params);
		
		// 파일명 인코딩 (한글 깨짐 방지)
		String fileName = result.getFileName();
		String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
		String contentDisposition = "attachment; filename*=UTF-8''" + encodedFileName;
		
		// 바이트 배열 반환
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(result.getBytes());
	}
    
	@GetMapping("/survey/user/response")
	@CheckMenuPermission(permission = PermissionType.READ)
    public ResponseEntity<ApiResponse<List<Admin502RespondentDTO>>> selectSurveyRespondentList(@LogParam Admin502RespondentDTO admin502RespondentDTO) {
		List<Admin502RespondentDTO> admin502List = admin502Service.selectSurveyRespondentList(admin502RespondentDTO);
		return new ResponseEntity<>(ApiResponse.success(admin502List), HttpStatus.OK);
    }
	
	@DeleteMapping("/survey/user/response/delete")
	@CheckMenuPermission(permission = PermissionType.DELETE)
    public ResponseEntity<ApiResponse<Integer>> deleteSurveyRespondents(@RequestBody List<Admin502RespondentDTO> admin502RespondentList) {
		Integer result = admin502Service.deleteSurveyRespondents(admin502RespondentList);
		return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
    }    
}
