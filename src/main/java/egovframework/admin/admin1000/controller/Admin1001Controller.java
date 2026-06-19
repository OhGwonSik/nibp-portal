package egovframework.admin.admin1000.controller;

import com.github.pagehelper.PageInfo;
import egovframework.admin.admin1000.domain.Admin1001DeleteDTO;
import egovframework.admin.admin1000.domain.Admin1001DTO;
import egovframework.admin.admin1000.domain.Admin1001FilterDTO;
import egovframework.admin.admin1000.domain.Admin1001VO;
import egovframework.admin.admin1000.service.Admin1001Service;
import egovframework.common.annotation.LogParam;
import egovframework.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName : Admin1001Controller.java
 * @Description : 부서 관리 Controller
 *
 * @author : j.h.kim
 * @since  : 2026. 01. 07
 * @version : 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/admin1001")
@RequiredArgsConstructor
public class Admin1001Controller {
    
    private final Admin1001Service admin1001Service;

    /**
     * 부서 목록 조회 (페이징)
     */
    @PostMapping("/list/filter")
    public ResponseEntity<ApiResponse<PageInfo<Admin1001VO>>> selectAdmin1001List(@LogParam @RequestBody Admin1001FilterDTO filter) {
        PageInfo<Admin1001VO> result = admin1001Service.selectAdmin1001List(filter);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 부서 트리 전체 조회
     */
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<Admin1001VO>>> selectAdmin1001Tree() {
        List<Admin1001VO> result = admin1001Service.selectAdmin1001Tree();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 부서 상세 조회
     */
    @GetMapping("/select")
    public ResponseEntity<ApiResponse<Admin1001VO>> selectAdmin1001(@LogParam @RequestParam Long deptOid) {
        Admin1001VO result = admin1001Service.selectAdmin1001(deptOid);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 부서 등록
     */
    @PutMapping("/insert")
    public ResponseEntity<ApiResponse<Void>> insertAdmin1001(@LogParam @RequestBody Admin1001DTO dto) {
        admin1001Service.insertAdmin1001(dto);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 부서 수정
     */
    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<Void>> updateAdmin1001(@LogParam @RequestBody Admin1001DTO dto) {
        admin1001Service.updateAdmin1001(dto);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 부서 삭제
     */
    @PatchMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteAdmin1001(@LogParam@RequestBody Admin1001DeleteDTO dto) {
        admin1001Service.deleteAdmin1001(dto);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 정렬 순서 변경
     */
    @PatchMapping("/order")
    public ResponseEntity<ApiResponse<Void>> updateSortSeq(@LogParam @RequestBody Admin1001DTO dto) {
        admin1001Service.updateSortSeq(dto);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
