package egovframework.common.audit.mapper;

import egovframework.common.audit.domain.PersonalInfoProcLog;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

@Mapper
public interface PersonalInfoProcLogMapper {
    /**
     * 개인정보 처리 로그 저장
     * @param personalInfoProcLog
     * @return
     */
    int insertPersonalInfoProcLog(PersonalInfoProcLog personalInfoProcLog);
}
