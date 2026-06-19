package egovframework.common.audit.service.impl;

import egovframework.common.audit.domain.LoginLog;
import egovframework.common.audit.mapper.AuditMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogAsyncWriter {

    private final AuditMapper auditMapper;

    @Async("asyncExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertLoginLog(LoginLog loginLog) {
        log.info("Inserting login log for user: {}", loginLog.getUserId());
        try {
            int result = auditMapper.insertLoginLog(loginLog);
            if (result == 0) {
                log.error("Failed to insert login log for user: {}", loginLog.getUserId());
            }
        } catch (Exception e) {
            log.error("Failed to insert login log for user: {}", loginLog.getUserId(), e);
        }
        log.info("Inserting end login log for user: {}", loginLog.getUserId());
    }
}
