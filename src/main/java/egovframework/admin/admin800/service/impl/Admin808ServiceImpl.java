package egovframework.admin.admin800.service.impl;

import egovframework.admin.admin800.domain.Admin808DTO;
import egovframework.admin.admin800.domain.Admin808DeleteDTO;
import egovframework.admin.admin800.domain.Admin808VO;
import egovframework.admin.admin800.mapper.Admin808Mapper;
import egovframework.admin.admin800.service.Admin808Service;
import egovframework.common.excel.component.ExcelComponent;
import egovframework.common.excel.domain.ExcelConfig;
import egovframework.common.excel.domain.ExcelExportResult;
import egovframework.common.excel.domain.ExcelPageConfigDTO;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import egovframework.portal.menu.service.UserMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @ClassName : Admin808ServiceImpl.java
 * @Description : 메뉴 관리 서비스 구현체
 *
 * @author : balee
 * @since  : 2025. 11. 13
 * @version : 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Admin808ServiceImpl extends EgovAbstractServiceImpl implements Admin808Service {
    private final Admin808Mapper admin808Mapper;
    private final ExcelComponent excelComponent;
    private final ExcelConfig excelConfig;
    private final UserMenuService userMenuService;

    @Override
    public List<Admin808DTO> selectAdmin808List() {
        // 1. DB에서 VO 조회
        List<Admin808VO> voList = admin808Mapper.selectAdmin808List();

        // 2. VO -> DTO 변환 및 Map 생성
        Map<Long, Admin808DTO> dtoMap = new HashMap<>();
        for (Admin808VO vo : voList) {
            Admin808DTO dto = Admin808DTO.convertToDto(vo);
            dtoMap.put(dto.getMenuOid(), dto);
        }

        // 3. 부모-자식 관계 구성
        for (Admin808DTO dto : dtoMap.values()) {
            Long parentMenuNo = dto.getUpMenuOid();
            if (parentMenuNo != null) {
                Admin808DTO parent = dtoMap.get(parentMenuNo);
                if (parent != null) {
                    parent.getSubMenus().add(dto);
                }
            }
        }

        // 4. 모든 subMenus 정렬 (기준 : menuOrder, regDt)
        for (Admin808DTO dto : dtoMap.values()) {
            if (!dto.getSubMenus().isEmpty()) {
                dto.getSubMenus().sort(Comparator
                        .comparing(Admin808DTO::getMenuSeq, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Admin808DTO::getRegDt, Comparator.nullsLast(Comparator.naturalOrder()))
                );
            }
        }

        // 5. 최상위 메뉴만 추출
        List<Admin808DTO> rootMenus = new ArrayList<>();
        for (Admin808DTO dto : dtoMap.values()) {
            if (dto.getUpMenuOid() == null) {
                rootMenus.add(dto);
            }
        }

        // 6. 최상위 메뉴 정렬
        rootMenus.sort(Comparator
                .comparing(Admin808DTO::getMenuSeq, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Admin808DTO::getRegDt, Comparator.nullsLast(Comparator.naturalOrder()))
        );

        return rootMenus;
    }

    @Override
    public int insertAdmin808(Admin808DTO admin808DTO) {
        try {
            // menuCd 중복 체크
            Admin808VO existingMenu = admin808Mapper.selectByMenuCd(admin808DTO.getMenuCd());
            if (existingMenu != null) {
                throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 메뉴코드입니다.");
            }

            int result = admin808Mapper.insertAdmin808(admin808DTO);
            if(result == 0) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "메뉴 등록에 실패했습니다.");
            }

            // 메뉴 캐시 무효화
            userMenuService.evictMenuCache();

            return result;
        } catch(BusinessException e) {
            throw e;
        } catch(Exception e) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "메뉴 등록 중 오류 발생");
        }
    }

    @Override
    public int updateAdmin808(Admin808DTO admin808DTO) {
        try {
            // 메뉴코드 중복 체크
            Admin808VO existingMenu = admin808Mapper.selectByMenuCd(admin808DTO.getMenuCd());
            if (existingMenu != null && !existingMenu.getMenuOid().equals(admin808DTO.getMenuOid())) {
                throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 메뉴코드입니다.");
            }

            int result = admin808Mapper.updateAdmin808(admin808DTO);
            if(result == 0) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "메뉴 수정에 실패했습니다.");
            }

            // 메뉴 캐시 무효화
            userMenuService.evictMenuCache();

            return result;
        } catch(BusinessException e) {
            throw e;
        } catch(Exception e) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "메뉴 수정 중 오류 발생");
        }
    }

    @Override
    public int deleteAdmin808(List<Admin808DeleteDTO> admin808DeleteDTOS) {
        try {
            int result = 0;
            for (Admin808DeleteDTO admin808DeleteDTO : admin808DeleteDTOS) {
                int count = admin808Mapper.deleteAdmin808(admin808DeleteDTO);
                if(count == 0) {
                    throw new BusinessException(ErrorCode.DATABASE_ERROR, "메뉴 삭제에 실패했습니다.");
                }
                result += count;
            }

            // 메뉴 캐시 무효화
            userMenuService.evictMenuCache();

            return result;
        } catch(BusinessException e) {
            throw e;
        } catch(Exception e) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "메뉴 삭제 중 오류 발생");
        }
    }

    @Override
    public ExcelExportResult admin808ExportExcel(EgovMap cond) throws IOException {
        List<Admin808VO> menuList = admin808Mapper.selectAdmin808ExcelInfo(cond);

        if (menuList == null || menuList.isEmpty()) {
            throw new BusinessException(ErrorCode.EXCEL_DOWNLOAD_NO_DATA, "엑셀 데이터가 존재하지 않습니다.");
        }

        String pageId = (String) cond.get("pageId");

        byte[] bytes = excelComponent.excelExportByPage(pageId, menuList);

        ExcelPageConfigDTO pageInfo = excelConfig.getPages().get(pageId);

        String title = pageInfo.getTitle();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = title + "_" + date + ".xlsx";

        return new ExcelExportResult(fileName, bytes);
    }
    
	@Override
	public int getNextMenuSequence() {
		return admin808Mapper.getNextMenuSequence("bbs_menu_cd_seq");
	}

}