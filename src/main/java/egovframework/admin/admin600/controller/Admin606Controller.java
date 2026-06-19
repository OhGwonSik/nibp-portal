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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.PageInfo;

import egovframework.admin.admin600.domain.Admin606CategoryVO;
import egovframework.admin.admin600.domain.Admin606DeleteDTO;
import egovframework.admin.admin600.domain.Admin606DetailDTO;
import egovframework.admin.admin600.domain.Admin606FilterDTO;
import egovframework.admin.admin600.domain.Admin606SaveDTO;
import egovframework.admin.admin600.domain.Admin606VO;
import egovframework.admin.admin600.service.Admin606Service;
import egovframework.common.annotation.CheckMenuPermission;
import egovframework.common.annotation.LogParam;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.component.EgovMapComponent;
import egovframework.common.enums.PermissionType;
import egovframework.common.excel.domain.ExcelExportResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName : Admin606Controller.java
 * @Description : FAQ 관리 controller
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
@RequestMapping("/api/admin/admin606")
@PreAuthorize("hasRole('ADMIN')")
public class Admin606Controller {
	private final Admin606Service admin606Service;
	private final EgovMapComponent egovMapComponent;

	/**
	 * FAQ 목록 조회
	 *
	 * @param filter Admin606FilterDTO
	 * @return ResponseEntity<ApiResponse<PageInfo<Admin606DetailVO>>>
	 */
	@PostMapping("/list/filter")
	@CheckMenuPermission(permission = PermissionType.READ)
	public ResponseEntity<ApiResponse<PageInfo<Admin606VO>>> selectAdmin606List(@LogParam @Valid @RequestBody Admin606FilterDTO filter) {
		PageInfo<Admin606VO> response = admin606Service.selectAdmin606List(filter);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * FAQ 카테고리 목록 조회
	 *
	 * @return ResponseEntity<ApiResponse<List<Admin606CategoryVO>>>
	 */
	@GetMapping("/category/list")
	@CheckMenuPermission(permission = PermissionType.READ)
	public ResponseEntity<ApiResponse<List<Admin606CategoryVO>>> selectAdmin606CategoryList() {
		List<Admin606CategoryVO> admin606CategoryList = admin606Service.selectAdmin606CategoryList();

		if (admin606CategoryList.isEmpty()) {
			return new ResponseEntity<>(ApiResponse.error(HttpStatus.NOT_FOUND, "FAQ 카테고리 정보를 찾을 수 없습니다."), HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(ApiResponse.success(admin606CategoryList), HttpStatus.OK);
	}

	/**
	 * FAQ 단건 조회
	 *
	 * @param faqDtlOid       Q&A번호
	 * @return ResponseEntity<ApiResponse<admin606List>>
	 */
	@GetMapping("/select")
	@CheckMenuPermission(permission = PermissionType.READ)
	public ResponseEntity<ApiResponse<Admin606DetailDTO>> selectAdmin606(@LogParam @RequestParam("faqDtlOid") Long faqDtlOid) {
			Admin606DetailDTO admin606DetailDTO = admin606Service.selectAdmin606(faqDtlOid);

			if (admin606DetailDTO == null) {
				return new ResponseEntity<>(ApiResponse.error(HttpStatus.NOT_FOUND, "FAQ 정보를 찾을 수 없습니다."), HttpStatus.NOT_FOUND);
			}

			return new ResponseEntity<>(ApiResponse.success(admin606DetailDTO), HttpStatus.OK);
	}

	/**
	 * FAQ 등록
	 *
	 * @param admin606DetailDTO       Admin606DetailDTO
	 * @return ResponseEntity<ApiResponse<Long>> 생성된 FAQ 번호
	 */
	@PutMapping("/insert")
	@CheckMenuPermission(permission = PermissionType.WRITE)
	public ResponseEntity<ApiResponse<Long>> insertAdmin606(@LogParam @RequestBody @Valid Admin606DetailDTO admin606DetailDTO,
														 @AuthenticationPrincipal BaseUser user) {
		// 사용자 ID를 등록자,수정자 ID로 설정
		if (user != null) {
			admin606DetailDTO.setRegId(user.getUserId());
			admin606DetailDTO.setMdfcnId(user.getUserId());
		}
		Long faqDtlOid = admin606Service.insertAdmin606(admin606DetailDTO);
		return new ResponseEntity<>(ApiResponse.success(faqDtlOid), HttpStatus.OK);
	}

	/**
	 * FAQ 수정
	 *
	 * @param admin606DetailDTO       Admin606DetailDTO
	 * @return ResponseEntity<ApiResponse<Void>>
	 */
	@PostMapping("/update")
	@CheckMenuPermission(permission = PermissionType.WRITE)
	public ResponseEntity<ApiResponse<T>> updateAdmin606(@LogParam @RequestBody @Valid Admin606DetailDTO admin606DetailDTO,
														 @AuthenticationPrincipal BaseUser user) {
		// 사용자 ID를 수정자 ID로 설정
		if (user != null) {
			admin606DetailDTO.setMdfcnId(user.getUserId());
		}
		admin606Service.updateAdmin606(admin606DetailDTO);
		return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
	}

	/**
	 * FAQ 삭제
	 *
	 * @param admin606DeleteDTO    Admin606DeleteDTO
	 * @return ResponseEntity<ApiResponse<Void>>
	 */
	@PatchMapping("/delete")
	@CheckMenuPermission(permission = PermissionType.DELETE)
	public ResponseEntity<ApiResponse<T>> deleteAdmin606(@LogParam @RequestBody @Valid Admin606DeleteDTO admin606DeleteDTO,
														 @AuthenticationPrincipal BaseUser user) {
		// 사용자 ID를 수정자 ID로 설정
		if (user != null) {
			admin606DeleteDTO.setMdfcnId(user.getUserId());
		}
		admin606Service.deleteAdmin606(admin606DeleteDTO);
		return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
	}

	/**
	 * FAQ 조회수 증가
	 *
	 * @param faqDtlOid FAQ 번호
	 * @return ResponseEntity<ApiResponse<Void>>
	 */
	@PatchMapping("/view-count/{faqDtlOid}")
	@CheckMenuPermission(permission = PermissionType.READ)
	public ResponseEntity<ApiResponse<T>> updateAdmin606inqCnt(@LogParam @PathVariable("faqDtlOid") Long faqDtlOid) {
		admin606Service.updateAdmin606inqCnt(faqDtlOid);
		return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
	}

	/**
	 * FAQ 엑셀 다운로드
	 *
	 * @param params EgovMap
	 * @return ResponseEntity<byte[]>
	 */
	@PostMapping("/list/excel/export")
	@CheckMenuPermission(permission = PermissionType.EXCEL)
	public ResponseEntity<byte[]> admin606ExportExcel(@LogParam @RequestBody EgovMap params) throws IOException {
		EgovMap cond = egovMapComponent.convertToEgovMap(params);
        if(cond.get("reason") == null || cond.get("reason").toString().trim().length() < 2) {
            throw new IllegalArgumentException("사유는 2자 이상 입력해주세요.");
        }
		ExcelExportResult result = admin606Service.admin606ExportExcel(cond);

		String fileName = result.getFileName();
		String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
		String contentDisposition = "attachment; filename*=UTF-8''" + encodedFileName;
		
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(result.getBytes());
	}

    @PostMapping("/faq/save")
    @CheckMenuPermission(permission = PermissionType.WRITE)
    public ResponseEntity<ApiResponse<T>> saveFaqData(
        @LogParam @RequestPart("data") @Valid Admin606SaveDTO admin606SaveDTO,
        @RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {

        EgovMap egovMap = new EgovMap();
        egovMap.put("admin606SaveDTO", admin606SaveDTO);
        egovMap.put("uploadFiles", files);

        admin606Service.insertFaqData(egovMap);

        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);

    }

    @PatchMapping("/faq/update")
    @CheckMenuPermission(permission = PermissionType.WRITE)
    public ResponseEntity<ApiResponse<Integer>> updateFaqData(
        @LogParam @RequestPart("data") @Valid Admin606SaveDTO admin606SaveDTO,
        @RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {
        EgovMap egovMap = new EgovMap();
        egovMap.put("admin606SaveDTO", admin606SaveDTO);
        egovMap.put("uploadFiles", files);

        int result = admin606Service.updateFaqData(egovMap);
        return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
    }
}
