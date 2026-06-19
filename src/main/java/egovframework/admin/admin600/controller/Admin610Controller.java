package egovframework.admin.admin600.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.validation.Valid;

import egovframework.common.api.ApiResponse;
import org.apache.poi.ss.formula.functions.T;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.PageInfo;

import egovframework.admin.admin600.domain.Admin610DTO;
import egovframework.admin.admin600.domain.Admin610DeleteDTO;
import egovframework.admin.admin600.domain.Admin610FilterDTO;
import egovframework.admin.admin600.domain.Admin610VO;
import egovframework.admin.admin600.service.Admin610Service;
import egovframework.common.annotation.CheckMenuPermission;
import egovframework.common.annotation.LogParam;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.component.EgovMapComponent;
import egovframework.common.enums.PermissionType;
import egovframework.common.excel.domain.ExcelExportResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName : Admin610Controller.java
 * @Description : Q&A 관리 controller
 *
 * @author : balee
 * @since : 2025. 11. 25
 * @version : 1.0
 *
 * << 개정이력(Modification Information) >>
 *
 *      수정일         수정자              수정내용
 *  -------------  ------------   ---------------------
 *   2025. 11. 25     balee             최초 생성
 *
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/admin610")
@PreAuthorize("hasRole('ADMIN')")
public class Admin610Controller {
	private final Admin610Service admin610Service;
	private final EgovMapComponent egovMapComponent;

	/**
	 * Q&A 목록 조회
	 *
	 * @param filter Admin610FilterDTO
	 * @return ResponseEntity<ApiResponse<PageInfo<Admin610VO>>>
	 */
	@PostMapping("/list/filter")
	@CheckMenuPermission(permission = PermissionType.READ)
	public ResponseEntity<ApiResponse<PageInfo<Admin610VO>>> selectAdmin610List(@LogParam @Valid @RequestBody Admin610FilterDTO filter) {
		PageInfo<Admin610VO> response = admin610Service.selectAdmin610List(filter);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * Q&A 단건 조회
	 *
	 * @param qnaOid       Q&A번호
	 * @return ResponseEntity<ApiResponse<admin610List>>
	 */
	@GetMapping("/select")
	@CheckMenuPermission(permission = PermissionType.READ)
	public ResponseEntity<ApiResponse<Admin610VO>> selectAdmin610(@LogParam @RequestParam("qnaOid") Long qnaOid) {
		Admin610VO admin610 = admin610Service.selectAdmin610(qnaOid);

		if (admin610 == null) {
			return new ResponseEntity<>(ApiResponse.error(HttpStatus.NOT_FOUND, "Q&A 정보를 찾을 수 없습니다."), HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(ApiResponse.success(admin610), HttpStatus.OK);
	}

	/**
	 * Q&A 저장
	 *
	 * @param admin610dto       Admin610DTO
	 * @return ResponseEntity<ApiResponse<Void>>
	 */
	@PostMapping("/upsert")
	@CheckMenuPermission(permission = PermissionType.WRITE)
	public ResponseEntity<ApiResponse<T>> upsertAdmin610(@LogParam @RequestPart("data") @Valid Admin610DTO admin610dto,
														@RequestPart(value = "files", required = false) List<MultipartFile> files,
														 @AuthenticationPrincipal BaseUser user) {
		// 답변 작성자 ID, 답변 작성자명 설정
		if (user != null) {
			admin610dto.setUserId(user.getUserId());
			admin610dto.setWrtNm(user.getUserNmKorn());
		}
		admin610Service.upsertAdmin610(admin610dto, files);
		return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
	}

	/**
	 * Q&A 삭제
	 *
	 * @param admin610DeleteDTO    Admin610DeleteDTO
	 * @return ResponseEntity<ApiResponse<Void>>
	 */
	@PatchMapping("/delete")
	@CheckMenuPermission(permission = PermissionType.DELETE)
	public ResponseEntity<ApiResponse<T>> deleteAdmin610(@LogParam @RequestBody Admin610DeleteDTO admin610DeleteDTO,
														 @AuthenticationPrincipal BaseUser user) {
		// 사용자 ID를 작성자 ID로 설정
		/*if (user != null) {
			admin610DeleteDTO.setUserId(user.getUserId());
		}*/
		admin610Service.deleteAdmin610(admin610DeleteDTO);
		return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
	}

	/**
	 * Q&A 엑셀 다운로드
	 *
	 * @param params    EgovMap
	 * @return ResponseEntity<byte[]>
	 */
	@PostMapping("/list/excel/export")
	@CheckMenuPermission(permission = PermissionType.EXCEL)
	public ResponseEntity<byte[]> admin610ExportExcel(@LogParam @RequestBody EgovMap params) throws IOException {
		EgovMap cond = egovMapComponent.convertToEgovMap(params);
        if(cond.get("reason") == null || cond.get("reason").toString().trim().length() < 2) {
            throw new IllegalArgumentException("사유는 2자 이상 입력해주세요.");
        }
		ExcelExportResult result = admin610Service.admin610ExportExcel(cond);

		String strgFileNm = result.getFileName();
		String encodedStrgFileNm = URLEncoder.encode(strgFileNm, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
		String contentDisposition = "attachment; filename*=UTF-8''" + encodedStrgFileNm;

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(result.getBytes());
	}
}
