package egovframework.admin.admin1000.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.admin.admin1000.domain.Admin1001DeleteDTO;
import egovframework.admin.admin1000.domain.Admin1001DTO;
import egovframework.admin.admin1000.domain.Admin1001FilterDTO;
import egovframework.admin.admin1000.domain.Admin1001VO;
import egovframework.admin.admin1000.mapper.Admin1001Mapper;
import egovframework.admin.admin1000.service.Admin1001Service;
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
 * @ClassName : Admin1001ServiceImpl.java
 * @Description : 부서 관리 Service 구현체
 *
 * @author : j.h.kim
 * @since  : 2026. 01. 07
 * @version : 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Admin1001ServiceImpl extends EgovAbstractServiceImpl implements Admin1001Service {
    
    private final Admin1001Mapper admin1001Mapper;

    @Override
    public PageInfo<Admin1001VO> selectAdmin1001List(Admin1001FilterDTO filter) {
        PageHelper.startPage(filter.getPage(), filter.getSize(), filter.getSortBy());
        List<Admin1001VO> list = admin1001Mapper.selectAdmin1001List(filter);
        return new PageInfo<>(list);
    }

    @Override
    public Admin1001VO selectAdmin1001(Long deptOid) {
        if (deptOid == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "부서 ID가 필요합니다.");
        }
        
        Admin1001VO dept = admin1001Mapper.selectAdmin1001(deptOid);
        if (dept == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "부서를 찾을 수 없습니다.");
        }
        
        return dept;
    }

    @Override
    public List<Admin1001VO> selectAdmin1001Tree() {
        return admin1001Mapper.selectAdmin1001Tree();
    }

    @Transactional
    @Override
    public void insertAdmin1001(Admin1001DTO dto) {
        try {
            // 유효성 검사
            validateDeptData(dto);
            
            // 순환 참조 체크
            if (dto.getUpDeptOid() != null) {
                checkCircularReference(dto.getUpDeptOid(), null);
            }
            
            // 등록자 설정
            String userId = SecurityUtil.getUser() != null ? SecurityUtil.getUser().getUserId() : "SYSTEM";
            dto.setRegId(userId);
            dto.setMdfcnId(userId);
            
            int result = admin1001Mapper.insertAdmin1001(dto);
            if (result == 0) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "부서 등록에 실패했습니다.");
            }
            
            log.info("부서 등록 완료: {}", dto.getDeptNm());
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("부서 등록 중 오류 발생", e);
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "부서 등록 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    @Override
    public void updateAdmin1001(Admin1001DTO dto) {
        try {
            if (dto.getDeptOid() == null) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "부서 ID가 필요합니다.");
            }
            
            // 유효성 검사
            validateDeptData(dto);
            
            // 순환 참조 체크
            if (dto.getUpDeptOid() != null) {
                checkCircularReference(dto.getUpDeptOid(), dto.getDeptOid());
            }
            
            // 수정자 설정
            String userId = SecurityUtil.getUser() != null ? SecurityUtil.getUser().getUserId() : "SYSTEM";
            dto.setMdfcnId(userId);
            
            int result = admin1001Mapper.updateAdmin1001(dto);
            if (result == 0) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "부서 수정에 실패했습니다.");
            }
            
            log.info("부서 수정 완료: {}", dto.getDeptNm());
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("부서 수정 중 오류 발생", e);
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "부서 수정 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    @Override
    public void deleteAdmin1001(Admin1001DeleteDTO dto) {
        try {
            if (dto.getDeptOids() == null || dto.getDeptOids().isEmpty()) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "삭제할 부서를 선택해주세요.");
            }
            
            // 각 부서별로 삭제 가능 여부 확인
            for (Long deptOid : dto.getDeptOids()) {
                // 하위 부서 존재 여부 확인
                int childCount = admin1001Mapper.checkChildDept(deptOid);
                if (childCount > 0) {
                    Admin1001VO dept = admin1001Mapper.selectAdmin1001(deptOid);
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                        String.format("'%s' 부서는 하위 부서가 존재하여 삭제할 수 없습니다.", dept.getDeptNm()));
                }
                
                // 구성원 존재 여부 확인
                int memberCount = admin1001Mapper.checkDeptMembers(deptOid);
                if (memberCount > 0) {
                    Admin1001VO dept = admin1001Mapper.selectAdmin1001(deptOid);
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                        String.format("'%s' 부서는 소속 구성원이 존재하여 삭제할 수 없습니다.", dept.getDeptNm()));
                }
            }
            
            // 수정자 설정
            String userId = SecurityUtil.getUser() != null ? SecurityUtil.getUser().getUserId() : "SYSTEM";
            dto.setMdfcnId(userId);
            
            int result = admin1001Mapper.deleteAdmin1001(dto);
            if (result == 0) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "부서 삭제에 실패했습니다.");
            }
            
            log.info("부서 삭제 완료: {} 건", dto.getDeptOids().size());
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("부서 삭제 중 오류 발생", e);
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "부서 삭제 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    @Override
    public void updateSortSeq(Admin1001DTO dto) {
        try {
            if (dto.getDeptOid() == null || dto.getSortSeq() == null) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "부서 ID와 정렬 순서가 필요합니다.");
            }
            
            String userId = SecurityUtil.getUser() != null ? SecurityUtil.getUser().getUserId() : "SYSTEM";
            dto.setMdfcnId(userId);
            
            int result = admin1001Mapper.updateSortSeq(dto);
            if (result == 0) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "정렬 순서 변경에 실패했습니다.");
            }
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("정렬 순서 변경 중 오류 발생", e);
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "정렬 순서 변경 중 오류가 발생했습니다.");
        }
    }

    /**
     * 부서 데이터 유효성 검사
     */
    private void validateDeptData(Admin1001DTO dto) {
        if (dto.getDeptNm() == null || dto.getDeptNm().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "부서명을 입력해주세요.");
        }
        
        if (dto.getSortSeq() == null) {
            dto.setSortSeq(0);
        }
        
        if (dto.getUseYn() == null || dto.getUseYn().trim().isEmpty()) {
            dto.setUseYn("Y");
        }
    }

    /**
     * 순환 참조 체크 (부서 A의 상위를 부서 A로 설정하는 경우 방지)
     */
    private void checkCircularReference(Long upDeptOid, Long currentDeptOid) {
        if (upDeptOid == null) {
            return;
        }

        if (currentDeptOid != null && upDeptOid.equals(currentDeptOid)) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "자기 자신을 상위 부서로 설정할 수 없습니다.");
        }

        // 상위 부서의 parent를 재귀적으로 확인하여 순환 참조 체크
        Admin1001VO parent = admin1001Mapper.selectAdmin1001(upDeptOid);
        if (parent != null && parent.getUpDeptOid() != null) {
            if (currentDeptOid != null && parent.getUpDeptOid().equals(currentDeptOid)) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "순환 참조가 발생합니다.");
            }
            checkCircularReference(parent.getUpDeptOid(), currentDeptOid);
        }
    }
}
