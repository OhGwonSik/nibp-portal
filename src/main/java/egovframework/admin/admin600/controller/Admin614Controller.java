package egovframework.admin.admin600.controller;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin600.domain.Admin614DTO;
import egovframework.admin.admin600.domain.Admin614DeleteDTO;
import egovframework.admin.admin600.domain.Admin614FilterDTO;
import egovframework.admin.admin600.domain.Admin614VO;
import egovframework.admin.admin600.service.Admin614Service;
import egovframework.common.annotation.CheckMenuPermission;
import egovframework.common.annotation.LogParam;
import egovframework.common.api.ApiResponse;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.component.EgovMapComponent;
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

/**
 * @ClassName : Admin614Controller.java
 * @Description : 안내(팝업) 관리 controller
 *
 * @author : balee
 * @since : 2025. 11. 24
 * @version : 1.0
 *
 * << 개정이력(Modification Information) >>
 *
 *      수정일         수정자              수정내용
 *  -------------  ------------   ---------------------
 *   2025. 11. 24     balee             최초 생성
 *
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/admin614")
@PreAuthorize("hasRole('ADMIN')")
public class Admin614Controller {
	private final Admin614Service admin614Service;
	private final EgovMapComponent egovMapComponent;

	/**
	 * 안내(팝업) 목록 조회
	 *
	 * @param filter Admin614FilterDTO
	 * @return ResponseEntity<ApiResponse<PageInfo<Admin614VO>>>
	 */
	@PostMapping("/list/filter")
	@CheckMenuPermission(permission = PermissionType.READ)
	public ResponseEntity<ApiResponse<PageInfo<Admin614VO>>> selectAdmin614List(@LogParam @Valid @RequestBody Admin614FilterDTO filter) {
		PageInfo<Admin614VO> response = admin614Service.selectAdmin614List(filter);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 안내(팝업) 단건 조회
	 *
	 * @param popupOid       팝업번호
	 * @return ResponseEntity<ApiResponse<admin614List>>
	 */
	@GetMapping("/select")
	@CheckMenuPermission(permission = PermissionType.READ)
	public ResponseEntity<ApiResponse<Admin614VO>> selectAdmin614(@LogParam @RequestParam("popupOid") String popupOid) {
		Admin614VO admin614 = admin614Service.selectAdmin614(popupOid);

		if (admin614 == null) {
			return new ResponseEntity<>(ApiResponse.error(HttpStatus.NOT_FOUND, "팝업 정보를 찾을 수 없습니다."), HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(ApiResponse.success(admin614), HttpStatus.OK);
	}

	/**
	 * 안내(팝업) 등록
	 *
	 * @param admin614dto       Admin614DTO
	 * @return ResponseEntity<ApiResponse<Void>>
	 */
	@PutMapping("/insert")
	@CheckMenuPermission(permission = PermissionType.WRITE)
	public ResponseEntity<ApiResponse<T>> insertAdmin614(@LogParam @RequestPart("data") @Valid Admin614DTO admin614dto,
														@RequestPart(value = "popupFile", required = false) MultipartFile popupFile,
														@AuthenticationPrincipal BaseUser user ) throws IOException {
		// 사용자 ID를 등록자,수정자 ID로 설정
		if (user != null) {
			admin614dto.setRegId(user.getUserId());
			admin614dto.setMdfcnId(user.getUserId());
		}
		admin614Service.insertAdmin614(admin614dto, popupFile);
		return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
	}

	/**
	 * 안내(팝업) 수정
	 *
	 * @param admin614dto       Admin614DTO
	 * @return ResponseEntity<ApiResponse<Void>>
	 */
	@PatchMapping("/update")
	@CheckMenuPermission(permission = PermissionType.WRITE)
	public ResponseEntity<ApiResponse<T>> updateAdmin614(@LogParam @RequestPart("data") @Valid Admin614DTO admin614dto,
														@RequestPart(value = "popupFile", required = false) MultipartFile popupFile,
														 @AuthenticationPrincipal BaseUser user) throws IOException {
		// 사용자 ID를 수정자 ID로 설정
		if (user != null) {
			admin614dto.setMdfcnId(user.getUserId());
		}
		admin614Service.updateAdmin614(admin614dto, popupFile);
		return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
	}

	/**
	 * 안내(팝업) 삭제
	 *
	 * @param admin614DeleteDTO    Admin614DeleteDTO
	 * @return ResponseEntity<ApiResponse<Void>>
	 */
	@PatchMapping("/delete")
	@CheckMenuPermission(permission = PermissionType.DELETE)
	public ResponseEntity<ApiResponse<T>> deleteAdmin614(@LogParam @RequestBody Admin614DeleteDTO admin614DeleteDTO,
														 @AuthenticationPrincipal BaseUser user) {
		// 사용자 ID를 수정자 ID로 설정
		if (user != null) {
			admin614DeleteDTO.setMdfcnId(user.getUserId());
		}
		admin614Service.deleteAdmin614(admin614DeleteDTO);
		return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
	}

	/**
	 * 안내(팝업) 엑셀 다운로드
	 *
	 * @param params EgovMap
	 * @return ResponseEntity<byte[]>
	 */
	@PostMapping("/list/excel/export")
	@CheckMenuPermission(permission = PermissionType.EXCEL)
	public ResponseEntity<byte[]> admin614ExportExcel(@LogParam @RequestBody EgovMap params) throws IOException {
		EgovMap cond = egovMapComponent.convertToEgovMap(params);
        if(cond.get("reason") == null || cond.get("reason").toString().trim().length() < 2) {
            throw new IllegalArgumentException("사유는 2자 이상 입력해주세요.");
        }
		ExcelExportResult result = admin614Service.admin614ExportExcel(cond);

		String strgFileNm = result.getFileName();
		String encodedStrgFileNm = URLEncoder.encode(strgFileNm, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
		String contentDisposition = "attachment; filename*=UTF-8''" + encodedStrgFileNm;

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(result.getBytes());

	}

}