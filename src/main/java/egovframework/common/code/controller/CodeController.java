package egovframework.common.code.controller;

import egovframework.common.api.ApiResponse;
import egovframework.common.code.domain.CodeResponseDTO;
import egovframework.common.code.service.CodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common/code")
public class CodeController {
    private final CodeService codeService;

    /**
     * group_code_no로 공통 코드 조회 (페이징 없음)
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<CodeResponseDTO>>> getCodeList(@RequestParam("groupCodeNo") Integer groupCodeNo) {
        List<CodeResponseDTO> codes = codeService.getCodeList(groupCodeNo);
        return ResponseEntity.ok(ApiResponse.success(codes));
    }

    /**
     * group_cd로 공통 코드 조회 (페이징 없음)
     */
    @GetMapping("/groupCd/list")
    public ResponseEntity<ApiResponse<List<CodeResponseDTO>>> getCodeListByGrpCd(@RequestParam("grpCd") String grpCd) {
        List<CodeResponseDTO> codes = codeService.getCodeListByGrpCd(grpCd);
        return ResponseEntity.ok(ApiResponse.success(codes));
    }

    @GetMapping("/boardType/list")
    public ResponseEntity<ApiResponse<List<CodeResponseDTO>>> selectBoardTypeList() {
        List<CodeResponseDTO> codes = codeService.selectBoardTypeList();
        return ResponseEntity.ok(ApiResponse.success(codes));
    }
}
