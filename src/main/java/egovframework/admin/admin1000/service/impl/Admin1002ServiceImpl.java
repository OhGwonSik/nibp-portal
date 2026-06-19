package egovframework.admin.admin1000.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.admin.admin1000.domain.*;
import egovframework.admin.admin1000.mapper.Admin1002Mapper;
import egovframework.admin.admin1000.service.Admin1002Service;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import egovframework.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @ClassName : Admin1002ServiceImpl.java
 * @Description : 부서 구성원 관리 Service 구현체
 *
 * @author : j.h.kim
 * @since  : 2026. 01. 08
 * @version : 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Admin1002ServiceImpl extends EgovAbstractServiceImpl implements Admin1002Service {

    private final Admin1002Mapper admin1002Mapper;

    @Override
    public PageInfo<Admin1002VO> selectAdmin1002List(Admin1002FilterDTO filter) {
        if (filter.getDeptOid() == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "부서를 선택해주세요.");
        }

        PageHelper.startPage(filter.getPage(), filter.getSize(), filter.getSortBy());
        List<Admin1002VO> list = admin1002Mapper.selectAdmin1002List(filter);
        return new PageInfo<>(list);
    }

    @Override
    public Admin1002VO selectAdmin1002(Long deptMmbrOid) {
        if (deptMmbrOid == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "매핑 ID가 필요합니다.");
        }

        Admin1002VO member = admin1002Mapper.selectAdmin1002(deptMmbrOid);
        if (member == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "구성원을 찾을 수 없습니다.");
        }

        return member;
    }

    @Transactional
    @Override
    public void insertAdmin1002(Admin1002DTO dto) {
        try {
            // 유효성 검사
            validateMemberData(dto);

            // 중복 확인 (같은 부서에 같은 이름+이메일)
            int count = admin1002Mapper.checkDuplicateMember(dto);
            if (count > 0) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "이미 해당 부서에 동일한 구성원이 있습니다.");
            }

            // 등록자 설정
            String userId = SecurityUtil.getUser() != null ? SecurityUtil.getUser().getUserId() : "SYSTEM";
            dto.setRegId(userId);
            dto.setMdfcnId(userId);

            int result = admin1002Mapper.insertAdmin1002(dto);
            if (result == 0) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "구성원 추가에 실패했습니다.");
            }

            log.info("구성원 추가 완료: userNm={}, deptOid={}", dto.getUserNm(), dto.getDeptOid());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("구성원 추가 중 오류 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "구성원 추가 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    @Override
    public void updateAdmin1002(Admin1002DTO dto) {
        try {
            if (dto.getDeptMmbrOid() == null) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "매핑 ID가 필요합니다.");
            }

            // 유효성 검사
            validateMemberData(dto);

            // 수정자 설정
            String userId = SecurityUtil.getUser() != null ? SecurityUtil.getUser().getUserId() : "SYSTEM";
            dto.setMdfcnId(userId);

            int result = admin1002Mapper.updateAdmin1002(dto);
            if (result == 0) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "구성원 수정에 실패했습니다.");
            }

            log.info("구성원 수정 완료: deptMmbrOid={}", dto.getDeptMmbrOid());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("구성원 수정 중 오류 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "구성원 수정 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    @Override
    public void deleteAdmin1002(Admin1002DeleteDTO dto) {
        try {
            if (dto.getDeptMmbrOids() == null || dto.getDeptMmbrOids().isEmpty()) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "삭제할 구성원을 선택해주세요.");
            }

            // 수정자 설정
            String userId = SecurityUtil.getUser() != null ? SecurityUtil.getUser().getUserId() : "SYSTEM";
            dto.setMdfcnId(userId);

            int result = admin1002Mapper.deleteAdmin1002(dto);
            if (result == 0) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "구성원 삭제에 실패했습니다.");
            }

            log.info("구성원 삭제 완료: {} 건", dto.getDeptMmbrOids().size());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("구성원 삭제 중 오류 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "구성원 삭제 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    @Override
    public void updateDisplayOrder(Admin1002DTO dto) {
        try {
            if (dto.getDeptMmbrOid() == null || dto.getIndctSeq() == null) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "매핑 ID와 표시 순서가 필요합니다.");
            }

            String userId = SecurityUtil.getUser() != null ? SecurityUtil.getUser().getUserId() : "SYSTEM";
            dto.setMdfcnId(userId);

            int result = admin1002Mapper.updateDisplayOrder(dto);
            if (result == 0) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "순서 변경에 실패했습니다.");
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("순서 변경 중 오류 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "순서 변경 중 오류가 발생했습니다.");
        }
    }

    /**
     * 구성원 데이터 유효성 검사
     */
    private void validateMemberData(Admin1002DTO dto) {
        if (dto.getDeptOid() == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "부서를 선택해주세요.");
        }

        if (dto.getUserNm() == null || dto.getUserNm().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "사용자명을 입력해주세요.");
        }

        if (dto.getIndctSeq() == null) {
            dto.setIndctSeq(0);
        }
    }
}