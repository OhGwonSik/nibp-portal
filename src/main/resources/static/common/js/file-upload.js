/**
 * 공통 파일 업로드 모듈
 *
 * 사용법:
 * const uploader = CommonFileUpload.create({
 *     fileInputId: 'notice_file_input',
 *     fileListId: 'notice_file_list',
 *     uploadGuideId: 'file_upload_guide',
 *     rfrncSeCd: 'NOTICE',
 *     chunkSize: 10 * 1024 * 1024, // 10MB
 *     chunkThreshold: 10 * 1024 * 1024, // 10MB 이상이면 청크 업로드
 *     onFilesChange: (files) => { console.log('파일 변경됨', files); }
 * });
 *
 * // 파일 선택 시:
 * // - 작은 파일: File 객체만 저장
 * // - 큰 파일: 즉시 temp에 청크 업로드
 *
 * // 저장 버튼 클릭 시:
 * const smallFiles = uploader.getSelectedFiles(); // 작은 파일들 (FormData에 추가)
 * const chunkFiles = uploader.getChunkUploadedFiles(); // 청크 업로드된 파일 정보 (attachedFiles로 전송)
 */

const CommonFileUpload = {
    /**
     * 파일 업로더 인스턴스 생성
     */
    create: function(config) {
        return new FileUploader(config);
    }
};

class FileUploader {
    constructor(config) {
        this.config = {
            fileInputId: config.fileInputId || 'file_input',
            fileListId: config.fileListId || 'file_list',
            uploadGuideId: config.uploadGuideId || 'file_upload_guide',
            rfrncSeCd: config.rfrncSeCd || '',
            chunkSize: (10 * 1024 * 1024),
            chunkThreshold: config.chunkThreshold || (10 * 1024 * 1024), // 10MB 이상 청크 업로드
            uploadUrl: config.uploadUrl || '/api/upload/file',
            chunkUrl: config.chunkUrl || '/api/upload/chunk',
            tempDeleteUrl: config.tempDeleteUrl || '/api/upload/chunk/temp-file',
            onFilesChange: config.onFilesChange || null,
            uploaderStorageKey: config.uploaderStorageKey || 'tempUploadKey_default'
        };

        this.state = {
            uploadTargetId: null,
            selectedFiles: [], // 선택된 File 객체들
            existingFiles: [], // 기존 파일 (수정 시)
            uploadedFiles: [], // 업로드 완료된 파일 정보
            deleteAttachNos: [], // 삭제할 파일 번호
            isUploading: false
        };

        this.init();
    }

    init() {
        this.initUploaderTarget();
        this.bindEvents();
    }

    initUploaderTarget() {
        let tempId = sessionStorage.getItem(this.config.uploaderStorageKey);
        if (!tempId) {
            tempId = `temp-${Date.now()}-${Math.random().toString(36).substring(2, 8)}`;
            sessionStorage.setItem(this.config.uploaderStorageKey, tempId);
        }
        this.state.uploadTargetId = tempId;
    }

    bindEvents() {
        const _this = this;
        const fileInput = document.getElementById(this.config.fileInputId);
        const fileList = document.getElementById(this.config.fileListId);

        if (fileInput) {
            fileInput.addEventListener('change', function(e) {
                const files = Array.from(e.target.files || []);
                if (files.length > 0) {
                    _this.addFiles(files);
                    e.target.value = ''; // 입력 초기화
                }
            });
        }

        if (fileList) {
            fileList.addEventListener('click', function(e) {
                if (e.target.classList.contains('icon_del')) {
                    e.preventDefault();
                    const item = e.target.closest('p');
                    if (item) {
                        const fileType = item.dataset.fileType;
                        if (fileType === 'existing') {
                            const attachNo = Number(item.dataset.fileAttachNo);
                            _this.removeExistingFile(attachNo);
                        } else if (fileType === 'selected') {
                            const index = Number(item.dataset.fileIndex);
                            _this.removeSelectedFile(index);
                        } else if (fileType === 'uploaded') {
                            const index = Number(item.dataset.fileIndex);
                            _this.removeUploadedFile(index);
                        }
                    }
                }
            });
        }
    }

