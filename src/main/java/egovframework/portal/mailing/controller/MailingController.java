package egovframework.portal.mailing.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import egovframework.common.api.ApiResponse;
import egovframework.portal.mailing.domain.MailingRequestDTO;
import egovframework.portal.mailing.service.MailingService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common/mailing")
public class MailingController {
	private final MailingService mailingService;
	
	@PostMapping("/mail/auth")
	public ResponseEntity<ApiResponse<Integer>> requestMailAuth(@RequestBody MailingRequestDTO mailingRequestDTO) throws Exception {
        Integer result = mailingService.requestMailAuth(mailingRequestDTO);
        return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
    }
	
	@PostMapping("/mail/auth/update") 
	public ResponseEntity<ApiResponse<Integer>> requestMailUpdateAuth(@RequestBody MailingRequestDTO mailingRequestDTO) throws Exception {
        Integer result = mailingService.requestMailUpdateAuth(mailingRequestDTO);
        return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
    }
	
	@PostMapping("/mail/verify/auth") 
	public ResponseEntity<ApiResponse<Boolean>> verifyMailAuth(@RequestBody MailingRequestDTO mailingRequestDTO) throws Exception {
		Boolean result = mailingService.verifyMailAuth(mailingRequestDTO);
        return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
    }
	
	@PutMapping("/insert")
	public ResponseEntity<ApiResponse<Integer>> insertMailing(@RequestBody MailingRequestDTO mailingRequestDTO) throws Exception {
		Integer result = mailingService.insertMailing(mailingRequestDTO);
        return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
	}
	
	@PatchMapping("/update")
	public ResponseEntity<ApiResponse<Integer>> updateMailing(@RequestBody MailingRequestDTO mailingRequestDTO) throws Exception{
		Integer result = mailingService.updateMailing(mailingRequestDTO);
        return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
	}
	
	@PatchMapping("/cancel")
    public ResponseEntity<ApiResponse<Integer>> cancelMailing(@RequestBody MailingRequestDTO mailingRequestDTO) {
		Integer result =  mailingService.cancelMailing(mailingRequestDTO);
		return new ResponseEntity<>(ApiResponse.success(result), HttpStatus.OK);
    }
}
