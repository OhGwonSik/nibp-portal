package egovframework.common.file.controller;

import egovframework.common.file.domain.FileUploadCategory;
import egovframework.common.file.domain.UploadedFileInfo;
import egovframework.common.file.service.FileService;
import egovframework.common.file.service.FileUploadService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/common/file")
@RequiredArgsConstructor
public class FileApiController {
	private final FileUploadService fileUploadService;
	private final FileService fileService;

	@Data
	private static class TempFileDeleteRequest {
		private String strgFilePath;
		private String storedStrgFileNm;
	}

	// 첨부 파일 인풋에서 첨부를 했을 때 바로 temp로 업로드
	@PostMapping("/chunk")
	public ResponseEntity<?> uploadNoticeChunk(@RequestParam("file") MultipartFile chunk,
		@RequestParam("resumableChunkNumber") int chunkNumber,
		@RequestParam("resumableTotalChunks") int totalChunks,
		@RequestParam("resumableIdentifier") String identifier,
		@RequestParam("resumableFilename") String filename,
		@RequestParam("strgFileCpct") long expectedStrgFileCpct,
		@RequestParam("targetId") String targetId,
		@RequestParam(value = "rfrncSeCd", required = false) String rfrncSeCd) throws IOException {

		String safeIdentifier = identifier.replaceAll("[^a-zA-Z0-9\\-]", "_");
		fileUploadService.saveChunk(chunk, chunkNumber, safeIdentifier);

		if (chunkNumber == totalChunks) {
			UploadedFileInfo fileInfo = fileUploadService.mergeChunks(safeIdentifier, filename, totalChunks, expectedStrgFileCpct);
			Map<String, Object> response = new HashMap<>();
			response.put("originalStrgFileNm", fileInfo.getOriginalStrgFileNm());
			response.put("storedStrgFileNm", fileInfo.getStoredStrgFileNm());
			response.put("strgFilePath", fileInfo.getStrgFilePath());
			response.put("strgFileCpct", fileInfo.getStrgFileCpct());
			response.put("fileTypeNm", fileInfo.getFileTypeNm());
			return ResponseEntity.ok(response);
		}
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/chunk/temp-file")
	public ResponseEntity<Void> deleteTempUploadedFile(@RequestBody TempFileDeleteRequest request) throws IOException {
		if (request == null || !StringUtils.hasText(request.getStrgFilePath())
			|| !StringUtils.hasText(request.getStoredStrgFileNm())) {
			return ResponseEntity.badRequest().build();
		}
		fileUploadService.deleteStoredFile(request.getStrgFilePath(), request.getStoredStrgFileNm());
		return ResponseEntity.ok().build();
	}

	@GetMapping("/debug/temp-ids")
	public ResponseEntity<Map<String, List<String>>> getAllTempIds() {
//		List<String> tempIds = fileUploadMapper.findAllTempIds();

		Map<String, List<String>> response = new HashMap<>();
//		response.put("tempIds", tempIds);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/cancel")
	public ResponseEntity<Void> cancelUpload(@RequestParam("identifier") String identifier) {
		try {
			String safeIdentifier = identifier.replaceAll("[^a-zA-Z0-9\\-]", "_");
			fileUploadService.cleanupTempDirectory(safeIdentifier);
			return ResponseEntity.ok().build();
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PostMapping("/temp/{tempId}")
	public ResponseEntity<Void> cleanupTempFiles(@PathVariable("tempId") String tempId) {
		try {
			if (tempId == null || !tempId.startsWith("temp-")) {
				return ResponseEntity.badRequest().build();
			}
			fileUploadService.cleanupTempDirectory(tempId);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
	}

	// ck에디터 이미지 일반 업로드
	@PostMapping("/tmp/ck-image")
	public ResponseEntity<Map<String, Object>> uploadTmpFileCk(@RequestParam("upload") MultipartFile file) {
	    Map<String, Object> response = new HashMap<>();
	    try {
//	        Map<String, Object> fileInfo = simpleFileUploadService.uploadFile(
//	                file, FileUploadCategory.TEMP, "admin/ckeditor");
			Map<String, Object> fileInfo = fileService.uploadTempFile(file, FileUploadCategory.TEMP, "ckeditor");

	        String storedStrgFileNm = (String) fileInfo.get("storedStrgFileNm");
	        String relativePath = (String) fileInfo.get("strgFilePath");
	        String fileUrl = fileUploadService.buildPublicUrl(relativePath, storedStrgFileNm);

	        response.put("uploaded", 1);
	        response.put("strgFileNm", storedStrgFileNm);
	        // CKEditor 본문 저장 시 원본 파일명/사이즈가 필요하므로 단건 업로드에도 동일 정보 제공
	        response.put("originalStrgFileNm", fileInfo.get("originalStrgFileNm"));
	        response.put("strgFileCpct", fileInfo.get("strgFileCpct"));
	        response.put("fileTypeNm", fileInfo.get("fileTypeNm"));
	        response.put("url", fileUrl);
	        response.put("strgFilePath", relativePath);

	        return ResponseEntity.ok(response);

	    } catch (IllegalArgumentException e) {
	        Map<String, Object> error = new HashMap<>();
	        error.put("uploaded", 0);
	        Map<String, String> message = new HashMap<>();
	        message.put("message", "업로드 실패: " + e.getMessage());
	        error.put("error", message);
	        return ResponseEntity.ok(error);
	    } catch (Exception e) {
	        // CKEditor는 error 구조도 엄격히 따름
	        Map<String, Object> error = new HashMap<>();
	        error.put("uploaded", 0);
	        Map<String, String> message = new HashMap<>();
	        message.put("message", "업로드 실패: " + e.getMessage());
	        error.put("error", message);
			e.printStackTrace();
	        return ResponseEntity.ok(error);
	    }
	}

	// ckeditor 이미지 청크 업로드
	@PostMapping("/tmp/ck-image/chunk")
	public ResponseEntity<Map<String, Object>> uploadTmpFileCkChunk(
			@RequestParam("file") MultipartFile chunk,
			@RequestParam("resumableChunkNumber") int chunkNumber,
			@RequestParam("resumableTotalChunks") int totalChunks,
			@RequestParam("resumableIdentifier") String identifier,
			@RequestParam("resumableFilename") String filename,
			@RequestParam("strgFileCpct") long expectedStrgFileCpct) {
		try {
			log.info("ckeditor 청크 업로드 파일 ===================> {}", chunk);
			fileUploadService.saveChunk(chunk, chunkNumber, identifier);

			if (chunkNumber == totalChunks) {
				UploadedFileInfo fileInfo = fileUploadService.mergeChunks(identifier, filename, totalChunks,
						expectedStrgFileCpct);
				String targetPath = buildCkEditorUploadPath();
				String movedPath = fileUploadService.moveTempFileToDestination(fileInfo.getStrgFilePath(),
						fileInfo.getStoredStrgFileNm(), targetPath);
				String fileUrl = fileUploadService.buildPublicUrl(movedPath, fileInfo.getStoredStrgFileNm());

				Map<String, Object> response = new HashMap<>();
				response.put("uploaded", 1);
				response.put("url", fileUrl);
				response.put("strgFileNm", fileInfo.getStoredStrgFileNm());
				response.put("originalStrgFileNm", fileInfo.getOriginalStrgFileNm());
				response.put("strgFilePath", movedPath);
				response.put("strgFileCpct", fileInfo.getStrgFileCpct());
				response.put("fileTypeNm", fileInfo.getFileTypeNm());
				return ResponseEntity.ok(response);
			}

			Map<String, Object> progress = new HashMap<>();
			progress.put("uploaded", 0);
			progress.put("chunkNumber", chunkNumber);
			progress.put("totalChunks", totalChunks);
			return ResponseEntity.ok(progress);
		} catch (Exception e) {
			Map<String, Object> error = new HashMap<>();
			error.put("uploaded", 0);
			Map<String, String> message = new HashMap<>();
			message.put("message", "업로드 실패: " + e.getMessage());
			error.put("error", message);
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
		}
	}

	private String buildCkEditorUploadPath() {
		LocalDate now = LocalDate.now();
		return Paths.get("temp", "ckeditor",
				String.valueOf(now.getYear()),
				now.format(DateTimeFormatter.ofPattern("MM")),
				now.format(DateTimeFormatter.ofPattern("dd")))
				.toString()
				.replace("\\", "/");
	}

    @GetMapping("/ci/download")
    public ResponseEntity<Resource> downloadCI() throws IOException {

        // 실제 서버 파일명 (영문)
        ClassPathResource resource =
                new ClassPathResource("static/portal/files/ci/konibp_ci.ai");

        // 사용자에게 보여줄 파일명 (한글)
        String downloadFileName = "국가생명윤리정책원 CI.ai";
        String encodedFileName = URLEncoder.encode(downloadFileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/form/download/{type}")
    public ResponseEntity<Resource> downloadForm(@PathVariable String type) throws IOException {

        String classpath;
        String downloadName;

        switch (type) {
            case "opinion":
                classpath = "static/portal/files/form/form_opinion.hwpx";
                downloadName = "제안서 양식.hwpx";
                break;

            case "request":
                classpath = "static/portal/files/form/form_request.hwp";
                downloadName = "재심사 요청 양식.hwp";
                break;

            case "clean_form":
                classpath = "static/portal/files/form/clean_form.hwp";
                downloadName = "신고서 양식.hwp";
                break;

            case "clean_guide":
                classpath = "static/portal/files/form/anti_corruption_whistleblower_guide_v2.pdf";
                downloadName = "국가생명윤리정책원 부패 및 공익신고자 보호제도 안내(V2).pdf";
                break;

            case "donation_consent":
                classpath = "static/portal/files/form/별지_제41호서식_인체유래물등의_기증_동의서.hwp";
                downloadName = "별지_제41호서식_인체유래물등의_기증_동의서.hwp";
                break;

            case "embryo_disposal":
                classpath = "static/portal/files/form/별지_제17호서식_배아폐기대장.hwp";
                downloadName = "별지_제17호서식_배아폐기대장.hwp";
                break;

			case "advance_directive":
				classpath = "static/portal/files/form/별지_제6호서식_사전연명의료의향서(2024.2.1. 시행).hwp";
				downloadName = "별지_제6호서식_사전연명의료의향서(2024.2.1. 시행).hwp";
				break;

			case "life_sustaining":
				classpath = "static/portal/files/form/별지_제1호서식_연명의료계획서(2024.2.1. 시행).hwp";
				downloadName = "별지_제1호서식_연명의료계획서(2024.2.1. 시행).hwp";
				break;

			case "judgement_terminal_state":
				classpath = "static/portal/files/form/[별지 제9호서식] 임종과정에 있는 환자 판단서 (2019.3.28 시행).hwp";
				downloadName = "[별지 제9호서식] 임종과정에 있는 환자 판단서 (2019.3.28 시행).hwp";
				break;

			case "patient_confirmation":
				classpath = "static/portal/files/form/[별지 제10호서식] 연명의료중단등결정에 대한 환자의사 확인서(사전연명의료의향서).hwp";
				downloadName = "[별지 제10호서식] 연명의료중단등결정에 대한 환자의사 확인서 (사전연명의료의향서).hwp";
				break;

			case "patient_confirmation_family_1":
				classpath = "static/portal/files/form/[별지 제11호서식] 연명의료중단등결정에 대한 환자의사확인서 (환자가족진술) (2019.3.28 시행).hwp";
				downloadName = "[별지 제11호서식] 연명의료중단등결정에 대한 환자의사확인서 (환자가족진술) (2019.3.28 시행).hwp";
				break;

			case "patient_confirmation_family_2":
				classpath = "static/portal/files/form/[별지 제12호서식] 연명의료중단등결정에 대한 친권자 및 환자가족 의사 확인서 (2024.2.1. 시행).hwp";
				downloadName = "[별지 제12호서식] 연명의료중단등결정에 대한 친권자 및 환자가족 의사 확인서 (2024.2.1. 시행).hwp";
				break;

			case "provision_status_xlsx":
				classpath = "static/portal/files/form/붙임2. 개인정보 제3자 제공 현황(20250909).xlsx";
				downloadName = "붙임2. 개인정보 제3자 제공 현황(20250909).xlsx";
				break;

			case "provision_status_pdf":
				classpath = "static/portal/files/form/붙임2. 개인정보 제3자 제공 현황(20250909).pdf";
				downloadName = "붙임2. 개인정보 제3자 제공 현황(20250909).pdf";
				break;

			case "provision_status_hwpx":
				classpath = "static/portal/files/form/붙임2. 개인정보 제3자 제공 현황(20250909).hwpx";
				downloadName = "붙임2. 개인정보 제3자 제공 현황(20250909).hwpx";
				break;

			case "consignment_status_xlsx":
				classpath = "static/portal/files/form/붙임1. 개인정보 처리업무 위탁현황표(20250909).xlsx";
				downloadName = "붙임1. 개인정보 처리업무 위탁현황표(20250909).xlsx";
				break;

			case "consignment_status_pdf":
				classpath = "static/portal/files/form/붙임1. 개인정보 처리업무 위탁현황표(20250909).pdf";
				downloadName = "붙임1. 개인정보 처리업무 위탁현황표(20250909).pdf";
				break;

			case "consignment_status_hwpx":
				classpath = "static/portal/files/form/붙임1. 개인정보 처리업무 위탁현황표(20250909).hwpx";
				downloadName = "붙임1. 개인정보 처리업무 위탁현황표(20250909).hwpx";
				break;

			case "rights_obligations_pdf":
				classpath = "static/portal/files/form/붙임. 정보주체와 법정대리인의 권리의무 및 그 행사방법에 관한 사항(20250909).pdf";
				downloadName = "붙임. 정보주체와 법정대리인의 권리의무 및 그 행사방법에 관한 사항(20250909).pdf";
				break;

			case "rights_obligations_hwpx":
				classpath = "static/portal/files/form/붙임. 정보주체와 법정대리인의 권리의무 및 그 행사방법에 관한 사항(20250909).hwpx";
				downloadName = "붙임. 정보주체와 법정대리인의 권리의무 및 그 행사방법에 관한 사항(20250909).hwpx";
				break;

            default:
                return ResponseEntity.notFound().build();
        }

        ClassPathResource resource = new ClassPathResource(classpath);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String encoded = URLEncoder.encode(downloadName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private static class DownloadFile {
        final String classpath;
        final String downloadName;

        DownloadFile(String classpath, String downloadName) {
            this.classpath = classpath;
            this.downloadName = downloadName;
        }
    }
}