package egovframework.common.email.dto;

import java.io.File;
import java.util.List;

import lombok.Builder;

/**
 * 이메일 메시지를 나타내는 DTO 입니다.
 *
 * @param to 수신자 이메일 주소
 * @param subject 제목
 * @param content 내용 (HTML 가능)
 * @param from 발신자 이메일 주소 (설정하지 않으면 기본값 사용)
 * @param attachments 첨부파일 목록
 */
@Builder
public record EmailMessage(
    String[] to,
    String subject,
    String content,
    String from,
    List<File> attachments
) {
}
