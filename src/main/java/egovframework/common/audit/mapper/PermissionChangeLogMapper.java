package egovframework.common.audit.mapper;

import egovframework.common.audit.domain.PermissionChangeLog;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface PermissionChangeLogMapper {
    /**
     * 권한 변경 로그 저장
     * @param log 권한 변경 로그 객체
     * @return
     */
    int insertPermissionChangeLog(List<PermissionChangeLog> log);
}