    /**
     * 파일 선택
     * - 작은 파일: File 객체만 저장
     * - 큰 파일: 즉시 temp에 청크 업로드
     */
    async addFiles(files) {
        if (!files || files.length === 0) return;

        for (const file of files) {
            // 파일 크기에 따라 처리 방식 결정
            if (file.size >= this.config.chunkThreshold) {
                // 큰 파일: 즉시 청크 업로드
                try {
                    this.setUploading(true, `${file.name} 업로드 중...`);
                    const uploadedInfo = await this.uploadChunkedFile(file);
                    if (uploadedInfo) {
                        this.state.uploadedFiles.push(uploadedInfo);
                    }
                    this.setUploading(false);
                } catch (error) {
                    console.error('청크 업로드 실패:', error);
                    this.setUploading(false);
                    this.updateUploadGuide(`${file.name} 업로드 실패`, true);
                }
            } else {
                // 작은 파일: 선택만 저장
                this.state.selectedFiles.push(file);
            }
        }

        this.renderFileList();
        this.updateUploadGuide('파일을 첨부해주세요');

        if (this.config.onFilesChange) {
            this.config.onFilesChange(this.getAllFiles());
        }
    }

    /**
     * 선택된 파일 삭제
     */
    removeSelectedFile(index) {
        if (!confirm('해당 파일을 삭제하시겠습니까?')) return;

        this.state.selectedFiles.splice(index, 1);
        this.renderFileList();

        if (this.config.onFilesChange) {
            this.config.onFilesChange(this.getAllFiles());
        }
    }

    /**
     * 업로드된 파일 삭제 (임시 저장된 파일)
     */
    removeUploadedFile(index) {
        const fileInfo = this.state.uploadedFiles[index];
        if (!fileInfo) return;
        if (!confirm('해당 파일을 삭제하시겠습니까?')) return;

        this.deleteTempUploadedFile(fileInfo)
            .then(() => {
                this.state.uploadedFiles.splice(index, 1);
                this.renderFileList();

                if (this.config.onFilesChange) {
                    this.config.onFilesChange(this.getAllFiles());
                }
            })
            .catch((error) => {
                console.error(error);
                this.updateUploadGuide(error.message || '파일 삭제 중 오류가 발생했습니다.', true);
            });
    }

    /**
     * 기존 파일 삭제 (DB에 저장된 파일)
     */
    removeExistingFile(attachNo) {
        if (!attachNo) return;
        if (!confirm('해당 파일을 삭제하시겠습니까?')) return;

        if (!this.state.deleteAttachNos.includes(attachNo)) {
            this.state.deleteAttachNos.push(attachNo);
        }
        this.state.existingFiles = this.state.existingFiles.filter(file => file.fileOid !== attachNo);
        this.renderFileList();

        if (this.config.onFilesChange) {
            this.config.onFilesChange(this.getAllFiles());
        }
    }

    /**
     * 작은 파일들 가져오기 (FormData에 추가하여 서버에 전송)
     */
    getSelectedFiles() {
        return this.state.selectedFiles;
    }

    /**
     * 청크 업로드된 파일 정보 가져오기 (attachedFiles로 서버에 전송)
     */
    getChunkUploadedFiles() {
        return this.state.uploadedFiles;
    }

