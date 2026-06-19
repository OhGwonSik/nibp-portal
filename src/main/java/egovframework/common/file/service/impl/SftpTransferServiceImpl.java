package egovframework.common.file.service.impl;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import egovframework.common.file.config.FileConfig;
import egovframework.common.file.config.FileStorageType;
import egovframework.common.file.service.SftpTransferService;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;
import java.util.Vector;

@Component
@RequiredArgsConstructor
public class SftpTransferServiceImpl extends EgovAbstractServiceImpl implements SftpTransferService {

	private final FileConfig fileConfig;

	public boolean isSftpEnabled() {
		return fileConfig.getStorageType() == FileStorageType.SFTP;
	}

	public void uploadFile(Path localFile, String relativePath, String remoteFileName) throws IOException {
		execute(channel -> {
			String remoteDir = buildRemoteDirectory(relativePath);
			ensureRemoteDirectory(channel, remoteDir);
			try (InputStream inputStream = Files.newInputStream(localFile)) {
				channel.put(inputStream, remoteDir + "/" + remoteFileName);
			}
			return null;
		});
	}

	public void uploadStream(InputStream inputStream, String relativePath, String remoteFileName) throws IOException {
		execute(channel -> {
			String remoteDir = buildRemoteDirectory(relativePath);
			ensureRemoteDirectory(channel, remoteDir);
			channel.put(inputStream, remoteDir + "/" + remoteFileName);
			return null;
		});
	}

	public long mergeChunks(String tempRelativePath, int totalChunks, String targetRelativePath, String targetFileName,
			boolean cleanupDirectory) throws IOException {
		return execute(channel -> {
			String tempDir = buildRemoteDirectory(tempRelativePath);
			String targetDir = buildRemoteDirectory(targetRelativePath);
			ensureRemoteDirectory(channel, targetDir);
			String targetFile = targetDir + "/" + targetFileName;

			long totalBytes = 0L;
			for (int i = 1; i <= totalChunks; i++) {
				String chunkPath = tempDir + "/chunk" + i;
				ByteArrayOutputStream chunkBuffer = new ByteArrayOutputStream();
				try (InputStream in = channel.get(chunkPath)) {
					byte[] buffer = new byte[8192];
					int read;
					while ((read = in.read(buffer)) != -1) {
						chunkBuffer.write(buffer, 0, read);
					}
				}
				byte[] chunkBytes = chunkBuffer.toByteArray();
				totalBytes += chunkBytes.length;
				try (InputStream chunkInput = new ByteArrayInputStream(chunkBytes)) {
					if (i == 1) {
						channel.put(chunkInput, targetFile);
					} else {
						channel.put(chunkInput, targetFile, ChannelSftp.APPEND);
					}
				}
			}

			for (int i = 1; i <= totalChunks; i++) {
				String chunkPath = tempDir + "/chunk" + i;
				try {
					channel.rm(chunkPath);
				} catch (SftpException e) {
					if (e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
						throw e;
					}
				}
			}
			if (cleanupDirectory) {
				try {
					channel.rmdir(tempDir);
				} catch (SftpException e) {
					if (e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
						throw e;
					}
				}
			}
			return totalBytes;
		});
	}

