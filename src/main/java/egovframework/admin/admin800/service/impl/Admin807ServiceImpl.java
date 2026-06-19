package egovframework.admin.admin800.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.admin.admin800.domain.*;
import egovframework.admin.admin800.mapper.Admin807Mapper;
import egovframework.admin.admin800.mapper.Admin808Mapper;
import egovframework.admin.admin800.service.Admin807Service;
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

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @ClassName : Admin807ServiceImpl.java
 * @Description : 게시판 관리 서비스 구현체
 *
 * @author : 
 * @since  : 2025. 11. 14
 * @version : 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Admin807ServiceImpl extends EgovAbstractServiceImpl implements Admin807Service {
	private final Admin807Mapper admin807Mapper;
	private final Admin808Mapper admin808Mapper;
	private final ExcelComponent excelComponent;
	private final ExcelConfig excelConfig;

	@Override
	public PageInfo<Admin807VO> selectBoardList(Admin807FilterDTO filter) {
		PageHelper.startPage(filter.getPage(), filter.getSize(), filter.getSortBy());
		List<Admin807VO> boardList = admin807Mapper.selectBoardList(filter);
		
		return new PageInfo<>(boardList);
	}

	@Override
	public int insertBoard(Admin807DTO admin807DTO) {
		int count = 0;
		
		count = admin807Mapper.insertBoard(admin807DTO);
		
		if(count == 0) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "저장 에러");
		}
		
		return count;
	}
	
	@Override
	public int updateBoard(Admin807DTO admin807DTO) {
		int count = 0;
		
		count = admin807Mapper.updateBoard(admin807DTO);
		
		if(count == 0) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "수정 에러");
		}
		
		return count;
	}	

	@Override
	public int deleteBoard(Admin807DeleteDTO admin807DeleteDTO) {
		int count = 0;
		String userId = SecurityUtil.getUser().getUserId();
		admin807DeleteDTO.setRegId(userId);
		admin807DeleteDTO.setMdfcnId(userId);
		
		count = admin807Mapper.deleteBoard(admin807DeleteDTO);
		
		if(count == 0) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "삭제(soft delete) 에러");
		}
		
		Admin808DeleteDTO admin808DeleteDTO = new Admin808DeleteDTO();
		admin808DeleteDTO.setMenuOid(admin807DeleteDTO.getMenuOid());
		admin808DeleteDTO.setUseYn(admin807DeleteDTO.getUseYn());
		admin808DeleteDTO.setMdfcnId(userId);
		
		admin808Mapper.deleteAdmin808(admin808DeleteDTO);

		return count;
	}

	@Override
	public ExcelExportResult admin807ExportExcel(EgovMap cond) throws IOException {
		Admin807FilterDTO filter = new Admin807FilterDTO();
	
		filter.setRegStartDt((String) cond.get("regStartDt"));
		filter.setRegEndDt((String) cond.get("regEndDt"));
		filter.setBbsSeCd((String) cond.get("bbsSeCd"));
		filter.setUseYn((String) cond.get("useYn"));
		filter.setPage(1);
		filter.setSize(Integer.MAX_VALUE);
		
		List<Admin807VO> admin807List = admin807Mapper.selectBoardList(filter);
		if(admin807List == null || admin807List.isEmpty()) {
			throw new BusinessException(ErrorCode.EXCEL_DOWNLOAD_NO_DATA, "엑셀 데이터가 존재하지 않습니다.");
		}

		String pageId = (String) cond.get("pageId");
		byte[] bytes = excelComponent.excelExportByPage(pageId, admin807List);

		ExcelPageConfigDTO pageInfo = excelConfig.getPages().get(pageId);
		String title = pageInfo.getTitle();
		String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String fileName = title + "_" + date + ".xlsx";

		return new ExcelExportResult(fileName, bytes);
	}
}
