# ============== Multi-stage build for Spring Boot application ==============
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /workspace

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle* ./

RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# Download dependencies
RUN ./gradlew dependencies --no-daemon || return 0

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN ./gradlew --no-daemon clean bootWar -x test

RUN BOOT_WAR="$(ls build/libs/*.war | grep -Ev 'plain|sources|javadoc' | head -n 1)" \
 && echo "Using boot war: ${BOOT_WAR}" \
 && cp "${BOOT_WAR}" /workspace/app.war

# ============== Runtime stage ==============================
FROM eclipse-temurin:17-jdk-jammy

# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# --------- 시스템 패키지 업데이트 + libvips + fonts 설치 ----------
RUN apt-get update && apt-get install -y --no-install-recommends \
    bash \
    # libvips 및 관련 라이브러리
    libvips-tools \
    libvips42 \
    # WebP 지원
    webp \
    libwebp-dev \
    # HEIF/AVIF 지원 (선택사항)
    libheif1 \
    # 기타 이미지 포맷 지원
    libjpeg-turbo-progs \
    libpng-tools \
    # 폰트 (Jasper Reports용)
    fontconfig \
    fonts-nanum \
    fonts-nanum-coding \
    fonts-noto-cjk \
    # 타임존 설정
    tzdata \
    # 프로세스 관리
    tini \
    && rm -rf /var/lib/apt/lists/* \
    && fc-cache -f -v

# TZ setting
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# libvips 버전 확인 (빌드 시 로그용)
RUN vips --version
# -------------------------------------------------------------------------

# Create app directory, upload directory and logs directory
RUN mkdir -p /app /data/upload /data/logs

# Set working directory
WORKDIR /app

# Copy the built WAR file from build stage
COPY --from=build /workspace/app.war ./app.war

# Copy jasper folder from build stage
# COPY --from=build /workspace/src/main/resources/jasper /app/jasper

# Set jasper path environment variable
# ENV JASPER_PATH=/app/jasper

# Expose port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8082/ || exit 1

# Run the application
ENTRYPOINT ["sh", "-c"]
CMD ["java $JAVA_OPTS -jar app.war"]