	public Resource downloadFile(String relativePath, String remoteFileName) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		execute(channel -> {
			String remoteFile = buildRemoteDirectory(relativePath) + "/" + remoteFileName;
			channel.get(remoteFile, out);
			return null;
		});
		return new ByteArrayResource(out.toByteArray());
	}

	public void deleteFile(String relativePath, String remoteFileName) throws IOException {
		execute(channel -> {
			String remoteFile = buildRemoteDirectory(relativePath) + "/" + remoteFileName;
			try {
				channel.rm(remoteFile);
			} catch (SftpException e) {
				if (e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
					throw e;
				}
			}
			return null;
		});
	}

	public void moveFile(String sourceRelativePath, String remoteFileName, String targetRelativePath,
			String targetFileName) throws IOException {
		execute(channel -> {
			String sourceFile = buildRemoteDirectory(sourceRelativePath) + "/" + remoteFileName;
			String targetDir = buildRemoteDirectory(targetRelativePath);
			ensureRemoteDirectory(channel, targetDir);
			String targetFile = targetDir + "/" + targetFileName;
			channel.rename(sourceFile, targetFile);
			return null;
		});
	}

	public void deleteDirectory(String relativePath) throws IOException {
		execute(channel -> {
			String remoteDir = buildRemoteDirectory(relativePath);
			try {
				deleteDirectoryRecursive(channel, remoteDir);
			} catch (SftpException e) {
				if (e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
					throw e;
				}
			}
			return null;
		});
	}

	private void deleteDirectoryRecursive(ChannelSftp channel, String remoteDir) throws SftpException {
		Vector<?> entries = channel.ls(remoteDir);
		for (Object entryObj : entries) {
			if (!(entryObj instanceof LsEntry)) {
				continue;
			}
			LsEntry entry = (LsEntry) entryObj;
			String fileName = entry.getFilename();
			if (".".equals(fileName) || "..".equals(fileName)) {
				continue;
			}
			String childPath = remoteDir + "/" + fileName;
			if (entry.getAttrs().isDir()) {
				deleteDirectoryRecursive(channel, childPath);
			} else {
				channel.rm(childPath);
			}
		}
		channel.rmdir(remoteDir);
	}

	private <T> T execute(SftpCallback<T> callback) throws IOException {
		if (!isSftpEnabled()) {
			throw new IOException("SFTP storage is not enabled.");
		}

		FileConfig.SftpProperties props = fileConfig.getSftp();
		Objects.requireNonNull(props.getHost(), "SFTP host must be configured");
		Objects.requireNonNull(props.getUsername(), "SFTP username must be configured");

		Session session = null;
		ChannelSftp channel = null;
		try {
			JSch jsch = new JSch();
			if (props.getPrivateKey() != null && !props.getPrivateKey().isBlank()) {
				if (props.getPassphrase() != null && !props.getPassphrase().isBlank()) {
					jsch.addIdentity(props.getPrivateKey(), props.getPassphrase());
				} else {
					jsch.addIdentity(props.getPrivateKey());
				}
			}

			session = jsch.getSession(props.getUsername(), props.getHost(), props.getPort());
			if (props.getPassword() != null && !props.getPassword().isBlank()) {
				session.setPassword(props.getPassword());
			}
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", props.isSkipHostKeyChecking() ? "no" : "yes");
			session.setConfig(config);
			session.connect();

			channel = (ChannelSftp) session.openChannel("sftp");
			channel.connect();
			return callback.doWithChannel(channel);
		} catch (JSchException | SftpException e) {
			throw new IOException("SFTP operation failed", e);
		} finally {
			if (channel != null && channel.isConnected()) {
				channel.disconnect();
			}
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
		}
	}

	private void ensureRemoteDirectory(ChannelSftp channel, String remoteDir) throws SftpException {
		String[] folders = remoteDir.split("/");
		StringBuilder builder = new StringBuilder();
		for (String folder : folders) {
			if (folder == null || folder.isBlank()) {
				continue;
			}
			builder.append('/').append(folder);
			String currentPath = builder.toString();
			try {
				channel.cd(currentPath);
			} catch (SftpException e) {
				channel.mkdir(currentPath);
			}
		}
	}

	private String buildRemoteDirectory(String relativePath) {
		String baseDir = fileConfig.getSftp().getBaseDir();
		if (baseDir == null || baseDir.isBlank()) {
			baseDir = fileConfig.getDir();
		}
		Path result = Paths.get(".");
		if (baseDir != null && !baseDir.isBlank()) {
			result = Paths.get(baseDir);
		}
		if (relativePath != null && !relativePath.isBlank()) {
			result = result.resolve(relativePath);
		}
		String normalized = result.normalize().toString().replace("\\", "/");
		return ".".equals(normalized) ? "" : normalized;
	}

	@FunctionalInterface
	private interface SftpCallback<T> {
		T doWithChannel(ChannelSftp channel) throws SftpException, IOException;
	}
}
