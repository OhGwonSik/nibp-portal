package egovframework.common.file.scheduler;

import egovframework.common.file.mapper.FileMapper;
import egovframework.common.file.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileCleanupScheduler {
    @Value("${file.cleanup.interval-seconds:3600}")
    private int cleanupIntervalSeconds;

    @Value("${file.cleanup.retention-seconds:172800}") // 기본 2일 (48시간)
    private int fileRetentionSeconds;

    @Value("${file.cleanup.chunk-retention-seconds:172800}") // 청크 파일 보관 기간 (기본 2일)
    private int chunkRetentionSeconds;

    @Value("${file.temp-dir}")
    private String tempDir;

    @Value("${file.chunk-dir:/app/temp-chunk}") // 청크 파일 디렉토리
    private String chunkDir;

    @Value("${file.dir:/app/upload}") // 업로드 디렉토리
    private String uploadDir;

    @Value("${file.cleanup.orphan.enabled:true}") // 고아 파일 정리 활성화
    private boolean orphanCleanupEnabled;

    @Value("${file.cleanup.orphan.retention-days:7}") // 고아 파일 보관 기간 (일)
    private int orphanRetentionDays;

    @Value("${file.cleanup.orphan.dry-run:false}") // Dry-run 모드
    private boolean dryRun;

    private final FileUploadService fileUploadService;
    private final FileMapper fileMapper;

    /**
     * 임시 파일 정리 스케줄러
     * - 매일 새벽 4시에 실행
     * - temp 폴더와 temp-chunk 폴더를 재귀적으로 탐색하여 오래된 파일을 삭제
     * - 파일 삭제 후 빈 폴더를 정리
     */
    @Scheduled(cron = "0 0 4 * * ?") // 매일 새벽 4시
    public void cleanupStaleTempFiles() {
        log.info("===== 임시 파일 정리 작업 시작 =====");
        log.info("시작 시간: {}", LocalDateTime.now());

        int totalDeletedFiles = 0;
        int totalDeletedFolders = 0;

        try {
            // 1. temp 디렉토리 정리
            log.info(">>> temp 디렉토리 정리 시작: {}", tempDir);
            log.info("보관 기간: {}초 ({}일)", fileRetentionSeconds, fileRetentionSeconds / 86400.0);

            Path tempDirPath = Paths.get(tempDir);
            if (Files.exists(tempDirPath) && Files.isDirectory(tempDirPath)) {
                long tempCutoffTimeMillis = System.currentTimeMillis() - (fileRetentionSeconds * 1000L);
                LocalDateTime tempCutoffTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(tempCutoffTimeMillis),
                        ZoneId.systemDefault()
                );
                log.info("삭제 기준 시간: {} 이전", tempCutoffTime);

                int deletedFileCount = deleteOldFiles(tempDirPath, tempCutoffTimeMillis);
                int deletedFolderCount = cleanupEmptyFolders(tempDirPath);

                totalDeletedFiles += deletedFileCount;
                totalDeletedFolders += deletedFolderCount;

                log.info("temp 디렉토리 정리 완료 - 파일: {}개, 폴더: {}개", deletedFileCount, deletedFolderCount);
            } else {
                log.warn("temp 디렉토리가 존재하지 않거나 디렉토리가 아닙니다: {}", tempDir);
            }

            // 2. chunk 디렉토리 정리
            log.info(">>> chunk 디렉토리 정리 시작: {}", chunkDir);
            log.info("보관 기간: {}초 ({}일)", chunkRetentionSeconds, chunkRetentionSeconds / 86400.0);

            Path chunkDirPath = Paths.get(chunkDir);
            if (Files.exists(chunkDirPath) && Files.isDirectory(chunkDirPath)) {
                long chunkCutoffTimeMillis = System.currentTimeMillis() - (chunkRetentionSeconds * 1000L);
                LocalDateTime chunkCutoffTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(chunkCutoffTimeMillis),
                        ZoneId.systemDefault()
                );
                log.info("삭제 기준 시간: {} 이전", chunkCutoffTime);

                int deletedFileCount = deleteOldFiles(chunkDirPath, chunkCutoffTimeMillis);
                int deletedFolderCount = cleanupEmptyFolders(chunkDirPath);

                totalDeletedFiles += deletedFileCount;
                totalDeletedFolders += deletedFolderCount;

                log.info("chunk 디렉토리 정리 완료 - 파일: {}개, 폴더: {}개", deletedFileCount, deletedFolderCount);
            } else {
                log.warn("chunk 디렉토리가 존재하지 않거나 디렉토리가 아닙니다: {}", chunkDir);
            }

            log.info("===== 전체 정리 작업 완료 - 파일: {}개, 폴더: {}개 삭제됨 =====",
                    totalDeletedFiles, totalDeletedFolders);

        } catch (Exception e) {
            log.error("임시 파일 정리 중 오류 발생", e);
        }

        log.info("===== 임시 파일 정리 작업 종료 =====");
    }

    /**
     * 디렉토리를 재귀적으로 탐색하여 오래된 파일만 삭제
     * @param dirPath 탐색할 디렉토리
     * @param cutoffTimeMillis 삭제 기준 시간 (밀리초)
     * @return 삭제된 파일 개수
     */
    private int deleteOldFiles(Path dirPath, long cutoffTimeMillis) {
        int deletedCount = 0;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    // 하위 디렉토리 재귀 탐색
                    deletedCount += deleteOldFiles(entry, cutoffTimeMillis);
                } else if (Files.isRegularFile(entry)) {
                    // 파일인 경우 날짜 확인 후 삭제
                    try {
                        BasicFileAttributes attrs = Files.readAttributes(entry, BasicFileAttributes.class);
                        long creationTime = attrs.creationTime().toMillis();
                        long lastModifiedTime = attrs.lastModifiedTime().toMillis();

                        // 생성 시간과 수정 시간 중 더 최근 시간을 기준으로 판단
                        long latestTime = Math.max(creationTime, lastModifiedTime);

                        if (latestTime < cutoffTimeMillis) {
                            // 오래된 파일 삭제
                            LocalDateTime fileTime = LocalDateTime.ofInstant(
                                    Instant.ofEpochMilli(latestTime),
                                    ZoneId.systemDefault()
                            );

                            Files.delete(entry);
                            deletedCount++;
                            log.info("파일 삭제: {} (최종 시간: {})", entry, fileTime);
                        }
                    } catch (IOException e) {
                        log.error("파일 삭제 실패: {}", entry, e);
                    }
                }
            }
        } catch (IOException e) {
            log.error("디렉토리 탐색 오류: {}", dirPath, e);
        }

        return deletedCount;
    }

    /**
     * 빈 폴더를 재귀적으로 정리 (하위부터 상위로)
     * @param dirPath 정리할 디렉토리
     * @return 삭제된 폴더 개수
     */
    private int cleanupEmptyFolders(Path dirPath) {
        int deletedCount = 0;

        try {
            // 하위 디렉토리 먼저 정리 (깊이 우선)
            List<Path> subdirs = new ArrayList<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
                for (Path entry : stream) {
                    if (Files.isDirectory(entry)) {
                        subdirs.add(entry);
                    }
                }
            }

            // 각 하위 디렉토리 재귀 정리
            for (Path subdir : subdirs) {
                deletedCount += cleanupEmptyFolders(subdir);
            }

            // 현재 디렉토리가 비었는지 확인 (루트 디렉토리 자체는 제외)
            Path normalizedDirPath = dirPath.toAbsolutePath().normalize();
            if (!normalizedDirPath.equals(Paths.get(tempDir).toAbsolutePath().normalize())
                    && !normalizedDirPath.equals(Paths.get(chunkDir).toAbsolutePath().normalize())
                    && isDirectoryEmpty(dirPath)) {
                Files.delete(dirPath);
                deletedCount++;
                log.info("빈 폴더 삭제: {}", dirPath);
            }

        } catch (IOException e) {
            log.error("폴더 정리 오류: {}", dirPath, e);
        }

        return deletedCount;
    }

    /**
     * 디렉토리가 비어있는지 확인
     * @param dirPath 확인할 디렉토리
     * @return 비어있으면 true
     */
    private boolean isDirectoryEmpty(Path dirPath) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
            return !stream.iterator().hasNext();
        } catch (IOException e) {
            log.error("디렉토리 확인 오류: {}", dirPath, e);
            return false;
        }
    }

    /**
     * 고아 파일 정리 스케줄러
     * - 매주 일요일 새벽 3시 30분에 실행
     * - DB에 레코드가 없는 물리적 파일을 고아 파일로 식별
     * - 7일 이상 지난 고아 파일만 삭제
     */
    // @Scheduled(cron = "0 30 3 * * 0") // 매주 일요일 새벽 3시 30분
    // @Scheduled(cron = "0 30 3 * * ?") // 매일 새벽 3시 30분
    public void cleanupOrphanedFiles() {
        if (!orphanCleanupEnabled) {
            log.debug("고아 파일 정리가 비활성화되어 있습니다.");
            return;
        }

        log.info("===== 고아 파일 정리 작업 시작 =====");
        log.info("시작 시간: {}", LocalDateTime.now());
        log.info("대상 디렉토리: {}", uploadDir);
        log.info("보관 기간: {}일", orphanRetentionDays);
        log.info("Dry-run 모드: {}", dryRun);

        try {
            Path uploadDirPath = Paths.get(uploadDir);

            // 디렉토리 존재 여부 확인
            if (!Files.exists(uploadDirPath) || !Files.isDirectory(uploadDirPath)) {
                log.warn("업로드 디렉토리가 존재하지 않거나 디렉토리가 아닙니다: {}", uploadDir);
                return;
            }

            // 1단계: DB에서 모든 파일 경로 조회
            log.info(">>> 1단계: DB 파일 경로 조회 중...");
            Set<String> dbFilePaths = getAllFilePathsFromDB();
            log.info("DB에 등록된 파일 경로 수: {}개", dbFilePaths.size());

            // 2단계: 파일 시스템에서 실제 파일 수집
            log.info(">>> 2단계: 파일 시스템 스캔 중...");
            List<Path> physicalFiles = scanUploadDirectory(uploadDirPath);
            log.info("물리적 파일 수: {}개", physicalFiles.size());

            // 3단계: 고아 파일 식별
            log.info(">>> 3단계: 고아 파일 식별 중...");
            List<Path> orphanedFiles = findOrphanedFiles(physicalFiles, dbFilePaths);
            log.info("고아 파일 수: {}개", orphanedFiles.size());

            // 4단계: 오래된 고아 파일 삭제
            log.info(">>> 4단계: 오래된 고아 파일 삭제 중...");
            int deletedCount = deleteOldOrphanedFiles(orphanedFiles, orphanRetentionDays);

            if (dryRun) {
                log.info("===== Dry-run 모드: 실제 삭제 없이 시뮬레이션만 수행됨 =====");
            }
            log.info("===== 고아 파일 정리 완료 - 삭제된 파일: {}개 =====", deletedCount);

        } catch (Exception e) {
            log.error("고아 파일 정리 중 오류 발생", e);
        }

        log.info("===== 고아 파일 정리 작업 종료 =====");
    }

    /**
     * DB에서 모든 파일 경로를 조회
     * @return 파일 경로 Set
     */
    private Set<String> getAllFilePathsFromDB() {
        try {
            List<String> paths = fileMapper.selectAllFilePaths();
            // 절대 경로로 정규화
            return paths.stream()
                    .filter(path -> path != null && !path.isEmpty())
                    .map(this::normalizeFilePath)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("DB 파일 경로 조회 중 오류 발생", e);
            return new HashSet<>();
        }
    }

    /**
     * 파일 경로를 정규화 (절대 경로로 변환)
     * @param filePath 파일 경로
     * @return 정규화된 경로
     */
    private String normalizeFilePath(String filePath) {
        // /files/... 형태를 /app/upload/... 형태로 변환
        if (filePath.startsWith("/files/")) {
            return uploadDir + filePath.substring(6);
        }
        // 이미 절대 경로면 그대로 반환
        if (filePath.startsWith("/")) {
            return filePath;
        }
        // 상대 경로면 업로드 디렉토리와 결합
        return uploadDir + "/" + filePath;
    }

    /**
     * 업로드 디렉토리를 재귀적으로 스캔하여 모든 파일 수집
     * @param uploadDirPath 업로드 디렉토리 경로
     * @return 파일 목록
     */
    private List<Path> scanUploadDirectory(Path uploadDirPath) {
        List<Path> files = new ArrayList<>();
        Path tempDirPath = Paths.get(tempDir);
        Path chunkDirPath = Paths.get(chunkDir);

        try (Stream<Path> walk = Files.walk(uploadDirPath)) {
            files = walk
                    .filter(Files::isRegularFile)
                    // temp, chunk 디렉토리 제외 (별도 스케줄러로 관리)
                    .filter(path -> !path.startsWith(tempDirPath))
                    .filter(path -> !path.startsWith(chunkDirPath))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("파일 시스템 스캔 오류: {}", uploadDirPath, e);
        }

        return files;
    }

    /**
     * 고아 파일 식별 (물리적 파일이 DB에 없는 경우)
     * @param physicalFiles 물리적 파일 목록
     * @param dbFilePaths DB 파일 경로 Set
     * @return 고아 파일 목록
     */
    private List<Path> findOrphanedFiles(List<Path> physicalFiles, Set<String> dbFilePaths) {
        return physicalFiles.stream()
                .filter(file -> {
                    String filePath = file.toString();
                    boolean isOrphan = !dbFilePaths.contains(filePath);
                    if (isOrphan) {
                        log.debug("고아 파일 발견: {}", filePath);
                    }
                    return isOrphan;
                })
                .collect(Collectors.toList());
    }

    /**
     * 오래된 고아 파일 삭제
     * @param orphanedFiles 고아 파일 목록
     * @param retentionDays 보관 기간 (일)
     * @return 삭제된 파일 수
     */
    private int deleteOldOrphanedFiles(List<Path> orphanedFiles, int retentionDays) {
        int deletedCount = 0;
        long cutoffTimeMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(retentionDays);

        for (Path file : orphanedFiles) {
            try {
                BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
                long creationTime = attrs.creationTime().toMillis();
                long lastModifiedTime = attrs.lastModifiedTime().toMillis();
                long latestTime = Math.max(creationTime, lastModifiedTime);

                if (latestTime < cutoffTimeMillis) {
                    LocalDateTime fileTime = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(latestTime),
                            ZoneId.systemDefault()
                    );

                    if (dryRun) {
                        log.info("[Dry-run] 삭제 대상: {} (최종 시간: {})", file, fileTime);
                        deletedCount++;
                    } else {
                        Files.delete(file);
                        deletedCount++;
                        log.info("고아 파일 삭제: {} (최종 시간: {})", file, fileTime);
                    }
                } else {
                    log.debug("고아 파일 보관 유지: {} (아직 보관 기간 내)", file);
                }
            } catch (IOException e) {
                log.error("고아 파일 삭제 실패: {}", file, e);
            }
        }

        return deletedCount;
    }
}