package egovframework.common.file.service.impl;

import egovframework.common.constant.Constants;
import egovframework.common.enums.FileType;
import egovframework.common.exception.BusinessException;
import egovframework.common.exception.ErrorCode;
import egovframework.common.file.config.FileConfig;
import egovframework.common.file.config.FileStorageType;
import egovframework.common.file.domain.AttachedFileDTO;
import egovframework.common.file.domain.FileDTO;
import egovframework.common.file.domain.FileUploadCategory;
import egovframework.common.file.mapper.FileMapper;
import egovframework.common.file.service.FileService;
import egovframework.common.file.service.FileUploadService;
import egovframework.common.util.CommonUtil;
import egovframework.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.psl.dataaccess.util.EgovMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl extends EgovAbstractServiceImpl implements FileService {
    /**
     * 업로드 흐름을 통합 관리하는 서비스.
     * <p>
     * 주요 사용처 예시:
     * - Admin101/602/614 등: 화면에서 올려둔 temp 파일을 실제 경로로 이동시킨 뒤 {@link #saveFileMeta(EgovMap)}로 DB(file)에 기록.
     * - CKEditor 업로드: {@link #uploadTempFile(MultipartFile, FileUploadCategory, String)}로 temp 저장 후, 본문 저장 시 {@link #processFiles(EgovMap)}가 경로 치환.
     * - 파일 삭제: 각 도메인 서비스에서 {@link #deleteFilesByFileNos(List, String)}를 호출해 물리 파일 삭제 + file use_yn='N' 처리.
     */
    private final FileMapper fileMapper;
    private final FileConfig fileConfig;
    private final SftpTransferServiceImpl sftpTransferService;
    private final FileUploadService fileUploadService;
    private final Environment environment;

    private static final String CKEDITOR_TEMP_PREFIX = "temp/ckeditor";
    private static final String UPLOAD_PUBLIC_PREFIX = "/files/";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM");
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("dd");

    private static final List<String> DEFAULT_ALLOWED_EXTENSIONS = Arrays.asList(
        "jpg", "jpeg", "png", "gif", "bmp",
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
        "txt", "hwp", "hwpx", "rtf", "zip", "rar", "7z"
    );
    private volatile Set<String> cachedAllowedExtensions;
    private volatile String allowedExtensionsText;

    @Transactional
    public void processFiles(EgovMap egovMap) throws IOException {
        // 역할: 임시 첨부/CKEditor temp 이미지를 실제 경로로 이동하고, 본문/attachedFiles를 정규화한다.ㅁ
        // 결과: egovMap.content가 치환되고, egovMap.attachedFiles(Map 리스트) 세팅

        // 입력 분기 개요
        // - rawContent: CKEditor 본문이 오면 temp 경로 이미지를 실제 경로로 옮기고 img src를 치환, 없으면 건너뜀.
        // - dtoAttachedFiles: 프론트에서 청크 업로드 응답(temp/...)을 그대로 보내는 경우, temp→실경로 이동. 없으면 다음 단계로.
        // - uploadFiles: 멀티파트 첨부를 바로 받는 경우, 여기서 파일을 저장. 위 두 케이스가 없을 때만 동작.
        // - dtoEditorFiles: CKEditor 업로드 응답 리스트(temp/...)가 오면 인라인 파일 메타에 포함. 없으면 건너뜀.
        // 최종적으로 attachments에 위 경로의 모든 파일 정보를 모아 egovMap.attachedFiles로 설정한다.

        String apiPath = resolvePathSegment(egovMap.get("path")); // admin/<path>/yyyy/MM/dd 빌드
        @SuppressWarnings("unchecked")
        List<MultipartFile> uploadFiles = (List<MultipartFile>) egovMap.get("uploadFiles");
        @SuppressWarnings("unchecked")
        List<Object> dtoAttachedFiles = (List<Object>) egovMap.get("attachedFiles"); // temp/청크 응답 등
        @SuppressWarnings("unchecked")
        List<Object> dtoEditorFiles = (List<Object>) egovMap.get("editorFiles"); // CKEditor 응답
        String rawContent = egovMap.get("content") == null ? null : String.valueOf(egovMap.get("content"));


        // CKEditor 본문 파싱 → temp 이미지 이동 → src 치환
        EditorContentProcessResult editorResult = processEditorContent(rawContent, apiPath);
        if (editorResult.content() != null) {
            egovMap.put("content", editorResult.content());
        }

        List<Map<String, Object>> attachments = new ArrayList<>();

        // temp 응답이 오면 temp→실경로 이동
        if (!CollectionUtils.isEmpty(dtoAttachedFiles)) {
            List<Map<String, Object>> converted = convertFileInfoFromDto(dtoAttachedFiles);
            moveTempFilesIfNeeded(converted, apiPath);
            attachments.addAll(converted);
        }
        if (!CollectionUtils.isEmpty(uploadFiles)) {
            // 새 multipart 첨부 업로드
            List<Map<String, Object>> uploaded = uploadMultipartFiles(uploadFiles, apiPath);
            attachments.addAll(uploaded);
        }

        // CKEditor 인라인 이미지도 첨부 목록에 포함
        List<Map<String, Object>> editorAttachments = convertEditorFileInfo(dtoEditorFiles, editorResult.movedImages());

        if (!CollectionUtils.isEmpty(editorAttachments)) {
            attachments.addAll(editorAttachments);
        }

        // 이미지 파일이면 리사이즈(small/medium) 버전도 생성하고 메타 정보에 포함
        generateResizedImages(attachments);

        // saveFileMeta가 이 attachedFiles를 순회하며 file에 정보 저장
        if (!attachments.isEmpty()) {
            egovMap.put("attachedFiles", attachments);
        }
    }

    @Transactional
    public int saveFileMeta(EgovMap egovMap) {
        // 파일 정보 file에 저장
        // - 선행: processFiles로 egovMap.attachedFiles를 준비한 상태여야 한다.
        // - Admin101/601/602/614 등에서 tblNm/tablePk를 채운 뒤 호출한다.
        int insertedCount = 0;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> attachments = (List<Map<String, Object>>) egovMap.get("attachedFiles");
        String tblNm = egovMap.get("tblNm") == null ? null : String.valueOf(egovMap.get("tblNm"));
        Long tblOid = CommonUtil.toLong(egovMap.get("tblOid"));
        if (!StringUtils.hasText(tblNm) || tblOid == null || CollectionUtils.isEmpty(attachments)) {
            return 0;
        }

        String thmbYn = egovMap.get("thmbYn") == null
                ? "N" : String.valueOf(egovMap.get("thmbYn"));
        int atchFileSeq = 1;
        String userId = egovMap.get("regId") != null ? String.valueOf(egovMap.get("regId"))
                : (SecurityUtil.getUser() != null ? SecurityUtil.getUser().getUserId() : Constants.SYSTEM_ID);
        for (Map<String, Object> fileInfo : attachments) {
            if (fileInfo == null) {
                continue;
            }
            EgovMap attachParam = new EgovMap();
            attachParam.put("tblNm", tblNm);
            attachParam.put("tblOid", tblOid);
            attachParam.put("orgnlFileNm", fileInfo.get("originalStrgFileNm"));
            attachParam.put("strgFileNm", fileInfo.get("storedStrgFileNm"));
            attachParam.put("strgFilePath", fileInfo.get("strgFilePath"));
            attachParam.put("strgFileCpct", fileInfo.get("strgFileCpct"));
            attachParam.put("strgSmlFileNm", fileInfo.get("strgSmlFileNm"));
            attachParam.put("strgSmlFilePath", fileInfo.get("strgSmlFilePath"));
            attachParam.put("strgSmlFileCpct", fileInfo.get("strgSmlFileCpct"));
            attachParam.put("strgMdFileNm", fileInfo.get("strgMdFileNm"));
            attachParam.put("strgMdFilePath", fileInfo.get("strgMdFilePath"));
            attachParam.put("strgMdFileCpct", fileInfo.get("strgMdFileCpct"));
            attachParam.put("fileTypeNm", resolveFileExt(fileInfo));
            attachParam.put("fileType", fileInfo.getOrDefault("fileType", FileType.ATTACHMENT.name()));
            attachParam.put("thmbYn", thmbYn);
            attachParam.put("atchFileSeq", atchFileSeq++);
            attachParam.put("fileExpln", fileInfo.get("fileExpln")); // 리사이즈 오류 등 비고 정보 저장
            attachParam.put("useYn", fileInfo.getOrDefault("useYn", "Y"));
            attachParam.put("regId", userId);
            attachParam.put("mdfcnId", userId);

            int insertResult = fileMapper.insertFile(attachParam);
            if (insertResult == 0) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 첨부 저장 실패");
            }
            if (attachParam.get("fileOid") != null) {
                Long convertedFileOid = Long.valueOf(String.valueOf(attachParam.get("fileOid")));
                egovMap.put("fileOid", convertedFileOid);
            }
            insertedCount += insertResult;
        }
        return insertedCount;
    }

    /**
     * file에서 tblNm/tablePk로 논리 삭제(use_yn='N') 처리한다.
     */
    public void deleteFilesByTable(String tblNm, Long tblOid, String mdfcnId) {
        if (!StringUtils.hasText(tblNm) || tblOid == null) {
            return;
        }

        EgovMap queryParam = new EgovMap();
        queryParam.put("tblNm", tblNm);
        queryParam.put("tblOid", tblOid);
        List<FileDTO> filesToDelete = fileMapper.selectAttachmentFileByTableNameAndTablePk(queryParam);

        if (filesToDelete == null || filesToDelete.isEmpty()) {
            return;
        }

        List<Long> fileNos = filesToDelete.stream()
                .map(FileDTO::getFileOid)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        if (!fileNos.isEmpty()) {
            deleteFilesByFileNos(fileNos, mdfcnId);
        }
    }

    /**
     * file에서 fileOid 리스트로 논리 삭제(use_yn='N') 처리한다.
     */
    public void deleteFilesByFileNos(List<Long> fileNos, String mdfcnId) {
        if (CollectionUtils.isEmpty(fileNos)) {
            return;
        }
        // 1) 물리 삭제에 필요한 경로/파일명을 먼저 조회
        List<EgovMap> fileInfos = fileMapper.selectFileInfosByFileNos(fileNos);
        if (!CollectionUtils.isEmpty(fileInfos)) {
            for (EgovMap info : fileInfos) {
                String path = info.get("strgFilePath") == null ? null : String.valueOf(info.get("strgFilePath"));
                String name = info.get("strgFileNm") == null ? null : String.valueOf(info.get("strgFileNm"));
                if (!StringUtils.hasText(path) || !StringUtils.hasText(name)) {
                    continue;
                }
                try {
                    fileUploadService.deleteStoredFile(path, name); // 실제 파일 삭제
                } catch (IOException e) {
                    log.warn("물리 파일 삭제 실패 path={}, name={}", path, name, e);
                }
            }
        }
        // 2) file use_yn = 'N' 처리
        for (Long fileOid : fileNos) {
            if (fileOid == null) {
                continue;
            }
            EgovMap param = new EgovMap();
            param.put("fileOid", fileOid);
            param.put("mdfcnId", mdfcnId);
            fileMapper.deleteFileByFileNo(param);
        }
    }

    /**
     * 컨트롤러나 서비스에서 넘겨준 path(Object)를 안전한 경로 조각으로 변환한다.
     * (미지정 시 admin notice 기준인 "notice"를 기본값으로 사용)
     */
    private String resolvePathSegment(Object pathObj) {
        String raw = pathObj == null ? null : String.valueOf(pathObj);
        if (!StringUtils.hasText(raw)) {
            return "notice";
        }
        String normalized = raw.replace("\\", "/").trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized.isEmpty() ? "notice" : normalized;
    }

    /**
     * CKEditor 본문에서 temp 경로에 업로드 된 이미지를 찾아 실제 경로로 이동시키고, src를 치환한다.
     */
    private EditorContentProcessResult processEditorContent(String content, String apiPath) throws IOException {
        if (!StringUtils.hasText(content)) {
            return new EditorContentProcessResult(content, Collections.emptyList());
        }
        Document document = Jsoup.parseBodyFragment(content);
        Elements images = document.select("img[src]");
        if (images.isEmpty()) {
            return new EditorContentProcessResult(content, Collections.emptyList());
        }
        boolean updated = false;
        String destinationPath = null;
        List<EditorImageMoveResult> movedImages = new ArrayList<>();
        for (Element image : images) {
            EditorImageLocation location = resolveEditorImageLocation(image.attr("src"));
            if (location == null || !isTempEditorPath(location.relativeDir())) {
                continue;
            }
            if (destinationPath == null) {
                destinationPath = buildEditorImagePath(apiPath);
            }
            fileUploadService.moveFile(location.relativeDir(), location.strgFileNm(), destinationPath);
            movedImages.add(new EditorImageMoveResult(location.relativeDir(), destinationPath, location.strgFileNm()));
            String newUrl = fileUploadService.buildPublicUrl(destinationPath, location.strgFileNm());
            image.attr("src", newUrl);
            updated = true;
        }
        String processedContent = updated ? document.body().html() : content;
        return new EditorContentProcessResult(processedContent, movedImages);
    }

    private boolean isTempEditorPath(String path) {
        if (!StringUtils.hasText(path)) {
            return false;
        }
        return path.replace("\\", "/").startsWith(CKEDITOR_TEMP_PREFIX);
    }

    private EditorImageLocation resolveEditorImageLocation(String rawSrc) {
        if (!StringUtils.hasText(rawSrc)) {
            return null;
        }
        String cleaned = stripUrlMetadata(rawSrc.trim());
        if (!StringUtils.hasText(cleaned) || cleaned.startsWith("data:")) {
            return null;
        }
        String withoutBase = removePublicBaseUrl(cleaned);
        if (!StringUtils.hasText(withoutBase)) {
            return null;
        }
        String normalized = withoutBase.replace("\\", "/");
        if (!normalized.startsWith("http") && !normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (!normalized.startsWith(UPLOAD_PUBLIC_PREFIX)) {
            int idx = normalized.indexOf(UPLOAD_PUBLIC_PREFIX);
            if (idx > -1) {
                normalized = normalized.substring(idx);
            } else {
                return null;
            }
        }
        String storagePath = normalized.substring(UPLOAD_PUBLIC_PREFIX.length());
        int lastSlash = storagePath.lastIndexOf('/');
        if (lastSlash < 0) {
            return null;
        }
        String dir = storagePath.substring(0, lastSlash);
        String strgFileNm = storagePath.substring(lastSlash + 1);
        if (!StringUtils.hasText(dir) || !StringUtils.hasText(strgFileNm)) {
            return null;
        }
        return new EditorImageLocation(dir, strgFileNm);
    }

    private String stripUrlMetadata(String src) {
        int queryIdx = src.indexOf('?');
        String result = queryIdx >= 0 ? src.substring(0, queryIdx) : src;
        int hashIdx = result.indexOf('#');
        return hashIdx >= 0 ? result.substring(0, hashIdx) : result;
    }

    private String removePublicBaseUrl(String src) {
        String processed = src;
        String baseUrl = fileConfig.getPublicBaseUrl();
        if (StringUtils.hasText(baseUrl)) {
            String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
            if (processed.startsWith(normalizedBase)) {
                processed = processed.substring(normalizedBase.length());
            }
        }
        return processed;
    }

    private String buildEditorImagePath(String apiPath) {
        LocalDate now = LocalDate.now();
        return Paths.get("admin", apiPath, String.valueOf(now.getYear()),
                        now.format(DateTimeFormatter.ofPattern("MM")), now.format(DateTimeFormatter.ofPattern("dd")))
                .toString().replace("\\", "/");
    }

    private String buildUploadPath(String apiPath) {
        LocalDate now = LocalDate.now();
        return Paths.get("admin", apiPath, String.valueOf(now.getYear()),
                        now.format(DateTimeFormatter.ofPattern("MM")), now.format(DateTimeFormatter.ofPattern("dd")))
                .toString().replace("\\", "/");
    }

    /**
     * DTO(List<Object>)를 DB insert용 Map 리스트로 변환한다.
     * (Map, VO, DTO 어떤 형태로 오더라도 동일 키로 변환)
     */
    private List<Map<String, Object>> convertFileInfoFromDto(List<Object> files) {
        List<Map<String, Object>> converted = new ArrayList<>();
        for (Object file : files) {
            if (file == null) {
                continue;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("originalStrgFileNm", readProperty(file, "originalStrgFileNm", "orgnlFileNm", "fileNm"));
            map.put("storedStrgFileNm", readProperty(file, "storedStrgFileNm", "strgFileNm"));
            map.put("strgFilePath", readProperty(file, "strgFilePath"));
            map.put("strgFileCpct", CommonUtil.toLong(readProperty(file, "strgFileCpct")));
            map.put("fileTypeNm", readProperty(file, "fileTypeNm", "fileExtn"));
            map.put("fileType", readProperty(file, "fileType"));
            map.put("useYn", readProperty(file, "useYn"));

            if (!StringUtils.hasText((String) map.get("fileType"))) {
                map.put("fileType", FileType.ATTACHMENT.name());
            }
            if (!StringUtils.hasText((String) map.get("useYn"))) {
                map.put("useYn", "Y");
            }

            converted.add(map);
        }
        return converted;
    }

    /**
     * temp 경로로 올라온 파일을 최종 경로(???/<path>/yyyy/MM/dd)로 이동시킨다.
     * - 첨부 파일 temp에 임시 업로드 하는 방식으로 사용하는 경우에만 사용
     */
    private void moveTempFilesIfNeeded(List<Map<String, Object>> files, String apiPath) throws IOException {
        if (CollectionUtils.isEmpty(files)) {
            return;
        }
        String finalPath = buildUploadPath(apiPath);
        for (Map<String, Object> file : files) {
            if (file == null) {
                continue;
            }
            String strgFilePath = file.get("strgFilePath") == null ? null : String.valueOf(file.get("strgFilePath"));
            String storedStrgFileNm = file.get("storedStrgFileNm") == null ? null : String.valueOf(file.get("storedStrgFileNm"));
            if (!StringUtils.hasText(strgFilePath) || !StringUtils.hasText(storedStrgFileNm)) {
                continue;
            }
            if (fileUploadService.isTempStrgFilePath(strgFilePath)) {
                String newPath = fileUploadService.moveTempFileToDestination(strgFilePath, storedStrgFileNm, finalPath);
                file.put("strgFilePath", newPath);
            }
        }
    }

    /**
     * CKEditor 업로드 기록 + 이동 결과를 합쳐 INLINE 파일 메타를 만든다.
     */
    private List<Map<String, Object>> convertEditorFileInfo(List<Object> editorFiles,
                                                            List<EditorImageMoveResult> movedImages) {
        if (CollectionUtils.isEmpty(editorFiles) || CollectionUtils.isEmpty(movedImages)) {
            return Collections.emptyList();
        }
        Map<String, String> moveMap = new HashMap<>();
        for (EditorImageMoveResult move : movedImages) {
            if (move == null) {
                continue;
            }
            String key = buildEditorFileKey(move.originalDir(), move.strgFileNm());
            if (key != null) {
                moveMap.put(key, move.destinationDir());
            }
        }
        if (moveMap.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> converted = new ArrayList<>();
        for (Object raw : editorFiles) {
            if (raw == null) {
                continue;
            }
            String path = readProperty(raw, "strgFilePath");
            String storedName = readProperty(raw, "storedStrgFileNm", "strgFileNm");
            String key = buildEditorFileKey(path, storedName);
            if (key == null || !moveMap.containsKey(key)) {
                continue;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("originalStrgFileNm", readProperty(raw, "originalStrgFileNm", "orgnlFileNm", "fileNm"));
            map.put("storedStrgFileNm", storedName);
            map.put("strgFilePath", moveMap.get(key));
            map.put("strgFileCpct", CommonUtil.toLong(readProperty(raw, "strgFileCpct")));
            map.put("fileTypeNm", readProperty(raw, "fileTypeNm", "fileExtn"));
            map.put("fileType", FileType.INLINE.name());
            map.put("useYn", "Y");
            converted.add(map);
        }
        return converted;
    }

    /**
     * 다중 multipart 첨부를 실제 저장소(SFTP 포함)에 저장하고, 공통 Map 포맷으로 변환한다.
     * - low-level 저장/이동은 fileUploadService + sftpTransferService를 사용
     * - 반환 Map 키: originalStrgFileNm, storedStrgFileNm, strgFilePath, strgFileCpct, fileTypeNm, fileType, useYn
     * - 첨부 파일 temp에 임시 업로드 하고 옮기는 방식이 아니라 그냥 바로 업로드 하는 경우
     */
    private List<Map<String, Object>> uploadMultipartFiles(List<MultipartFile> files, String apiPath) throws IOException {
        if (CollectionUtils.isEmpty(files)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        String relativePath = buildUploadPath(apiPath); // admin/<path>/yyyy/MM/dd
        boolean useSftp = fileConfig.getStorageType() == FileStorageType.SFTP && sftpTransferService.isSftpEnabled();
        Path baseDir = fileUploadService.getBaseUploadPath();
        Path uploadDir = baseDir.resolve(relativePath);
        if (!useSftp) {
            Files.createDirectories(uploadDir);
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            validateExtension(file.getOriginalFilename());
            String originalStrgFileNm = file.getOriginalFilename();
            String extension = getFileExtension(originalStrgFileNm);
            String storedStrgFileNm = UUID.randomUUID().toString() + (extension.isEmpty() ? "" : "." + extension);

            if (useSftp) {
                try (InputStream inputStream = file.getInputStream()) {
                    sftpTransferService.uploadStream(inputStream, relativePath, storedStrgFileNm);
                }
            } else {
                Path target = uploadDir.resolve(storedStrgFileNm);
                file.transferTo(target.toFile());
            }

            Map<String, Object> map = new HashMap<>();
            map.put("originalStrgFileNm", originalStrgFileNm);
            map.put("storedStrgFileNm", storedStrgFileNm);
            map.put("strgFilePath", relativePath);
            map.put("strgFileCpct", file.getSize());
            map.put("fileTypeNm", extension);
            map.put("fileType", FileType.ATTACHMENT.name());
            map.put("useYn", "Y");
            result.add(map);
        }
        return result;
    }

    /**
     * 이미지 확장자라면 small/medium 리사이즈 파일을 생성하고 메타 정보를 추가한다.
     * temp 단계에서는 호출되지 않고, 최종 저장 시점(processFiles)에서만 실행된다.
     */
    private void generateResizedImages(List<Map<String, Object>> attachments) {
        if (CollectionUtils.isEmpty(attachments)) {
            return;
        }
        for (Map<String, Object> fileInfo : attachments) {
            if (fileInfo == null) {
                continue;
            }
            String ext = resolveFileExt(fileInfo);
            if (!isImageExtension(ext)) {
                continue; // 이미지가 아니면 스킵
            }
            String strgFilePath = asString(fileInfo.get("strgFilePath"));
            String storedName = asString(fileInfo.get("storedStrgFileNm"));
            if (!StringUtils.hasText(strgFilePath) || !StringUtils.hasText(storedName)) {
                continue;
            }
            try {
                ResizeResult small = createThumbnail(strgFilePath, storedName, ext, 320, 320, "_s");
                ResizeResult medium = createThumbnail(strgFilePath, storedName, ext, 1024, 1024, "_m");
                if (small != null) {
                    fileInfo.put("strgSmlFileNm", small.strgFileNm());
                    fileInfo.put("strgSmlFilePath", small.strgFilePath());
                    fileInfo.put("strgSmlFileCpct", small.strgFileCpct());
                }
                if (medium != null) {
                    fileInfo.put("strgMdFileNm", medium.strgFileNm());
                    fileInfo.put("strgMdFilePath", medium.strgFilePath());
                    fileInfo.put("strgMdFileCpct", medium.strgFileCpct());
                }
            } catch (Exception e) {
                log.warn("이미지 리사이즈 실패 path={}, name={}", strgFilePath, storedName, e);
                fileInfo.put("fileExpln", "리사이즈 오류"); // 리사이즈 실패 여부를 DB에 표시하기 위해 메타 정보에 기록
            }
        }
    }

    private boolean isImageExtension(String ext) {
        if (!StringUtils.hasText(ext)) {
            return false;
        }
        String lower = ext.toLowerCase(Locale.ROOT);
        return lower.equals("jpg") || lower.equals("jpeg") || lower.equals("png")
                || lower.equals("gif") || lower.equals("bmp") || lower.equals("webp");
    }

    /**
     * 원본을 기준으로 썸네일을 생성한다. (SFTP/로컬 공통)
     * <p>
     * 기존 Thumbnailator 대신 리눅스의 {@code vips thumbnail} 명령을 직접 호출하는 방식으로 리사이즈한다.
     * Animated GIF의 경우 {@code [n=-1]} 옵션을 붙여 모든 프레임을 유지한 채 축소하고, 일반 이미지라면 정적 축소 명령을 사용한다.
     * width/height 파라미터는 기존 시그니처를 유지하기 위해 그대로 받지만, vips 명령은 폭 기준으로 축소하므로 width만 사용한다.
     */
    private ResizeResult createThumbnail(String strgFilePath, String storedName, String ext,
                                         int width, int height, String suffix) throws IOException {
        // 1) 결과 파일명/경로 정보 구성
        String baseName = storedName; // 확장자를 제외한 순수 파일명
        String extension = ""; // 점이 포함된 확장자 (.jpg)
        int dot = storedName.lastIndexOf('.');
        if (dot > -1) {
            baseName = storedName.substring(0, dot);
            extension = storedName.substring(dot); // 예: .jpg
        }
        String resizedName = baseName + suffix + extension; // 최종 리사이즈 파일명 (abc_s.jpg)
        String normalizedPath = strgFilePath.replace("\\", "/"); // 업로드 경로를 OS 상관없이 사용하기 위해 슬래시 통일
        boolean useSftp = fileConfig.getStorageType() == FileStorageType.SFTP && sftpTransferService.isSftpEnabled(); // 원격 저장소 여부
        boolean isGif = StringUtils.hasText(ext) && "gif".equalsIgnoreCase(ext); // 움직이는 GIF인지 여부
        int targetWidth = width > 0 ? width : 300; // vips에 넘길 목표 폭 (height는 사용하지 않으므로 보호용 기본값)
        boolean useLegacyThumbnail = isLocalRuntimeProfile(); // local-runtime(윈도우 개발 환경)에서는 vips 대신 기존 Thumbnailator 사용

        if (useLegacyThumbnail) {
            // local-runtime (윈도우 로컬 개발환경)에서는 vips CLI가 없으므로 기존 Thumbnailator 기반 리사이즈를 수행한다.
            if (useSftp) {
                // SFTP 저장소: 원격 파일을 메모리로 가져와 리사이즈한 뒤 다시 업로드
                try (InputStream in = sftpTransferService.downloadFile(normalizedPath, storedName).getInputStream()) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    Thumbnails.of(in).size(width, height).keepAspectRatio(true).toOutputStream(baos);
                    byte[] bytes = baos.toByteArray();
                    try (ByteArrayInputStream uploadStream = new ByteArrayInputStream(bytes)) {
                        sftpTransferService.uploadStream(uploadStream, normalizedPath, resizedName);
                    }
                    return new ResizeResult(resizedName, normalizedPath, (long) bytes.length);
                }
            } else {
                // 로컬 저장소: 기존 로직과 동일하게 Thumbnailator로 파일 리사이즈
                Path baseDir = fileUploadService.getBaseUploadPath();
                Path targetDir = baseDir.resolve(normalizedPath);
                Files.createDirectories(targetDir);
                Path source = targetDir.resolve(storedName);
                Path target = targetDir.resolve(resizedName);
                try (InputStream in = Files.newInputStream(source)) {
                    Thumbnails.of(in).size(width, height).keepAspectRatio(true).toFile(target.toFile());
                }
                long size = Files.size(target);
                return new ResizeResult(resizedName, normalizedPath, size);
            }
        }

        // 2) 공통으로 사용할 vips 명령어 인자 준비 (소스/타겟 경로는 스토리지 타입별로 달라지므로 아래에서 채운다)
        List<String> command = new ArrayList<>(); // ProcessBuilder에 전달할 명령 리스트
        command.add("vips");
        command.add("thumbnail");

        if (useSftp) {
            // SFTP 저장소: 원격 파일 → 임시 로컬 파일로 내려받은 뒤 vips 실행 → 결과를 원격으로 재업로드
            Path tempDir = Files.createTempDirectory("vips-thumb-"); // 임시 작업 공간
            Path localSource = tempDir.resolve(storedName); // 원격 → 로컬로 받은 원본 저장 위치
            Path localTarget = tempDir.resolve(resizedName); // vips 결과물 저장 위치
            try {
                Resource remoteFile = sftpTransferService.downloadFile(normalizedPath, storedName);
                try (InputStream in = remoteFile.getInputStream()) {
                    Files.copy(in, localSource, StandardCopyOption.REPLACE_EXISTING); // 원본 다운로드
                }

                String sourceArg = isGif
                    ? localSource.toAbsolutePath() + "[n=-1]"
                    : localSource.toAbsolutePath().toString();
                command.add(sourceArg);
                command.add(localTarget.toAbsolutePath().toString()); // 썸네일 저장 위치
                command.add(String.valueOf(targetWidth)); // 축소 기준 폭
                command.add("--size");
                command.add("down"); // 원본보다 작은 경우에만 축소

                ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true); // stderr/stdout 통합 수집
                Process process = builder.start(); // vips 명령 실행
                String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8); // vips 실행 로그
                int exitCode;
                try {
                    exitCode = process.waitFor(); // 프로세스 종료까지 대기
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("vips 명령 대기 중 인터럽트 발생", e);
                }
                if (exitCode != 0) {
                    throw new IOException("vips 명령 실패 (exit=" + exitCode + ") : " + output);
                }

                long strgFileCpct = Files.size(localTarget); // 업로드 전에 사이즈 추출
                try (InputStream uploadStream = Files.newInputStream(localTarget)) {
                    sftpTransferService.uploadStream(uploadStream, normalizedPath, resizedName);
                }
                return new ResizeResult(resizedName, normalizedPath, strgFileCpct);
            } finally {
                try {
                    Files.deleteIfExists(localSource);
                } catch (IOException ignored) {
                }
                try {
                    Files.deleteIfExists(localTarget);
                } catch (IOException ignored) {
                }
                try {
                    Files.deleteIfExists(tempDir);
                } catch (IOException ignored) {
                }
            }
        } else {
            // 로컬 저장소: 업로드 베이스 경로에서 직접 vips 명령 실행
            Path baseDir = fileUploadService.getBaseUploadPath(); // 업로드 루트 디렉터리
            Path targetDir = baseDir.resolve(normalizedPath); // 원본/결과 파일이 들어있는 실제 경로
            Files.createDirectories(targetDir);
            Path source = targetDir.resolve(storedName); // 원본 이미지
            Path target = targetDir.resolve(resizedName); // 리사이즈 결과

            String sourceArg = isGif
                ? source.toAbsolutePath() + "[n=-1]"
                : source.toAbsolutePath().toString();
            command.add(sourceArg);
            command.add(target.toAbsolutePath().toString()); // 썸네일 저장 위치
            command.add(String.valueOf(targetWidth));
            command.add("--size");
            command.add("down");

            ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true);
            Process process = builder.start(); // 로컬에서도 동일하게 vips 실행
            String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            int exitCode;
            try {
                exitCode = process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("vips 명령 대기 중 인터럽트 발생", e);
            }
            if (exitCode != 0) {
                throw new IOException("vips 명령 실패 (exit=" + exitCode + ") : " + output);
            }

            long size = Files.size(target);
            return new ResizeResult(resizedName, normalizedPath, size);
        }
    }

    private String asString(Object obj) {
        return obj == null ? null : String.valueOf(obj);
    }

    private record ResizeResult(String strgFileNm, String strgFilePath, Long strgFileCpct) {}

    /**
     * 현재 실행 중인 Spring 프로필이 local-runtime(윈도우 로컬 개발 환경)인지 여부를 확인한다.
     * local-runtime일 때는 vips CLI를 사용할 수 없으므로 Thumbnailator 기반 리사이즈를 사용해야 한다.
     */
    private boolean isLocalRuntimeProfile() {
        if (environment == null) {
            return false;
        }
        String[] profiles = environment.getActiveProfiles();
        if (profiles == null || profiles.length == 0) {
            return false;
        }
        for (String profile : profiles) {
            if ("local-runtime".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }

    private String buildEditorFileKey(String path, String strgFileNm) {
        if (!StringUtils.hasText(path) || !StringUtils.hasText(strgFileNm)) {
            return null;
        }
        String normalizedPath = path.replace("\\", "/");
        return normalizedPath + "::" + strgFileNm;
    }

    private String readProperty(Object target, String... candidates) {
        if (target == null || candidates == null || candidates.length == 0) {
            return null;
        }
        if (target instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) target;
            for (String name : candidates) {
                if (map.containsKey(name) && map.get(name) != null) {
                    return String.valueOf(map.get(name));
                }
            }
            return null;
        }
        for (String name : candidates) {
            try {
                Method method = target.getClass().getMethod("get" + capitalize(name));
                Object value = method.invoke(target);
                if (value != null) {
                    return String.valueOf(value);
                }
            } catch (Exception e) {
                // getter가 없으면 다음 후보로 진행
                continue;
            }
        }
        return null;
    }

    private String capitalize(String name) {
        if (!StringUtils.hasText(name)) {
            return name;
        }
        if (name.length() == 1) {
            return name.toUpperCase(Locale.ROOT);
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private String resolveFileExt(Map<String, Object> fileInfo) {
        Object extObj = fileInfo.get("fileTypeNm");
        String fileTypeNm = extObj == null ? null : String.valueOf(extObj);
        if (!StringUtils.hasText(fileTypeNm)) {
            Object stored = fileInfo.get("storedStrgFileNm");
            if (stored != null) {
                String storedName = String.valueOf(stored);
                int lastDot = storedName.lastIndexOf('.');
                if (lastDot > -1 && lastDot < storedName.length() - 1) {
                    fileTypeNm = storedName.substring(lastDot + 1);
                }
            }
        }
        return fileTypeNm;
    }

    private String getFileExtension(String strgFileNm) {
        if (!StringUtils.hasText(strgFileNm)) {
            return "";
        }
        int lastDotIndex = strgFileNm.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == strgFileNm.length() - 1) {
            return "";
        }
        return strgFileNm.substring(lastDotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private void validateExtension(String strgFileNm) {
        String extension = getFileExtension(strgFileNm);
        if (extension.isEmpty()) {
            throw new IllegalArgumentException("확장자를 확인할 수 없는 파일은 업로드할 수 없습니다.");
        }
        Set<String> allowedExtensions = getAllowedExtensions();
        if (!allowedExtensions.isEmpty() && !allowedExtensions.contains(extension.toLowerCase(
            Locale.ROOT))) {
            String allowedText = allowedExtensionsText != null ? allowedExtensionsText : String.join(", ", allowedExtensions);
            throw new IllegalArgumentException(
                "허용되지 않는 파일 형식입니다. (" + extension + ") 허용 확장자: " + allowedText);
        }
    }

    private Set<String> getAllowedExtensions() {
        Set<String> cached = cachedAllowedExtensions;
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            if (cachedAllowedExtensions == null) {
                Set<String> built = buildAllowedExtensions();
                cachedAllowedExtensions = Collections.unmodifiableSet(built);
                allowedExtensionsText = String.join(", ", built);
            }
            return cachedAllowedExtensions;
        }
    }

    private Set<String> buildAllowedExtensions() {
        Set<String> extensions = new LinkedHashSet<>();
        if (StringUtils.hasText(fileConfig.getAllowedExtensions())) {
            extensions.addAll(parseExtensions(fileConfig.getAllowedExtensions()));
        } else if (!CollectionUtils.isEmpty(fileConfig.getExtensionGroups())) {
            fileConfig.getExtensionGroups().values().forEach(value -> extensions.addAll(parseExtensions(value)));
        }
        if (extensions.isEmpty()) {
            extensions.addAll(DEFAULT_ALLOWED_EXTENSIONS);
        }
        return extensions.stream()
            .map(value -> value.toLowerCase(Locale.ROOT).trim())
            .filter(value -> !value.isEmpty())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<String> parseExtensions(String rawExtensions) {
        if (!StringUtils.hasText(rawExtensions)) {
            return Collections.emptyList();
        }
        String normalized = rawExtensions.replace(";", ",");
        return Arrays.stream(normalized.split(","))
            .map(String::trim)
            .map(value -> value.startsWith(".") ? value.substring(1) : value)
            .filter(value -> !value.isEmpty())
            .collect(Collectors.toList());
    }

    private record EditorContentProcessResult(String content, List<EditorImageMoveResult> movedImages) {}
    private record EditorImageMoveResult(String originalDir, String destinationDir, String strgFileNm) {}
    private record EditorImageLocation(String relativeDir, String strgFileNm) {}

    public Map<String, Object> uploadTempFile(MultipartFile file, FileUploadCategory category, String apiPath)
        throws IOException {
        // 단건 업로드를 temp 영역에 저장하고 CKEditor·첨부 업로드 API(FileApiController.uploadTmpFileCk 등)에 응답할 때 사용
        // 추후 processFiles에서 temp → 실제 경로 이동 시 메타 정보 그대로 재사용한다.
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 존재하지 않습니다.");
        }

        // 확장자 검사
        validateExtension(file.getOriginalFilename());

        Map<String, Object> fileInfo = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        List<String> relativeSegments = new ArrayList<>();
        relativeSegments.addAll(splitAndSanitize(resolveCategorySegment(category)));
        relativeSegments.addAll(splitAndSanitize(apiPath));
        relativeSegments.add(now.format(YEAR_FORMATTER));
        relativeSegments.add(now.format(MONTH_FORMATTER));
        relativeSegments.add(now.format(DAY_FORMATTER));

        Path baseDir = fileUploadService.getBaseUploadPath();
        Path target = baseDir;
        for (String segment : relativeSegments) {
            if (segment.isEmpty()) {
                continue;
            }
            target = target.resolve(segment);
        }

        boolean useSftp = fileConfig.getStorageType() == FileStorageType.SFTP && sftpTransferService.isSftpEnabled();
        if (!useSftp) {
            try {
                Files.createDirectories(target);
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "디렉토리 생성 중 오류가 발생했습니다.", e);
            }

        }

        String originalStrgFileNm = file.getOriginalFilename();
        String extension = getFileExtension(originalStrgFileNm);
        String storedStrgFileNm = UUID.randomUUID().toString() + (extension.isEmpty() ? "" : "." + extension);

        Path savedFile = target.resolve(storedStrgFileNm);
        String relativePath = String.join("/", relativeSegments);

        if (useSftp) {
            try (InputStream inputStream = file.getInputStream()) {
                sftpTransferService.uploadStream(inputStream, relativePath, storedStrgFileNm);
            }
        } else {
            file.transferTo(savedFile.toFile());
        }

        fileInfo.put("originalStrgFileNm", originalStrgFileNm);
        fileInfo.put("storedStrgFileNm", storedStrgFileNm);
        fileInfo.put("strgFilePath", relativePath);
        String webPath = relativePath.isEmpty() ? "/files" : UPLOAD_PUBLIC_PREFIX + relativePath;
        fileInfo.put("webPath", webPath);
        fileInfo.put("strgFileCpct", file.getSize());
        fileInfo.put("fileTypeNm", extension);
        fileInfo.put("status", "A");
        fileInfo.put("uploadDate", now.format(DATE_TIME_FORMATTER));

        return fileInfo;
    }

    private List<String> splitAndSanitize(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Collections.emptyList();
        }
        String normalized = raw.trim().replace("\\", "/");
        return Arrays.stream(normalized.split("/"))
            .map(String::trim)
            .filter(segment -> !segment.isEmpty())
            .filter(segment -> !".".equals(segment))
            .filter(segment -> !"..".equals(segment))
            .collect(Collectors.toList());
    }

    private String resolveCategorySegment(FileUploadCategory category) {
        Map<String, String> categoryDirectories = fileConfig.getCategoryDirectories();
        if (categoryDirectories != null) {
            String key = category.name().toLowerCase(Locale.ROOT);
            if (categoryDirectories.containsKey(key)) {
                return categoryDirectories.get(key);
            }
        }
        return category.getDefaultDirectory();
    }

    public List<FileDTO> selectInlineFileByTableNameAndTablePk(EgovMap egovMap){
        return fileMapper.selectAttachmentFileByTableNameAndTablePk(egovMap);
    }

    /**
     * AttachedFileDTO 리스트를 Map 리스트로 변환
     */
    public List<Map<String, Object>> convertCkEditorFileDtoToMap(List<AttachedFileDTO> files) {
        if (CollectionUtils.isEmpty(files)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (AttachedFileDTO file : files) {
            if (file == null) continue;
            Map<String, Object> map = new HashMap<>();
            map.put("originalStrgFileNm", file.getOriginalStrgFileNm());
            map.put("storedStrgFileNm", file.getStoredStrgFileNm());
            map.put("strgFilePath", file.getStrgFilePath());
            map.put("strgFileCpct", file.getStrgFileCpct());
            map.put("fileTypeNm", file.getFileTypeNm());
            result.add(map);
        }
        return result;
    }

    /**
     * 파일 타입별로 분리하여 저장 (ATTACHMENT와 INLINE 분리)
     */
    @Transactional
    public void saveFilesByType(List<Map<String, Object>> attachedFiles, Long tblOid, String userId, String tblNm) {
        // ATTACHMENT 타입 파일만 필터링
        List<Map<String, Object>> attachmentFiles = attachedFiles.stream()
                .filter(file -> FileType.ATTACHMENT.name().equals(file.getOrDefault("fileType", FileType.ATTACHMENT.name())))
                .collect(Collectors.toList());

        // INLINE 타입 파일만 필터링
        List<Map<String, Object>> inlineFiles = attachedFiles.stream()
                .filter(file -> FileType.INLINE.name().equals(file.get("fileType")))
                .collect(Collectors.toList());

        // ATTACHMENT 파일 저장
        if (!attachmentFiles.isEmpty()) {
            EgovMap attachEgovMap = new EgovMap();
            attachEgovMap.put("attachedFiles", attachmentFiles);
            attachEgovMap.put("tblNm", tblNm);
            attachEgovMap.put("tblOid", tblOid);
            attachEgovMap.put("fileType", FileType.ATTACHMENT.name());
            attachEgovMap.put("regId", userId);
            attachEgovMap.put("mdfcnId", userId);
            saveFileMeta(attachEgovMap);
        }

        // INLINE 파일 저장
        if (!inlineFiles.isEmpty()) {
            EgovMap inlineEgovMap = new EgovMap();
            inlineEgovMap.put("attachedFiles", inlineFiles);
            inlineEgovMap.put("tblNm", tblNm);
            inlineEgovMap.put("tblOid", tblOid);
            inlineEgovMap.put("fileType", FileType.INLINE.name());
            inlineEgovMap.put("regId", userId);
            inlineEgovMap.put("mdfcnId", userId);
            saveFileMeta(inlineEgovMap);
        }
    }

    /**
     * 파일 레코드 복사 (table_pk만 변경하여 새 row 생성)
     */
    @Transactional
    public int copyFilesByTablePk(String tblNm, Long oldTblOid, Long newTblOid, String regId) {
        if (!StringUtils.hasText(tblNm) || oldTblOid == null || newTblOid == null) {
            return 0;
        }
        EgovMap param = new EgovMap();
        param.put("tblNm", tblNm);
        param.put("oldTblOid", oldTblOid);
        param.put("newTblOid", newTblOid);
        param.put("regId", regId);
        return fileMapper.copyFilesByTablePk(param);
    }

    /**
     * AttachedFileDTO 리스트를 Map 리스트로 변환
     */
    public List<Map<String, Object>> convertAttachedFileDtoToMap(List<AttachedFileDTO> files) {
        if (CollectionUtils.isEmpty(files)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (AttachedFileDTO file : files) {
            if (file == null) continue;
            Map<String, Object> map = new HashMap<>();
            map.put("originalStrgFileNm", file.getOriginalStrgFileNm());
            map.put("storedStrgFileNm", file.getStoredStrgFileNm());
            map.put("strgFilePath", file.getStrgFilePath());
            map.put("strgFileCpct", file.getStrgFileCpct());
            map.put("fileTypeNm", file.getFileTypeNm());
            result.add(map);
        }
        return result;
    }
}