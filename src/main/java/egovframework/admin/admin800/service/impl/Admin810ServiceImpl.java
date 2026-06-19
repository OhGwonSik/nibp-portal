package egovframework.admin.admin800.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.admin.admin800.domain.Admin810;
import egovframework.admin.admin800.domain.Admin810DTO;
import egovframework.admin.admin800.domain.Admin810FilterDTO;
import egovframework.admin.admin800.domain.Admin810VO;
import egovframework.admin.admin800.mapper.Admin810Mapper;
import egovframework.admin.admin800.service.Admin810Service;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName : Admin810ServiceImpl.java
 * @Description : 기관 관리 서비스 구현체
 *
 * @author : balee
 * @since  : 2025. 11. 11
 * @version : 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Admin810ServiceImpl extends EgovAbstractServiceImpl implements Admin810Service {
	private final Admin810Mapper admin810Mapper;

	@Override
	public PageInfo<Admin810VO> selectAdmin810List(Admin810FilterDTO filter) {
		PageHelper.startPage(filter.getPage(), filter.getSize(), filter.getSortBy());

		List<Admin810VO> admin810List = admin810Mapper.selectAdmin810List(filter);

		return new PageInfo<>(admin810List);
	}

	@Override
	public Admin810VO selectAdmin810(String instOid) {
		return admin810Mapper.selectAdmin810(instOid);
	}

	@Override
	public void insertAdmin810(Admin810 admin810) {
		try {
			log.debug("insert org: {}", admin810.getInstOid());

			// 사업자등록번호 중복 체크
			Admin810VO existingOrg = admin810Mapper.selectByBizRegNo(admin810.getBizRegNo());
			if (existingOrg != null) {
				throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 사업자등록번호입니다.");
			}

			admin810.setUseYn("Y");
			int result = admin810Mapper.insertAdmin810(admin810);
			if(result == 0) {
				throw new BusinessException(ErrorCode.DATABASE_ERROR, "기관 등록에 실패했습니다.");
			}
		} catch(BusinessException e) {
			throw e;
		} catch(Exception e) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR, "기관 등록 중 오류 발생");
		}
	}

	@Override
	public void updateAdmin810(Admin810 admin810) {
		try {
			log.debug("update org: {}", admin810.getInstOid());

			// 사업자등록번호 중복 체크
			Admin810VO existingOrg = admin810Mapper.selectByBizRegNo(admin810.getBizRegNo());
			if (existingOrg != null && !existingOrg.getInstOid().equals(admin810.getInstOid())) {
				throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 사업자등록번호입니다.");
			}

			admin810.setUseYn("Y");
			int result = admin810Mapper.updateAdmin810(admin810);
			if(result == 0) {
				throw new BusinessException(ErrorCode.DATABASE_ERROR, "기관 수정에 실패했습니다.");
			}
		} catch(BusinessException e) {
			throw e;
		} catch(Exception e) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR, "기관 수정 중 오류 발생");
		}
	}

	@Override
	public void deleteAdmin810(Admin810DTO admin810DTO) {
		log.debug("delete org: {}", admin810DTO.getInstOid());
		try {
			int result = admin810Mapper.deleteAdmin810(admin810DTO);
			if(result == 0) {
				throw new BusinessException(ErrorCode.DATABASE_ERROR, "기관 삭제에 실패했습니다.");
			}
		} catch(BusinessException e) {
			throw e;
		} catch(Exception e) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR, "기관 삭제 중 오류 발생");
		}
	}
}
