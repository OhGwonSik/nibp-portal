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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.PageInfo;

import egovframework.admin.admin600.domain.Admin617DTO;
import egovframework.admin.admin600.domain.Admin617DeleteDTO;
import egovframework.admin.admin600.domain.Admin617FilterDTO;
import egovframework.admin.admin600.domain.Admin617VO;
import egovframework.admin.admin600.service.Admin617Service;
import egovframework.common.annotation.CheckMenuPermission;
import egovframework.common.annotation.LogParam;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.component.EgovMapComponent;
import egovframework.common.enums.PermissionType;
import egovframework.common.excel.domain.ExcelExportResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName : Admin617Controller.java
 * @Description : 카드뉴스 관리 controller
 *
 * @author : balee
 * @since : 2025. 11. 27
 * @version : 1.0
 *
 * << 개정이력(Modification Information) >>
 *
 *      수정일         수정자              수정내용
 *  -------------  ------------   ---------------------
 *   2025. 11. 27     balee             최초 생성
 *
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/admin617")
@PreAuthorize("hasRole('ADMIN')")
public class Admin617Controller {
	private final Admin617Service admin617Service;
	private final EgovMapComponent egovMapComponent;

	/**
	 * 카드뉴스 목록 조회
	 *
	 * @param filter Admin617FilterDTO
	 * @return ResponseEntity<ApiResponse<PageInfo<Admin617VO>>>
	 */
	@PostMapping("/list/filter")
	@CheckMenuPermission(permission = PermissionType.READ)
	public ResponseEntity<ApiResponse<PageInfo<Admin617VO>>> selectAdmin617List(@LogParam @Valid @RequestBody Admin617FilterDTO filter) {
		PageInfo<Admin617VO> response = admin617Service.selectAdmin617List(filter);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 카드뉴스 단건 조회
	 *
	 * @param cardNewsOid       카드뉴스번호
	 * @return ResponseEntity<ApiResponse<admin617List>>
	 */
	@GetMapping("/select")
	@CheckMenuPermission(permission = PermissionType.READ)
	public ResponseEntity<ApiResponse<Admin617VO>> selectAdmin617(@LogParam @RequestParam("cardNewsOid") Long cardNewsOid) {
		Admin617VO admin617 = admin617Service.selectAdmin617(cardNewsOid);

		if (admin617 == null) {
			return new ResponseEntity<>(ApiResponse.error(HttpStatus.NOT_FOUND, "카드뉴스 정보를 찾을 수 없습니다."), HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(ApiResponse.success(admin617), HttpStatus.OK);
	}

	/**
	 * 카드뉴스 등록
	 *
	 * @param admin617dto       Admin617DTO
	 * @return ResponseEntity<ApiResponse<Void>>
	 */
	@PutMapping("/insert")
	@CheckMenuPermission(permission = PermissionType.WRITE)
	public ResponseEntity<ApiResponse<T>> insertAdmin617(@LogParam @RequestPart("data") @Valid Admin617DTO admin617dto,
														@RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
														@RequestPart(value = "files", required = false) List<MultipartFile> files,
														@AuthenticationPrincipal BaseUser user ) {
		// 사용자 ID를 등록자,수정자 ID로 설정
		if (user != null) {
			admin617dto.setRegId(user.getUserId());
			admin617dto.setMdfcnId(user.getUserId());
		}

		log.info("카드 뉴스 (admin617-1) 파일 업로드 테스트 컨트롤러 admin617dto ==================> {}", admin617dto);
		log.info("카드 뉴스 (admin617-1) 파일 업로드 테스트 컨트롤러 files ==================> {}", files);

		admin617Service.insertAdmin617(admin617dto, thumbnailFile, files);
		return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
	}

	/**
	 * 카드뉴스 수정
	 *
	 * @param admin617dto       Admin617DTO
	 * @return ResponseEntity<ApiResponse<Void>>
	 */
	@PatchMapping("/update")
	@CheckMenuPermission(permission = PermissionType.WRITE)
	public ResponseEntity<ApiResponse<T>> updateAdmin617(@LogParam @RequestPart("data") @Valid Admin617DTO admin617dto,
														 @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
														 @RequestPart(value = "files", required = false) List<MultipartFile> files,
														 @AuthenticationPrincipal BaseUser user) {
		// 사용자 ID를 수정자 ID로 설정
		if (user != null) {
			admin617dto.setMdfcnId(user.getUserId());
		}
		admin617Service.updateAdmin617(admin617dto, thumbnailFile, files);
		return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
	}

	/**
	 * 카드뉴스 삭제
	 *
	 * @param admin617DeleteDTO    Admin617DeleteDTO
	 * @return ResponseEntity<ApiResponse<Void>>
	 */
	@PatchMapping("/delete")
	@CheckMenuPermission(permission = PermissionType.DELETE)
	public ResponseEntity<ApiResponse<T>> deleteAdmin617(@LogParam @RequestBody Admin617DeleteDTO admin617DeleteDTO,
														 @AuthenticationPrincipal BaseUser user) {
		// 사용자 ID를 수정자 ID로 설정
		if (user != null) {
			admin617DeleteDTO.setMdfcnId(user.getUserId());
		}
		admin617Service.deleteAdmin617(admin617DeleteDTO);
		return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
	}

	/**
	 * 카드뉴스 엑셀 다운로드
	 *
	 * @param params EgovMap
	 * @return ResponseEntity<byte[]>
	 */
	@PostMapping("/list/excel/export")
	@CheckMenuPermission(permission = PermissionType.EXCEL)
	public ResponseEntity<byte[]> admin617ExportExcel(@LogParam @RequestBody EgovMap params) throws IOException{
		EgovMap cond = egovMapComponent.convertToEgovMap(params);
        if(cond.get("reason") == null || cond.get("reason").toString().trim().length() < 2) {
            throw new IllegalArgumentException("사유는 2자 이상 입력해주세요.");
        }
		ExcelExportResult result = admin617Service.admin617ExportExcel(cond);

		String strgFileNm = result.getFileName();
		String encodedStrgFileNm = URLEncoder.encode(strgFileNm, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
		String contentDisposition = "attachment; filename*=UTF-8''" + encodedStrgFileNm;

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(result.getBytes());

	}
}