    /**
     * 청크 파일 업로드 (큰 파일)
     */
    async uploadChunkedFile(file) {
        const chunkSize = this.config.chunkSize;
        const totalChunks = Math.max(Math.ceil(file.size / chunkSize), 1);
        const identifier = `${this.state.uploadTargetId}-${file.name}-${file.size}-${file.lastModified}`;
        let uploadedInfo = null;

        for (let chunkNumber = 1; chunkNumber <= totalChunks; chunkNumber++) {
            const start = (chunkNumber - 1) * chunkSize;
            const end = Math.min(file.size, start + chunkSize);
            const chunk = file.slice(start, end);

            const formData = new FormData();
            formData.append('file', chunk, file.name);
            formData.append('resumableChunkNumber', chunkNumber);
            formData.append('resumableTotalChunks', totalChunks);
            formData.append('resumableIdentifier', identifier);
            formData.append('resumableFilename', file.name);
            formData.append('fileSize', file.size);
            formData.append('targetId', this.state.uploadTargetId);
            formData.append('rfrncSeCd', this.config.rfrncSeCd);

            const response = await API.upload(this.config.chunkUrl, formData);

            if (chunkNumber === totalChunks) {
                uploadedInfo = response;
            }

            this.updateUploadGuide(`${file.name} 업로드 중... (${chunkNumber}/${totalChunks})`);
        }

        return uploadedInfo;
    }

    /**
     * 임시 업로드 파일 삭제
     */
    deleteTempUploadedFile(fileInfo) {
        if (!fileInfo) return Promise.resolve();

        return fetch(this.config.tempDeleteUrl, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                filePath: fileInfo.filePath,
                storedFileName: fileInfo.storedFileName
            })
        }).then(res => {
            if (!res.ok) {
                throw new Error('파일 삭제에 실패했습니다.');
            }
        });
    }

    /**
     * 임시 파일 전체 정리
     */
    cleanupTempFiles() {
        if (!this.state.uploadedFiles.length) {
            return Promise.resolve();
        }

        const deletions = this.state.uploadedFiles.map(fileInfo =>
            this.deleteTempUploadedFile(fileInfo).catch((err) => {
                console.error(err);
            })
        );

        this.state.uploadedFiles = [];
        return Promise.allSettled(deletions);
    }

    /**
     * 파일 목록 렌더링
     */
    renderFileList() {
        const listEl = document.getElementById(this.config.fileListId);
        if (!listEl) return;

        const fragments = [];

        // 기존 파일 (DB에 저장된 파일)
        this.state.existingFiles.forEach(file => {
            const sizeText = this.formatFileSize(file.fileSize);
            fragments.push(`<p data-file-type="existing" data-file-attach-no="${file.fileOid}">
                ${file.originFileName} (${sizeText})
                <a class="icon_del" href="javascript:;">파일삭제</a>
            </p>`);
        });

        // 선택된 파일 (아직 업로드 안 된 파일)
        this.state.selectedFiles.forEach((file, index) => {
            const sizeText = this.formatFileSize(file.size);
            fragments.push(`<p data-file-type="selected" data-file-index="${index}" style="color: #888;">
                ${file.name}
                <a class="icon_del" href="javascript:;">파일삭제</a>
            </p>`);
        });

        // 업로드된 파일 (임시 저장된 파일)
        this.state.uploadedFiles.forEach((file, index) => {
            const sizeText = this.formatFileSize(file.fileSize);
            fragments.push(`<p data-file-type="uploaded" data-file-index="${index}">
                ${file.originalFileName} (${sizeText})
                <a class="icon_del" href="javascript:;">파일삭제</a>
            </p>`);
        });

        listEl.innerHTML = fragments.join('');
    }

    /**
     * 업로드 상태 설정
     */
    setUploading(uploading, message) {
        this.state.isUploading = uploading;
        if (message) {
            this.updateUploadGuide(message);
        }

        const fileInput = document.getElementById(this.config.fileInputId);
        if (fileInput) {
            fileInput.disabled = uploading;
        }
    }

    /**
     * 업로드 가이드 메시지 업데이트
     */
    updateUploadGuide(message, isError) {
        const guideEl = document.getElementById(this.config.uploadGuideId);
        if (!guideEl) return;

        guideEl.textContent = message || '';
        if (isError) {
            guideEl.classList.add('error');
        } else {
            guideEl.classList.remove('error');
        }
    }

    /**
     * 파일 크기 포맷팅
     */
    formatFileSize(bytes) {
        if (!bytes) return '0 B';
        const units = ['B', 'KB', 'MB', 'GB', 'TB'];
        const i = Math.floor(Math.log(bytes) / Math.log(1024));
        return `${(bytes / Math.pow(1024, i)).toFixed(2)} ${units[i]}`;
    }

    /**
     * 기존 파일 설정 (수정 시)
     */
    setExistingFiles(files) {
        this.state.existingFiles = files || [];
        this.renderFileList();
    }

    /**
     * 업로드 타겟 ID 설정 (수정 시)
     */
    setUploadTargetId(targetId) {
        this.state.uploadTargetId = targetId;
    }

    /**
     * 업로드된 파일 정보 가져오기
     */
    getUploadedFiles() {
        return this.state.uploadedFiles;
    }

    /**
     * 삭제할 파일 번호 가져오기
     */
    getDeleteAttachNos() {
        return this.state.deleteAttachNos;
    }

    /**
     * 전체 파일 정보 가져오기 (기존 + 업로드된)
     */
    getAllFiles() {
        return {
            existingFiles: this.state.existingFiles,
            selectedFiles: this.state.selectedFiles,
            uploadedFiles: this.state.uploadedFiles,
            deleteAttachNos: this.state.deleteAttachNos
        };
    }

    /**
     * 선택된 파일 개수
     */
    getSelectedFilesCount() {
        return this.state.selectedFiles.length;
    }
}

