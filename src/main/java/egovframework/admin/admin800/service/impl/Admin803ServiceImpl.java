package egovframework.admin.admin800.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.admin.admin800.domain.*;
import egovframework.admin.admin800.mapper.Admin803Mapper;
import egovframework.admin.admin800.service.Admin803Service;
import egovframework.common.audit.domain.PermissionChangeLog;
import egovframework.common.audit.service.AuditService;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.auth.service.AuthService;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import egovframework.common.util.MaskingUtil;
import egovframework.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @ClassName : Admin803ServiceImpl.java
 * @Description : 권한 관리 서비스 구현체
 *
 * @author : balee
 * @since  : 2025. 11. 17
 * @version : 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Admin803ServiceImpl extends EgovAbstractServiceImpl implements Admin803Service {
    private final Admin803Mapper admin803Mapper;
    private final AuditService auditService; // AuditService 주입
    private final AuthService authService;
    private final MaskingUtil maskingUtil;

    @Override
    public PageInfo<Admin803AccountVO> selectAdmin803AccountList(Admin803FilterDTO filter) {
        PageHelper.startPage(filter.getPage(), filter.getSize(), filter.getSortBy());
        List<Admin803AccountVO> selectAdmin803AccountList = admin803Mapper.selectAdmin803AccountList(filter);

        if(SecurityUtil.getUser() != null && "N".equals(SecurityUtil.getUser().getPrvcUseYn())) {
            List<Admin803AccountVO> maskedList = new ArrayList<>();
            maskedList = maskingUtil.maskList(selectAdmin803AccountList);
            return new PageInfo<>(maskedList);
        }
        return new PageInfo<>(selectAdmin803AccountList);
    }

    @Override
    public List<Admin803MenuDTO> selectAdmin803MenuList(Admin803MenuDTO admin803MenuDTO) {
        // 1. DB에서 조회한 리스트
        List<Admin803MenuDTO> menuList = admin803Mapper.selectAdmin803MenuList(admin803MenuDTO);

        // 2. Map 생성
        Map<Long, Admin803MenuDTO> dtoMap = new HashMap<>();
        for (Admin803MenuDTO dto : menuList) {
            dtoMap.put(dto.getMenuOid(), dto);
        }

        // 3. 부모-자식 관계 구성
        for (Admin803MenuDTO dto : dtoMap.values()) {
            Long parentMenuNo = dto.getUpMenuOid();
            if (parentMenuNo != null) {
                Admin803MenuDTO parent = dtoMap.get(parentMenuNo);
                if (parent != null) {
                    parent.getSubMenus().add(dto);
                }
            }
        }

        // 4. 모든 subMenus 정렬 (기준 : menuOrder, regDt)
        for (Admin803MenuDTO dto : dtoMap.values()) {
            if (!dto.getSubMenus().isEmpty()) {
                dto.getSubMenus().sort(Comparator
                        .comparing(Admin803MenuDTO::getMenuSeq, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Admin803MenuDTO::getRegDt, Comparator.nullsLast(Comparator.naturalOrder()))
                );
            }
        }

        // 5. 최상위 메뉴만 추출
        List<Admin803MenuDTO> rootMenus = new ArrayList<>();
        for (Admin803MenuDTO dto : dtoMap.values()) {
            if (dto.getUpMenuOid() == null) {
                rootMenus.add(dto);
            }
        }

        // 6. 최상위 메뉴 정렬
        rootMenus.sort(Comparator
                .comparing(Admin803MenuDTO::getMenuSeq, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Admin803MenuDTO::getRegDt, Comparator.nullsLast(Comparator.naturalOrder()))
        );

        return rootMenus;
    }

    @Override
    public void upsertAdmin803MenuAuth(List<Admin803AuthDTO> authList, String changeReason) {
        String chnrgUserId = SecurityUtil.getUser().getUserId();
        String trgtUserId = null;

        if (authList == null || authList.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "변경할 메뉴 권한 목록이 비어 있습니다.");
        }

        BaseUser targetUser = authService.getUserByNo(authList.get(0).getUserOid());
        if (targetUser != null) {
            trgtUserId = targetUser.getUserId();
        } else {
            log.warn("Target user not found for userOid: {}", authList.get(0).getUserOid());
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "대상 사용자를 찾을 수 없습니다.");
        }

        List<Admin803AuthDTO> itemsToInsert = new ArrayList<>();
        List<Admin803AuthDTO> itemsToUpdate = new ArrayList<>();
        List<PermissionChangeLog> permissionChangeLogs = new ArrayList<>();

        try {
            log.debug("upsert menu auth for userOid: {}", authList.get(0).getUserOid());

            // 사용자의 모든 메뉴 권한을 한 번에 조회하여 Map으로 변환 (성능 최적화)
            List<Admin803AuthDTO> existingAuthList = admin803Mapper.selectUserMenuAuthListByUserNo(authList.get(0).getUserOid());
            Map<Long, Admin803AuthDTO> existingAuthMap = new HashMap<>();
            for (Admin803AuthDTO auth : existingAuthList) {
                existingAuthMap.put(auth.getMenuOid(), auth);
            }

            for (Admin803AuthDTO authItem : authList) {
                Admin803AuthDTO existingAuth = existingAuthMap.get(authItem.getMenuOid());

                authItem.setUserOid(authList.get(0).getUserOid());
                authItem.setRegId(chnrgUserId); // 변경자 ID로 설정
                authItem.setMdfcnId(chnrgUserId); // 변경자 ID로 설정

                // readYn 값으로 나머지 권한 설정
                // 개별 변환 할 시 반영할 것
                if ("Y".equals(authItem.getInqAuthrtYn())) {
                    authItem.setWrtAuthrtYn("Y");
                    authItem.setDelAuthrtYn("Y");
                    authItem.setExcelAuthrtYn("Y");
                    authItem.setOtptAuthrtYn("Y");
                    authItem.setUseYn("Y");
                } else if ("N".equals(authItem.getInqAuthrtYn())) {
                    authItem.setWrtAuthrtYn("N");
                    authItem.setDelAuthrtYn("N");
                    authItem.setExcelAuthrtYn("N");
                    authItem.setOtptAuthrtYn("N");
                    authItem.setUseYn("N");
                }

                if (existingAuth == null) {
                    if("Y".equals(authItem.getUseYn())){ // Y로 지정된 것만 생성하도록
                        // INSERT 대상
                        LocalDate authStartDt = LocalDate.now();
                        LocalDate authEndDt = LocalDate.now().plusYears(2); // 2년
                        authItem.setAuthBgngDt(authStartDt);
                        authItem.setAuthEndDt(authEndDt);
                        itemsToInsert.add(authItem);

                        // ADD 로그 준비
                        addPermissionChangeLog(permissionChangeLogs, chnrgUserId, trgtUserId, null, authItem.getMenuOid(), "USE_YN", null, authItem.getUseYn(), "ADD", changeReason);
                        addPermissionChangeLog(permissionChangeLogs, chnrgUserId, trgtUserId, null, authItem.getMenuOid(), "READ_YN", null, authItem.getInqAuthrtYn(), "ADD", changeReason);
                        addPermissionChangeLog(permissionChangeLogs, chnrgUserId, trgtUserId, null, authItem.getMenuOid(), "WRITE_YN", null, authItem.getWrtAuthrtYn(), "ADD", changeReason);
                        addPermissionChangeLog(permissionChangeLogs, chnrgUserId, trgtUserId, null, authItem.getMenuOid(), "DELETE_YN", null, authItem.getDelAuthrtYn(), "ADD", changeReason);
                        addPermissionChangeLog(permissionChangeLogs, chnrgUserId, trgtUserId, null, authItem.getMenuOid(), "EXCEL_YN", null, authItem.getExcelAuthrtYn(), "ADD", changeReason);
                        addPermissionChangeLog(permissionChangeLogs, chnrgUserId, trgtUserId, null, authItem.getMenuOid(), "PRINT_YN", null, authItem.getOtptAuthrtYn(), "ADD", changeReason);
                    }
                } else {
                    // UPDATE 대상 - 변경사항이 있는 경우만 처리
                    authItem.setUserMenuAuthrtOid(existingAuth.getUserMenuAuthrtOid());

                    // 변경 여부 체크
                    boolean hasChanges = false;

                    if (!Objects.equals(existingAuth.getUseYn(), authItem.getUseYn())) {
                        hasChanges = true;
                        addPermissionChangeLog(permissionChangeLogs, chnrgUserId, trgtUserId, authItem.getUserMenuAuthrtOid(), authItem.getMenuOid(), "USE_YN", existingAuth.getUseYn(), authItem.getUseYn(), "MODIFY", changeReason);
                    }
                    if (!Objects.equals(existingAuth.getInqAuthrtYn(), authItem.getInqAuthrtYn())) {
                        hasChanges = true;
                        addPermissionChangeLog(permissionChangeLogs, chnrgUserId, trgtUserId, authItem.getUserMenuAuthrtOid(), authItem.getMenuOid(), "READ_YN", existingAuth.getInqAuthrtYn(), authItem.getInqAuthrtYn(), "MODIFY", changeReason);
                    }
                    if (!Objects.equals(existingAuth.getWrtAuthrtYn(), authItem.getWrtAuthrtYn())) {
                        hasChanges = true;
                        addPermissionChangeLog(permissionChangeLogs, chnrgUserId, trgtUserId, authItem.getUserMenuAuthrtOid(), authItem.getMenuOid(), "WRITE_YN", existingAuth.getWrtAuthrtYn(), authItem.getWrtAuthrtYn(), "MODIFY", changeReason);
                    }
                    if (!Objects.equals(existingAuth.getDelAuthrtYn(), authItem.getDelAuthrtYn())) {
                        hasChanges = true;
                        addPermissionChangeLog(permissionChangeLogs, chnrgUserId, trgtUserId, authItem.getUserMenuAuthrtOid(), authItem.getMenuOid(), "DELETE_YN", existingAuth.getDelAuthrtYn(), authItem.getDelAuthrtYn(), "MODIFY", changeReason);
                    }
                    if (!Objects.equals(existingAuth.getExcelAuthrtYn(), authItem.getExcelAuthrtYn())) {
                        hasChanges = true;
                        addPermissionChangeLog(permissionChangeLogs, chnrgUserId, trgtUserId, authItem.getUserMenuAuthrtOid(), authItem.getMenuOid(), "EXCEL_YN", existingAuth.getExcelAuthrtYn(), authItem.getExcelAuthrtYn(), "MODIFY", changeReason);
                    }
                    if (!Objects.equals(existingAuth.getOtptAuthrtYn(), authItem.getOtptAuthrtYn())) {
                        hasChanges = true;
                        addPermissionChangeLog(permissionChangeLogs, chnrgUserId, trgtUserId, authItem.getUserMenuAuthrtOid(), authItem.getMenuOid(), "PRINT_YN", existingAuth.getOtptAuthrtYn(), authItem.getOtptAuthrtYn(), "MODIFY", changeReason);
                    }

                    // 변경사항이 있을 때만 업데이트 목록에 추가
                    if (hasChanges) {
                        itemsToUpdate.add(authItem);
                    }
                }
            }

            // 배치 INSERT
            if (!itemsToInsert.isEmpty()) {
                int insertResult = admin803Mapper.insertUserMenuAuth(itemsToInsert);
                if (insertResult == 0) {
                    throw new BusinessException(ErrorCode.DATABASE_ERROR, "메뉴 권한 등록에 실패했습니다.");
                }

                // INSERT 후 userMenuAuthrtOid 조회
                List<Admin803AuthDTO> insertedAuthList = admin803Mapper.selectUserMenuAuthListByUserNo(authList.get(0).getUserOid());
                Map<Long, Long> menuNoToAuthNoMap = new HashMap<>();
                for (Admin803AuthDTO auth : insertedAuthList) {
                    menuNoToAuthNoMap.put(auth.getMenuOid(), auth.getUserMenuAuthrtOid());
                }

                // 조회한 userMenuAuthNo를 itemsToInsert에 설정
                for (Admin803AuthDTO insertedItem : itemsToInsert) {
                    Long authNo = menuNoToAuthNoMap.get(insertedItem.getMenuOid());
                    insertedItem.setUserMenuAuthrtOid(authNo);
                }

                // 생성된 userMenuAuthNo를 로그에 반영
                for (Admin803AuthDTO insertedItem : itemsToInsert) {
                    for (PermissionChangeLog permissionlog : permissionChangeLogs) {
                        if (permissionlog.getChgType().equals("ADD") && permissionlog.getTrgtUserId().equals(trgtUserId) && permissionlog.getMenuOid().equals(insertedItem.getMenuOid())) {
                            permissionlog.setUserMenuAuthrtOid(insertedItem.getUserMenuAuthrtOid());
                        }
                    }
                }
            }

            // 배치 UPDATE
            if (!itemsToUpdate.isEmpty()) {
                int updateResult = admin803Mapper.updateUserMenuAuth(itemsToUpdate);
                if (updateResult == 0) {
                    throw new BusinessException(ErrorCode.DATABASE_ERROR, "메뉴 권한 수정에 실패했습니다.");
                }
            }

            // 권한 변경 로그 일괄 기록
            if (!permissionChangeLogs.isEmpty()) {
                auditService.logPermissionChanges(permissionChangeLogs);
            }

        } catch(BusinessException e) {
            throw e;
        } catch(Exception e) {
            log.error("메뉴 권한 저장 중 오류 발생", e);
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "메뉴 권한 저장 중 오류 발생");
        }
    }

    /**
     * 권한 변경 로그를 리스트에 추가하는 헬퍼 메서드
     */
    private void addPermissionChangeLog(List<PermissionChangeLog> logs, String chnrgUserId, String trgtUserId, Long userMenuAuthrtOid, Long menuOid, String prmsnType, String oldVl, String newVl, String chgType, String rsn) {
        if (Objects.equals(oldVl, newVl) && !chgType.equals("ADD") && !chgType.equals("REMOVE")) {
            return; // 값이 변경되지 않았으면 로그 기록 안함 (ADD/REMOVE는 값 동일해도 기록)
        }
        PermissionChangeLog log = PermissionChangeLog.builder()
                .chnrgUserId(chnrgUserId)
                .trgtUserId(trgtUserId)
                .userMenuAuthrtOid(userMenuAuthrtOid)
                .menuOid(menuOid)
                .prmsnType(prmsnType)
                .oldVl(oldVl)
                .newVl(newVl)
                .chgType(chgType)
                .chgDt(LocalDateTime.now())
                .rsn(rsn)
                .regDt(LocalDateTime.now())
                .regId(chnrgUserId)
                .build();
        logs.add(log);
    }

    @Override
    public void copyMenuAuth(Admin803CopyAuthRequestDTO requestDTO, String chnrgUserId) {
        Long sourceUserNo = requestDTO.getSourceUserNo();
        List<Long> targetUserNos = requestDTO.getTargetUserNos();
        String changeReason = requestDTO.getChangeReason();

        // 소스 유저 검증
        BaseUser sourceUser = authService.getUserByNo(sourceUserNo);
        if (sourceUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "소스 사용자를 찾을 수 없습니다.");
        }

        try {
            // 1. 타겟 유저들의 기존 메뉴 권한 삭제
            admin803Mapper.deleteUserMenuAuthByUserNos(targetUserNos);

            // 2. 소스 유저의 활성 권한을 타겟 유저들에게 복사 (INSERT ... SELECT CROSS JOIN)
            int copyResult = admin803Mapper.copyMenuAuthFromSource(sourceUserNo, targetUserNos, chnrgUserId);
            log.debug("권한 복사 완료: 소스={}, 타겟={}명, 삽입={}건", sourceUser.getUserId(), targetUserNos.size(), copyResult);

            // 3. 감사 로그 일괄 기록
            List<PermissionChangeLog> logs = new ArrayList<>();
            for (Long targetUserNo : targetUserNos) {
                BaseUser targetUser = authService.getUserByNo(targetUserNo);
                if (targetUser != null) {
                    logs.add(PermissionChangeLog.builder()
                        .chnrgUserId(chnrgUserId)
                        .trgtUserId(targetUser.getUserId())
                        .userMenuAuthrtOid(null)
                        .menuOid(null)
                        .prmsnType("ALL")
                        .oldVl(null)
                        .newVl("COPY_FROM:" + sourceUser.getUserId())
                        .chgType("COPY")
                        .chgDt(LocalDateTime.now())
                        .rsn(changeReason)
                        .regDt(LocalDateTime.now())
                        .regId(chnrgUserId)
                        .build());
                }
            }
            if (!logs.isEmpty()) {
                auditService.logPermissionChanges(logs);
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("권한 복사 중 오류 발생", e);
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "권한 복사 중 오류 발생");
        }
    }

    @Override
    public void updateAdmin803PrivacyAuth(Admin803AccountDTO admin803AccountDTO) {
        try {
            log.debug("update privacy auth for userOid: {}", admin803AccountDTO.getUserOid());

            int updateResult = admin803Mapper.updateAdmin803PrivacyAuth(admin803AccountDTO);
            if (updateResult == 0) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "개인정보취급권한 수정에 실패했습니다.");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Update privacy auth error", e);
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "개인정보취급권한 수정 중 오류 발생");
        }
    }
}