package egovframework.admin.admin800.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.admin.admin800.domain.*;
import egovframework.admin.admin800.mapper.Admin801Mapper;
import egovframework.admin.admin800.mapper.Admin810Mapper;
import egovframework.admin.admin800.service.Admin801Service;
import egovframework.common.audit.domain.PermissionChangeLog;
import egovframework.common.audit.service.AuditService;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.auth.mapper.AuthMapper;
import egovframework.common.component.AESComponent;
import egovframework.common.enums.AuthLevel;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import egovframework.common.util.CryptoUtil;
import egovframework.common.util.MaskingUtil;
import egovframework.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName : Admin801ServiceImpl.java
 * @Description : 관리자 관리 서비스 구현체
 *
 * @author : balee
 * @since  : 2025. 11. 19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Admin801ServiceImpl extends EgovAbstractServiceImpl implements Admin801Service {
	private final Admin801Mapper admin801Mapper;
    private final PasswordEncoder passwordEncoder;
    private final MaskingUtil maskingUtil;
    private final CryptoUtil cryptoUtil;
    private final AESComponent aesComponent;
    private final Admin810Mapper admin810Mapper;
    private final AuthMapper authMapper;
    private final AuditService auditService;

    @Override
    public PageInfo<Admin801ResponseDto> selectAdmin801ListWithFilter(Admin801filterDto filter) {
        PageHelper.startPage(filter.getPage(), filter.getSize(), filter.getSortBy());
        List<Admin801ResponseDto> list = admin801Mapper.selectAdmin801ListWithFilter(filter, aesComponent.getSecretKey());

        if(SecurityUtil.getUser() != null && "N".equals(SecurityUtil.getUser().getPrvcUseYn())) {
            List<Admin801ResponseDto> maskedList = new ArrayList<>();
            maskedList = maskingUtil.maskList(list);
            return new PageInfo<>(maskedList);
        }
        return new PageInfo<>(list);
    }

    @Override
    public Admin801VO selectAdmin801Detail(EgovMap egovMap) {
        Admin801VO list = admin801Mapper.selectAdmin801Detail(egovMap, aesComponent.getSecretKey());
        if(SecurityUtil.getUser() != null && "N".equals(SecurityUtil.getUser().getPrvcUseYn())) {
            Admin801VO maskedList = new Admin801VO();
            maskedList = maskingUtil.mask(list);
            return maskedList;
        }
        return list;
    }

    @Override
    public int upsertAdmin801(Admin801DTO dto) {
        try {
            // 사용자 입력값 VO로 변환
            Admin801VO vo = dto.convertToVO();
            BaseUser changerUser = SecurityUtil.getUser();
            LocalDate now = LocalDate.now();
            LocalDate endDt = LocalDate.now().plusYears(2);
            vo.setUserAuthrt(AuthLevel.ADMIN.name());
            vo.setUserType(AuthLevel.ADMIN.getName());

            // 신규 등록 시 기관 정보 할당 - (재)국가생명윤리정책원
            if (dto.getUserOid() == null) {
                Admin810VO orgInfo = admin810Mapper.selectAdmin810("1");
                vo.setInstOid(orgInfo.getInstOid());
                vo.setInstNm(orgInfo.getInstNm());
            }

            int result;
            cryptoUtil.encrypt(vo);
            PermissionChangeLog permissionChangelog = null;

            if (dto.getUserOid() == null) {
                // 신규는 비밀번호 필수
                if (dto.getPswd() == null || dto.getPswd().isBlank()) {
                    throw new BusinessException(ErrorCode.REQUIRED_FIELD_MISSING, "신규 등록 시 비밀번호는 필수입니다.");
                }
                vo.setPswd(passwordEncoder.encode(dto.getPswd()));
                if("Y".equals(vo.getPrvcUseYn())){
                    vo.setPrvcRegId(changerUser.getUserId());
                    vo.setPrvcStartDt(now);
                    vo.setPrvcEndDt(endDt);
                } else {
                    vo.setPrvcRegId(null);
                    vo.setPrvcStartDt(null);
                    vo.setPrvcEndDt(null);
                }
                result = admin801Mapper.insertAdmin801(vo);
                BaseUser newUser = authMapper.selectUserByUserId(vo.getUserId(), aesComponent.getSecretKey());
                permissionChangelog = createPermissionChangeLog(changerUser.getUserId(), newUser.getUserId(), "PRVC_USE_YN", newUser.getPrvcUseYn(), vo.getPrvcUseYn(), "ADD", "관리자 생성");
            } else {
                // 수정 시: 비밀번호 입력한 경우에만 변경
                if (dto.getPswd() != null && !dto.getPswd().isBlank()) {
                    vo.setPswd(passwordEncoder.encode(dto.getPswd()));
                }
                BaseUser targetUser = authMapper.selectUserByUserId(vo.getUserId(), aesComponent.getSecretKey());

                // PRVC_USE_YN 값이 변경된 경우에만 로깅
                if (targetUser != null && !Objects.equals(targetUser.getPrvcUseYn(), vo.getPrvcUseYn())) {
                    if("Y".equals(vo.getPrvcUseYn())){
                        vo.setPrvcRegId(changerUser.getUserId());
                        vo.setPrvcStartDt(now);
                        vo.setPrvcEndDt(endDt);
                    } else {
                        vo.setPrvcRegId("SYSTEM");
                        if (targetUser.getPrvcStartDt() != null) {
                            vo.setPrvcStartDt(targetUser.getPrvcStartDt().toLocalDate());
                        }
                        vo.setPrvcEndDt(now);
                    }
                    permissionChangelog = createPermissionChangeLog(changerUser.getUserId(), targetUser.getUserId(), "PRVC_USE_YN", targetUser.getPrvcUseYn(), vo.getPrvcUseYn(), "MODIFY", "관리자 개인정보처리권한 수정");
                } else {
                    // 변경되지 않은 경우 기존 값 유지
                    if (targetUser != null) {
                        vo.setPrvcRegId(targetUser.getPrvcRegId());
                        if (targetUser.getPrvcStartDt() != null) {
                            vo.setPrvcStartDt(targetUser.getPrvcStartDt().toLocalDate());
                        }
                        if (targetUser.getPrvcEndDt() != null) {
                            vo.setPrvcEndDt(targetUser.getPrvcEndDt().toLocalDate());
                        }
                    }
                }
                result = admin801Mapper.updateAdmin801(vo);
            }

            // 로깅
            if (permissionChangelog != null) {
                auditService.logPermissionChanges(List.of(permissionChangelog));
            }

            return result;
        } catch(BusinessException e) {
            throw e;
        } catch(Exception e) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "관리자 정보 저장 중 오류 발생");
        }
    }

    @Override
    public int unlockAccount(UnlockAccountDto dto) {
        try {
            int result = admin801Mapper.unlockAccount(dto);

            if (result == 0) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND, "해당 사용자를 찾을 수 없습니다.");
            }
            return result;
        } catch(BusinessException e) {
            throw e;
        } catch(Exception e) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "계정 잠금 해제 중 오류 발생");
        }
    }

    /**
     * 권한 변경 로그를 생성하는 헬퍼 메서드
     */
    private PermissionChangeLog createPermissionChangeLog(String chnrgUserId, String trgtUserId, String prmsnType, String oldVl, String newVl, String chgType, String rsn) {
        if (Objects.equals(oldVl, newVl)) {
            return null;
        }
        return PermissionChangeLog.builder()
                .chnrgUserId(chnrgUserId)
                .trgtUserId(trgtUserId)
                .prmsnType(prmsnType)
                .oldVl(oldVl)
                .newVl(newVl)
                .chgType(chgType)
                .chgDt(LocalDateTime.now())
                .rsn(rsn)
                .regDt(LocalDateTime.now())
                .regId(chnrgUserId)
                .build();
    }
}