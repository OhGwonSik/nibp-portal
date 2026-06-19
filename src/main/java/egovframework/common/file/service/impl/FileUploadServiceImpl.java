package egovframework.common.file.service.impl;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import egovframework.common.file.config.FileConfig;
import egovframework.common.file.config.FileStorageType;
import egovframework.common.file.domain.UploadedFileInfo;
import egovframework.common.file.service.FileUploadService;
import egovframework.common.file.vo.FileVO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl extends EgovAbstractServiceImpl implements FileUploadService {

	private final FileConfig fileConfig;
	private final SftpTransferServiceImpl sftpTransferService;
	private static final String TEMP_DIR = "temp-chunk";
	private final Tika tika = new Tika();

	public Path getBaseUploadPath() {
		String configuredPath = fileConfig.getDir();
		if (fileConfig.getStorageType() == FileStorageType.SFTP) {
			if (fileConfig.getTempDir() != null && !fileConfig.getTempDir().isBlank()) {
				configuredPath = fileConfig.getTempDir();
			} else {
				configuredPath = Paths.get(System.getProperty("java.io.tmpdir"), "uploads").toString();
			}
		}

		if (configuredPath == null || configuredPath.isBlank()) {
			configuredPath = Paths.get(System.getProperty("user.dir"), "uploads").toString();
		}

		Path uploadBaseDir = Paths.get(configuredPath);
		return uploadBaseDir.isAbsolute() ? uploadBaseDir
				: Paths.get(System.getProperty("user.dir")).resolve(uploadBaseDir).normalize();
	}

	public void saveChunk(MultipartFile chunk, int chunkNumber, String identifier) throws IOException {
		String safeIdentifier = sanitizeIdentifier(identifier);
		if (isSftpStorage()) {
			String tempRelativePath = buildTempRelativePath(safeIdentifier);
			// MultipartInputStream이 업로드 중간에 닫히는 문제를 방지하기 위해 메모리에 복사
			byte[] chunkBytes;
			try (InputStream inputStream = chunk.getInputStream()) {
				chunkBytes = inputStream.readAllBytes();
			}
			try (InputStream uploadStream = new ByteArrayInputStream(chunkBytes)) {
				sftpTransferService.uploadStream(uploadStream, tempRelativePath, "chunk" + chunkNumber);
			}
			return;
		}

		Path tempDir = getBaseUploadPath().resolve(Paths.get(TEMP_DIR, safeIdentifier));
		Files.createDirectories(tempDir);
		Path chunkPath = tempDir.resolve("chunk" + chunkNumber);
		chunk.transferTo(chunkPath);
	}

	public UploadedFileInfo mergeChunks(String identifier, String filename, int totalChunks, long expectedSize)
			throws IOException {
		String safeIdentifier = sanitizeIdentifier(identifier);
		String tempRelativePath = buildTempRelativePath(safeIdentifier);
		if (isSftpStorage()) {
			return mergeChunksSftp(tempRelativePath, filename, totalChunks, expectedSize);
		}
		return mergeChunksLocal(tempRelativePath, filename, totalChunks, expectedSize);
	}

	private UploadedFileInfo mergeChunksLocal(String tempRelativePath, String filename, int totalChunks,
			long expectedSize) throws IOException {
		Path tempDir = getBaseUploadPath().resolve(tempRelativePath);
		Files.createDirectories(tempDir);

        String fileTypeNm = getFileExtension(filename);
        String storedStrgFileNm = generateRandomStoredName(fileTypeNm);
        Path mergedFile = tempDir.resolve(storedStrgFileNm);

        writeChunks(tempDir, mergedFile, totalChunks, expectedSize);
        validateFileType(mergedFile, fileTypeNm);
        deleteChunkParts(tempDir, totalChunks);

        return new UploadedFileInfo(filename, storedStrgFileNm, tempRelativePath, expectedSize, fileTypeNm);
    }

    private UploadedFileInfo mergeChunksSftp(String tempRelativePath, String filename, int totalChunks,
                                             long expectedSize) throws IOException {
        String fileTypeNm = getFileExtension(filename);
        String storedStrgFileNm = generateRandomStoredName(fileTypeNm);

        long mergedSize = sftpTransferService.mergeChunks(tempRelativePath, totalChunks, tempRelativePath,
                storedStrgFileNm, false);
        if (mergedSize != expectedSize) {
            sftpTransferService.deleteFile(tempRelativePath, storedStrgFileNm);
            throw new IOException("File size mismatch. Expected: " + expectedSize + ", Actual: " + mergedSize);
        }
        validateRemoteFile(tempRelativePath, storedStrgFileNm, fileTypeNm);

        return new UploadedFileInfo(filename, storedStrgFileNm, tempRelativePath, mergedSize, fileTypeNm);
    }

	private boolean isSftpStorage() {
		return fileConfig.getStorageType() == FileStorageType.SFTP && sftpTransferService.isSftpEnabled();
	}

	private String sanitizeIdentifier(String identifier) {
		if (identifier == null || identifier.isBlank()) {
			return "";
		}
		return identifier.replaceAll("[^a-zA-Z0-9\\-]", "_");
	}

	private String buildTempRelativePath(String identifier) {
		return Paths.get(TEMP_DIR, sanitizeIdentifier(identifier)).toString().replace("\\", "/");
	}

	private String resolveRelativePath(String relativePath) {
		if (relativePath != null && !relativePath.isBlank()) {
			return relativePath.replace("\\", "/");
		}

		LocalDate now = LocalDate.now();
		return Paths.get(String.valueOf(now.getYear()), now.format(DateTimeFormatter.ofPattern("MM"))).toString()
				.replace("\\", "/");
	}

	private void validateRemoteFile(String relativePath, String storedStrgFileNm, String extension) throws IOException {
		validateExtension(extension);
		Set<String> allowedMimeTypes = getAllowedMimeTypes();
		if (allowedMimeTypes.isEmpty()) {
			return;
		}

		try (InputStream remoteStream = sftpTransferService.downloadFile(relativePath, storedStrgFileNm).getInputStream()) {
			String detected = tika.detect(remoteStream);
			if (detected == null || !allowedMimeTypes.contains(detected.toLowerCase(Locale.ROOT))) {
				sftpTransferService.deleteFile(relativePath, storedStrgFileNm);
				throw new IOException("허용되지 않는 파일 형식입니다. (" + detected + ")");
			}
		}
	}

	public boolean isTempStrgFilePath(String strgFilePath) {
		if (strgFilePath == null || strgFilePath.isBlank()) {
			return false;
		}
		String normalized = strgFilePath.replace("\\", "/");
		return normalized.startsWith(TEMP_DIR + "/") || normalized.equals(TEMP_DIR);
	}

    public String moveTempFileToDestination(String currentPath, String storedStrgFileNm, String targetRelativePath)
            throws IOException {
        if (!isTempStrgFilePath(currentPath)) {
            return currentPath;
        }
        String normalizedCurrent = currentPath.replace("\\", "/");
        String normalizedTarget = resolveRelativePath(targetRelativePath);
        moveFileInternal(normalizedCurrent, storedStrgFileNm, normalizedTarget);
        return normalizedTarget;
    }

    public void moveFile(String currentRelativePath, String storedStrgFileNm, String targetRelativePath)
            throws IOException {
        if (!StringUtils.hasText(currentRelativePath) || !StringUtils.hasText(storedStrgFileNm)) {
            throw new IllegalArgumentException("이동할 파일 정보를 확인할 수 없습니다.");
        }
        String normalizedCurrent = currentRelativePath.replace("\\", "/");
        String normalizedTarget = resolveRelativePath(targetRelativePath);
        moveFileInternal(normalizedCurrent, storedStrgFileNm, normalizedTarget);
    }

    private void moveFileInternal(String currentRelativePath, String storedStrgFileNm, String targetRelativePath)
            throws IOException {
        if (isSftpStorage()) {
            sftpTransferService.moveFile(currentRelativePath, storedStrgFileNm, targetRelativePath, storedStrgFileNm);
            return;
        }

        Path baseDir = getBaseUploadPath();
        Path source = baseDir.resolve(currentRelativePath).resolve(storedStrgFileNm);
        if (!Files.exists(source)) {
            throw new IOException("원본 파일을 찾을 수 없습니다: " + source);
        }
        Path targetDir = baseDir.resolve(targetRelativePath);
        Files.createDirectories(targetDir);
        Path target = targetDir.resolve(storedStrgFileNm);
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    public String buildPublicUrl(String relativePath, String storedStrgFileNm) {
        StringBuilder urlBuilder = new StringBuilder();
        String baseUrl = fileConfig.getPublicBaseUrl();
        if (StringUtils.hasText(baseUrl)) {
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            urlBuilder.append(baseUrl);
        }
        urlBuilder.append("/files");
        if (StringUtils.hasText(relativePath)) {
            String normalized = relativePath.replace("\\", "/");
            if (urlBuilder.charAt(urlBuilder.length() - 1) != '/') {
                urlBuilder.append('/');
            }
            urlBuilder.append(normalized.replaceFirst("^/+", ""));
        }
        if (StringUtils.hasText(storedStrgFileNm)) {
            if (urlBuilder.charAt(urlBuilder.length() - 1) != '/') {
                urlBuilder.append('/');
            }
            urlBuilder.append(storedStrgFileNm);
        }
        return urlBuilder.toString();
    }

    public void deleteStoredFile(String relativePath, String storedStrgFileNm) throws IOException {
        if (relativePath == null || storedStrgFileNm == null) {
            throw new IllegalArgumentException("삭제할 파일 정보를 확인할 수 없습니다.");
        }

        if (isSftpStorage()) {
            sftpTransferService.deleteFile(relativePath, storedStrgFileNm);
        } else {
            Path baseDir = getBaseUploadPath();
            Path targetPath = baseDir.resolve(relativePath).resolve(storedStrgFileNm);
            Files.deleteIfExists(targetPath);
        }
    }

    public void cleanupTempDirectory(String tempId) throws IOException {
        String safeIdentifier = sanitizeIdentifier(tempId);
        if (isSftpStorage()) {
            sftpTransferService.deleteDirectory(buildTempRelativePath(safeIdentifier));
            return;
        }

        Path tempDir = getBaseUploadPath().resolve(TEMP_DIR).resolve(safeIdentifier);
        if (Files.exists(tempDir)) {
            FileUtils.deleteDirectory(tempDir.toFile());
        }
    }

    public String getStoredStrgFileNm(FileVO fileVO) {
        String extension = fileVO.getFileExtn();
        if (extension == null || extension.isBlank()) {
            return fileVO.getFileId();
        }
        return fileVO.getFileId() + "." + extension;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains("."))
            return "";
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private String generateRandomStoredName(String extension) {
        String base = UUID.randomUUID().toString().replace("-", "");
        if (extension == null || extension.isBlank())
            return base;
        return base + "." + extension;
    }

    private void writeChunks(Path tempDir, Path destination, int totalChunks, long expectedSize) throws IOException {
        try (OutputStream os = new FileOutputStream(destination.toFile())) {
            for (int i = 1; i <= totalChunks; i++) {
                Path part = tempDir.resolve("chunk" + i);
                if (!Files.exists(part))
                    throw new IOException("Chunk file is missing: " + part);
                Files.copy(part, os);
            }
        }

        long actualSize = Files.size(destination);
        if (actualSize != expectedSize) {
            Files.deleteIfExists(destination);
            throw new IOException("File size mismatch. Expected: " + expectedSize + ", Actual: " + actualSize);
        }
    }

    private void deleteChunkParts(Path tempDir, int totalChunks) {
        for (int i = 1; i <= totalChunks; i++) {
            Path part = tempDir.resolve("chunk" + i);
            try {
                Files.deleteIfExists(part);
            } catch (IOException ignored) {
            }
        }
    }

    private void validateFileType(Path mergedFile, String extension) throws IOException {
        validateExtension(extension);
        validateMimeType(mergedFile);
    }

    private void validateExtension(String extension) throws IOException {
        Set<String> allowedExtensions = getAllowedExtensions();
        if (allowedExtensions.isEmpty()) {
            return;
        }
        String lowerExt = extension == null ? "" : extension.toLowerCase(Locale.ROOT);
        if (!allowedExtensions.contains(lowerExt)) {
            throw new IOException("허용되지 않는 확장자입니다. (" + String.join(", ", allowedExtensions) + ")");
        }
    }

    private void validateMimeType(Path mergedFile) throws IOException {
        Set<String> allowedMimeTypes = getAllowedMimeTypes();
        if (allowedMimeTypes.isEmpty()) {
            return;
        }
        String detected = tika.detect(mergedFile.toFile());
        if (detected == null || !allowedMimeTypes.contains(detected.toLowerCase(Locale.ROOT))) {
            throw new IOException("허용되지 않는 파일 형식입니다. (" + detected + ")");
        }
    }

    private Set<String> getAllowedExtensions() {
        return parseCommaSeparated(fileConfig.getAllowedExtensions());
    }

    private Set<String> getAllowedMimeTypes() {
        return parseCommaSeparated(fileConfig.getAllowedMimeTypes());
    }

    private Set<String> parseCommaSeparated(String input) {
        if (input == null || input.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(input.split(","))
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 단일 MultipartFile 업로드
     * - chunk 업로드가 아니므로 mergeAndSaveFile 사용 X
     * - 일반 파일 업로드용
     */
    @Transactional
    public FileVO uploadFile(MultipartFile file, String rfrncSeCd, String targetId, String userId) throws IOException {

        // 기본 업로드 경로
        Path baseDir = getBaseUploadPath();

        // 연/월 폴더 생성
        LocalDate now = LocalDate.now();
        String relativePath = Paths.get(
                String.valueOf(now.getYear()),
                now.format(DateTimeFormatter.ofPattern("MM"))
        ).toString().replace("\\", "/");

        Path uploadPath = baseDir.resolve(relativePath);
        Files.createDirectories(uploadPath);

        // FileVO 생성
        String originalName = file.getOriginalFilename();
        String ext = getFileExtension(originalName);

        FileVO fileVO = new FileVO();
        fileVO.setFileNm(originalName);
        fileVO.setFileExtn(ext);
        fileVO.setStrgFilePath(relativePath);
        fileVO.setStrgFileCpct(file.getSize());
        fileVO.setRfrncSeCd(rfrncSeCd);    // 예: "QNA/ANSWER"
        fileVO.setRfrncKeyId(targetId);    // 예: qnaOid
        fileVO.setRgtrId(userId);

        // 실제 저장 파일명
        String storedName = getStoredStrgFileNm(fileVO);

        // 서버에 저장
        Path savePath = uploadPath.resolve(storedName);
        file.transferTo(savePath);

        // SFTP 사용 시 전송
        if (isSftpStorage()) {
            sftpTransferService.uploadFile(savePath, relativePath, storedName);
            Files.deleteIfExists(savePath);
        }

        return fileVO;
    }

    /**
     * 여러 파일 업로드
     */
    @Transactional
    public List<FileVO> uploadFiles(List<MultipartFile> files, String rfrncSeCd, String targetId, String userId) throws IOException {
        List<FileVO> result = new java.util.ArrayList<>();

        if (files == null || files.isEmpty()) {
            return result;
        }

        for (MultipartFile file : files) {
            if (file.isEmpty() || file.getSize() == 0) continue;

            FileVO vo = uploadFile(file, rfrncSeCd, targetId, userId);
            result.add(vo);
        }

        return result;
    }
}