package egovframework.portal.opinion.controller;

import java.io.IOException;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import egovframework.common.api.ApiResponse;
import egovframework.common.exception.BusinessException;
import egovframework.portal.opinion.domain.PublicDataOpinionSaveDTO;
import egovframework.portal.opinion.service.OpinionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common/opinion")
public class OpinionController {
	private final OpinionService opinonService;
	
	@PostMapping("/insert")
	public ResponseEntity<ApiResponse<Integer>> insertPublicDataOpinion(@RequestBody @Valid PublicDataOpinionSaveDTO publicDataOpinionSaveDTO) throws BusinessException, IOException{
		Integer result = opinonService.insertPublicDataOpinion(publicDataOpinionSaveDTO);
		return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
	}
}
