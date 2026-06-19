package egovframework.admin.admin1000.controller;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin1000.domain.*;
import egovframework.admin.admin1000.service.Admin1002Service;
import egovframework.common.annotation.LogParam;
import egovframework.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName : Admin1002Controller.java
 * @Description : 부서 구성원 관리 Controller
 *
 * @author : j.h.kim
 * @since  : 2026. 01. 07
 * @version : 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/admin1002")
@RequiredArgsConstructor
public class Admin1002Controller {
    
    private final Admin1002Service admin1002Service;

    /**
     * 구성원 목록 조회
     */
    @PostMapping("/list/filter")
    public ResponseEntity<ApiResponse<PageInfo<Admin1002VO>>> selectAdmin1002List(@LogParam @RequestBody Admin1002FilterDTO filter) {
        PageInfo<Admin1002VO> result = admin1002Service.selectAdmin1002List(filter);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 구성원 상세 조회
     */
    @GetMapping("/select")
    public ResponseEntity<ApiResponse<Admin1002VO>> selectAdmin1002(@LogParam @RequestParam Long deptMmbrOid) {
        Admin1002VO result = admin1002Service.selectAdmin1002(deptMmbrOid);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 구성원 추가
     */
    @PutMapping("/insert")
    public ResponseEntity<ApiResponse<Void>> insertAdmin1002(@LogParam @RequestBody Admin1002DTO dto) {
        admin1002Service.insertAdmin1002(dto);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 구성원 수정
     */
    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<Void>> updateAdmin1002(@LogParam@RequestBody Admin1002DTO dto) {
        admin1002Service.updateAdmin1002(dto);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 구성원 삭제
     */
    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteAdmin1002(@LogParam @RequestBody Admin1002DeleteDTO dto) {
        admin1002Service.deleteAdmin1002(dto);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 표시 순서 변경
     */
    @PatchMapping("/order")
    public ResponseEntity<ApiResponse<Void>> updateDisplayOrder(@LogParam @RequestBody Admin1002DTO dto) {
        admin1002Service.updateDisplayOrder(dto);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
