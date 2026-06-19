package egovframework.common.board.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import egovframework.common.board.domain.BoardDataSetResponseDTO;
import egovframework.common.board.domain.BoardFilter;
import egovframework.common.board.domain.BoardRequestDto;
import egovframework.common.board.dto.*;
import egovframework.common.board.enums.BoardType;
import egovframework.common.board.enums.PostStatus;
import egovframework.common.board.mapper.BoardMapper;
import egovframework.common.board.service.BoardService;
import egovframework.common.content.ContentProcessService;
import egovframework.common.excel.component.ExcelComponent;
import egovframework.common.excel.domain.ExcelConfig;
import egovframework.common.excel.domain.ExcelExportResult;
import egovframework.common.excel.domain.ExcelPageConfigDTO;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import egovframework.common.file.domain.FileDTO;
import egovframework.common.file.mapper.FileMapper;
import egovframework.common.file.service.FileService;
import egovframework.common.util.HtmlUtil;
import egovframework.common.auth.domain.BaseUser;
import egovframework.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardServiceImpl extends EgovAbstractServiceImpl implements BoardService {

    private final BoardMapper boardMapper;
    private final PasswordEncoder passwordEncoder;
	private final FileService fileService;
    private final FileMapper fileMapper;
    private final ContentProcessService contentProcessService;
	private final ExcelConfig excelConfig;
	private final ExcelComponent excelComponent;
    private static final String TABLE_BOARD_POST = "bbs_pst";

    @Override
    public BoardDTO selectBoard(BoardRequestDto boardRequestDto) {
        return boardMapper.selectBoard(boardRequestDto);
    }

    @Override
    public List<BoardDataSetResponseDTO> selectBoardsBySubBoardType(List<BoardRequestDto> requests) {
        List<BoardDataSetResponseDTO> resultList = new ArrayList<>();
        List<Long> firstBbsPstOids = new ArrayList<>(); // 첫 번째 게시글 번호들 수집

        for (BoardRequestDto requestDto : requests) {
            try {
                // 게시판 설정 정보 조회 (1회만 조회 - 중복 쿼리 제거)
                BoardDTO boardInfo = boardMapper.selectBoardBySubBoardType(requestDto);

                if (boardInfo != null) {
                    // 게시판 번호와 메뉴 코드 설정
                    requestDto.setBbsOid(boardInfo.getBbsOid());
                    requestDto.setMenuCd(boardInfo.getMenuCd());

                    // 페이징 설정 (메인 페이지는 4개만 표시)
                    PageHelper.startPage(1, 4);

                    // 대시보드용 경량 쿼리 직접 호출 (N+1 없음, 중복 selectBoard 호출 제거)
                    List<BoardPostDTO> postList = boardMapper.selectDashboardPostList(requestDto);
                    PageInfo<BoardPostDTO> pageInfo = PageInfo.of(postList);

                    // 첫 번째 게시글 번호 수집 (나중에 첨부파일 일괄 조회용)
                    if (!CollectionUtils.isEmpty(postList)) {
                        firstBbsPstOids.add(postList.get(0).getBbsPstOid());
                    }

                    // 키 생성 (MEDIA_TRENDS의 경우 카테고리별로 구분)
                    String key;
                    if ("MEDIA_TRENDS".equals(requestDto.getBbsSubSeCd()) && StringUtils.hasText(requestDto.getCtgry())) {
                        if ("국내언론동향".equals(requestDto.getCtgry())) {
                            key = "MEDIA_TRENDS_DOMESTIC";
                        } else if ("해외언론동향".equals(requestDto.getCtgry())) {
                            key = "MEDIA_TRENDS_OVERSEAS";
                        } else {
                            key = requestDto.getBbsSubSeCd();
                        }
                    } else {
                        key = requestDto.getBbsSubSeCd();
                    }

                    // DTO 생성 및 추가
                    BoardDataSetResponseDTO responseDTO = BoardDataSetResponseDTO.builder()
                            .key(key)
                            .board(boardInfo)
                            .list(pageInfo)
                            .build();

                    resultList.add(responseDTO);
                }
            } catch (Exception e) {
                log.error("게시판 데이터 조회 중 오류 발생 - subBoardType: {}, category: {}",
                        requestDto.getBbsSubSeCd(), requestDto.getCtgry(), e);
                // 에러 발생 시 빈 데이터 추가
                BoardDataSetResponseDTO errorDTO = BoardDataSetResponseDTO.builder()
                        .key(requestDto.getBbsSubSeCd())
                        .board(null)
                        .list(null)
                        .build();
                resultList.add(errorDTO);
            }
        }

        // 첨부파일 일괄 조회 (N+1 방지 - 1회 쿼리로 모든 첫 번째 게시글 첨부파일 조회)
        if (!CollectionUtils.isEmpty(firstBbsPstOids)) {
            List<FileDTO> allAttachments = boardMapper.selectFirstPostAttachmentsByBbsPstOids(firstBbsPstOids);

            // 게시글 번호별로 첨부파일 그룹핑
            Map<Long, List<FileDTO>> attachmentMap = new HashMap<>();
            for (FileDTO file : allAttachments) {
                attachmentMap.computeIfAbsent(file.getTblOid(), k -> new ArrayList<>()).add(file);
            }

            // 각 결과의 첫 번째 게시글에 첨부파일 설정
            for (BoardDataSetResponseDTO responseDTO : resultList) {
                if (responseDTO.getList() != null && !CollectionUtils.isEmpty(responseDTO.getList().getList())) {
                    @SuppressWarnings("unchecked")
                    List<BoardPostDTO> posts = (List<BoardPostDTO>) responseDTO.getList().getList();
                    if (!posts.isEmpty()) {
                        BoardPostDTO firstPost = posts.get(0);
                        List<FileDTO> attachments = attachmentMap.get(firstPost.getBbsPstOid());
                        if (attachments != null) {
                            firstPost.setAttachments(attachments);
                        }
                    }
                }
            }
        }

        return resultList;
    }

    @Override
    public PageInfo<?> selectBoardPostListWithFilter(BoardRequestDto boardRequestDto, BoardFilter filter) {
        // 게시판 정보 조회
        BoardDTO boardInfo = boardMapper.selectBoard(boardRequestDto);

        // 페이징 설정
        if(filter != null && filter.getPage() != null && filter.getSize() != null){
            PageHelper.startPage(filter.getPage(), filter.getSize());
        }

        // 결과 페이징 리스트
        List<?> resultList;

        // 게시판 타입에 따른 분기 처리
        if (boardInfo != null && boardInfo.getBbsSeCd() != null) {
            BoardType boardType = BoardType.fromCode(boardInfo.getBbsSeCd());
            if (boardType != null) {
                switch (boardType) {
                    case COMMON_BOARD, USER_BOARD, FAQ_BOARD:
                        // 일반 게시판, 사용자 게시판
                        boardRequestDto.setIsPermanentDisplayBoard(
                            BoardType.isPermanentDisplayBoard(boardInfo.getBbsSubSeCd()) ? "Y" : "N"
                        );
                        resultList = boardMapper.selectBoardPostList(boardRequestDto);
                        break;
                    case QNA_BOARD:
                        // FAQ, QnA
                        resultList = boardMapper.selectBoardPostListTypeB(boardRequestDto);
                        break;
                    case PHOTO_GALLERY_BOARD, THUMBNAIL_BOARD:
                        // 포토갤러리 게시판, 썸네일 게시판
                        resultList = boardMapper.selectBoardPostListTypeImage(boardRequestDto);
                        break;
                    default:
                        // 기본 처리
                        resultList = boardMapper.selectBoardPostList(boardRequestDto);
                        break;
                }
            } else {
                // 게시판 타입이 null인 경우
                resultList = boardMapper.selectBoardPostList(boardRequestDto);
            }
        } else {
            // 게시판 정보가 없는 경우
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "게시판 정보가 존재하지 않습니다.");
        }

        return PageInfo.of(resultList);
    }

    @Override
    public BoardPostDTO selectBoardPostDetail(BoardRequestDto boardRequestDto) {
        // 게시판 정보 조회
        BoardDTO boardInfo = boardMapper.selectBoard(boardRequestDto);

        if (boardInfo == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "게시판 정보가 존재하지 않습니다.");
        }

        // 게시판 타입에 따른 분기 처리
        BoardPostDTO post = null;
        BoardType boardType = BoardType.fromCode(boardInfo.getBbsSeCd());

        if (boardType != null) {
            switch (boardType) {
                case QNA_BOARD:
                    // QnA
                    post = boardMapper.selectBoardPostDetailWithAnswer(boardRequestDto);
                    break;
                case PHOTO_GALLERY_BOARD, THUMBNAIL_BOARD:
                    // 포토갤러리 게시판, 썸네일 게시판
                	post = boardMapper.selectBoardPostTypeImage(boardRequestDto);
                    break;
                default:
                    // 일반 게시판
                    post = boardMapper.selectBoardPostDetail(boardRequestDto);
                    break;
            }
        } else {
            post = boardMapper.selectBoardPostDetail(boardRequestDto);
        }

        if (post == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글이 존재하지 않습니다.");
        }

        // 게시글 내용 내 HTML 엔티티 디코딩 (모든 타입 공통)
        if (post.getBbsPstCn() != null) {
            post.setBbsPstCn(HtmlUtil.htmlDecode(post.getBbsPstCn()));
        }

        // QnA 게시판인 경우에만 답변 내용 내 HTML 엔티티 디코딩
        if (BoardType.QNA_BOARD.name().equals(boardInfo.getBbsSeCd())) {
            BoardPostDTO answer = post.getAnswers();
            if (answer != null && answer.getBbsPstCn() != null) {
                answer.setBbsPstCn(HtmlUtil.htmlDecode(answer.getBbsPstCn()));
            }
        }

        // 해당 게시글 첨부 파일 목록 조회
        if (boardInfo.getFileUldCnt() != null && boardInfo.getFileUldCnt() > 0) {
            EgovMap egovMap = new EgovMap();
            egovMap.put("tblNm", "bbs_pst");              // 어떤 테이블의 첨부 파일인지
            egovMap.put("tblOid", boardRequestDto.getBbsPstOid());    // 어떤 테이블의 어떤 pk의 첨부 파일인지
            List<FileDTO> attachFileList = fileMapper.selectAttachmentFileByTableNameAndTablePk(egovMap);

            // 해당 게시글 첨부 파일 목록 DTO에 추가
            post.setAttachments(attachFileList);
        }

        return post;
    }

    @Override
    public BoardPostDTO selectBoardPostById(Long bbsPstOid) {
        if (bbsPstOid == null) {
            throw new IllegalArgumentException("게시글 번호는 필수입니다.");
        }

        return boardMapper.selectBoardPostById(bbsPstOid);
    }
    
    @Override
    public BoardPostDTO selectPrevBoardPost(BoardPostDTO boardPostDTO) {
        return boardMapper.selectPrevBoardPost(boardPostDTO);
    }

    @Override
    public BoardPostDTO selectNextBoardPost(BoardPostDTO boardPostDTO) {
        return boardMapper.selectNextBoardPost(boardPostDTO);
    }

    @Transactional
    @Override
    public void insertBoardPost(BoardPostInsertDTO boardPostInsertDTO, List<MultipartFile> files, MultipartFile thumbnailFile) {
        try {
        	// 썸네일 먼저 처리
        	String userId = "SYSTEM";
        	String userNm = boardPostInsertDTO.getWrtrNm();
        	Long userOid = null;
        	if(SecurityUtil.getAuthentication() != null && SecurityUtil.getAuthentication().isAuthenticated() && SecurityUtil.getUser() != null) {
        		userId = SecurityUtil.getUser().getUserId();
        		userNm = SecurityUtil.getUser().getUserNmKorn();
        		userOid = SecurityUtil.getUser().getUserOid();
        	}

            if(boardPostInsertDTO.getWrtrNm() == null) {
                boardPostInsertDTO.setWrtrNm(userNm);
            }

            if(userOid != null) {
                boardPostInsertDTO.setUserOid(userOid);
            }

            EgovMap thumbnailMap = new EgovMap();
            
        	if(thumbnailFile != null && !thumbnailFile.isEmpty()) {
        		thumbnailMap.put("path", "board"); // 공통 파일 경로 지정
        		thumbnailMap.put("uploadFiles", Collections.singletonList(thumbnailFile));
        		thumbnailMap.put("thmbYn", "Y");
        		thumbnailMap.put("fileType", "THUMBNAIL");
                thumbnailMap.put("regUserId", userId);
                thumbnailMap.put("mdfcnId", userId);
                
                //파일/본문 정규화
                fileService.processFiles(thumbnailMap);
        	}

            EgovMap egovMap = new EgovMap();
            egovMap.put("path", "board"); // 공통 파일 경로 지정
            egovMap.put("uploadFiles", files);
            egovMap.put("attachedFiles", boardPostInsertDTO.getAttachedFiles());
            egovMap.put("editorFiles", boardPostInsertDTO.getEditorFiles());
            egovMap.put("regUserId", userId);
            egovMap.put("mdfcnId", userId);
            egovMap.put("content", boardPostInsertDTO.getBbsPstCn());

            // 1) 파일/본문 정규화 (임시 → 실제 경로 이동, CKEditor 본문 치환)
            fileService.processFiles(egovMap);

        	//파일 처리
            boardPostInsertDTO.setBbsPstCn(contentProcessService.processHtmlContent((String) egovMap.get("content")));

            // contents_text 생성
            boardPostInsertDTO.setBbsPstCnTxt(HtmlUtil.stripHtml(boardPostInsertDTO.getBbsPstCn()));

            // 비밀번호 암호화 처리
            // encodePasswordIfPresent(boardPostInsertDTO);

            // 게시글 저장
            savePost(boardPostInsertDTO);

            // 게시글 번호 검증
            Long bbsPstOid = validateBbsPstOid(boardPostInsertDTO.getBbsPstOid());

            if (bbsPstOid == 0) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 저장 에러");
            }
            
            // Q&A인 경우 생명윤리QNA 확인 및 비밀글 여부 확인
            BoardPostDTO qnaPostConfig = getBoardPostParentSecretYn(boardPostInsertDTO.getBbsPstOid());
            String boardType = BoardType.QNA_BOARD.getBoardTypeCd(); // QNA
            
            if(qnaPostConfig != null){
                // 생명윤리 QNA인 경우 히스토리(사용자만)
                if(boardType.equals(qnaPostConfig.getBbsSeCd())) {
                    
                    boardPostInsertDTO.setTblNm("bbs_pst");
                    if(boardPostInsertDTO.getUpBbsPstOid() != null && boardPostInsertDTO.getUpBbsPstOid() != 0) {
                        boardPostInsertDTO.setTblOid(boardPostInsertDTO.getUpBbsPstOid());
                    }else {
                        boardPostInsertDTO.setTblOid(boardPostInsertDTO.getBbsPstOid());
                    }
                    
                    boardPostInsertDTO.setRegId(userId);
                    
                    int count = boardMapper.insertUserBoardHistory(boardPostInsertDTO);
                    if(count == 0) {
                        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 히스토리 저장 에러");
                    }
                }
            }
            // 3) file 메타 기록: 방금 생성된 bbsPstOid tablePk로 연결 (썸네일)
            if(thumbnailFile != null && !thumbnailFile.isEmpty()) {
                thumbnailMap.put("tblNm", TABLE_BOARD_POST);
                thumbnailMap.put("tblOid", bbsPstOid);
                fileService.saveFileMeta(thumbnailMap);
            }

            // 4) file 메타 기록: 방금 생성된 bbsPstOid tablePk로 연결 (파일 / 사진)
            egovMap.put("tblNm", TABLE_BOARD_POST);
            egovMap.put("tblOid", bbsPstOid);
            fileService.saveFileMeta(egovMap);

            // 5) 첫 번째 첨부파일의 alt_text 업데이트
            if (StringUtils.hasText(boardPostInsertDTO.getFirstFileAltText())) {
                updateFirstFileAltText(bbsPstOid, boardPostInsertDTO.getFirstFileAltText(), userId);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("게시글 저장 중 오류 발생", e);
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "게시글 저장 중 오류 발생");
        }
    }
    
	@Override
	public void updateBoardPost(BoardPostInsertDTO boardPostInsertDTO, List<MultipartFile> files,
			MultipartFile thumbnailFile) {
		try {
        	String userId = "SYSTEM";
        	String userNm = boardPostInsertDTO.getWrtrNm();
        	Long userOid = null;
        	if(SecurityUtil.getAuthentication() != null && SecurityUtil.getAuthentication().isAuthenticated() && SecurityUtil.getUser() != null) {
        		userId = SecurityUtil.getUser().getUserId();
        		userNm = SecurityUtil.getUser().getUserNmKorn();
        		userOid = SecurityUtil.getUser().getUserOid();
        	}

            boardPostInsertDTO.setMdfcnId(userId);
            
            if(boardPostInsertDTO.getWrtrNm() == null) {
                boardPostInsertDTO.setWrtrNm(userNm);
            }
            
            if(userOid != null) {
                boardPostInsertDTO.setUserOid(userOid);
            }   

            // 3. 파일 처리를 위한 공통 설정값 준비
            Long bbsPstOid = boardPostInsertDTO.getBbsPstOid();

            EgovMap commonMap = new EgovMap();
            commonMap.put("tblNm", TABLE_BOARD_POST);
            commonMap.put("tblOid", bbsPstOid);
            commonMap.put("path", "board");
            commonMap.put("regUserId", userId);
            commonMap.put("mdfcnId", userId);

            // 5. [일반/에디터 파일 처리] Raw, Chunk, Editor 중 하나라도 있으면 수행
            boolean hasRaw = (files != null && !files.isEmpty());
            boolean hasChunk = !CollectionUtils.isEmpty(boardPostInsertDTO.getAttachedFiles());
            boolean hasEditor = !CollectionUtils.isEmpty(boardPostInsertDTO.getEditorFiles());

            if (hasRaw || hasChunk || hasEditor) {

                EgovMap fileMap = new EgovMap();
                fileMap.putAll(commonMap);

                fileMap.put("uploadFiles", files); // 일반 파일 (List)
                fileMap.put("attachedFiles", boardPostInsertDTO.getAttachedFiles()); // Chunk 메타
                fileMap.put("editorFiles", boardPostInsertDTO.getEditorFiles()); // 에디터 메타
                fileMap.put("content", boardPostInsertDTO.getBbsPstCn());

                // fileType, thmbYn은 공통 서비스 기본값 사용 ('ATTACHMENT', 'N')

                fileService.processFiles(fileMap); // 파일 정규화

                boardPostInsertDTO.setBbsPstCn(contentProcessService.processHtmlContent((String) fileMap.get("content")));

                fileService.saveFileMeta(fileMap); // DB 메타 저장
            }

            // contents_text 생성
            boardPostInsertDTO.setBbsPstCnTxt(HtmlUtil.stripHtml(boardPostInsertDTO.getBbsPstCn()));

            // 비밀번호 암호화 및 게시글 정보 Update
            // encodePasswordIfPresent(boardPostInsertDTO);
            if (boardMapper.updateBoardPost(boardPostInsertDTO) == 0) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "게시글 수정 실패 (대상 없음)");
            }

            // 2. 삭제 대상 파일(일반+썸네일+에디터) 일괄 삭제 처리
            List<Long> delIds = new ArrayList<>();
            
            // 일반/썸네일 파일 삭제 목록 체크
            if (!CollectionUtils.isEmpty(boardPostInsertDTO.getDeleteAttachNos())) {
                delIds.addAll(boardPostInsertDTO.getDeleteAttachNos());
            }

            // 에디터 이미지 삭제 목록 체크
            if (!CollectionUtils.isEmpty(boardPostInsertDTO.getDeleteEditorAttachNos())) {
                delIds.addAll(boardPostInsertDTO.getDeleteEditorAttachNos());
            }
            
            if (!delIds.isEmpty()) {
                fileService.deleteFilesByFileNos(delIds, userId);
            }

            // 4. [썸네일 처리] 파일이 넘어온 경우에만 수행 (다른 게시판 영향 없음)
            if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
                EgovMap thumbMap = new EgovMap();
                thumbMap.putAll(commonMap);
                
                // 단일 파일을 리스트로 감싸서 전달 (ClassCastException 방지 필수)
                thumbMap.put("uploadFiles", Collections.singletonList(thumbnailFile));
                
                thumbMap.put("thmbYn", "Y");
                thumbMap.put("fileType", "THUMBNAIL");

                fileService.processFiles(thumbMap); // 파일 물리 저장
                fileService.saveFileMeta(thumbMap); // DB 메타 저장
            }

            // 5) 첫 번째 첨부파일의 alt_text 업데이트
            if (StringUtils.hasText(boardPostInsertDTO.getFirstFileAltText())) {
                updateFirstFileAltText(bbsPstOid, boardPostInsertDTO.getFirstFileAltText(), userId);
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("게시글 수정 중 오류 발생", e);
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "게시글 수정 중 오류 발생");
        }
    }

	// DB단에서 하기로 변경
