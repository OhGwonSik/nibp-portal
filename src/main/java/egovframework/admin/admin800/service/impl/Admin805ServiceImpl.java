package egovframework.admin.admin800.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.admin.admin800.domain.Admin805VO;
import egovframework.admin.admin800.domain.Admin805filterDto;
import egovframework.admin.admin800.mapper.Admin805Mapper;
import egovframework.admin.admin800.service.Admin805Service;
import egovframework.common.excel.component.ExcelComponent;
import egovframework.common.excel.domain.ExcelConfig;
import egovframework.common.excel.domain.ExcelExportResult;
import egovframework.common.excel.domain.ExcelPageConfigDTO;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import egovframework.common.util.MaskingUtil;
import egovframework.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class Admin805ServiceImpl extends EgovAbstractServiceImpl implements Admin805Service {
	private final Admin805Mapper admin805Mapper;
	private final MaskingUtil maskingUtil;
	private final ExcelComponent excelComponent;
	private final ExcelConfig excelConfig;

	@Override
	public PageInfo<Admin805VO> selectPermissionChangeLogWithFilter(Admin805filterDto filter) {
		if(filter.getPage() != null && filter.getPage() > 0 && filter.getSize() != null && filter.getSize() > 0) {
			PageHelper.startPage(filter.getPage(), filter.getSize(), filter.getSortBy());
		}

		List<Admin805VO> searchList = admin805Mapper.selectPermissionChangeLogWithFilter(filter);
		if(SecurityUtil.getUser() != null && "N".equals(SecurityUtil.getUser().getPrvcUseYn())){
			List<Admin805VO> maskedList = new ArrayList<>();
			maskedList = maskingUtil.maskList(searchList);
			return new PageInfo<>(maskedList);
		}
		return new PageInfo<>(searchList);
	}

	@Override
	public ExcelExportResult admin805ExportExcel(Admin805filterDto filter) throws IOException {
		List<Admin805VO> list = admin805Mapper.selectPermissionChangeLogWithFilter(filter);

		if (list == null || list.isEmpty()) {
			throw new BusinessException(ErrorCode.EXCEL_DOWNLOAD_NO_DATA, "엑셀 데이터가 존재하지 않습니다.");
		}

		String pageId = "admin805";
		byte[] bytes = excelComponent.excelExportByPage(pageId, list);

		ExcelPageConfigDTO pageInfo = excelConfig.getPages().get(pageId);
		String title = pageInfo.getTitle();
		String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String fileName = title + "_" + date + ".xlsx";

		return new ExcelExportResult(fileName, bytes);
	}
}
