package egovframework.common.file.domain;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class UploadedFileInfo {
	private final String originalStrgFileNm;
	private final String storedStrgFileNm;
	private final String strgFilePath;
	private final long strgFileCpct;
	private final String fileTypeNm;

	public UploadedFileInfo(String originalStrgFileNm, String storedStrgFileNm, String strgFilePath, long strgFileCpct,
			String fileTypeNm) {
		this.originalStrgFileNm = originalStrgFileNm;
		this.storedStrgFileNm = storedStrgFileNm;
		this.strgFilePath = strgFilePath;
		this.strgFileCpct = strgFileCpct;
		this.fileTypeNm = fileTypeNm;
	}
}
