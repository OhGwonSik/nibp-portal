package egovframework.portal.opinion.mapper;

import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import egovframework.portal.opinion.domain.PublicDataOpinionSaveDTO;

@Mapper
public interface OpinionMapper {

    /**
     * 공공데이터 의겸수렴 저장
     */
    int insertPublicDataOpinion(PublicDataOpinionSaveDTO publicDataOpinionSaveDTO);
}