/**
 * CKEditor 파일 관리 모듈
 *
 * 사용법:
 * const ckManager = CommonCKEditorFileManager.create();
 *
 * // CKEditor 초기화 시
 * CommonCKEditor.init({
 *     chunkUpload: {
 *         onUploadComplete: (payload) => ckManager.handleUploadComplete(payload)
 *     }
 * });
 *
 * // 저장 시
 * const formData = {
 *     content: content,
 *     editorFiles: ckManager.getEditorFiles(),
 *     deleteEditorAttachNos: ckManager.prepareEditorDeletionPayload(content, attachList)
 * };
 */
const CommonCKEditorFileManager = {
    create: function() {
        return new CKEditorFileManager();
    }
};

class CKEditorFileManager {
    constructor() {
        this.state = {
            editorFiles: [],
            existingEditorFileKeys: []
        };
    }

    /**
     * CKEditor 업로드 완료 시 호출
     */
    handleUploadComplete(payload) {
        if (!payload || !payload.filePath || !payload.fileName) {
            return;
        }
        const meta = {
            originalFileName: payload.originalFileName || payload.fileName,
            storedFileName: payload.fileName,
            filePath: payload.filePath,
            fileSize: payload.fileSize || null,
            fileExt: payload.fileExt || this.extractFileExtension(payload.originalFileName || payload.fileName)
        };
        this.upsertEditorFile(meta);
    }

    /**
     * 동일 경로/파일명은 덮어쓰고 나머지는 push
     */
    upsertEditorFile(fileMeta) {
        if (!fileMeta || !fileMeta.filePath || !fileMeta.storedFileName) {
            return;
        }
        const key = `${fileMeta.filePath}__${fileMeta.storedFileName}`;
        const idx = this.state.editorFiles.findIndex(item => `${item.filePath}__${item.storedFileName}` === key);
        if (idx > -1) {
            this.state.editorFiles[idx] = fileMeta;
        } else {
            this.state.editorFiles.push(fileMeta);
        }
    }

    /**
     * 파일 확장자 추출
     */
    extractFileExtension(fileName) {
        if (!fileName) {
            return '';
        }
        const pos = fileName.lastIndexOf('.');
        return pos > -1 ? fileName.substring(pos + 1).toLowerCase() : '';
    }

