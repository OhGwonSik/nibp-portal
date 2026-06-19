package egovframework.common.file.service;

import egovframework.common.file.domain.AttachedFileDTO;
import egovframework.common.file.domain.FileDTO;
import egovframework.common.file.domain.FileUploadCategory;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface FileService {
    void processFiles(EgovMap egovMap) throws IOException;
    int saveFileMeta(EgovMap egovMap);
    void deleteFilesByTable(String tblNm, Long tblOid, String mdfcnId);
    void deleteFilesByFileNos(List<Long> fileNos, String mdfcnId);
    List<FileDTO> selectInlineFileByTableNameAndTablePk(EgovMap egovMap);
    List<Map<String, Object>> convertCkEditorFileDtoToMap(List<AttachedFileDTO> files);
    Map<String, Object> uploadTempFile(MultipartFile file, FileUploadCategory category, String apiPath) throws IOException;
    void saveFilesByType(List<Map<String, Object>> attachedFiles, Long emailTemplateNo, String userId, String tblNm);

    /**
     * 파일 레코드 복사 (table_pk만 변경하여 새 row 생성)
     * @param tblNm 테이블명
     * @param oldTblOid 기존 테이블 PK
     * @param newTblOid 새 테이블 PK
     * @param regId 등록자 ID
     * @return 복사된 row 수
     */
    int copyFilesByTablePk(String tblNm, Long oldTblOid, Long newTblOid, String regId);
    List<Map<String, Object>> convertAttachedFileDtoToMap(List<AttachedFileDTO> files);
}