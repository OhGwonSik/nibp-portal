package egovframework.portal.opinion.service;

import java.io.IOException;

import egovframework.portal.opinion.domain.PublicDataOpinionSaveDTO;

public interface OpinionService {

	int insertPublicDataOpinion(PublicDataOpinionSaveDTO publicDataOpinionSaveDTO) throws RuntimeException, IOException;
}
