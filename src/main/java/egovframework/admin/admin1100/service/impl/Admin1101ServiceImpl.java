package egovframework.admin.admin1100.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.admin.admin1100.domain.*;
import egovframework.admin.admin1100.mapper.Admin1101Mapper;
import egovframework.admin.admin1100.service.Admin1101Service;
import egovframework.common.excel.component.ExcelComponent;
import egovframework.common.excel.domain.ExcelConfig;
import egovframework.common.excel.domain.ExcelExportResult;
import egovframework.common.excel.domain.ExcelPageConfigDTO;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import egovframework.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import egovframework.common.file.mapper.FileMapper;
import egovframework.common.file.service.FileService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @ClassName : Admin1101ServiceImpl.java
 * @Description : 정기발간자료 관리 Service 구현체
 *
 * @author : j.h.kim
 * @since : 2025. 01. 12
 * @version : 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Admin1101ServiceImpl extends EgovAbstractServiceImpl implements Admin1101Service {
    
    private final Admin1101Mapper admin1101Mapper;
    private final ExcelComponent excelComponent;
    private final ExcelConfig excelConfig;
    private final FileService fileService;
    private final FileMapper fileMapper;
    
    @Override
    public PageInfo<Admin1101VO> selectAdmin1101List(Admin1101FilterDTO filter) {
        PageHelper.startPage(filter.getPage(), filter.getSize(), filter.getSortBy());
        List<Admin1101VO> list = admin1101Mapper.selectAdmin1101List(filter);
        return new PageInfo<>(list);
    }
    
    @Override
    public Admin1101DetailDTO selectAdmin1101Detail(Long fxtmPblsDataOid) {
        // 메인 정보 조회
        Admin1101VO periodical = admin1101Mapper.selectAdmin1101(fxtmPblsDataOid);
        if (periodical == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "정기발간자료를 찾을 수 없습니다.");
        }

        // 표지 이미지 파일 정보 조회
        if (periodical.getCvrImg1Oid() != null) {
            periodical.setCoverFile(fileMapper.selectFileByFileNo(periodical.getCvrImg1Oid()));
        }

        // 총권 파일 정보 조회
        if (periodical.getCvrImg2Oid() != null) {
            periodical.setFullFile(fileMapper.selectFileByFileNo(periodical.getCvrImg2Oid()));
        }

        // 섹션 목록 조회
        List<Admin1101SectionVO> sections = admin1101Mapper.selectAdmin1101SectionList(fxtmPblsDataOid);

        // 각 섹션의 아이템 목록 조회
        if (!CollectionUtils.isEmpty(sections)) {
            for (Admin1101SectionVO section : sections) {
                List<Admin1101ItemVO> items = admin1101Mapper.selectAdmin1101ItemList(section.getFxtmPblsSectOid());

                // 각 아이템의 첨부파일 정보 조회
                if (!CollectionUtils.isEmpty(items)) {
                    for (Admin1101ItemVO item : items) {
                        if (item.getAtchFileOid() != null) {
                            item.setAttachments(fileMapper.selectFileByFileNo(item.getAtchFileOid()));
                        }
                    }
                }

                section.setItems(items);
            }
        }

        // DTO 구성
        Admin1101DetailDTO detailDTO = new Admin1101DetailDTO();
        detailDTO.setPeriodical(periodical);
        detailDTO.setSections(sections);

        return detailDTO;
    }
    
    @Override
    @Transactional
    public Long insertAdmin1101(Admin1101SaveDTO dto, String userId) {
        try {
            // 1. 메인 정보 등록
            Admin1101VO vo = new Admin1101VO();
            vo.setFxtmPblsDataTtl(dto.getFxtmPblsDataTtl());
            vo.setAut(dto.getAut());
            vo.setPageNum(dto.getPageNum());
            vo.setPblcnDt(dto.getPblcnDt());
            vo.setCvrImg1Oid(dto.getCvrImg1Oid());
            vo.setCvrImg2Oid(dto.getCvrImg2Oid());
            vo.setOpenYn(dto.getOpenYn());
            vo.setUseYn("Y");
            vo.setInqCnt(0);
            vo.setSortSeq(0);
            vo.setRegId(userId);
            vo.setRegDt(LocalDateTime.now());

            int result = admin1101Mapper.insertAdmin1101(vo);
            if (result == 0) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "정기발간자료 등록에 실패했습니다.");
            }

            Long fxtmPblsDataOid = vo.getFxtmPblsDataOid();

            // 표지 및 총권 파일의 tbl_oid 업데이트
            updateFilesTablePk(dto.getCvrImg1Oid(), fxtmPblsDataOid, userId);
            updateFilesTablePk(dto.getCvrImg2Oid(), fxtmPblsDataOid, userId);

            // 2. 섹션 및 아이템 등록
            if (!CollectionUtils.isEmpty(dto.getSections())) {
                for (Admin1101SaveDTO.SectionSaveDTO sectionDto : dto.getSections()) {
                    insertSection(fxtmPblsDataOid, sectionDto, userId);
                }
            }

            return fxtmPblsDataOid;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("정기발간자료 등록 중 오류 발생", e);
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "정기발간자료 등록 중 오류가 발생했습니다.");
        }
    }

    @Override
    @Transactional
    public int updateAdmin1101(Admin1101SaveDTO dto, String userId) {
        try {
            Long fxtmPblsDataOid = dto.getFxtmPblsDataOid();
            if (fxtmPblsDataOid == null) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "정기발간자료 번호가 필요합니다.");
            }

            // 0. 기존 데이터 조회 (파일 삭제 비교를 위해)
            Admin1101VO existingData = admin1101Mapper.selectAdmin1101(fxtmPblsDataOid);
            if (existingData == null) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "정기발간자료를 찾을 수 없습니다.");
            }

            // 1. 메인 정보 수정 (기존 로직 유지)
            Admin1101VO vo = new Admin1101VO();
            vo.setFxtmPblsDataOid(fxtmPblsDataOid);
            vo.setFxtmPblsDataTtl(dto.getFxtmPblsDataTtl());
            vo.setAut(dto.getAut());
            vo.setPageNum(dto.getPageNum());
            vo.setPblcnDt(dto.getPblcnDt());
            vo.setCvrImg1Oid(dto.getCvrImg1Oid());
            vo.setCvrImg2Oid(dto.getCvrImg2Oid());
            vo.setOpenYn(dto.getOpenYn());
            vo.setMdfcnId(userId);

            // 1-1. 파일 삭제 처리 (기존 파일이 있었는데 새로운 파일로 변경되거나 null인 경우)
            // 표지 파일 삭제 체크
            if (existingData.getCvrImg1Oid() != null &&
                !existingData.getCvrImg1Oid().equals(dto.getCvrImg1Oid())) {
                deleteFile(existingData.getCvrImg1Oid());
            }

            // 총권 파일 삭제 체크
            if (existingData.getCvrImg2Oid() != null &&
                !existingData.getCvrImg2Oid().equals(dto.getCvrImg2Oid())) {
                deleteFile(existingData.getCvrImg2Oid());
            }

            // 표지 및 총권 파일의 tbl_oid 업데이트
            updateFilesTablePk(dto.getCvrImg1Oid(), fxtmPblsDataOid, userId);
            updateFilesTablePk(dto.getCvrImg2Oid(), fxtmPblsDataOid, userId);

            int result = admin1101Mapper.updateAdmin1101(vo);
            if (result == 0) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "정기발간자료 수정에 실패했습니다.");
            }

            // 2. 섹션 및 아이템 Smart Update 처리
            // 화면에서 넘어온 섹션 리스트가 있다면 처리
            if (!CollectionUtils.isEmpty(dto.getSections())) {
                for (Admin1101SaveDTO.SectionSaveDTO sectionDto : dto.getSections()) {

                    // 2-1. 섹션 처리 (기존 섹션 ID가 있으면 수정, 없으면 삽입)
                    // (화면 UI 구조상 섹션은 고정되어 있다고 가정되나, 안전하게 처리)
                    Long fxtmPblsSectOid = sectionDto.getFxtmPblsSectOid();

                    if (fxtmPblsSectOid == null || fxtmPblsSectOid == 0) {
                        // 신규 섹션인 경우 -> INSERT
                        Admin1101SectionVO newSection = new Admin1101SectionVO();
                        newSection.setFxtmPblsDataOid(fxtmPblsDataOid);
                        newSection.setFxtmPblsSectType(sectionDto.getFxtmPblsSectType());
                        newSection.setFxtmPblsSectTtl(sectionDto.getFxtmPblsSectTtl());
                        newSection.setSortSeq(sectionDto.getSortSeq());
                        newSection.setRegId(userId);
                        admin1101Mapper.insertAdmin1101Section(newSection); // [cite: 19]
                        fxtmPblsSectOid = newSection.getFxtmPblsSectOid(); // 채번된 ID 확보
                    } else {
                        // 기존 섹션인 경우 -> UPDATE
                        Admin1101SectionVO updSection = new Admin1101SectionVO();
                        updSection.setFxtmPblsSectOid(fxtmPblsSectOid);
                        updSection.setFxtmPblsSectType(sectionDto.getFxtmPblsSectType());
                        updSection.setFxtmPblsSectTtl(sectionDto.getFxtmPblsSectTtl());
                        updSection.setSortSeq(sectionDto.getSortSeq());
                        updSection.setMdfcnId(userId);
                        admin1101Mapper.updateAdmin1101Section(updSection); // [cite: 28]
                    }

                    // 2-2. 아이템 처리 (핵심: Insert / Update / Delete 분기)
                    processItems(fxtmPblsSectOid, sectionDto.getItems(), userId);
                }
            }

            return result;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("정기발간자료 수정 중 오류 발생", e);
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "정기발간자료 수정 중 오류가 발생했습니다.");
        }
    }

    /**
     * 아이템 목록 비교 및 처리 (Smart Update 로직)
     */
    private void processItems(Long fxtmPblsSectOid, List<Admin1101SaveDTO.ItemSaveDTO> requestItems, String userId) {
        // 1. DB에 저장된 기존 아이템 목록 조회
        List<Admin1101ItemVO> dbItems = admin1101Mapper.selectAdmin1101ItemList(fxtmPblsSectOid);
        if (requestItems == null) requestItems = List.of();

        Set<Long> processedItemIds = new HashSet<>();

        // 2. 요청된 아이템(Insert/Update) 처리
        for (Admin1101SaveDTO.ItemSaveDTO itemDto : requestItems) {
            Long currentItemOid; // (1) 현재 처리 중인 아이템 번호를 담을 변수

            if (itemDto.getFxtmPblsItemOid() != null && itemDto.getFxtmPblsItemOid() > 0) {
                // [UPDATE] 기존 아이템 수정

                // 2-1. 기존 아이템의 파일 번호 조회 (파일 삭제 비교용)
                Admin1101ItemVO existingItem = dbItems.stream()
                    .filter(item -> item.getFxtmPblsItemOid().equals(itemDto.getFxtmPblsItemOid()))
                    .findFirst()
                    .orElse(null);

                // 2-2. 파일 변경 체크 및 삭제 처리
                if (existingItem != null && existingItem.getAtchFileOid() != null) {
                    // 기존 파일이 있었는데 새 파일로 변경되었거나 null이 된 경우
                    if (!existingItem.getAtchFileOid().equals(itemDto.getAtchFileOid())) {
                        deleteFile(existingItem.getAtchFileOid());
                    }
                }

                Admin1101ItemVO updateVO = new Admin1101ItemVO();
                updateVO.setFxtmPblsItemOid(itemDto.getFxtmPblsItemOid());
                updateVO.setFxtmPblsItemTtl(itemDto.getFxtmPblsItemTtl());
                updateVO.setAut(itemDto.getAut());
                updateVO.setPageNum(itemDto.getPageNum());
                updateVO.setAtchFileOid(itemDto.getAtchFileOid());
                updateVO.setSortSeq(itemDto.getSortSeq());
                updateVO.setMdfcnId(userId);

                admin1101Mapper.updateAdmin1101Item(updateVO);

                currentItemOid = itemDto.getFxtmPblsItemOid(); // ID 유지
                processedItemIds.add(currentItemOid);
            } else {
                // [INSERT] 신규 아이템 등록
                Admin1101ItemVO insertVO = new Admin1101ItemVO();
                insertVO.setFxtmPblsSectOid(fxtmPblsSectOid);
                insertVO.setFxtmPblsItemTtl(itemDto.getFxtmPblsItemTtl());
                insertVO.setAut(itemDto.getAut());
                insertVO.setPageNum(itemDto.getPageNum());
                insertVO.setAtchFileOid(itemDto.getAtchFileOid());
                insertVO.setSortSeq(itemDto.getSortSeq());
                insertVO.setRegId(userId);

                admin1101Mapper.insertAdmin1101Item(insertVO);

                currentItemOid = insertVO.getFxtmPblsItemOid(); // 채번된 새 ID 확보
            }

            // (2) [공통] 파일 매핑 (INSERT, UPDATE 모두 적용됨)
            // 파일 번호가 존재한다면, 해당 파일의 주인을 '현재 아이템 번호(currentItemOid)'로 확정
            if (itemDto.getAtchFileOid() != null && itemDto.getAtchFileOid() > 0) {
                updateFilesTablePk(itemDto.getAtchFileOid(), currentItemOid, userId);
            }
        }

        // 3. 삭제 대상 처리
        if (!CollectionUtils.isEmpty(dbItems)) {
            for (Admin1101ItemVO dbItem : dbItems) {
                if (!processedItemIds.contains(dbItem.getFxtmPblsItemOid())) {
                    // 아이템 삭제 시 첨부파일도 함께 삭제
                    if (dbItem.getAtchFileOid() != null) {
                        deleteFile(dbItem.getAtchFileOid());
                    }
                    admin1101Mapper.deleteAdmin1101Item(dbItem.getFxtmPblsItemOid());
                }
            }
        }
    }
    
    @Override
    @Transactional
    public int deleteAdmin1101(Admin1101DeleteDTO dto) {
        try {
            if (CollectionUtils.isEmpty(dto.getFxtmPblsDataOids())) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "삭제할 항목을 선택해주세요.");
            }

            int totalDeleted = 0;
            for (Long fxtmPblsDataOid : dto.getFxtmPblsDataOids()) {
                int result = admin1101Mapper.deleteAdmin1101(fxtmPblsDataOid);
                totalDeleted += result;
            }
            
            if (totalDeleted == 0) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "정기발간자료 삭제에 실패했습니다.");
            }
            
            return totalDeleted;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("정기발간자료 삭제 중 오류 발생", e);
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "정기발간자료 삭제 중 오류가 발생했습니다.");
        }
    }
    
    @Override
    public ExcelExportResult excelDownload(Admin1101FilterDTO filter) {
        try {
            List<Admin1101ExcelDTO> list = admin1101Mapper.selectAdmin1101ExcelList(filter);
            
            if (CollectionUtils.isEmpty(list)) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "엑셀 다운로드할 데이터가 없습니다.");
            }
            
            String pageId = "admin1101";
            byte[] bytes = excelComponent.excelExportByPage(pageId, list);
            
            ExcelPageConfigDTO pageInfo = excelConfig.getPages().get(pageId);
            String title = pageInfo != null ? pageInfo.getTitle() : "정기발간자료목록";
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String fileName = title + "_" + date + ".xlsx";
            
            return new ExcelExportResult(fileName, bytes);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("엑셀 다운로드 중 오류 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "엑셀 다운로드 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 섹션 및 아이템 등록 (내부 메서드)
     */
    private void insertSection(Long fxtmPblsDataOid, Admin1101SaveDTO.SectionSaveDTO sectionDto, String userId) {
        // 섹션 등록
        Admin1101SectionVO sectionVO = new Admin1101SectionVO();
        sectionVO.setFxtmPblsDataOid(fxtmPblsDataOid);
        sectionVO.setFxtmPblsSectType(sectionDto.getFxtmPblsSectType());
        sectionVO.setFxtmPblsSectTtl(sectionDto.getFxtmPblsSectTtl());
        sectionVO.setSortSeq(sectionDto.getSortSeq() != null ? sectionDto.getSortSeq() : 0);
        sectionVO.setRegId(userId);
        sectionVO.setRegDt(LocalDateTime.now());

        int result = admin1101Mapper.insertAdmin1101Section(sectionVO);
        if (result == 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "섹션 등록에 실패했습니다.");
        }

        Long fxtmPblsSectOid = sectionVO.getFxtmPblsSectOid();

        // 아이템 등록
        if (!CollectionUtils.isEmpty(sectionDto.getItems())) {
            for (Admin1101SaveDTO.ItemSaveDTO itemDto : sectionDto.getItems()) {
                Admin1101ItemVO itemVO = new Admin1101ItemVO();
                itemVO.setFxtmPblsSectOid(fxtmPblsSectOid);
                itemVO.setFxtmPblsItemTtl(itemDto.getFxtmPblsItemTtl());
                itemVO.setAut(itemDto.getAut());
                itemVO.setPageNum(itemDto.getPageNum());
                itemVO.setAtchFileOid(itemDto.getAtchFileOid());
                itemVO.setSortSeq(itemDto.getSortSeq() != null ? itemDto.getSortSeq() : 0);
                itemVO.setRegId(userId);
                itemVO.setRegDt(LocalDateTime.now());

                int itemResult = admin1101Mapper.insertAdmin1101Item(itemVO);
                if (itemResult == 0) {
                    throw new BusinessException(ErrorCode.DATABASE_ERROR, "아이템 등록에 실패했습니다.");
                }

                // 아이템 등록 후 첨부파일의 tbl_oid 업데이트
                Long fxtmPblsItemOid = itemVO.getFxtmPblsItemOid();
                updateFilesTablePk(itemDto.getAtchFileOid(), fxtmPblsItemOid, userId);
            }
        }
    }

    @Override
    @Transactional
    public Long saveFile(Long userOid, MultipartFile file, String fileType) throws IOException {
        String path = "cover"; // 표지(기본값)
        String tblNm = "fxtm_pbls_data"; // 기본값

        if ("FULL".equals(fileType)) {
            // 총권 -> fxtm_pbls_data
            path = "full";
            tblNm = "fxtm_pbls_data";
        } else if ("SPECIAL".equals(fileType)) {
            // 특집 -> fxtm_pbls_item
            path = "special";
            tblNm = "fxtm_pbls_item";
        } else if ("PAPER".equals(fileType)) {
            // 논문 -> fxtm_pbls_item
            path = "paper";
            tblNm = "fxtm_pbls_item";
        } else if ("APPENDIX".equals(fileType)) {
            // 부록 -> fxtm_pbls_item
            path = "appendix";
            tblNm = "fxtm_pbls_item";
        }

        EgovMap egovMap = new EgovMap();
        egovMap.put("path", path);
        egovMap.put("uploadFiles", List.of(file));

        String userId = Objects.requireNonNull(SecurityUtil.getUser()).getUserId();
        egovMap.put("regUserId", userId);
        egovMap.put("mdfcnId", userId);

        // 파일 업로드
        fileService.processFiles(egovMap);

        // 파일에 대한 정보 DB에 저장
        // 주의: 이 시점에는 아직 periodicalNo나 itemNo가 없으므로 table_pk는 0으로 임시 저장
        // 실제 본문 저장 시점(insertAdmin1101/updateAdmin1101)에서 파일의 table_pk를 업데이트해야 함
        egovMap.put("tblNm", tblNm);
        egovMap.put("tblOid", 0L); // 임시값 (본문 저장 시 업데이트)
        
        int savedCount = fileService.saveFileMeta(egovMap);
        
        if (savedCount == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 메타 정보 저장에 실패했습니다.");
        }

        // saveFileMeta 실행 후 egovMap에 fileNo가 설정됨
        Object fileNoObj = egovMap.get("fileOid");
        
        if (fileNoObj != null) {
            if (fileNoObj instanceof Long) {
                return (Long) fileNoObj;
            } else if (fileNoObj instanceof Integer) {
                return ((Integer) fileNoObj).longValue();
            } else if (fileNoObj instanceof String) {
                return Long.parseLong((String) fileNoObj);
            }
        }
        
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 업로드 후 파일 번호를 가져오는데 실패했습니다.");
    }
    
    /**
     * 파일의 tbl_oid 업데이트 헬퍼 메서드
     */
    private void updateFilesTablePk(Long fileOid, Long tblOid, String userId) {
        if (fileOid != null && fileOid > 0 && tblOid != null && tblOid > 0) {
            EgovMap param = new EgovMap();
            param.put("fileOid", fileOid);
            param.put("tblOid", tblOid);
            param.put("mdfcnId", userId);
            fileMapper.updateFileTablePk(param);
        }
    }
    
    /**
     * 파일 삭제 헬퍼 메서드 (논리적 삭제 - use_yn='N')
     */
    private void deleteFile(Long fileOid) {
        if (fileOid != null && fileOid > 0) {
            try {
                String userId = Objects.requireNonNull(SecurityUtil.getUser()).getUserId();
                EgovMap param = new EgovMap();
                param.put("fileOid", fileOid);
                param.put("mdfcnId", userId);
                
                // 논리 삭제 (use_yn = 'N')
                fileMapper.deleteFileByFileNo(param);
                
                log.info("파일 논리 삭제 완료: fileOid={}", fileOid);
            } catch (Exception e) {
                log.error("파일 삭제 실패: fileOid=" + fileOid, e);
                // 파일 삭제 실패해도 메인 프로세스는 계속 진행
            }
        }
    }
}
