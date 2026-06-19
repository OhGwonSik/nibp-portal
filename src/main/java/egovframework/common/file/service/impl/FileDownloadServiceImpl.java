package egovframework.common.file.service.impl;

import egovframework.common.file.config.FileConfig;
import egovframework.common.file.config.FileStorageType;
import egovframework.common.file.service.FileDownloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * @ClassName : FileDownloadService.java
 * @Description : 파일 다운로드 공통 서비스
 *
 * @since  : 2025. 11. 27
 * @version : 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileDownloadServiceImpl extends EgovAbstractServiceImpl implements FileDownloadService{

    private final FileUploadServiceImpl fileUploadService;
    private final SftpTransferServiceImpl sftpTransferService;
    private final FileConfig fileConfig;

    /**
     * 파일 리소스 로드 (로컬 또는 SFTP)
     *
     * @param strgFilePath 파일 저장 경로 (상대 경로)
     * @param strgFileNm 파일명 (저장된 파일명)
     * @return Resource 파일 리소스
     * @throws IOException 파일 로드 실패 시
     */
    public Resource loadFileResource(String strgFilePath, String strgFileNm) throws IOException {
        if (strgFileNm == null || strgFileNm.isEmpty()) {
            throw new IllegalArgumentException("파일명이 존재하지 않습니다.");
        }

        if (fileConfig.getStorageType() == FileStorageType.SFTP && sftpTransferService.isSftpEnabled()) {
            return sftpTransferService.downloadFile(strgFilePath, strgFileNm);
        }

        // 로컬 파일 시스템
        Path basePath = fileUploadService.getBaseUploadPath();
        Path targetPath = basePath;
        if (StringUtils.hasText(strgFilePath)) {
            targetPath = targetPath.resolve(strgFilePath);
        }
        targetPath = targetPath.resolve(strgFileNm);

        Resource resource = new UrlResource(targetPath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("파일을 찾을 수 없습니다: " + targetPath);
        }
        return resource;
    }

    /**
     * 파일명 인코딩 (UTF-8)
     *
     * @param originalStrgFileNm 원본 파일명
     * @param storedStrgFileNm   저장된 파일명 (원본 파일명이 없을 경우 대체)
     * @return 인코딩된 파일명
     */
    public String encodeStrgFileNm(String originalStrgFileNm, String storedStrgFileNm) {
        String strgFileNm = (originalStrgFileNm != null && !originalStrgFileNm.isEmpty())
                ? originalStrgFileNm
                : storedStrgFileNm;

        if (strgFileNm == null || strgFileNm.isEmpty()) {
            throw new IllegalArgumentException("파일명이 존재하지 않습니다.");
        }

        return URLEncoder.encode(strgFileNm, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
    }

    /**
     * 파일 다운로드 ResponseEntity 생성
     *
     * @param strgFilePath 파일 저장 경로 (상대 경로)
     * @param strgFileNm 파일명 (저장된 파일명)
     * @param originalStrgFileNm 원본 파일명
     * @param strgFileCpct 파일 크기 (null 가능)
     * @return ResponseEntity<Resource> 파일 다운로드 응답
     * @throws IOException 파일 로드 실패 시
     */
    public ResponseEntity<Resource> downloadFile(String strgFilePath, String strgFileNm, String originalStrgFileNm, Long strgFileCpct) throws IOException {
        Resource resource = loadFileResource(strgFilePath, strgFileNm);
        String encodedStrgFileNm = encodeStrgFileNm(originalStrgFileNm, strgFileNm);

        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedStrgFileNm + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM);

        if (strgFileCpct != null && strgFileCpct > 0) {
            builder.contentLength(strgFileCpct);
        }

        return builder.body(resource);
    }
}
