package egovframework.portal.periodical.controller;

import com.github.pagehelper.PageInfo;
import egovframework.common.api.ApiResponse;
import egovframework.portal.periodical.dto.Portal502DetailDTO;
import egovframework.portal.periodical.dto.Portal502Filter;
import egovframework.portal.periodical.dto.Portal502VO;
import egovframework.portal.periodical.service.Portal502Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName : Portal502Controller.java
 * @Description : 정기발간자료 Controller
 *
 * @author : j.h.kim
 * @since : 2025. 01. 13
 * @version : 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common/portal502")
public class Portal502Controller {
    
    private final Portal502Service portal502Service;
    
    /**
     * 정기발간자료 목록 조회
     *
     * @param filter Portal502Filter
     * @return ResponseEntity<ApiResponse<PageInfo<Portal502VO>>>
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<PageInfo<Portal502VO>>> selectPeriodicalList(
            @ModelAttribute Portal502Filter filter) {
        PageInfo<Portal502VO> response = portal502Service.selectPeriodicalList(filter);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 정기발간자료 상세 조회
     *
     * @param fxtmPblsDataOid 정기발간자료 번호
     * @return ResponseEntity<ApiResponse<Portal502DetailDTO>>
     */
    @GetMapping("/detail/{fxtmPblsDataOid}")
    public ResponseEntity<ApiResponse<Portal502DetailDTO>> selectPeriodicalDetail(
            @PathVariable("fxtmPblsDataOid") Long fxtmPblsDataOid) {
        Portal502DetailDTO response = portal502Service.selectPeriodicalDetail(fxtmPblsDataOid);
        
        if (response == null) {
            return new ResponseEntity<>(
                ApiResponse.error(HttpStatus.NOT_FOUND, "정기발간자료 정보를 찾을 수 없습니다."),
                HttpStatus.NOT_FOUND
            );
        }
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
