package egovframework.common.file.service;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface SftpTransferService {
	public boolean isSftpEnabled();
	public void uploadFile(Path localFile, String relativePath, String remoteFileName) throws IOException;
	public void uploadStream(InputStream inputStream, String relativePath, String remoteFileName) throws IOException;
	public long mergeChunks(String tempRelativePath, int totalChunks, String targetRelativePath, String targetFileName,	boolean cleanupDirectory) throws IOException;
	public Resource downloadFile(String relativePath, String remoteFileName) throws IOException;
	public void deleteFile(String relativePath, String remoteFileName) throws IOException;
	public void moveFile(String sourceRelativePath, String remoteFileName, String targetRelativePath, String targetFileName) throws IOException;
	public void deleteDirectory(String relativePath) throws IOException;
}
