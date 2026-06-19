package egovframework.common.file.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "file")
public class FileConfig {
	private String dir;
	private int defaultMaxFileSizeMb;
	private int defaultMaxFileCount;
	private Map<String, String> extensionGroups;
	private String allowedExtensions;
	private String allowedMimeTypes;
	private Map<String, String> categoryDirectories;
	private FileStorageType storageType = FileStorageType.LOCAL;
	private String tempDir;
	private String publicBaseUrl;
	private SftpProperties sftp = new SftpProperties();

    public static class SftpProperties {
		private String host;
		private int port = 22;
		private String username;
		private String password;
		private String privateKey;
		private String passphrase;
		private String baseDir;
		private boolean skipHostKeyChecking = true;

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getPrivateKey() {
			return privateKey;
		}

		public void setPrivateKey(String privateKey) {
			this.privateKey = privateKey;
		}

		public String getPassphrase() {
			return passphrase;
		}

		public void setPassphrase(String passphrase) {
			this.passphrase = passphrase;
		}

		public String getBaseDir() {
			return baseDir;
		}

		public void setBaseDir(String baseDir) {
			this.baseDir = baseDir;
		}

		public boolean isSkipHostKeyChecking() {
			return skipHostKeyChecking;
		}

		public void setSkipHostKeyChecking(boolean skipHostKeyChecking) {
			this.skipHostKeyChecking = skipHostKeyChecking;
		}
	}
}
