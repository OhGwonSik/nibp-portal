package egovframework.admin.admin800.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.admin.admin800.domain.Admin806VO;
import egovframework.admin.admin800.domain.Admin806filterDto;
import egovframework.admin.admin800.mapper.Admin806Mapper;
import egovframework.admin.admin800.service.Admin806Service;
import egovframework.common.component.AESComponent;
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
public class Admin806ServiceImpl extends EgovAbstractServiceImpl implements Admin806Service {
	private final Admin806Mapper admin806Mapper;
	private final MaskingUtil maskingUtil;
	private final AESComponent aesComponent;
	private final ExcelComponent excelComponent;
	private final ExcelConfig excelConfig;

	@Override
	public PageInfo<Admin806VO> selectPersonalInfoProcLogWithFilter(Admin806filterDto filter) {
		if(filter.getPage() != null && filter.getPage() > 0 && filter.getSize() != null && filter.getSize() > 0) {
			PageHelper.startPage(filter.getPage(), filter.getSize(), filter.getSortBy());
		}

		List<Admin806VO> searchList = admin806Mapper.selectPersonalInfoProcLogWithFilter(filter, aesComponent.getSecretKey());
		if(SecurityUtil.getUser() != null && "N".equals(SecurityUtil.getUser().getPrvcUseYn())){
			List<Admin806VO> maskedList = new ArrayList<>();
			maskedList = maskingUtil.maskList(searchList);
			return new PageInfo<>(maskedList);
		}
		return new PageInfo<>(searchList);
	}

	@Override
	public ExcelExportResult admin806ExportExcel(Admin806filterDto filter) throws IOException {
		List<Admin806VO> list = admin806Mapper.selectPersonalInfoProcLogWithFilter(filter, aesComponent.getSecretKey());

		if (list == null || list.isEmpty()) {
			throw new BusinessException(ErrorCode.EXCEL_DOWNLOAD_NO_DATA, "엑셀 데이터가 존재하지 않습니다.");
		}

		String pageId = "admin806";
		byte[] bytes = excelComponent.excelExportByPage(pageId, list);

		ExcelPageConfigDTO pageInfo = excelConfig.getPages().get(pageId);
		String title = pageInfo.getTitle();
		String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String fileName = title + "_" + date + ".xlsx";

		return new ExcelExportResult(fileName, bytes);
	}
}
