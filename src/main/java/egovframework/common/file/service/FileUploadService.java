package egovframework.common.file.service;

import egovframework.common.file.domain.UploadedFileInfo;
import egovframework.common.file.vo.FileVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FileUploadService {
	public Path getBaseUploadPath();
	public void saveChunk(MultipartFile chunk, int chunkNumber, String identifier) throws IOException;
	public UploadedFileInfo mergeChunks(String identifier, String filename, int totalChunks, long expectedSize) throws IOException;
	public boolean isTempStrgFilePath(String strgFilePath);
	public String moveTempFileToDestination(String currentPath, String storedStrgFileNm, String targetRelativePath) throws IOException;
	public void moveFile(String currentRelativePath, String storedStrgFileNm, String targetRelativePath) throws IOException;
	public String buildPublicUrl(String relativePath, String storedStrgFileNm);
	public void deleteStoredFile(String relativePath, String storedStrgFileNm) throws IOException;
	public void cleanupTempDirectory(String tempId) throws IOException;
	public String getStoredStrgFileNm(FileVO fileVO);
	public FileVO uploadFile(MultipartFile file, String rfrncSeCd, String targetId, String userId) throws IOException;
	public List<FileVO> uploadFiles(List<MultipartFile> files, String rfrncSeCd, String targetId, String userId) throws IOException;
}
