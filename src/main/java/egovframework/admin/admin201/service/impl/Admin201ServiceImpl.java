package egovframework.admin.admin201.service.impl;

import java.util.List;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import egovframework.admin.admin201.domain.Admin201DTO;
import egovframework.admin.admin201.domain.Admin201FilterDTO;
import egovframework.admin.admin201.mapper.Admin201Mapper;
import egovframework.admin.admin201.service.Admin201Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Admin201ServiceImpl extends EgovAbstractServiceImpl implements Admin201Service{

	private final Admin201Mapper admin201Mapper;
	
	@Override
	public PageInfo<Admin201DTO> selectOpinionList(Admin201FilterDTO admin201FilterDTO) {
		// PageHelper를 사용하여 페이징 처리
		PageHelper.startPage(admin201FilterDTO.getPageIndex(), admin201FilterDTO.getPageSize());

        List<Admin201DTO> opinionlist = admin201Mapper.selectOpinionList(admin201FilterDTO);

        return new PageInfo<>(opinionlist);
	}

	@Override
	public Admin201DTO selectOpinionDetail(Admin201FilterDTO admin201FilterDTO) {
		return admin201Mapper.selectOpinionDetail(admin201FilterDTO);
	}

}
