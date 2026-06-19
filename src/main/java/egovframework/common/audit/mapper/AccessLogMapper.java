package egovframework.common.audit.mapper;

import egovframework.common.audit.domain.ApiAccessLog;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

@Mapper
public interface AccessLogMapper {
    /**
     * 접근 로그 저장 (API, 페이지)
     * @param apiAccessLog
     * @return
     */
    int insertAccessLog(ApiAccessLog apiAccessLog);
}