//    private void encodePasswordIfPresent(BoardPostInsertDTO boardPostInsertDTO) {
//        String rawPassword = boardPostInsertDTO.getWrtrPswd();
//        if (StringUtils.hasText(rawPassword)) {
//            boardPostInsertDTO.setWrtrPswd(passwordEncoder.encode(rawPassword));
//        } else {
//            boardPostInsertDTO.setWrtrPswd(null);
//        }
//    }

    private void savePost(BoardPostInsertDTO boardPostInsertDTO) {
        int result = boardMapper.insertBoardPost(boardPostInsertDTO);
        if (result == 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "게시글 저장에 실패했습니다.");
        }
    }
    
    private void saveQnaPostSatisfaction(BoardPostInsertDTO boardPostInsertDTO) {
        int result = boardMapper.insertBoardPostQnaSatisfaction(boardPostInsertDTO);
        if (result == 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "Q&A 만족도 평가 생성에 실패했습니다.");
        }
    }

    private Long validateBbsPstOid(Long bbsPstOid) {
        if (bbsPstOid == null) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "게시글 번호 생성 실패");
        }
        return bbsPstOid;
    }

    @Override
    public int insertBoardComment(BoardCommentDTO boardCommentDTO) {
        int count = boardMapper.insertBoardComment(boardCommentDTO);
        return count;
    }

    @Override
    public List<BoardCommentDTO> selectBoardCommentList(BoardPostDTO boardPostDTO) {
        return boardMapper.selectBoardCommentList(boardPostDTO.getBbsPstOid());
    }

	@Override
	public List<FileDTO> selectBoardAttachList(FileDTO fileDTO) {
		return boardMapper.selectBoardAttachList(fileDTO);
	}

	@Override
	public void updateBoardViewCnt(BoardPostDTO boardPostDTO) {
		boardMapper.updateBoardViewCnt(boardPostDTO);
	}

	@Override
	public Boolean checkBoardPassword(Long bbsPstOid, String password) {
		if (bbsPstOid == null) {
			throw new IllegalArgumentException("게시글 번호는 필수입니다.");
		}
		if (password == null || password.trim().isEmpty()) {
			throw new IllegalArgumentException("비밀번호는 필수입니다.");
		}

		BoardPostDTO post = boardMapper.selectBoardPostById(bbsPstOid);
		if (post == null) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글이 존재하지 않습니다.");
		}

		// 저장되어있는 비밀번호 (암호화되어있음)
		String dbPassword = post.getWrtrPswd();
		if (dbPassword == null) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "비밀번호가 존재하지 않습니다.");
		}
		
		BoardPostDTO checkPass = boardMapper.checkBoardPostPassword(bbsPstOid, password);
		
		if(checkPass == null) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "비밀번호가 일치 하지 않습니다.");
		}

		// 저장된 패스워드 / 입력한 패스워드 비교
		return true;
	}

	@Override
	public int deleteBoardPostAndComment(List<BoardPostDTO> boardPostDTO) {
		int count = 0;
		BaseUser user = SecurityUtil.getUser();
		String userId = user != null ? user.getUserId() : "ANONYMOUS";
		
		for(BoardPostDTO param : boardPostDTO) {
			param.setMdfcnId(userId);
			
			if(param.getBbsPstOid() != null || param.getBbsCmntOid() != null) {
				boardMapper.deleteBoardComment(param);
			}
			
			count = boardMapper.deleteBoardPost(param);
			if(count == 0) {
				throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 삭제를 실패하였습니다.");
			}
		}
		
		return count;
	}

	@Override
	public boolean hasReply(Long bbsPstOid) {
		return boardMapper.countReplyByBbsPstOid(bbsPstOid) > 0;
	}

	@Override
	public int deleteBoardComment(BoardPostDTO boardPostDTO) {
		int count = 0;
		count = boardMapper.deleteBoardComment(boardPostDTO);
		if(count == 0) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "댓글 삭제 실패하였습니다.");
		}
		return count;
	}

    /**
     * 첫 번째 첨부파일의 alt_text 업데이트
     */
    private void updateFirstFileAltText(Long bbsPstOid, String altText, String userId) {
        if (bbsPstOid == null || !StringUtils.hasText(altText)) {
            return;
        }

        // 첫 번째 첨부파일 조회 (attach_order = 1, inline_yn = 'N')
        EgovMap egovMap = new EgovMap();
        egovMap.put("tblNm", TABLE_BOARD_POST);
        egovMap.put("tblOid", bbsPstOid);
        egovMap.put("inlineYn", "N");
        egovMap.put("attachOrder", 1);

        List<FileDTO> files = fileMapper.selectAttachmentFileByTableNameAndTablePk(egovMap);

        if (files != null && !files.isEmpty()) {
            FileDTO firstFile = files.get(0);
            log.info("첫 번째 첨부파일 alt_text 업데이트 - fileOid: {}, altText: {}", firstFile.getFileOid(), altText);

            EgovMap updateMap = new EgovMap();
            updateMap.put("fileOid", firstFile.getFileOid());
            updateMap.put("imgSbstTxtCn", altText);
            updateMap.put("mdfcnId", userId);

            fileMapper.updateFileAltText(updateMap);
        }
    }

    public Map<String, Object> getAdminDashboardModel() {
        Map<String, Object> resultMap = new HashMap<>();
        String menuAuthLv = "ADMIN";

        // 1. 생명윤리법 Q&A
        BoardDTO qnaBoard = getBoardInfo(BoardType.BIOETHICS_QNA_BOARD, menuAuthLv);
        resultMap.put("qnaList", getDashboardPosts(qnaBoard, 3));
        resultMap.put("qnaBbsOid", qnaBoard.getBbsOid());

        // 2. 정책원 앨범
        BoardDTO galleryBoard = getBoardInfo(BoardType.INSTITUTE_ALBUM_BOARD, menuAuthLv);
        resultMap.put("galleryList", getDashboardPosts(galleryBoard, 4));
        resultMap.put("galleryBbsOid", galleryBoard.getBbsOid());

        // 3. 국내 언론동향
        BoardDTO domesticNewsBoard = getBoardInfo(BoardType.MEDIA_TRENDS_DOMESTIC_BOARD, menuAuthLv);
        resultMap.put("domesticMediaBbsOid", domesticNewsBoard.getBbsOid());
        resultMap.put("domesticNewsList", getDashboardPosts(domesticNewsBoard, 4));

        // 4. 해외 언론동향
        BoardDTO foreignNewsBoard = getBoardInfo(BoardType.MEDIA_TRENDS_OVERSEAS_BOARD, menuAuthLv);
        resultMap.put("foreignMediaBbsOid", foreignNewsBoard.getBbsOid());
        resultMap.put("foreignNewsList", getDashboardPosts(foreignNewsBoard, 4));

        return resultMap;
    }

    // 공통 로직: 게시판 정보 조회
    private BoardDTO getBoardInfo(BoardType type, String menuAuthLv) {
        BoardRequestDto dto = new BoardRequestDto();
        dto.setBbsSubSeCd(type.getBbsSubSeCd());
        dto.setMenuAuthLv(menuAuthLv);
        return boardMapper.selectBoardBySubBoardType(dto);
    }

    // 공통 로직: 게시물 리스트 조회
    private List<?> getBoardPosts(BoardDTO board, int size) {
        BoardRequestDto requestDto = new BoardRequestDto();
        requestDto.setBbsOid(board.getBbsOid());

        BoardFilter filter = BoardFilter.builder()
                .page(1)
                .size(size)
                .build();

        return selectBoardPostListWithFilter(requestDto, filter).getList();
    }

    // 대시보드 전용: 경량 게시물 리스트 조회 (selectBoard 중복 제거, N+1 제거)
    private List<?> getDashboardPosts(BoardDTO board, int size) {
        BoardRequestDto requestDto = new BoardRequestDto();
        requestDto.setBbsOid(board.getBbsOid());

        PageHelper.startPage(1, size);

        // getBoardInfo에서 이미 조회한 bbs_se_cd으로 분기 (selectBoard 재호출 없음)
        BoardType boardType = BoardType.fromCode(board.getBbsSeCd());

        if (boardType == BoardType.QNA_BOARD) {
            // Q&A: 기존 JOIN 방식 쿼리 재사용 (N+1 없음)
            return boardMapper.selectBoardPostListTypeB(requestDto);
        } else if (boardType == BoardType.PHOTO_GALLERY_BOARD || boardType == BoardType.THUMBNAIL_BOARD) {
            // 갤러리: 대시보드 전용 쿼리 (N+1 제거, 썸네일 서브쿼리 유지)
            return boardMapper.selectDashboardPostListTypeImage(requestDto);
        } else {
            // 일반/뉴스: 대시보드 전용 쿼리 (N+1 제거)
            return boardMapper.selectDashboardPostList(requestDto);
        }
    }

	@Override
	public int insertSatisfactionByQnaBoardPost(BoardPostSatisfactionDTO boardPostSatisfactionDTO) {
		int count = 0;
		
		String updId = "SYSTEM";
    	if(SecurityUtil.getAuthentication() != null && SecurityUtil.getAuthentication().isAuthenticated() && SecurityUtil.getUser() != null) {
			updId = SecurityUtil.getUser().getUserId();
		}
    	
    	boardPostSatisfactionDTO.setTblNm("bbs_pst");
    	boardPostSatisfactionDTO.setTblOid(boardPostSatisfactionDTO.getBbsPstOid());
    	
    	int result = boardMapper.selectValidSatisfactionExists(boardPostSatisfactionDTO);
    	if(result > 0) {
	        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "이미 만족도 평가를 완료하셨습니다.");
    	}
		
    	boardPostSatisfactionDTO.setUpdId(updId);
    	boardPostSatisfactionDTO.setStts("COMPLETED");
    	count = boardMapper.updateSatisfactionCountByBbsPstOid(boardPostSatisfactionDTO);
    	if(count == 0) {
    		throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "만족도 평가 저장 에러");
    	}
		return count;
	}

	@Override
	public void insertBoardPostQNA(BoardPostInsertDTO boardPostInsertDTO, List<MultipartFile> files) {
        try {
        	// 썸네일 먼저 처리
        	String userId = "SYSTEM";
        	String userNm = boardPostInsertDTO.getWrtrNm();
        	Long userOid = null;
        	if(SecurityUtil.getAuthentication() != null && SecurityUtil.getAuthentication().isAuthenticated() && SecurityUtil.getUser() != null) {
        		userId = SecurityUtil.getUser().getUserId();
        		userNm = SecurityUtil.getUser().getUserNmKorn();
        		userOid = SecurityUtil.getUser().getUserOid();
        	}

            if(boardPostInsertDTO.getWrtrNm() == null) {
                boardPostInsertDTO.setWrtrNm(userNm);
            }

            if(userOid != null) {
                boardPostInsertDTO.setUserOid(userOid);
            }

            EgovMap egovMap = new EgovMap();
            egovMap.put("path", "board"); // 공통 파일 경로 지정
            egovMap.put("uploadFiles", files);
            egovMap.put("attachedFiles", boardPostInsertDTO.getAttachedFiles());
            egovMap.put("editorFiles", boardPostInsertDTO.getEditorFiles());
            egovMap.put("regUserId", userId);
            egovMap.put("mdfcnId", userId);
            egovMap.put("content", boardPostInsertDTO.getBbsPstCn());

            // 1) 파일/본문 정규화 (임시 → 실제 경로 이동, CKEditor 본문 치환)
            fileService.processFiles(egovMap);

        	//파일 처리
            boardPostInsertDTO.setBbsPstCn(contentProcessService.processHtmlContent((String) egovMap.get("content")));

            // contents_text 생성
            boardPostInsertDTO.setBbsPstCnTxt(HtmlUtil.stripHtml(boardPostInsertDTO.getBbsPstCn()));

            // 비밀번호 암호화 처리
            // encodePasswordIfPresent(boardPostInsertDTO);

            // QNA 상태값
            String processStat = "Y".equals(boardPostInsertDTO.getTempSaveYn()) ? PostStatus.TEMP.getStatusCode() : PostStatus.COMP.getStatusCode();
            boardPostInsertDTO.setStts(processStat);
            
            // 게시글 저장
            savePost(boardPostInsertDTO);

            // 게시글 번호 검증
            Long bbsPstOid = validateBbsPstOid(boardPostInsertDTO.getBbsPstOid());

            if (bbsPstOid == 0) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 저장 에러");
            }
            
            // Q&A인 경우 생명윤리QNA 확인 및 비밀글 여부 확인
            BoardPostDTO qnaPostConfig = getBoardPostParentSecretYn(boardPostInsertDTO.getUpBbsPstOid());
            String boardType = BoardType.QNA_BOARD.getBoardTypeCd(); // QNA
            
            // 생명윤리 QNA인 경우 히스토리 (어드민만)
            if (boardType.equals(qnaPostConfig.getBbsSeCd())) {
                userId = boardPostInsertDTO.getRegId();
                
                Long parentBbsPstOid = boardPostInsertDTO.getUpBbsPstOid();
                if (PostStatus.COMP.getStatusCode().equals(processStat)) {
                    insertHistoryHelper(bbsPstOid, userId, PostStatus.TEMP.getStatusCode());
                    
                    if(parentBbsPstOid != null && parentBbsPstOid != 0) {
                    	//질문 post 상태값 업데이트 후 히스토리 생성
                    	updateQnaBoardPostStatusHelper(parentBbsPstOid, userId, processStat, boardPostInsertDTO.getCtgry());
                    	insertHistoryHelper(parentBbsPstOid, userId, processStat);
                    }
                    
                }
                // QNA 히스토리 저장 (답변)
                insertHistoryHelper(bbsPstOid, userId, processStat);
            }
            
            if("Y".equals(qnaPostConfig.getPrvtPstYn()) && boardPostInsertDTO.getUpBbsPstOid() != null && boardPostInsertDTO.getUpBbsPstOid() != 0
            		&& PostStatus.COMP.getStatusCode().equals(processStat)) {
            	// 비밀글인 경우 만족도 평가 insert
            	boardPostInsertDTO.setTblNm("bbs_pst");
            	boardPostInsertDTO.setTblOid(boardPostInsertDTO.getUpBbsPstOid());
            	boardPostInsertDTO.setRegId(userId);
            	saveQnaPostSatisfaction(boardPostInsertDTO);
            }

            // 4) file 메타 기록: 방금 생성된 bbsPstOid tablePk로 연결 (파일 / 사진)
            egovMap.put("tblNm", TABLE_BOARD_POST);
            egovMap.put("tblOid", bbsPstOid);
            fileService.saveFileMeta(egovMap);

            // 5) 첫 번째 첨부파일의 alt_text 업데이트
            if (StringUtils.hasText(boardPostInsertDTO.getFirstFileAltText())) {
                updateFirstFileAltText(bbsPstOid, boardPostInsertDTO.getFirstFileAltText(), userId);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("게시글 저장 중 오류 발생", e);
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "게시글 저장 중 오류 발생");
        }
		
	}

	@Override
	public void updateBoardPostQNA(BoardPostInsertDTO boardPostInsertDTO, List<MultipartFile> files) {
		try {
            String userId = SecurityUtil.getUser().getUserId();
            boardPostInsertDTO.setMdfcnId(userId);

            // 파일 처리를 위한 공통 설정값 준비
            Long bbsPstOid = boardPostInsertDTO.getBbsPstOid();

            EgovMap commonMap = new EgovMap();
            commonMap.put("tblNm", TABLE_BOARD_POST);
            commonMap.put("tblOid", bbsPstOid);
            commonMap.put("path", "board");
            commonMap.put("regUserId", userId);
            commonMap.put("mdfcnId", userId);

            // 5. [일반/에디터 파일 처리] Raw, Chunk, Editor 중 하나라도 있으면 수행
            boolean hasRaw = (files != null && !files.isEmpty());
            boolean hasChunk = !CollectionUtils.isEmpty(boardPostInsertDTO.getAttachedFiles());
            boolean hasEditor = !CollectionUtils.isEmpty(boardPostInsertDTO.getEditorFiles());

            if (hasRaw || hasChunk || hasEditor) {

                EgovMap fileMap = new EgovMap();
                fileMap.putAll(commonMap);

                fileMap.put("uploadFiles", files); // 일반 파일 (List)
                fileMap.put("attachedFiles", boardPostInsertDTO.getAttachedFiles()); // Chunk 메타
                fileMap.put("editorFiles", boardPostInsertDTO.getEditorFiles()); // 에디터 메타
                fileMap.put("content", boardPostInsertDTO.getBbsPstCn());

                fileService.processFiles(fileMap); // 파일 정규화

                boardPostInsertDTO.setBbsPstCn(contentProcessService.processHtmlContent((String) fileMap.get("content")));

                fileService.saveFileMeta(fileMap); // DB 메타 저장
            }

            // contents_text 생성
            boardPostInsertDTO.setBbsPstCnTxt(HtmlUtil.stripHtml(boardPostInsertDTO.getBbsPstCn()));

            // 1. 비밀번호 암호화 및 게시글 정보 Update
            // encodePasswordIfPresent(boardPostInsertDTO);
            
            // QNA 상태값
            String processStat = "Y".equals(boardPostInsertDTO.getTempSaveYn()) ? PostStatus.TEMP.getStatusCode() : PostStatus.COMP.getStatusCode();
            boardPostInsertDTO.setStts(processStat);
            
            if (boardMapper.updateBoardPost(boardPostInsertDTO) == 0) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "게시글 수정 실패 (대상 없음)");
            }
            
            // 답변 상태에 따라 질문 상태 변경
            BoardPostDTO qnaPostConfig = getBoardPostParentSecretYn(boardPostInsertDTO.getUpBbsPstOid());
            String boardType = BoardType.QNA_BOARD.getBoardTypeCd(); // QNA

            // null check
            if(qnaPostConfig != null) {
                if (boardType.equals(qnaPostConfig.getBbsSeCd())) {
                    Long parentBbsPstOid = boardPostInsertDTO.getUpBbsPstOid();

                    // 답변 히스토리 중복 확인후 생성
                    if (!hasHistory(bbsPstOid, processStat)) {
                        insertHistoryHelper(bbsPstOid, userId, processStat);
                    }
                    
                    if (parentBbsPstOid != null && parentBbsPstOid != 0) {
                        if (PostStatus.COMP.getStatusCode().equals(processStat)) {
                            // 부모글(질문) 상태 및 카테고리 업데이트 (COMP)
                            updateQnaBoardPostStatusHelper(parentBbsPstOid, userId, PostStatus.COMP.getStatusCode(), boardPostInsertDTO.getCtgry());
                            
                            // 부모글(질문) 히스토리 (COMP) - 중복 없으면 1회 저장
                            if (!hasHistory(parentBbsPstOid, PostStatus.COMP.getStatusCode())) {
                                insertHistoryHelper(parentBbsPstOid, userId, PostStatus.COMP.getStatusCode());
                            }
                        } else {
                            // 부모글(질문) 상태 업데이트 (PEND)
                            updateQnaBoardPostStatusHelper(parentBbsPstOid, userId, PostStatus.PEND.getStatusCode(), boardPostInsertDTO.getCtgry());
                        }
                    }
                }
                
                if("Y".equals(qnaPostConfig.getPrvtPstYn()) && boardPostInsertDTO.getUpBbsPstOid() != null
                        && boardPostInsertDTO.getUpBbsPstOid() != 0 && PostStatus.COMP.getStatusCode().equals(processStat)) {
                    
                    // 만족도 평가 insert 유무 확인
                    BoardPostSatisfactionDTO param = new BoardPostSatisfactionDTO();
                    param.setTblNm("bbs_pst");
                    param.setTblOid(boardPostInsertDTO.getUpBbsPstOid());
                    int result = boardMapper.selectSatisfactionCountByBbsPstOid(param);
                    
                    // 만족도 평가가 없는 경우 (차례대로 초안 작성후 저장한 케이스)
                    if(result == 0) {
                        // 비밀글인 경우 만족도 평가 insert
                        boardPostInsertDTO.setTblNm("bbs_pst");
                        boardPostInsertDTO.setTblOid(boardPostInsertDTO.getUpBbsPstOid());
                        boardPostInsertDTO.setRegId(userId);
                        saveQnaPostSatisfaction(boardPostInsertDTO);
                    }
                }
            }
            
            // 2. 삭제 대상 파일(일반+썸네일+에디터) 일괄 삭제 처리
            List<Long> delIds = new ArrayList<>();
            
            // 일반/썸네일 파일 삭제 목록 체크
            if (!CollectionUtils.isEmpty(boardPostInsertDTO.getDeleteAttachNos())) {
                delIds.addAll(boardPostInsertDTO.getDeleteAttachNos());
            }

            // 에디터 이미지 삭제 목록 체크
            if (!CollectionUtils.isEmpty(boardPostInsertDTO.getDeleteEditorAttachNos())) { // <--- 여기 수정!
                delIds.addAll(boardPostInsertDTO.getDeleteEditorAttachNos());
            }
            
            if (!delIds.isEmpty()) {
                fileService.deleteFilesByFileNos(delIds, userId);
            }

            // 5) 첫 번째 첨부파일의 alt_text 업데이트
            if (StringUtils.hasText(boardPostInsertDTO.getFirstFileAltText())) {
                updateFirstFileAltText(bbsPstOid, boardPostInsertDTO.getFirstFileAltText(), userId);
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("게시글 수정 중 오류 발생", e);
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "게시글 수정 중 오류 발생");
        }
	}
	/**
     * 동적 QNA 히스토리 설정 중복 확인
     */
    private boolean hasHistory(Long tblOid, String status) {
        BoardPostInsertDTO checkDto = new BoardPostInsertDTO();
        checkDto.setTblNm("bbs_pst");
        checkDto.setTblOid(tblOid);
        checkDto.setStts(status);
        
        return boardMapper.selectUserBoardHistoryCount(checkDto) > 0;
    }
	
	/**
     * 동적 QNA 설정 값 조회
     */
	private BoardPostDTO getBoardPostParentSecretYn(Long parentBbsPstOid) {
		BoardPostInsertDTO param = new BoardPostInsertDTO();
		param.setBbsPstOid(parentBbsPstOid);
		
		return boardMapper.selectBoardPostParentSecretYn(param);
	}
	
	/**
     * QNA 상태값 변경 메서드
     */
    private void updateQnaBoardPostStatusHelper(Long parantBbsPstOid, String regUserId, String status, String category) {
        BoardPostInsertDTO QnaBoardPostStatusDto = new BoardPostInsertDTO();
        QnaBoardPostStatusDto.setUpBbsPstOid(parantBbsPstOid);
        QnaBoardPostStatusDto.setMdfcnId(regUserId);
        QnaBoardPostStatusDto.setStts(status);
        QnaBoardPostStatusDto.setCtgry(category);

        int count = boardMapper.updateQnaBoardPostStatus(QnaBoardPostStatusDto);
        if (count == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 히스토리 저장 에러");
        }
    }	
	
	/**
     * 히스토리 저장 전용 메서드
     */
    private void insertHistoryHelper(Long tblOid, String regUserId, String status) {
        BoardPostInsertDTO historyDto = new BoardPostInsertDTO();
        historyDto.setTblNm("bbs_pst");
        historyDto.setTblOid(tblOid);
        historyDto.setRegId(regUserId);
        historyDto.setStts(status);

        int count = boardMapper.insertUserBoardHistory(historyDto);
        if (count == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 히스토리 저장 에러");
        }
    }

    @Override
    public BoardDataSetResponseDTO selectMainPageBoardList(String subBoardType, String category) {
        try {
            // 게시판 설정 정보 조회
            BoardRequestDto requestDto = new BoardRequestDto();
            requestDto.setBbsSubSeCd(subBoardType);
            requestDto.setMenuAuthLv("COMMON");

            BoardDTO boardInfo = boardMapper.selectBoardBySubBoardType(requestDto);

            if (boardInfo == null) {
                return BoardDataSetResponseDTO.builder()
                        .key(subBoardType)
                        .board(null)
                        .list(null)
                        .build();
            }

            // 게시판 번호 설정
            requestDto.setBbsOid(boardInfo.getBbsOid());
            requestDto.setMenuCd(boardInfo.getMenuCd());

            // 카테고리 설정 (언론동향 등)
            if (StringUtils.hasText(category)) {
                requestDto.setCtgry(category);
            }

            // 페이징 설정 (메인 페이지는 4개만 표시)
            PageHelper.startPage(1, 4);

            // 대시보드용 경량 쿼리 호출 (N+1 없음)
            List<BoardPostDTO> postList = boardMapper.selectDashboardPostList(requestDto);
            PageInfo<BoardPostDTO> pageInfo = PageInfo.of(postList);

            // 첫 번째 게시글 첨부파일 조회 (썸네일용)
            if (!CollectionUtils.isEmpty(postList)) {
                Long firstBbsPstOid = postList.get(0).getBbsPstOid();
                List<FileDTO> attachments = boardMapper.selectFirstPostAttachmentsByBbsPstOids(
                        Collections.singletonList(firstBbsPstOid));
                if (!CollectionUtils.isEmpty(attachments)) {
                    postList.get(0).setAttachments(attachments);
                }
            }

            return BoardDataSetResponseDTO.builder()
                    .key(subBoardType)
                    .board(boardInfo)
                    .list(pageInfo)
                    .build();

        } catch (Exception e) {
            log.error("메인 페이지 게시판 데이터 조회 중 오류 발생 - subBoardType: {}, category: {}",
                    subBoardType, category, e);
            return BoardDataSetResponseDTO.builder()
                    .key(subBoardType)
                    .board(null)
                    .list(null)
                    .build();
        }
    }

	@Override
	public ExcelExportResult boardQnaExportExcel(BoardRequestDto boardRequestDto) throws IOException {
		
        // 게시판 정보 조회
        BoardDTO boardInfo = boardMapper.selectBoard(boardRequestDto);

        // 결과 페이징 리스트
        List<?> resultList;

        // 게시판 타입에 따른 분기 처리
        // 나중에 QNA 말고 다른 게시판에도 추가가 되어야한다면 조회쿼리 생성후 수정
        if (boardInfo != null && boardInfo.getBbsSeCd() != null) {
            BoardType boardType = BoardType.fromCode(boardInfo.getBbsSeCd());
            if (boardType != null) {
                switch (boardType) {
                    case COMMON_BOARD, USER_BOARD, FAQ_BOARD:
                        // 일반 게시판, 사용자 게시판
                        resultList = boardMapper.selectBoardPostList(boardRequestDto);
                        break;
                    case QNA_BOARD:
                        // FAQ, QnA
                        resultList = boardMapper.selectQnaDetailedStatList(boardRequestDto);
                        break;
                    case PHOTO_GALLERY_BOARD, THUMBNAIL_BOARD:
                        // 포토갤러리 게시판, 썸네일 게시판
                        resultList = boardMapper.selectBoardPostListTypeImage(boardRequestDto);
                        break;
                    default:
                        // 기본 처리
                        resultList = boardMapper.selectBoardPostList(boardRequestDto);
                        break;
                }
            } else {
                // 게시판 타입이 null인 경우
                resultList = boardMapper.selectBoardPostList(boardRequestDto);
            }
        } else {
            // 게시판 정보가 없는 경우
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "게시판 정보가 존재하지 않습니다.");
        }
        
	    if (resultList == null || resultList.isEmpty()) {
	        throw new BusinessException(ErrorCode.EXCEL_DOWNLOAD_NO_DATA, "엑셀 데이터가 존재하지 않습니다.");
	    }
	    
	    for(int i = 0; i < resultList.size(); i++) {
	        BoardQnaExcelDTO result = (BoardQnaExcelDTO) resultList.get(i);
	        
	        String cleanQuestion = HtmlUtil.stripHtml(HtmlUtil.htmlDecode(result.getQuestionContent()));
	        result.setQuestionContent(cleanQuestion);
	        
	        if (result.getAnswerContent() != null) {
	            String cleanAnswer = HtmlUtil.stripHtml(HtmlUtil.htmlDecode(result.getAnswerContent()));
	            result.setAnswerContent(cleanAnswer);
	        }
	    }
	    
	    String pageId = boardRequestDto.getPageId();
	    
	    byte[] bytes = excelComponent.excelExportByPage(pageId, resultList);
	    
	    ExcelPageConfigDTO pageInfo = excelConfig.getPages().get(pageId);
	    
	    String title = pageInfo.getTitle();
	    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
	    String fileName = title + "_" + date + ".xlsx";
	    
		return new ExcelExportResult(fileName, bytes);
	    
	}
}
