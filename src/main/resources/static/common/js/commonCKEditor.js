var CommonCKEditor = (function() {

    // basePath는 레이아웃에서 ckeditor.js 로드 전에 window.CKEDITOR_BASEPATH로 설정됨

    // 에디터 인스턴스를 저장하는 객체
    var editorInstances = {};
    var chunkUploadEditors = {};
    var chunkDialogListenerAttached = true;
    var activeChunkUploadTasks = {};

    var defaultChunkOptions = {
        enabled: false,
        thresholdBytes: 10 * 1024 * 1024,
        chunkSize: 5 * 1024 * 1024,
        chunkUrl: '',
        identifierPrefix: 'ck-image',
        extraFormData: null,
        headers: null,
        listenerPriority: 10,
        onUploadComplete: null
    };

    /**
     * 기본 설정(defaultConfig)
     * 나중에 initialize 호출 시 options.config에 병합되어 적용됩니다.
     */
    var defaultConfig = {
        versionCheck: false,           // 보안 알림 비활성화

        language: 'ko',            // 인터페이스 언어 (ko, en 등)
        height: 350,               // 에디터 높이(px)
        width: '100%',             // 에디터 너비(%, px)

        // 에디터 컨텐츠 영역에 font.css 로드
        contentsCss: [
            '/common/css/font.css'
        ],

        toolbar: [
            { name: 'document',   items: [
                'Source',
                // '-',
                // 'NewPage', // 작동안함
                // 'Preview', // 작동안함
                // 'Print',  // 작동안함
                '-',
                'Templates'
            ] },
            { name: 'clipboard',  items: [
                'Cut',
                'Copy',
                // 'Paste', // 작동안함
                // 'PasteText', // 작동안함
                // 'PasteFromWord', // 작동안함
                '-',
                'Undo',
                'Redo'
            ] },
            { name: 'editing',    items: ['Find','Replace','-','SelectAll'] },
            '/', // 줄바꿈
            { name: 'basicstyles',items: ['Bold','Italic','Underline','Strike','Subscript','Superscript','-','RemoveFormat'] },
            { name: 'paragraph',  items: ['NumberedList','BulletedList','-','Outdent','Indent','-','Blockquote','CreateDiv','-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'] },
            { name: 'links',      
                items: [
                'Link'
                ,'Unlink'
                ,'Anchor'
            ] },
            '/',
            { name: 'styles',     items: ['Styles','Format','Font','FontSize'] },
            { name: 'basicstyles',items: ['Bold','Italic','Underline','Strike'] },
            { name: 'colors',     items: ['TextColor','BGColor'] },
            { name: 'tools',      items: ['Maximize','ShowBlocks'] },
            { name: 'insert',     items: [
                'Image'
                ,'Table'
                ,'HorizontalRule'
                ,'SpecialChar'
            ] }
        ],

        // 업로드 엔드포인트
        filebrowserUploadUrl: '',           // 링크/파일 업로드 API
        filebrowserImageUploadUrl: '/api/common/file/tmp/ck-image?responseType=json', // 이미지 전용 업로드
        extraPlugins: 'image2,uploadimage,wordcount',  // image2: 드래그 리사이즈, uploadimage: 붙여넣기/드래그 업로드, wordcount: 글자수 제한
        removePlugins: 'image,elementspath,scayt,wsc,cloudservices,exportpdf',

        resize_enabled: true,      // 리사이즈 사용 여부
        resize_minWidth: 100,      // 최소 너비
        resize_minHeight: 100,     // 최소 높이

        enterMode: 1,              // Enter 키: <p>
        shiftEnterMode: 2,         // Shift+Enter 키: <br>
        allowedContent: true,       // 모든 콘텐츠 허용 (필터 해제)
        pasteFilter: false,         // 붙여넣기 시 콘텐츠 필터 해제
        clipboard_handleImages: false, // uploadimage와 충돌하여 해제

        // 폰트 목록 설정
        font_names: '맑은 고딕/Malgun Gothic, Gothic, sans-serif;' +
            '돋움/Dotum, sans-serif;' +
            '바탕/Batang, serif;' +
            '굴림/Gulim, sans-serif;' +
            '궁서/Gungsuh, serif;' +
            '나눔고딕/NanumGothic, sans-serif;' +
            '나눔명조/NanumMyeongjo, serif;' +
            '나눔스퀘어/NanumSquare, sans-serif;' +
            '나눔스퀘어라운드/NanumSquareRound, sans-serif;' +
            '나눔바른고딕/NanumBarunGothic, sans-serif;' +
            '나눔손글씨 펜/NanumPen, sans-serif;' +
            '나눔손글씨 붓/NanumBrush, sans-serif;' +
            'Arial/Arial, Helvetica, sans-serif;' +
            'Courier New/Courier New, Courier, monospace;' +
            'Georgia/Georgia, serif;' +
            'Lucida Sans Unicode/Lucida Sans Unicode, Lucida Grande, sans-serif;' +
            'Tahoma/Tahoma, Geneva, sans-serif;' +
            'Times New Roman/Times New Roman, Times, serif;' +
            'Trebuchet MS/Trebuchet MS, Helvetica, sans-serif;' +
            'Verdana/Verdana, Geneva, sans-serif',

        // 글자수 제한 설정
        wordcount: {
            showCharCount: true,
            showWordCount: false,
            maxCharCount: 5000,
            countSpacesAsChars: true,
            countHTML: false
        }
    };

    /**
     * 에디터 초기화
     * @param {Object} options
     * @param {string} options.editorId  textarea ID
     * @param {string} [options.content] 초기 HTML
     * @param {Object} [options.config]  추가/오버라이드 설정
     * @param {string} [options.uploadUrl] 업로드 API URL
     * @param {function} [options.onReady]  준비 완료 콜백
     * @param {function} [options.onChange] 변경 이벤트 콜백
     * @param {boolean} [options.showLoading=true] 로딩 표시 여부
     * @returns {Promise} CKEditor 인스턴스 반환
     */
    function initialize(options) {
        return new Promise(function(resolve, reject) {
            if (!options || !options.editorId) {
                return reject(new Error('editorId는 필수입니다.'));
            }
            var id     = options.editorId;
            var content= options.content || '';
            var cfg    = options.config || {};
            var url    = options.uploadUrl;
            var loader = options.showLoading !== false;
            var chunkOptions = normalizeChunkOptions(options.chunkUpload);


            if (loader) showLoadingSpinner(id);
            try {
                if (typeof CKEDITOR === 'undefined') {
                    throw new Error('CKEditor가 로드되지 않았습니다.');
                }
                if (editorInstances[id]) destroy(id);

                var config = $.extend(true, {}, defaultConfig, cfg);
                if (url) {
                    config.filebrowserUploadUrl      = url;
                    config.filebrowserImageUploadUrl = url;
                }
                config.on = $.extend({}, config.on, {
                    instanceReady: function(ev) {
                        if (loader) hideLoadingSpinner(id);
                        if (content) ev.editor.setData(content);
                        editorInstances[id] = ev.editor;
                        if (chunkOptions.enabled) {
                            setupChunkUpload(ev.editor, chunkOptions);
                        }
                        setupTableCopyPaste(ev.editor);
                        removeContextMenuPaste(ev.editor);
                        if (typeof options.onReady === 'function') options.onReady(ev.editor);
                        resolve(ev.editor);
                    },
                    change: function(ev) {
                        if (typeof options.onChange === 'function') options.onChange(ev.editor.getData(), ev.editor);
                    }
                });
                CKEDITOR.replace(id, config);
            } catch (e) {
                console.error(e);
                if (loader) hideLoadingSpinner(id);
                fallback(id, content);
                reject(e);
            }
        });
    }

    // 컨텐츠 읽기
    function getContent(id) {
        try {
            var inst = editorInstances[id] || CKEDITOR.instances[id];
            return inst ? inst.getData() : $('#' + id).val();
        } catch (e) {
            console.error(e);
            return $('#' + id).val();
        }
    }
    // 컨텐츠 쓰기
    function setContent(id, data) {
        try {
            var inst = editorInstances[id] || CKEDITOR.instances[id];
            if (inst) inst.setData(data||''); else $('#' + id).val(data||'');
        } catch (e) { console.error(e); $('#' + id).val(data||''); }
    }
    // 포커스
    function focus(id) {
        try { (editorInstances[id]||CKEDITOR.instances[id]).focus(); } catch(e){ $('#' + id).focus(); }
    }
    // 순수 텍스트
    function getPlainText(id) {
        var html = getContent(id);
        var div = document.createElement('div'); div.innerHTML = html;
        return div.textContent||div.innerText||'';
    }
    // 검증
    function validate(id, opt) {
        opt = opt||{};
        var text = getPlainText(id).trim();
        // if (opt.required && !text) return {valid:false,message:'내용을 입력하세요.'};
        if (opt.minLength && text.length<opt.minLength) return {valid:false,message:'최소 '+opt.minLength+'자 필요'};
        if (opt.maxLength && text.length>opt.maxLength) return {valid:false,message:'최대 '+opt.maxLength+'자까지'};
        if (opt.maxHtmlLength) {
            var html = getContent(id);
            if (html.length > opt.maxHtmlLength)
                return {valid:false, message:'내용이 너무 깁니다. (최대 ' + opt.maxHtmlLength.toLocaleString() + '자)'};
        }
        return {valid:true,message:''};
    }
    // 인스턴스 제거
    function destroy(id) {
        try {
            if (editorInstances[id]) { editorInstances[id].destroy(); delete editorInstances[id]; }
            else if (CKEDITOR.instances[id]) CKEDITOR.instances[id].destroy();
        } catch(e){ console.error(e); }
    }
    // 전체 제거
    function destroyAll() {
        for (var id in editorInstances) destroy(id);
        for (var nid in CKEDITOR.instances) CKEDITOR.instances[nid].destroy();
    }
    // 로딩
    function showLoadingSpinner(id) {
        var ta = $('#' + id);
        ta.after('<div id="'+id+'_load" class="ckeditor-loading text-center py-3"><div class="spinner-border" role="status"></div><p>로딩 중...</p></div>');
        ta.hide();
    }
    function hideLoadingSpinner(id) { $('#' + id + '_load').remove();
    // $('#' + id).show();
    }
    // 대체 textarea
    function fallback(id, content) {
        hideLoadingSpinner(id);
        // var ta = $('#' + id);
        // ta.show().val(content||'');
        uiCommon.fnShowAlertModal('CKEditor 로드 실패, 텍스트 모드로 전환');
    }

    function normalizeChunkOptions(option) {
        if (!option || !option.enabled) {
            return { enabled: false };
        }
        var merged = $.extend(true, {}, defaultChunkOptions, option);
        merged.thresholdBytes = merged.thresholdBytes || defaultChunkOptions.thresholdBytes;
        merged.chunkSize = merged.chunkSize || defaultChunkOptions.chunkSize;
        if (!merged.chunkUrl) {
            merged.enabled = false;
        }
        return merged;
    }

    function triggerChunkUploadCallback(options, payload, editor, file) {
        if (!options || typeof options.onUploadComplete !== 'function') {
            return;
        }
        try {
            options.onUploadComplete(payload, {
                editor: editor,
                file: file
            });
        } catch (err) {
            console.error('CKEditor chunk upload callback 오류', err);
        }
    }

    /**
     * fileUploadResponse 이벤트에서 공통 콜백에 맞는 정보를 추출한다.
     */
    function handleFileUploadResponse(evt, options, editor) {
        if (!options || typeof options.onUploadComplete !== 'function' || !evt) {
            return;
        }
        var payload = extractUploadPayload(evt);
        if (!payload) {
            return;
        }
        var fileLoader = evt.data && evt.data.fileLoader;
        var file = fileLoader && (fileLoader.file || fileLoader.uploaded || null);
        triggerChunkUploadCallback(options, payload, editor, file || null);
    }

    /**
     * CKEditor 기본 응답/커스텀 응답의 다양한 포맷을 단일 JSON으로 변환한다.
     */
    function extractUploadPayload(evt) {
        var raw = evt.data || {};
        if (raw.data && typeof raw.data === 'object') {
            return raw.data;
        }
        if (raw.responseData && typeof raw.responseData === 'object') {
            return raw.responseData;
        }
        if (raw.fileLoader && raw.fileLoader.responseData && typeof raw.fileLoader.responseData === 'object') {
            return raw.fileLoader.responseData;
        }
        var responseText = raw.fileLoader && raw.fileLoader.xhr && raw.fileLoader.xhr.responseText;
        if (typeof responseText === 'string' && responseText.trim().length) {
            try {
                return JSON.parse(responseText);
            } catch (err) {
                console.error('CKEditor 업로드 응답 파싱 실패', err);
            }
        }
        return null;
    }

    function setupChunkUpload(editor, options) {
        if (!editor || !options || !options.enabled || !options.chunkUrl) {
            return;
        }
        var editorKey = editor.name || editor.id;
        chunkUploadEditors[editorKey] = options;
        registerChunkDialogInterceptor();

        editor.on('fileUploadRequest', function(evt) {
            var loader = evt.data && evt.data.fileLoader;
            var file = loader && (loader.file || (loader.fileLoader && loader.fileLoader.file));
            if (!loader || !file || file.size <= options.thresholdBytes) {
                // console.log('용량 작아서 청크 업로드 안 하는 경우')
                return;
            }
            // console.log('용량 커서 청크 업로드 하는 경우');
            evt.stop();
            evt.cancel && evt.cancel();
            var abortToken = { cancelled: false };
            var previousAbort = loader.abort ? loader.abort.bind(loader) : null;
            loader.abort = function() {
                abortToken.cancelled = true;
                if (typeof abortToken.cancel === 'function') {
                    abortToken.cancel();
                }
            };
            loader.changeStatus && loader.changeStatus('uploading');
            uploadFileInChunks(file, options, function(percent) {
                loader.uploadTotal = file.size;
                loader.uploaded = Math.round(file.size * percent / 100);
                loader.update && loader.update();
                editor.fire('uploadProgress', { fileLoader: loader });
            }, abortToken).then(function(response) {
                var payload = normalizeChunkResponse(response, options);
                if (!payload.url) {
                    throw new Error('업로드 결과의 URL을 확인할 수 없습니다.');
                }
                loader.xhr = {
                    responseText: JSON.stringify(payload),
                    status: 200
                };
                var eventPayload = $.extend({
                    fileLoader: loader
                }, payload);
                // CKEditor 기본 업로드 로직이 최상위 필드(url 등)를 기대하므로 payload를 펼쳐 전달
                eventPayload.data = $.extend(true, {}, payload);
                var fireResult = editor.fire('fileUploadResponse', eventPayload);
                ['message', 'fileName', 'url'].forEach(function(key) {
                    if (typeof eventPayload[key] === 'string') {
                        loader[key] = eventPayload[key];
                    }
                });
                if (fireResult === false) {
                    loader.changeStatus && loader.changeStatus('error');
                } else {
                    loader.uploadTotal = file.size;
                    loader.uploaded = file.size;
                    loader.update && loader.update();
                    loader.responseData = $.extend(true, {}, eventPayload.data || {});
                    loader.changeStatus && loader.changeStatus('uploaded');
                }
            }).catch(function(error) {
                console.error(error);
                loader.message = (error && error.message) || '이미지 업로드 중 오류가 발생했습니다.';
                loader.changeStatus && loader.changeStatus('error');
            }).then(function() {
                if (previousAbort) {
                    loader.abort = previousAbort;
                } else {
                    delete loader.abort;
                }
            });
        }, null, null, options.listenerPriority || defaultChunkOptions.listenerPriority);

        editor.on('destroy', function() {
            delete chunkUploadEditors[editorKey];
        });

        editor.on('fileUploadResponse', function(evt) {
            handleFileUploadResponse(evt, options, editor);
        }, null, null, options.listenerPriority || defaultChunkOptions.listenerPriority);
    }

    function registerChunkDialogInterceptor() {
        if (chunkDialogListenerAttached || typeof CKEDITOR === 'undefined') {
            return;
        }
        CKEDITOR.on('dialogDefinition', function(evt) {
            var dialogName = evt.data && evt.data.name;
            if (dialogName !== 'image' && dialogName !== 'image2') {
                return;
            }
            var definition = evt.data.definition;
            if (!definition || typeof definition.getContents !== 'function') {
                return;
            }
            var uploadTab = definition.getContents('Upload');
            if (!uploadTab || typeof uploadTab.get !== 'function') {
                return;
            }
            var uploadButton = uploadTab.get('uploadButton');
            if (!uploadButton || uploadButton._chunkOverrideApplied) {
                return;
            }
            var originalClick = uploadButton.onClick;
            uploadButton._chunkOverrideApplied = true;
            uploadButton.onClick = function(btnEvt) {
                var dialog = btnEvt.sender && btnEvt.sender.getDialog ? btnEvt.sender.getDialog()
                    : (this.getDialog ? this.getDialog() : null);
                var editor = dialog && dialog.getParentEditor ? dialog.getParentEditor() : evt.editor;
                var editorKey = editor && (editor.name || editor.id);
                var runtimeOptions = editorKey ? chunkUploadEditors[editorKey] : null;
                if (!runtimeOptions || !runtimeOptions.enabled) {
                    if (typeof originalClick === 'function') {
                        originalClick.call(this, btnEvt);
                    }
                    return;
                }
                var uploadField = dialog && dialog.getContentElement ? dialog.getContentElement('Upload', 'upload') : null;
                var nativeInput = uploadField && uploadField.getInputElement ? uploadField.getInputElement().$ : null;
                var file = nativeInput && nativeInput.files && nativeInput.files[0];
                if (!file || file.size <= runtimeOptions.thresholdBytes) {
                    if (typeof originalClick === 'function') {
                        originalClick.call(this, btnEvt);
                    }
                    return;
                }
                if (btnEvt && btnEvt.data && typeof btnEvt.data.preventDefault === 'function') {
                    btnEvt.data.preventDefault();
                }
                btnEvt.cancel && btnEvt.cancel();
                var button = this;
                button.disable && button.disable();
                uploadFileInChunks(file, runtimeOptions, null, null).then(function(response) {
                    var payload = normalizeChunkResponse(response, runtimeOptions);
                    if (!payload.url) {
                        throw new Error('업로드 결과의 URL을 확인할 수 없습니다.');
                    }
                    var urlField = dialog.getContentElement('info', 'src') || dialog.getContentElement('info', 'txtUrl');
                    if (urlField && typeof urlField.setValue === 'function') {
                        urlField.setValue(payload.url);
                        dialog.selectPage && dialog.selectPage('info');
                    }
                }).catch(function(error) {
                    console.error(error);
                    var message = (error && error.message) || '이미지 업로드 중 오류가 발생했습니다.';
                    if (window.uiCommon && typeof uiCommon.fnShowAlertModal === 'function') {
                        uiCommon.fnShowAlertModal(message);
                    } else {
                        uiCommon.fnShowAlertModal(message);
                    }
                }).then(function() {
                    button.enable && button.enable();
                    if (nativeInput) {
                        nativeInput.value = '';
                    }
                });
            };
        });
    }

    function uploadFileInChunks(file, options, progressCallback, abortToken) {
        var chunkSize = options.chunkSize || defaultChunkOptions.chunkSize;
        var totalChunks = Math.max(Math.ceil(file.size / chunkSize), 1);
        var identifier = buildChunkIdentifier(file, options);
        var extraFields = typeof options.extraFormData === 'function' ? options.extraFormData(file)
            : (options.extraFormData || {});
        var currentController = null;
        var aborted = false;

        if (abortToken) {
            aborted = !!abortToken.cancelled;
            abortToken.cancelled = aborted;
            var cancel = function() {
                aborted = true;
                abortToken.cancelled = true;
                if (currentController && typeof currentController.abort === 'function') {
                    currentController.abort();
                }
            };
            abortToken.cancel = cancel;
        }

        return new Promise(function(resolve, reject) {
            function sendChunk(index) {
                if (aborted) {
                    return reject(new Error('업로드가 취소되었습니다.'));
                }
                var start = (index - 1) * chunkSize;
                var chunk = file.slice(start, Math.min(file.size, start + chunkSize));
                var formData = new FormData();
                formData.append('file', chunk, file.name);
                formData.append('resumableChunkNumber', index);
                formData.append('resumableTotalChunks', totalChunks);
                formData.append('resumableIdentifier', identifier);
                formData.append('resumableFilename', file.name);
                formData.append('fileSize', file.size);
                if (extraFields && typeof extraFields === 'object') {
                    Object.keys(extraFields).forEach(function(key) {
                        var value = extraFields[key];
                        if (value !== undefined && value !== null) {
                            formData.append(key, value);
                        }
                    });
                }
                currentController = (typeof AbortController !== 'undefined') ? new AbortController() : null;
                fetch(options.chunkUrl, {
                    method: 'POST',
                    body: formData,
                    headers: options.headers || {},
                    credentials: 'same-origin',
                    signal: currentController ? currentController.signal : undefined
                }).then(function(response) {
                    if (!response.ok) {
                        return response.text().then(function(text) {
                            var message = text || '청크 업로드 중 오류가 발생했습니다.';
                            throw new Error(message);
                        });
                    }
                    if (index === totalChunks) {
                        return response.text().then(function(body) {
                            if (!body) {
                                return {};
                            }
                            try {
                                return JSON.parse(body);
                            } catch (err) {
                                throw new Error('서버 응답을 파싱하지 못했습니다.');
                            }
                        });
                    }
                    if (typeof progressCallback === 'function') {
                        var uploaded = Math.min(file.size, index * chunkSize);
                        progressCallback((uploaded / file.size) * 100);
                    }
                    return null;
                }).then(function(data) {
                    if (aborted) {
                        return reject(new Error('업로드가 취소되었습니다.'));
                    }
                    if (index === totalChunks) {
                        if (typeof progressCallback === 'function') {
                            progressCallback(100);
                        }
                        resolve(data || {});
                    } else {
                        sendChunk(index + 1);
                    }
                }).catch(function(error) {
                    if (aborted || (abortToken && abortToken.cancelled)) {
                        return reject(new Error('업로드가 취소되었습니다.'));
                    }
                    reject(error);
                });
            }
            sendChunk(1);
        });
    }

    function buildChunkIdentifier(file, options) {
        var prefix = options.identifierPrefix || defaultChunkOptions.identifierPrefix;
        return [
            prefix,
            file.name,
            file.size,
            file.lastModified,
            Date.now(),
            Math.random().toString(36).slice(2)
        ].join('-');
    }

    function normalizeChunkResponse(response, options) {
        var payload = response || {};
        if (typeof options.transformResponse === 'function') {
            payload = options.transformResponse(payload) || payload;
        }
        if (!payload.url && typeof options.buildUrl === 'function') {
            payload.url = options.buildUrl(payload);
        }
        if (!payload.uploaded) {
            payload.uploaded = payload.url ? 1 : 0;
        }
        return payload;
    }
    
     /**
	     * CKEditor 통합 저장 함수 (공통 fetch + XHR 자동판별형)
	     * @param {Object} options
	     * @param {string} options.url - 서버 저장 URL
	     * @param {string} options.editorId - CKEditor textarea ID
	     * @param {Object} [options.extraData] - title, publicYn 등 추가 데이터
	     * @param {FileList|File[]} [options.files] - 첨부 파일
	     * @param {function} [options.onProgress] - 업로드 진행률 콜백(percent)
	     * @param {function} [options.onSuccess] - 성공 콜백
	     * @param {function} [options.onError] - 실패 콜백
	     * @returns {Object} - { abort() } 업로드 취소용
     */
    function saveWithFiles(options){
		if(!options || !options.url || !options.editorId){
			console.log('options는 필수입니다.');
			return;
		}

		// CKEditor 컨텐츠 검증
		var maxHtmlLength = options.maxHtmlLength || 500000;
		var validation = validate(options.editorId, { required: true, maxHtmlLength: maxHtmlLength });
		if (!validation.valid) {
			uiCommon.fnShowAlertModal(validation.message);
			return;
		}

		// FormData 생성
		const formData = new FormData();
		
		// JSON 데이터 추가
		if(options.data){
			const jsonPayload = typeof options.data === 'string' ? options.data : JSON.stringify(options.data);
			if(options.sendDataAsJsonBlob){
				formData.append('data', new Blob([jsonPayload], { type: 'application/json' }));
			}else{
				formData.append('data', jsonPayload);
			}
		}
		
		if(options.files){
			const fileList = options.files.length !== undefined ? options.files : [options.files];
			
			for(const fileData of fileList){
				if(fileData && fileData.size > 0){
					// 파일 유효성 검사
					const fileValidation = validateFile(fileData);
					if(fileValidation.valid){
						formData.append('files', fileData);
					}else{
						console.log('파일 오류: ' + fileData.name + ' ' + fileValidation.message);
						return;
					}
				}
			}
		}
		
		// 저장 api (진행률 표기는 XMLHttpRequest만 가능해서 $.ajax 사용)
		$.ajax({
			url : options.url,
			method : options.method || 'POST',
			data : formData,
			processData : false,
			contentType : false,
			//진행률 처리
			xhr : function(){
				const xhr = new window.XMLHttpRequest();
				
				xhr.upload.addEventListener("progress", function(event){
					if(event.lengthComputable){
						let percentComplete = Math.round((event.loaded / event.total) * 100);
						
						console.log("업로드 진행률:", percentComplete + "%");
						
						if(typeof options.onProgress === 'function'){
							options.onProgress(percentComplete);
						}
					}
				}, false);
				
				return xhr;
			},
			beforeSend: function(xhr){
				if(typeof authorization !== "undefined" && authorization){
					 xhr.setRequestHeader(authorization_str, authorization);
				}
			},
			success: function(response){
				// 응답 코드 처리
				switch(String(response.code)){
					case "401" : 
						console.log('로그인이 필요합니다.');
						break;
		            case "200":
		              if (typeof options.onSuccess === 'function') {
		                options.onSuccess(response);
		              }
		              break;
/*		            case "999":
		              if (typeof options.onSuccess === 'function') {
		                // options.success(res);
		              }
		              break;*/
		            default:
		              if (typeof options.onError === 'function') {
		                options.onError(response);
		                console.log('처리 중 오류가 발생했습니다.\n' + (response.message || ''));
		              } else {
		                console.log('처리 중 오류가 발생했습니다.\n' + (response.message || ''));
		              }
				}
			},
			error: function(xhr, status, error){
                options.onError(xhr);
				console.error('에러:', xhr.status, status, error);
			}
			
		})
		
	}
	
	/**
	 * 파일 확장자 추출 함수
	 * @param {string} fileName - 파일 이름
	 * @returns {string} - 확장자 (소문자)
	 */
	function getFileExtension(fileName) {
	  const parts = fileName.split('.');
	  return parts.length > 1 ? parts.pop().toLowerCase() : '';
	}
	
	/**
	 * 파일 크기를 보기 좋게 변환 (B → KB/MB/GB)
	 * @param {number} bytes - 파일 크기 (바이트)
	 * @returns {string} - 예: '12.3 MB'
	 */
	function formatFileSize(bytes) {
	  if (bytes === 0) return '0 Bytes';
	  const k = 1024;
	  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
	  const i = Math.floor(Math.log(bytes) / Math.log(k));
	  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
	}
	
	/**
	 * 파일 유효성 검증 함수
	 * @param {File} file - 검증할 파일 객체
	 * @returns {{ valid: boolean, message: string }}
	 */
	function validateFile(file) {
	  const result = { valid: true, message: '' };
	
	  if (!file) {
	    return { valid: false, message: '파일이 선택되지 않았습니다.' };
	  }
	
	  // 파일 크기 제한 (500MB)
	  const maxSize = 500 * 1024 * 1024;
	  if (file.size > maxSize) {
	    return {
	      valid: false,
	      message: `파일 크기는 500MB를 초과할 수 없습니다. (현재: ${formatFileSize(file.size)})`
	    };
	  }
	
	  // 빈 파일 체크
	  if (file.size === 0) {
	    return { valid: false, message: '빈 파일은 업로드할 수 없습니다.' };
	  }
	
	  // 확장자 체크
	  const allowedExtensions = [
	    'jpg', 'jpeg', 'png', 'gif', 'bmp',
	    'pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt',
	    'hwp', 'hwpx', 'rtf', 'zip', 'rar', '7z'
	  ];
	
	  const fileExtension = getFileExtension(file.name);
	  if (!allowedExtensions.includes(fileExtension)) {
	    return {
	      valid: false,
	      message: `허용되지 않는 파일 형식입니다. (${fileExtension})\n허용 형식: ${allowedExtensions.join(', ')}`
	    };
	  }
	
	  return result;
	}	
    


    /**
     * 테이블 복사/붙여넣기
     *
     * CKEditor 4의 clipboard + tableselection 플러그인 간 충돌로
     * 시스템 클립보드에 테이블 HTML이 제대로 전달되지 않는 문제를 우회한다.
     *
     * Ctrl+C 시 테이블 HTML을 시스템 클립보드에 직접 기록하여
     * 다른 창의 에디터에서도 border 등 속성이 유지되도록 한다.
     */
    function setupTableCopyPaste(editor) {
        // 복사 측: iframe document에 capture로 등록하여 CKEditor clipboard 플러그인보다 먼저 실행
        var doc = editor.document && editor.document.$;
        if (!doc) return;

        doc.addEventListener('copy', function(e) {
            try {
                var html = editor.getSelectedHtml(true);
                var tableHtml = null;

                if (html && hasTableContent(html)) {
                    tableHtml = wrapTableIfNeeded(html, editor);
                } else {
                    tableHtml = getParentTableHtml(editor);
                }

                if (tableHtml) {
                    e.clipboardData.setData('text/html', tableHtml);
                    e.clipboardData.setData('text/plain', getPlainTextFromHtml(tableHtml));
                    e.preventDefault();
                    e.stopImmediatePropagation(); // CKEditor clipboard 플러그인의 복사 처리 차단
                }
            } catch (err) {
                console.error('테이블 복사 처리 중 오류', err);
            }
        }, true); // capture phase

        // 붙여넣기 측: 테이블 HTML을 필터링 전에 보존하고, 필터링 후 복원
        editor.on('paste', function(evt) {
            if (evt.data && evt.data.dataValue && hasTableContent(evt.data.dataValue)) {
                evt.data._preservedTableHtml = evt.data.dataValue;
            }
        }, null, null, 1); // 필터링 전 (priority 1)

        editor.on('paste', function(evt) {
            if (evt.data && evt.data._preservedTableHtml) {
                evt.data.dataValue = evt.data._preservedTableHtml;
                delete evt.data._preservedTableHtml;
            }
        }, null, null, 15); // 필터링 후 (priority 15, 기본 처리는 10)
    }

    function getPlainTextFromHtml(html) {
        var div = document.createElement('div');
        div.innerHTML = html;
        return div.textContent || div.innerText || '';
    }

    /**
     * 선택 영역의 부모 테이블 전체 HTML을 반환한다.
     * 단일 셀 테이블 등에서 텍스트만 선택된 경우에도 테이블을 포함하여 복사할 수 있도록 한다.
     */
    function getParentTableHtml(editor) {
        try {
            var sel = editor.getSelection();
            var ranges = sel && sel.getRanges();
            if (!ranges || !ranges.length) return null;

            var tableEl = ranges[0].startContainer.getAscendant('table', true);
            if (!tableEl) return null;

            return tableEl.getOuterHtml();
        } catch (e) {
            return null;
        }
    }

    function hasTableContent(html) {
        return html.indexOf('<table') !== -1
            || html.indexOf('<td') !== -1
            || html.indexOf('<th') !== -1
            || html.indexOf('<tr') !== -1;
    }

    function wrapTableIfNeeded(html, editor) {
        if (html.indexOf('<table') !== -1) return html;
        if (html.indexOf('<td') === -1 && html.indexOf('<th') === -1) return html;

        try {
            var sel = editor.getSelection();
            var ranges = sel && sel.getRanges();
            var tableEl = null;
            if (ranges) {
                for (var i = 0; i < ranges.length; i++) {
                    tableEl = ranges[i].startContainer.getAscendant('table', true);
                    if (tableEl) break;
                }
            }

            var tableAttrs = '';
            if (tableEl && tableEl.$) {
                var attrs = tableEl.$.attributes;
                for (var j = 0; j < attrs.length; j++) {
                    if (attrs[j].name !== 'data-cke-expando') {
                        tableAttrs += ' ' + attrs[j].name + '="' + attrs[j].value + '"';
                    }
                }
            }

            if (html.indexOf('<tr') === -1) {
                html = '<tr>' + html + '</tr>';
            }
            return '<table' + tableAttrs + '><tbody>' + html + '</tbody></table>';
        } catch (e) {
            return '<table><tbody><tr>' + html + '</tr></tbody></table>';
        }
    }

    /**
     * 우클릭 메뉴에서 붙여넣기 제거 (브라우저 보안상 작동 안 되는 기능)
     */
    function removeContextMenuPaste(editor) {
        editor.on('instanceReady', function() {
            // CKEditor 내부 메뉴 아이템에서 paste 관련 제거
            if (editor._.menuItems) {
                delete editor._.menuItems.paste;
            }
        });
        // 이미 ready 상태일 수 있으므로 즉시도 실행
        if (editor._.menuItems) {
            delete editor._.menuItems.paste;
        }
    }

    /** 자주 쓰이는 기능 */
    var commonFeatures = [
        'Bold, Italic, Underline, Strike',
        'NumberedList, BulletedList',
        'Link, Unlink, Anchor',
        'Image insert & drag-drop',
        'Table create/merge/split',
        'Source view',
        'CodeSnippet plugin',
        'SpecialChar',
        'Styles & Format',
        'Font settings & Colors'
    ];

    // 공개 API
    return {
        init: initialize,
        quickInit: function(id, data, cfg){ return initialize({editorId:id,content:data,config:cfg}); },
        initMultiple: function(arr){ return Promise.all(arr.map(initialize)); },
        getContent: getContent,
        setContent: setContent,
        getPlainText: getPlainText,
        focus: focus,
        validate: validate,
        destroy: destroy,
        destroyAll: destroyAll,
        defaultConfig: defaultConfig,
        commonFeatures: commonFeatures,
        saveWithFiles: saveWithFiles
    };
})();

// 전역 등록
window.CommonCKEditor = CommonCKEditor;
