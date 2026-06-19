package egovframework.admin.admin201.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;

import egovframework.admin.admin201.domain.Admin201DTO;
import egovframework.admin.admin201.domain.Admin201FilterDTO;
import egovframework.admin.admin201.service.Admin201Service;
import egovframework.common.annotation.LogParam;
import egovframework.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/admin201")
public class Admin201Controller {
	private final Admin201Service admin201Service;
	
	@GetMapping("/list/filter")
	public ResponseEntity<ApiResponse<PageInfo<Admin201DTO>>> selectOpinionList(@LogParam @ModelAttribute Admin201FilterDTO Admin201FilterDTO){
		PageInfo<Admin201DTO> resultList = admin201Service.selectOpinionList(Admin201FilterDTO);
		return new ResponseEntity<>(ApiResponse.success(resultList), HttpStatus.OK);
	}
	
	@GetMapping("/detail")
	public ResponseEntity<ApiResponse<Admin201DTO>> selectOpinionDetail(@LogParam @ModelAttribute Admin201FilterDTO Admin201FilterDTO){
		Admin201DTO opinionDetail = admin201Service.selectOpinionDetail(Admin201FilterDTO);
		return new ResponseEntity<>(ApiResponse.success(opinionDetail), HttpStatus.OK);
	}	
}
