package egovframework.common.file.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

/**
 * @ClassName : FileDownloadService.java
 * @Description : 파일 다운로드 공통 서비스
 *
 * @since  : 2025. 11. 27
 * @version : 1.0
 */
public interface FileDownloadService {

    /**
     * 파일 리소스 로드 (로컬 또는 SFTP)
     *
     * @param strgFilePath 파일 저장 경로 (상대 경로)
     * @param strgFileNm 파일명 (저장된 파일명)
     * @return Resource 파일 리소스
     * @throws IOException 파일 로드 실패 시
     */
    public Resource loadFileResource(String strgFilePath, String strgFileNm) throws IOException;

    /**
     * 파일명 인코딩 (UTF-8)
     *
     * @param originalStrgFileNm 원본 파일명
     * @param storedStrgFileNm   저장된 파일명 (원본 파일명이 없을 경우 대체)
     * @return 인코딩된 파일명
     */
    public String encodeStrgFileNm(String originalStrgFileNm, String storedStrgFileNm);

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
    public ResponseEntity<Resource> downloadFile(String strgFilePath, String strgFileNm, String originalStrgFileNm, Long strgFileCpct) throws IOException;
}
