package egovframework.admin.admin600.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import egovframework.common.api.ApiResponse;
import org.apache.poi.ss.formula.functions.T;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.PageInfo;

import egovframework.admin.admin600.domain.Admin601DetailDTO;
import egovframework.admin.admin600.domain.Admin601SaveDTO;
import egovframework.admin.admin600.domain.Admin601VO;
import egovframework.admin.admin600.service.Admin601Service;
import egovframework.common.annotation.CheckMenuPermission;
import egovframework.common.annotation.LogParam;
import egovframework.common.component.EgovMapComponent;
import egovframework.common.enums.PermissionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/admin601/notice")
@PreAuthorize("hasRole('ADMIN')")
public class Admin601Controller {
	private final Admin601Service admin601Service;
	private final EgovMapComponent egovMapComponent;

    @GetMapping("/list") // 간단한 조회
    @CheckMenuPermission(permission = PermissionType.READ)
    public ResponseEntity<ApiResponse<PageInfo<Admin601VO>>> selectNoticeList(@LogParam @RequestParam Map<String, Object> params) {
        EgovMap egovMap = egovMapComponent.convertToEgovMap(params);
        PageInfo<Admin601VO> noticeList = admin601Service.selectNoticeList(egovMap);
        return new ResponseEntity<>(ApiResponse.success(noticeList), HttpStatus.OK);
    }

    @GetMapping("/{ntcOid}")
    @CheckMenuPermission(permission = PermissionType.READ)
    public ResponseEntity<ApiResponse<Admin601DetailDTO>> selectNotice(@LogParam @PathVariable("ntcOid") Long ntcOid) {
            Admin601DetailDTO notice = admin601Service.selectNotice(ntcOid);
            return new ResponseEntity<>(ApiResponse.success(notice), HttpStatus.OK);
    }

	@PostMapping("/save")
    @CheckMenuPermission(permission = PermissionType.WRITE)
	public ResponseEntity<ApiResponse<T>> saveNotice(
        @LogParam @RequestPart("data") @Valid Admin601SaveDTO admin601SaveDTO,
        @RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {

        EgovMap egovMap = new EgovMap();
        egovMap.put("admin601SaveDTO", admin601SaveDTO);
        egovMap.put("uploadFiles", files);

        admin601Service.insertNotice(egovMap);

        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
	}

    @PatchMapping("/update")
    @CheckMenuPermission(permission = PermissionType.WRITE)
    public ResponseEntity<ApiResponse<Integer>> updateNotice(@LogParam @RequestPart("data") @Valid Admin601SaveDTO admin601SaveDTO,
                                                              @RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {
        EgovMap egovMap = new EgovMap();
        egovMap.put("admin601SaveDTO", admin601SaveDTO);
        egovMap.put("uploadFiles", files);

        int result = admin601Service.updateNotice(egovMap);
        return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
    }

    @DeleteMapping("/{ntcOid}")
    @CheckMenuPermission(permission = PermissionType.DELETE)
    public ResponseEntity<ApiResponse<Integer>> deleteNotice(@LogParam @PathVariable("ntcOid") Long ntcOid) {
        int result = admin601Service.deleteNotice(ntcOid);
        return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
    }

    @PatchMapping("/inq-cnt/{ntcOid}")
    @CheckMenuPermission(permission = PermissionType.READ)
    public ResponseEntity<ApiResponse<T>> updateNoticeInqCnt(@LogParam @PathVariable("ntcOid") Long ntcOid) {
        admin601Service.updateNoticeInqCnt(ntcOid);
        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
    }
}
