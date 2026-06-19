package egovframework.admin.admin800.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.admin.admin800.domain.*;
import egovframework.admin.admin800.mapper.Admin802Mapper;
import egovframework.admin.admin800.service.Admin802Service;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName : Admin802ServiceImpl.java
 * @Description : 공통코드 관리 서비스 구현체
 *
 * @author : balee
 * @since  : 2025. 11. 18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Admin802ServiceImpl extends EgovAbstractServiceImpl implements Admin802Service {
	private final Admin802Mapper admin802Mapper;
	
	@Override
	public PageInfo<GroupCodeResponseDTO> selectGroupcodeWithFilter(GroupCodeFilterDTO filter) {
		PageHelper.startPage(filter.getPage(), filter.getSize(), filter.getSortBy());

		List<GroupCodeResponseDTO> groupCodeResponses = admin802Mapper.selectGroupcodeWithFilter(filter);

		return new PageInfo<>(groupCodeResponses);
	}

	@Override
	public PageInfo<CodeResponseDTO> selectCodeWithFilter(CodeFilterDTO filter) {
		PageHelper.startPage(filter.getPage(), filter.getSize(), filter.getSortBy());

		List<CodeResponseDTO> codeResponses = admin802Mapper.selectCodeWithFilter(filter);

		return new PageInfo<>(codeResponses);
	}

	@Override
	public int insertAdmin802Group(GroupCode groupCode) {
		try {
			// 그룹코드 중복체크
			GroupCode existingGcd = admin802Mapper.selectByGrpCd(groupCode.getGrpCd());
			if (existingGcd != null) {
				throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 그룹코드입니다");
			}

			int groupResult = admin802Mapper.insertAdmin802Group(groupCode);
			if (groupResult == 0) {
				throw new BusinessException(ErrorCode.DATABASE_ERROR, "그룹코드 등록에 실패했습니다.");
			}
			return groupResult;
		} catch(BusinessException e) {
			throw e;
		} catch(Exception e) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR, "그룹코드 등록 중 오류 발생");
		}
	}

	@Override
	public int insertAdmin802Code(Code code) {
		try {
			int count = admin802Mapper.selectByCd(code);
			
			if(count > 0) {
				throw new BusinessException(ErrorCode.DATABASE_ERROR, "이미 존재하는 코드입니다.");
			}
			
			int codeResult = admin802Mapper.insertAdmin802Code(code);
			if (codeResult == 0) {
				throw new BusinessException(ErrorCode.DATABASE_ERROR, "코드 등록에 실패했습니다.");
			}
			return codeResult;
		} catch(BusinessException e) {
			throw e;
		} catch(Exception e) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR, "코드 등록 중 오류 발생");
		}
	}

	@Override
	public int updateAdmin802Group(GroupCode groupCode) {
		try {
			// 그룹코드 중복체크
			GroupCode existingGcd = admin802Mapper.selectByGrpCd(groupCode.getGrpCd());
			if (existingGcd != null && !existingGcd.getGrpCd().equals(groupCode.getGrpCd())) {
				throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 그룹코드입니다");
			}

			int groupResult = admin802Mapper.updateAdmin802Group(groupCode);
			if (groupResult == 0) {
				throw new BusinessException(ErrorCode.DATABASE_ERROR, "그룹코드 수정에 실패했습니다.");
			}
			return groupResult;
		} catch(BusinessException e) {
			throw e;
		} catch(Exception e) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR, "그룹코드 수정 중 오류 발생");
		}
	}

	@Override
	public int updateAdmin802Code(Code code) {
		try {
			int codeResult = admin802Mapper.updateAdmin802Code(code);
			if (codeResult == 0) {
				throw new BusinessException(ErrorCode.DATABASE_ERROR, "코드 수정에 실패했습니다.");
			}
			return codeResult;
		} catch(BusinessException e) {
			throw e;
		} catch(Exception e) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR, "코드 수정 중 오류 발생");
		}
	}
}
