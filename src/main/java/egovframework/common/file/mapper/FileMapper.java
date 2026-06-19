package egovframework.common.file.mapper;

import egovframework.common.file.domain.FileDTO;
import io.lettuce.core.dynamic.annotation.Param;

import org.egovframe.rte.psl.dataaccess.mapper.Mapper;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;

import java.util.List;

@Mapper
public interface FileMapper {
    List<FileDTO> selectAttachmentFileByTableNameAndTablePk(EgovMap egovMap);

    List<FileDTO> selectAttachmentFilesByTableNameAndTablePks(EgovMap egovMap);

    List<FileDTO> selectInlineFileByTableNameAndTablePk(EgovMap egovMap);

    FileDTO selectFileByFileNo(Long fileOid);

    int insertFile(EgovMap egovMap);

    int updateFileMsg(EgovMap egovMap);

    int updateFileAltText(EgovMap egovMap);

    int updateFileTablePk(EgovMap egovMap);

    int deleteFileByTableNameAndTablePk(EgovMap egovMap);

    int deleteFileByFileNo(EgovMap egovMap);

    List<EgovMap> selectFileInfosByFileNos(@Param("list") List<Long> fileNos);

    int increaseDownloadCnt(Long fileOid);

    /**
     * 고아 파일 관리를 위한 모든 파일 경로 조회
     * @return 파일 경로 리스트 (file_path, small_file_path, medium_file_path)
     */
    List<String> selectAllFilePaths();

    /**
     * 파일 레코드 복사 (table_pk만 변경하여 새 row 생성)
     * @param egovMap tblNm, oldTblOid, newTblOid, regId
     * @return 복사된 row 수
     */
    int copyFilesByTablePk(EgovMap egovMap);
}