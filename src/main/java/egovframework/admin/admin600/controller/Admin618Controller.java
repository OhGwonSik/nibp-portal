package egovframework.admin.admin600.controller;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin600.domain.Admin618DTO;
import egovframework.admin.admin600.domain.Admin618DeleteDTO;
import egovframework.admin.admin600.domain.Admin618FilterDTO;
import egovframework.admin.admin600.domain.Admin618VO;
import egovframework.admin.admin600.service.Admin618Service;
import egovframework.common.annotation.CheckMenuPermission;
import egovframework.common.annotation.LogParam;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.component.EgovMapComponent;
import egovframework.common.api.ApiResponse;
import egovframework.common.enums.PermissionType;
import egovframework.common.excel.domain.ExcelExportResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @ClassName : Admin618Controller.java
 * @Description : 팝업존 관리 controller
 *
 * @author : j.h.kim
 * @since : 2026. 01. 07
 * @version : 1.0
 *
 * << 개정이력(Modification Information) >>
 *
 *      수정일         수정자              수정내용
 *  -------------  ------------   ---------------------
 *   2026. 01. 07     j.h.kim       최초 생성
 *
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/admin618")
@PreAuthorize("hasRole('ADMIN')")
public class Admin618Controller {
	private final Admin618Service admin618Service;
	private final EgovMapComponent egovMapComponent;

	/**
	 * 팝업존 목록 조회
	 *
	 * @param filter Admin618FilterDTO
	 * @return ResponseEntity<ApiResponse<PageInfo<Admin618VO>>>
	 */
	@PostMapping("/list/filter")
	@CheckMenuPermission(permission = PermissionType.READ)
	public ResponseEntity<ApiResponse<PageInfo<Admin618VO>>> selectAdmin618List(@LogParam @Valid @RequestBody Admin618FilterDTO filter) {
		PageInfo<Admin618VO> response = admin618Service.selectAdmin618List(filter);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 팝업존 단건 조회
	 *
	 * @param popupZoneOid       팝업존번호
	 * @return ResponseEntity<ApiResponse<admin618List>>
	 */
	@GetMapping("/select")
	@CheckMenuPermission(permission = PermissionType.READ)
	public ResponseEntity<ApiResponse<Admin618VO>> selectAdmin618(@LogParam @RequestParam("popupZoneOid") Long popupZoneOid) {
		Admin618VO admin618 = admin618Service.selectAdmin618(popupZoneOid);

		if (admin618 == null) {
			return new ResponseEntity<>(ApiResponse.error(HttpStatus.NOT_FOUND, "팝업존 정보를 찾을 수 없습니다."), HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(ApiResponse.success(admin618), HttpStatus.OK);
	}

	/**
	 * 팝업존 등록
	 *
	 * @param admin618dto       Admin618DTO
	 * @return ResponseEntity<ApiResponse<Void>>
	 */
	@PutMapping("/insert")
	@CheckMenuPermission(permission = PermissionType.WRITE)
	public ResponseEntity<ApiResponse<T>> insertAdmin618(@LogParam @RequestPart("data") @Valid Admin618DTO admin618dto,
														@RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
														@RequestPart(value = "files", required = false) List<MultipartFile> files,
														@AuthenticationPrincipal BaseUser user ) {
		// 사용자 ID를 등록자,수정자 ID로 설정
		if (user != null) {
			admin618dto.setRegId(user.getUserId());
			admin618dto.setMdfcnId(user.getUserId());
		}

		log.info("팝업존 (admin618-1) 파일 업로드 테스트 컨트롤러 admin618dto ==================> {}", admin618dto);
		log.info("팝업존 (admin618-1) 파일 업로드 테스트 컨트롤러 files ==================> {}", files);

		admin618Service.insertAdmin618(admin618dto, thumbnailFile, files);
		return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
	}

	/**
	 * 팝업존 수정
	 *
	 * @param admin618dto       Admin618DTO
	 * @return ResponseEntity<ApiResponse<Void>>
	 */
	@PatchMapping("/update")
	@CheckMenuPermission(permission = PermissionType.WRITE)
	public ResponseEntity<ApiResponse<T>> updateAdmin618(@LogParam @RequestPart("data") @Valid Admin618DTO admin618dto,
														 @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
														 @RequestPart(value = "files", required = false) List<MultipartFile> files,
														 @AuthenticationPrincipal BaseUser user) {
		// 사용자 ID를 수정자 ID로 설정
		if (user != null) {
			admin618dto.setMdfcnId(user.getUserId());
		}
		admin618Service.updateAdmin618(admin618dto, thumbnailFile, files);
		return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
	}

	/**
	 * 팝업존 삭제
	 *
	 * @param admin618DeleteDTO    Admin618DeleteDTO
	 * @return ResponseEntity<ApiResponse<Void>>
	 */
	@PatchMapping("/delete")
	@CheckMenuPermission(permission = PermissionType.DELETE)
	public ResponseEntity<ApiResponse<T>> deleteAdmin618(@LogParam @RequestBody Admin618DeleteDTO admin618DeleteDTO,
														 @AuthenticationPrincipal BaseUser user) {
		// 사용자 ID를 수정자 ID로 설정
		if (user != null) {
			admin618DeleteDTO.setMdfcnId(user.getUserId());
		}
		admin618Service.deleteAdmin618(admin618DeleteDTO);
		return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
	}

	/**
	 * 팝업존 엑셀 다운로드
	 *
	 * @param params EgovMap
	 * @return ResponseEntity<byte[]>
	 */
	@PostMapping("/list/excel/export")
	@CheckMenuPermission(permission = PermissionType.EXCEL)
	public ResponseEntity<byte[]> admin618ExportExcel(@LogParam @RequestBody EgovMap params) throws IOException{
		EgovMap cond = egovMapComponent.convertToEgovMap(params);
        if(cond.get("reason") == null || cond.get("reason").toString().trim().length() < 2) {
            throw new IllegalArgumentException("사유는 2자 이상 입력해주세요.");
        }
		ExcelExportResult result = admin618Service.admin618ExportExcel(cond);

		String fileName = result.getFileName();
		String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
		String contentDisposition = "attachment; filename*=UTF-8''" + encodedFileName;

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(result.getBytes());

	}
}
