package egovframework.portal.user.mapper;

import egovframework.portal.user.domain.User;
import egovframework.portal.user.domain.UserCertificateDto;
import egovframework.portal.user.domain.UserDTO;
import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;

@Mapper
public interface UserMapper {
    int insertUser(User vo);

    int updateUser(User vo);

    void insertUserCertificate(UserCertificateDto dto);

    boolean existsCertificateByUserNo(Long userOid);

    int countUserIdForInsert(EgovMap map);

    int countUserIdForUpdate(EgovMap egovMap);

    int countUserEmail(EgovMap map);

    int countUserByIdentity(EgovMap map);

    User selectUserByUserNoDecrypted(@Param("userOid") Long userOid, @Param("aesKey") String aesKey);

    UserCertificateDto selectCertificateByUserNo(@Param("userOid") Long userOid);

    int updatePwd(UserDTO userDTO);

    String getNextWithdrawSeq();

    int withdrawUser(UserDTO userDTO);
}