    /**
     * 에디터 본문에서 img src를 모두 추출해 상대 경로/파일명 키 목록을 만든다
     */
    extractImageKeysFromHtml(html) {
        if (!html) {
            return [];
        }
        const container = document.createElement('div');
        container.innerHTML = html;
        const nodes = Array.from(container.querySelectorAll('img[src]'));
        const uniqueKeys = {};
        const results = [];
        nodes.forEach(img => {
            const parsed = this.normalizeImageSrc(img.getAttribute('src'));
            if (!parsed || uniqueKeys[parsed.key]) {
                return;
            }
            uniqueKeys[parsed.key] = true;
            results.push(parsed);
        });
        return results;
    }

    /**
     * 절대경로/쿼리 등이 섞인 src를 /uploads 기준의 상대 경로 + 파일명으로 정규화
     */
    normalizeImageSrc(src) {
        if (!src || typeof src !== 'string') {
            return null;
        }
        let cleaned = src.trim();
        if (!cleaned || cleaned.indexOf('data:') === 0) {
            return null;
        }
        const qIdx = cleaned.indexOf('?');
        if (qIdx > -1) {
            cleaned = cleaned.substring(0, qIdx);
        }
        const hashIdx = cleaned.indexOf('#');
        if (hashIdx > -1) {
            cleaned = cleaned.substring(0, hashIdx);
        }
        const origin = window.location.origin;
        if (cleaned.startsWith(origin)) {
            cleaned = cleaned.substring(origin.length);
        }
        const uploadsIdx = cleaned.indexOf('/uploads/');
        if (uploadsIdx === -1) {
            return null;
        }
        const relative = cleaned.substring(uploadsIdx + '/uploads/'.length);
        const lastSlash = relative.lastIndexOf('/');
        if (lastSlash === -1) {
            return null;
        }
        const filePath = relative.substring(0, lastSlash);
        const fileName = relative.substring(lastSlash + 1);
        if (!filePath || !fileName) {
            return null;
        }
        return {
            filePath: filePath.replace(/^\/+/, '').replace(/\\/g, '/'),
            storedFileName: fileName,
            key: this.buildEditorFileKey(filePath, fileName)
        };
    }

    /**
     * 에디터 파일 키 생성
     */
    buildEditorFileKey(filePath, storedFileName) {
        const normalizedPath = (filePath || '').replace(/^\/+/, '').replace(/\\/g, '/');
        return `${normalizedPath}__${storedFileName}`;
    }

    /**
     * 수정 화면 진입 시 본문에 남아 있던 CKEditor 첨부(attachNo 포함) 목록을 기록해 둔다
     */
    syncExistingEditorImages(content, attachments) {
        this.state.existingEditorFileKeys = [];
        if (!attachments || !attachments.length || !content) {
            return;
        }
        const attachMap = {};
        attachments.forEach(file => {
            if (!file || !file.filePath || !file.fileName) {
                return;
            }
            const key = this.buildEditorFileKey(file.filePath, file.fileName);
            attachMap[key] = file.fileOid;
        });
        const imageKeys = this.extractImageKeysFromHtml(content);
        imageKeys.forEach(item => {
            const fileOid = attachMap[item.key];
            if (fileOid) {
                this.state.existingEditorFileKeys.push({
                    key: item.key,
                    fileOid: fileOid
                });
            }
        });
    }

    /**
     * 저장 직전에 본문을 재파싱해 제거된 CKEditor 첨부의 ID를 수집한다
     */
    prepareEditorDeletionPayload(currentContent) {
        const currentKeys = this.extractImageKeysFromHtml(currentContent);
        const keySet = currentKeys.reduce((acc, item) => {
            acc[item.key] = true;
            return acc;
        }, {});
        const removed = this.state.existingEditorFileKeys
            .filter(item => item && item.fileOid && !keySet[item.key])
            .map(item => item.fileOid);
        return removed;
    }

    /**
     * 에디터 파일 정보 가져오기
     */
    getEditorFiles() {
        return this.state.editorFiles;
    }

    /**
     * 상태 초기화
     */
    reset() {
        this.state.editorFiles = [];
        this.state.existingEditorFileKeys = [];
    }
}

// DOMContentLoaded는 각 페이지에서 직접 처리