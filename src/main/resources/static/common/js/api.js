/**
 * API 공통 Fetch 유틸리티
 *
 * 기능:
 * - ApiResponse 자동 파싱 (정상 응답)
 * - ErrorResponse 자동 파싱 (비즈니스 에러)
 * - Spring Security 에러 처리 (401, 403)
 * - 자동 로그인 리다이렉트
 * - 유효성 검증 에러 처리
 *
 * @author tspark
 * @since 2025.11.05
 */

const API = {
    /**
     * 기본 설정
     */
    config: {
        baseURL:  window.contextPath || '',
        timeout: 30000,
        handleAuthError: true, // 401/403 자동 처리 여부
        showValidationAlert: true, // 유효성 검증 에러 알림 표시 여부
    },

    /**
     * API 요청 속도 제한 상태
     */
    rateLimit: {
        history: new Map(), // 요청 기록 (key: 요청 고유값, value: [타임스탬프 배열])
        limit: 5,           // 5회
        interval: 5000,     // 5초 (ms)
        isBlocked: false,   // 현재 차단 상태 여부
        blockDuration: 5000 // 차단 시간 (ms)
    },

    /**
     * 알림 모달 표시 (uiCommon 있으면 사용, 없으면 alert 사용)
     * @param {string} message - 표시할 메시지
     * @param {function} callback - 확인 클릭 후 실행할 콜백 함수
     * @param {number} duration - 자동으로 닫힐 시간 (ms). 0이면 자동 닫힘 없음.
     */
    showAlert(message, callback = null, duration = 0) {
        if (typeof uiCommon !== 'undefined' && typeof uiCommon.fnShowAlertModal === 'function') {
            // uiCommon에 duration 파라미터 추가 필요
            uiCommon.fnShowAlertModal(message, callback, duration);
        } else {
            uiCommon.fnShowAlertModal(message);
            if (typeof callback === 'function') {
                callback();
            }
        }
    },

    /**
     * 요청 고유 키 생성
     * @param {string} url 
     * @param {object} options 
     * @returns {string}
     */
    _createRequestKey(url, options) {
        const body = options.body || '';
        return `${options.method || 'GET'}@${url}@${body}`;
    },

    /**
     * GET 요청
     * @param {string} url - API 엔드포인트
     * @param {object} params - 쿼리스트링으로 변환할 데이터 객체
     * @param {object} options - 추가 fetch 옵션
     * @returns {Promise} 응답 데이터
     */
    async get(url, params = {}, options = {showLoading: false}) {
        let finalUrl = url;
        const filteredParams = Object.fromEntries(Object.entries(params).filter(([_, v]) => v !== null && v !== undefined)); // null & undefined 제외
        const queryString = new URLSearchParams(filteredParams).toString();

        if (queryString) {
            finalUrl += (url.includes('?') ? '&' : '?') + queryString;
        }

        return this.request(finalUrl, {
            method: 'GET',
            ...options
        });
    },

    /**
     * POST 요청
     * @param {string} url - API 엔드포인트
     * @param {object} data - 요청 데이터
     * @param {object} options - 추가 옵션
     * @returns {Promise} 응답 데이터
     */
    async post(url, data, options = {showLoading: false}) {
        return this.request(url, {
            method: 'POST',
            body: JSON.stringify(data),
            ...options
        });
    },

    /**
     * PUT 요청
     * @param {string} url - API 엔드포인트
     * @param {object} data - 요청 데이터
     * @param {object} options - 추가 옵션
     * @returns {Promise} 응답 데이터
     */
    async put(url, data, options = {}) {
        return this.request(url, {
            method: 'PUT',
            body: JSON.stringify(data),
            ...options
        });
    },

    /**
     * DELETE 요청
     * @param {string} url - API 엔드포인트
     * @param {object} options - 추가 옵션
     * @returns {Promise} 응답 데이터
     */
    async delete(url, options = {}) {
        return this.request(url, {
            method: 'DELETE',
            ...options
        });
    },

    /**
     * PATCH 요청
     * @param {string} url - API 엔드포인트
     * @param {object} data - 요청 데이터
     * @param {object} options - 추가 옵션
     * @returns {Promise} 응답 데이터
     */
    async patch(url, data, options = {}) {
        return this.request(url, {
            method: 'PATCH',
            body: JSON.stringify(data),
            ...options
        });
    },

    /**
     * 파일 업로드 (multipart/form-data)
     * @param {string} url - API 엔드포인트
     * @param {FormData} formData - FormData 객체
     * @param {object} options - 추가 옵션
     * @returns {Promise} 응답 데이터
     */
    async upload(url, formData, options = {showLoading : true}) {
        // FormData는 Content-Type 자동 설정되므로 제거
        const headers = { ...options.headers };
        delete headers['Content-Type'];
    
        return this.request(url, {
            method: options.method || 'POST', 
            body: formData,
            ...options,
            headers
        });
    },

    /**
     * 공통 요청 처리
     * @param {string} url - API 엔드포인트
     * @param {object} options - fetch 옵션
     * @returns {Promise} 응답 데이터
     */
    async request(url, options = {
        showLoading: false
    }) {
        // // --- 속도 제한 로직 시작 ---
        // if (this.rateLimit.isBlocked) {
        //     const message = '요청이 너무 많아 일시적으로 차단되었습니다.';
        //     console.warn(message);
        //     // 이미 차단 중일 때는 추가 알림 없이 요청만 거부
        //     return Promise.reject(new APIError('RATE_LIMIT_BLOCKED', message, 'COMMON_004'));
        // }

        // const requestKey = this._createRequestKey(url, options);
        // const now = Date.now();
        // const history = this.rateLimit.history.get(requestKey) || [];

        // // 5초가 지난 기록은 삭제
        // const recentHistory = history.filter(timestamp => now - timestamp < this.rateLimit.interval);

        // if (recentHistory.length >= this.rateLimit.limit) {
        //     console.warn(`Rate limit exceeded for request: ${requestKey}`);
        //     this.rateLimit.isBlocked = true;
        //     this.rateLimit.history.delete(requestKey); // 차단된 요청의 기록은 초기화

        //     this.showAlert(
        //         `단기간에 너무 많은 요청을 보냈습니다. ${this.rateLimit.blockDuration / 1000}초 후 다시 시도해주세요.`,
        //         null,
        //         this.rateLimit.blockDuration
        //     );

        //     setTimeout(() => {
        //         this.rateLimit.isBlocked = false;
        //     }, this.rateLimit.blockDuration);

        //     return Promise.reject(new APIError('RATE_LIMIT_EXCEEDED', '너무 많은 요청을 보냈습니다.', 'COMMON_003'));
        // }

        // 현재 요청 기록 추가
        // recentHistory.push(now);
        // this.rateLimit.history.set(requestKey, recentHistory);
        // --- 속도 제한 로직 끝 ---

        // 기본 헤더 설정
        const headers = {
            'Accept': 'application/json',
            ...options.headers
        };
        
        if (!(options.body instanceof FormData)) {
            headers['Content-Type'] = 'application/json';
        } else {
            delete headers['Content-Type'];
        }

        // fetch 옵션 구성
        const fetchOptions = {
            ...options,
            headers,
            credentials: 'same-origin', // 쿠키 포함
        };

        // 로딩 표시 (showLoading 옵션이 false가 아닌 경우에만)
        const showLoading = options.showLoading !== false;

        if (showLoading && typeof uiCommon !== 'undefined' && typeof uiCommon.fnShowLoading === 'function') {
            uiCommon.fnShowLoading();
        }

        try {
            const response = await fetch(this.config.baseURL + url, fetchOptions);

            // HTTP 상태 코드별 처리
            return await this.handleResponse(response, url, fetchOptions);

        } catch (error) {
            // APIError인 경우 alert 표시 후 재throw
            if (error instanceof APIError) {
                // 401, 403은 이미 처리했으므로 alert 하지 않음
                if (error.status !== 401 && error.status !== 403) {
                    this.showAlert(error.message);
                }
                throw error;
            }

            // 네트워크 에러 등
            console.error('API Request Error:', error);
            const networkError = new APIError('NETWORK_ERROR', '네트워크 오류가 발생했습니다.', null, null, error);
            this.showAlert(networkError.message);
            throw networkError;
        } finally {
            // 로딩 숨김
            if (showLoading && typeof uiCommon !== 'undefined' && typeof uiCommon.fnHideLoading === 'function') {
                uiCommon.fnHideLoading();
            }
        }
    },

    /**
     * 응답 처리
     * @param {Response} response - Fetch Response 객체
     * @param {string} url - 요청 URL
     * @param {object} options - fetch 옵션 (재시도용)
     * @returns {Promise} 파싱된 데이터
     */
    async handleResponse(response, url, options = {}) {
        const contentType = response.headers.get('content-type');
        const isJSON = contentType && contentType.includes('application/json');

        // 401 Unauthorized - 인증 실패
        if (response.status === 401) {
            return this.handle401Error(response, url, options, isJSON);
        }

        // 403 Forbidden - 권한 부족
        if (response.status === 403) {
            return this.handle403Error(response, url, options, isJSON);
        }

        // 404 Not Found
        if (response.status === 404) {
            if (isJSON) {
                const errorData = await response.json();
                throw this.createErrorFromResponse(errorData, response.status);
            }
            throw new APIError('NOT_FOUND', '요청한 리소스를 찾을 수 없습니다.', 'COMMON_001', response.status);
        }

        // 423 Locked - 계정 잠김
        if (response.status === 423) {
            if (isJSON) {
                const errorData = await response.json();
                throw this.createErrorFromResponse(errorData, response.status);
            }
            throw new APIError('ACCOUNT_LOCKED', '계정이 잠겼습니다. 비빌번호 초기화를 진행해주세요..', 'AUTH_002', response.status);
        }

        // 409 Conflict - 세션 충돌 (이미 로그인 중)
        if (response.status === 409) {
            if (isJSON) {
                const errorData = await response.json();
                // 세션 충돌 정보 포함하여 에러 생성
                const apiError = this.createErrorFromResponse(errorData, response.status);
                apiError.sessionInfo = errorData.data; // {sessionExists, loginAt}
                throw apiError;
            }
            throw new APIError('SESSION_CONFLICT', '이미 로그인된 세션이 있습니다.', 'AUTH_003', response.status);
        }

        // 429 Too Many Requests
        if (response.status === 429) {
            if (isJSON) {
                const errorData = await response.json();
                throw this.createErrorFromResponse(errorData, response.status);
            }
            throw new APIError('RATE_LIMIT_EXCEEDED', '너무 많은 요청을 보냈습니다. 잠시 후 다시 시도하세요.', 'COMMON_003', response.status);
        }

        // 500 Internal Server Error
        if (response.status >= 500) {
            if (isJSON) {
                const errorData = await response.json();
                throw this.createErrorFromResponse(errorData, response.status);
            }
            throw new APIError('SERVER_ERROR', '서버 오류가 발생했습니다.', 'SERVER_001', response.status);
        }

        // 응답 성공 (200-299)
        if (response.ok) {
            // 세션 타이머 리셋 (API 호출 성공 시 세션 연장 - Sliding Session)
            if (typeof SessionManager !== 'undefined' && typeof SessionManager.resetTimer === 'function') {
                // sessionExpiry(ms)를 초 단위로 변환하여 전달
                const expiresIn = (SessionManager.config.sessionExpiry || 30 * 60 * 1000) / 1000;
                SessionManager.resetTimer(expiresIn);
            }

            if (isJSON) {
                const data = await response.json();

                // ApiResponse 형식인지 확인 (code, message, data 필드)
                if (data.code !== undefined && data.message !== undefined && 'data' in data) {
                    // ApiResponse의 data 필드 반환
                    return data.data;
                }

                // 일반 JSON 응답
                return data;
            }

            // JSON이 아닌 경우 (텍스트, Blob 등)
            return response;
        }

        // 기타 에러 (400, 409 등)
        if (isJSON) {
            const errorData = await response.json();
            throw this.createErrorFromResponse(errorData, response.status);
        }

        throw new APIError('UNKNOWN_ERROR', '알 수 없는 오류가 발생했습니다.', null, response.status);
    },

    /**
     * ErrorResponse로부터 APIError 생성
     * @param {object} errorData - 에러 응답 데이터
     * @param {number} status - HTTP 상태 코드
     * @returns {APIError}
     */
    createErrorFromResponse(errorData, status) {
        // ErrorResponse 형식 (code 필드 존재)
        if (errorData.code) {
            return new APIError(
                errorData.code,
                errorData.message || '오류가 발생했습니다.',
                errorData.code,
                status,
                errorData
            );
        }

        // Spring Security 에러 형식 (error 필드만 존재)
        if (errorData.error) {
            return new APIError(
                errorData.error.replace(/\s+/g, '_').toUpperCase(),
                errorData.message || errorData.error,
                null,
                status,
                errorData
            );
        }

        // 기타 형식
        return new APIError(
            'API_ERROR',
            errorData.message || '요청 처리 중 오류가 발생했습니다.',
            null,
            status,
            errorData
        );
    },

    // 토큰 갱신 관련 상태
    _isRefreshing: false,
    _failedQueue: [],

    /**
     * 실패한 요청 큐 처리
     * @param {Error|null} error - 갱신 실패 시의 에러
     */
    _processFailedQueue(error = null) {
        this._failedQueue.forEach(prom => {
            if (error) {
                prom.reject(error);
            } else {
                prom.resolve();
            }
        });
        this._failedQueue = [];
    },

    /**
     * 401 에러 처리 (인증 실패 - 토큰 갱신 또는 로그인 필요)
     */
    async handle401Error(response, url, options = {}, isJSON = false) {
        console.warn('401 Unauthorized:', url);

        // 토큰 갱신 로직
        if (!this._isRefreshing) {
            this._isRefreshing = true;

            try {
                // 1. 토큰 갱신 API 호출
                const refreshResponse = await fetch(this.config.baseURL + '/auth/refresh', {
                    method: 'POST',
                    credentials: 'same-origin',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });

                // response body 타입 확인
                const contentType = refreshResponse.headers.get('content-type');

                if (!refreshResponse.ok) {
                    // 갱신 실패 시, 기존 로그인 리다이렉트 로직 수행
                    throw new Error('Failed to refresh token: ' + refreshResponse.status);
                }

                // JSON 응답 확인
                if (!contentType || !contentType.includes('application/json')) {
                    const responseText = await refreshResponse.text();
                    throw new Error('Unexpected response format: ' + contentType);
                }

                // 응답에서 expiresIn 추출
                let expiresIn = null;
                try {
                    const refreshData = await refreshResponse.json();
                    expiresIn = refreshData.data ? refreshData.data.expiresIn : refreshData.expiresIn;
                } catch (e) {
                    console.warn('Failed to parse refresh response:', e);
                }

                // 2. 갱신 성공 시, 대기열에 있던 모든 요청 재시도
                this._processFailedQueue(null);

                // 세션 타이머 리셋 (expiresIn 전달)
                if (typeof SessionManager !== 'undefined' && typeof SessionManager.resetTimer === 'function') {
                    SessionManager.resetTimer(expiresIn);
                }

                // 3. 원래 실패했던 요청 재시도
                //    새 토큰이 쿠키에 저장되었으므로, 동일한 옵션으로 재요청
                return this.request(url, options);

            } catch (refreshError) {
                console.error('Token refresh failed:', refreshError);
                this._processFailedQueue(refreshError);

                // 갱신에 최종 실패했으므로 로그인 페이지로 리다이렉트
                this.showAlert('세션이 만료되었습니다. 다시 로그인해주세요.', () => {
                    const currentPath = window.location.pathname;
                    const isAdminPage = currentPath.startsWith(this.config.baseURL + '/admin');
                    const loginUrl = isAdminPage ? this.config.baseURL + '/admin/login' : this.config.baseURL + '/login';

                    // 현재 페이지 URL을 sessionStorage에 저장 (로그인 페이지는 제외)
                    if (!currentPath.includes('/login')) {
                        sessionStorage.setItem('returnUrl', window.location.href);
                    }

                    window.location.href = loginUrl;
                });

                // APIError를 throw하여 finally 블록이 실행되도록 함
                throw new APIError('UNAUTHORIZED', '세션이 만료되었습니다.', 'AUTH_001', 401);

            } finally {
                this._isRefreshing = false;
            }

        } else {
            // 이미 갱신이 진행 중인 경우, 갱신이 끝날 때까지 대기하는 Promise를 반환
            return new Promise((resolve, reject) => {
                this._failedQueue.push({
                    resolve: () => {
                        // 갱신 성공 시, 원래 요청을 다시 시도하도록 resolve
                        resolve(this.request(url, options));
                    },
                    reject
                });
            });
        }
    },


    /**
     * 403 에러 처리 (권한 부족 - 로그인했지만 권한 없음)
     */
    async handle403Error(response, url, options, isJSON) {
        console.warn('403 Forbidden:', url);

        let errorData = null;
        let errorMessage = '접근 권한이 없습니다.';

        if (isJSON) {
            try {
                errorData = await response.json();
                errorMessage = errorData.message || errorData.error || errorMessage;
            } catch (e) {
                console.error('Failed to parse 403 error response:', e);
            }
        }

        if (options.handleAuthError) {
            const isAdminRequired = errorMessage.includes('관리자');

            // 모달 사용
            this.showAlert(errorMessage, () => {
                if (isAdminRequired) {
                    window.location.href = this.config.baseURL + '/main';
                }
            });
        }

        // APIError를 throw하여 finally 블록이 실행되도록 함
        throw new APIError('FORBIDDEN', errorMessage, errorData?.code || 'ROLE_003', 403, errorData);
    },

    /**
     * 유효성 검증 에러 메시지 포맷팅
     * @param {Array} validationErrors - 유효성 검증 에러 배열
     * @returns {string} 포맷팅된 메시지
     */
    formatValidationErrors(validationErrors) {
        if (!validationErrors || validationErrors.length === 0) {
            return '';
        }

        return validationErrors
            .map(err => err.message)
            .join('\n');
    },

    /**
     * 로그인 요청 (세션 충돌 처리 포함)
     * @param {string} userId - 사용자 ID
     * @param {string} password - 비밀번호
     * @param {object} options - 추가 옵션
     * @param {boolean} options.force - 기존 세션 강제 종료 여부
     * @returns {Promise} 로그인 응답 데이터
     */
    async login(userId, password, options = {}) {
        const { force = false } = options;
        const url = `/auth/login${force ? '?force=true' : ''}`;

        try {
            const response = await fetch(this.config.baseURL + url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'same-origin',
                body: JSON.stringify({ userId, password })
            });

            const contentType = response.headers.get('content-type');
            const isJSON = contentType && contentType.includes('application/json');

            // 409 Conflict - 세션 충돌
            if (response.status === 409 && isJSON) {
                const data = await response.json();
                const sessionInfo = data.data || {};

                // 세션 충돌 정보 반환 (호출자가 모달 처리)
                return {
                    sessionConflict: true,
                    message: data.message,
                    loginAt: sessionInfo.loginAt || '알 수 없음'
                };
            }

            // 기타 에러 처리
            if (!response.ok) {
                if (isJSON) {
                    const errorData = await response.json();
                    throw this.createErrorFromResponse(errorData, response.status);
                }
                throw new APIError('LOGIN_FAILED', '로그인에 실패했습니다.', null, response.status);
            }

            // 성공
            if (isJSON) {
                const data = await response.json();
                return data.data || data;
            }

            return response;
        } catch (error) {
            if (error instanceof APIError) {
                throw error;
            }
            throw new APIError('NETWORK_ERROR', '네트워크 오류가 발생했습니다.', null, null, error);
        }
    },

    /**
     * 세션 충돌 알림 모달을 표시하고 강제 로그인 처리
     * @param {string} userId - 사용자 ID
     * @param {string} password - 비밀번호
     * @param {object} conflictInfo - 세션 충돌 정보 {loginAt}
     * @param {function} onSuccess - 로그인 성공 시 콜백
     * @param {function} onCancel - 취소 시 콜백 (선택)
     */
    showSessionConflictModal(userId, password, conflictInfo, onSuccess, onCancel = null) {
        const _this = this;
        const message = `다른 곳에서 이미 로그인 중입니다.\n(로그인 시간: ${conflictInfo.loginAt})\n\n기존 세션이 로그아웃되고 새로 로그인됩니다.\n계속하시겠습니까?`;

        if (typeof uiCommon !== 'undefined' && typeof uiCommon.fnShowConfirmModal === 'function') {
            uiCommon.fnShowConfirmModal(
                message,
                // 확인 버튼 콜백
                async function() {
                    uiCommon.fnHideConfirmModal();
                    try {
                        // force=true로 재로그인
                        const result = await _this.login(userId, password, { force: true });
                        if (typeof onSuccess === 'function') {
                            onSuccess(result);
                        }
                    } catch (error) {
                        _this.showAlert(error.message || '로그인에 실패했습니다.');
                    }
                },
                // 취소 버튼 콜백
                function() {
                    uiCommon.fnHideConfirmModal();
                    if (typeof onCancel === 'function') {
                        onCancel();
                    }
                }
            );
        } else {
            // uiCommon이 없으면 기본 confirm 사용
            if (confirm(message)) {
                this.login(userId, password, { force: true })
                    .then(result => {
                        if (typeof onSuccess === 'function') {
                            onSuccess(result);
                        }
                    })
                    .catch(error => {
                        alert(error.message || '로그인에 실패했습니다.');
                    });
            } else {
                if (typeof onCancel === 'function') {
                    onCancel();
                }
            }
        }
    },

    /**
     * 로그인 처리 (세션 충돌 모달 자동 처리)
     * @param {string} userId - 사용자 ID
     * @param {string} password - 비밀번호
     * @param {function} onSuccess - 로그인 성공 시 콜백
     * @param {function} onError - 에러 발생 시 콜백 (선택)
     * @param {function} onCancel - 세션 충돌 모달에서 취소 시 콜백 (선택)
     */
    async loginWithSessionCheck(userId, password, onSuccess, onError = null, onCancel = null) {
        try {
            const result = await this.login(userId, password);

            // 세션 충돌인 경우 모달 표시
            if (result.sessionConflict) {
                this.showSessionConflictModal(userId, password, result, onSuccess, onCancel);
                return;
            }

            // 로그인 성공
            if (typeof onSuccess === 'function') {
                onSuccess(result);
            }
        } catch (error) {
            if (typeof onError === 'function') {
                onError(error);
            } else {
                this.showAlert(error.message || '로그인에 실패했습니다.');
            }
        }
    },

    /**
     * 관리자 로그인 요청 (세션 충돌 처리 포함)
     * @param {string} userId - 사용자 ID
     * @param {string} password - 비밀번호
     * @param {object} options - 추가 옵션
     * @param {boolean} options.force - 기존 세션 강제 종료 여부
     * @returns {Promise} 로그인 응답 데이터
     */
    async adminLogin(userId, password, options = {}) {
        const { force = false } = options;
        const url = `/auth/admin/login${force ? '?force=true' : ''}`;
        console.log('[DEBUG] adminLogin called, force:', force, 'url:', url);

        try {
            const response = await fetch(this.config.baseURL + url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'same-origin',
                body: JSON.stringify({ userId, password })
            });

            const contentType = response.headers.get('content-type');
            const isJSON = contentType && contentType.includes('application/json');
            console.log('[DEBUG] adminLogin response status:', response.status, 'isJSON:', isJSON);

            // 409 Conflict - 세션 충돌
            if (response.status === 409 && isJSON) {
                console.log('[DEBUG] Received 409 CONFLICT - session exists');
                const data = await response.json();
                const sessionInfo = data.data || {};

                // 세션 충돌 정보 반환 (호출자가 모달 처리)
                return {
                    sessionConflict: true,
                    message: data.message,
                    loginAt: sessionInfo.loginAt || '알 수 없음'
                };
            }

            // 기타 에러 처리
            if (!response.ok) {
                if (isJSON) {
                    const errorData = await response.json();
                    throw this.createErrorFromResponse(errorData, response.status);
                }
                throw new APIError('LOGIN_FAILED', '로그인에 실패했습니다.', null, response.status);
            }

            // 성공
            if (isJSON) {
                const data = await response.json();
                return data.data || data;
            }

            return response;
        } catch (error) {
            if (error instanceof APIError) {
                throw error;
            }
            throw new APIError('NETWORK_ERROR', '네트워크 오류가 발생했습니다.', null, null, error);
        }
    },

    /**
     * 관리자 세션 충돌 알림 모달을 표시하고 강제 로그인 처리
     * @param {string} userId - 사용자 ID
     * @param {string} password - 비밀번호
     * @param {object} conflictInfo - 세션 충돌 정보 {loginAt}
     * @param {function} onSuccess - 로그인 성공 시 콜백
     */
    showAdminSessionConflictModal(userId, password, conflictInfo, onSuccess, onCancel = null) {
        const _this = this;
        const message = `다른 곳에서 이미 로그인 중입니다.\n(로그인 시간: ${conflictInfo.loginAt})\n\n기존 세션이 로그아웃되고 새로 로그인됩니다.\n계속하시겠습니까?`;

        if (typeof uiCommon !== 'undefined' && typeof uiCommon.fnShowConfirmModal === 'function') {
            uiCommon.fnShowConfirmModal(
                message,
                // 확인 버튼 콜백
                async function() {
                    uiCommon.fnHideConfirmModal();
                    try {
                        // force=true로 재로그인
                        const result = await _this.adminLogin(userId, password, { force: true });
                        if (typeof onSuccess === 'function') {
                            onSuccess(result);
                        }
                    } catch (error) {
                        _this.showAlert(error.message || '로그인에 실패했습니다.');
                    }
                },
                // 취소 버튼 콜백
                function() {
                    uiCommon.fnHideConfirmModal();
                    if (typeof onCancel === 'function') {
                        onCancel();
                    }
                }
            );
        } else {
            // uiCommon이 없으면 기본 confirm 사용
            if (confirm(message)) {
                this.adminLogin(userId, password, { force: true })
                    .then(result => {
                        if (typeof onSuccess === 'function') {
                            onSuccess(result);
                        }
                    })
                    .catch(error => {
                        alert(error.message || '로그인에 실패했습니다.');
                    });
            } else {
                if (typeof onCancel === 'function') {
                    onCancel();
                }
            }
        }
    },

    /**
     * 관리자 로그인 처리 (세션 충돌 모달 자동 처리)
     * @param {string} userId - 사용자 ID
     * @param {string} password - 비밀번호
     * @param {function} onSuccess - 로그인 성공 시 콜백
     * @param {function} onError - 에러 발생 시 콜백 (선택)
     * @param {function} onCancel - 세션 충돌 모달에서 취소 시 콜백 (선택)
     */
    async adminLoginWithSessionCheck(userId, password, onSuccess, onError = null, onCancel = null) {
        try {
            const result = await this.adminLogin(userId, password);

            // 세션 충돌인 경우 모달 표시
            if (result.sessionConflict) {
                this.showAdminSessionConflictModal(userId, password, result, onSuccess, onCancel);
                return;
            }

            // 로그인 성공
            if (typeof onSuccess === 'function') {
                onSuccess(result);
            }
        } catch (error) {
            if (typeof onError === 'function') {
                onError(error);
            } else {
                this.showAlert(error.message || '로그인에 실패했습니다.');
            }
        }
    },

    /**
     * 로그아웃 요청
     * @returns {Promise<void>}
     */
    async logout() {
        try {
            const response = await this.post(this.config.baseURL + '/auth/logout');

            // 로그아웃 성공 처리
            // 2. 로그인 페이지로 리다이렉트합니다.
            //    현재 페이지가 관리자 페이지인지 확인하여 적절한 로그인 페이지로 이동합니다.
            const currentPath = window.location.pathname;
            const isAdminPage = currentPath.startsWith(this.config.baseURL + '/admin');
            const loginUrl = isAdminPage ? this.config.baseURL + '/admin/login' : this.config.baseURL + '/login';
            window.location.href = loginUrl;

        } catch (error) {
            console.error('로그아웃 실패:', error);
            // 로그아웃 실패 시 사용자에게 알림
            this.showAlert('로그아웃 처리 중 오류가 발생했습니다. 다시 시도해주세요.');
            // 필요하다면 에러를 다시 throw하여 상위 컴포넌트에서 처리하도록 할 수 있습니다.
            throw error;
        }
    },
    
	/**
	 * 파일 다운로드
	 * @param {string} url - 다운로드 API 엔드포인트
	 */
	download: async function(url) {
	    try {
	        const response = await this.request(url, {
	            method: 'GET',
	            showLoading: true
	        });

	        const blob = await response.blob();
	        let disposition = response.headers.get("content-disposition") || "";
	        let fileName = "download";

	        const utf8NameMatch = disposition.match(/filename\*=UTF-8''([^;\n]*)/);
	        if (utf8NameMatch) {
	            fileName = decodeURIComponent(utf8NameMatch[1]);
	        } else {
	            const asciiMatch = disposition.match(/filename="?([^"]+)"?/);
	            if (asciiMatch) {
	                fileName = decodeURIComponent(asciiMatch[1]);
	            }
	        }

	        const blobUrl = URL.createObjectURL(blob);
	        const link = document.createElement("a");
	        link.href = blobUrl;
	        link.download = fileName;
	        document.body.appendChild(link);
	        link.click();
	        link.remove();
	        URL.revokeObjectURL(blobUrl);

	    } catch (err) {
	        console.error(err);
	        this.showAlert('파일 다운로드 중 오류가 발생했습니다.');
	        throw err;
	    }
	},
    
	/**
	 * Excel 파일 다운로드
	 * @param {string} url - 다운로드 API 엔드포인트
	 * @param {object} param - POST 요청 바디(JSON)
	 */
	excelDownload: async function(url, param = {}) {
	    try {
	        const response = await this.request(url, {
	            method: 'POST',
	            body: JSON.stringify(param),
	            headers: { 'Content-Type': 'application/json' },
	            showLoading: true
	        });

	        const blob = await response.blob();
	        let disposition = response.headers.get("content-disposition") || "";
	        let fileName = "download.xlsx";

	        const utf8NameMatch = disposition.match(/filename\*=UTF-8''([^;\n]*)/);
	        if (utf8NameMatch) {
	            fileName = decodeURIComponent(utf8NameMatch[1]);
	        } else {
	            const asciiMatch = disposition.match(/filename="?([^"]+)"?/);
	            if (asciiMatch) fileName = asciiMatch[1];
	        }

	        const blobUrl = URL.createObjectURL(blob);
	        const link = document.createElement("a");
	        link.href = blobUrl;
	        link.download = fileName;
	        document.body.appendChild(link);
	        link.click();
	        link.remove();
	        URL.revokeObjectURL(blobUrl);

	    } catch (err) {
	        console.error(err);
	        this.showAlert('엑셀 다운로드 중 오류가 발생했습니다.');
	        throw err;
	    }
	},

    /**
     * 출력물 호출 공통 함수
     * @param {object} reportData - 출력물 데이터
     * @param {string} type - 출력물 이름 (예: 'certification')
     * @param {string} url - 출력물 호출 URL
     */
    async reportPrint(reportData, type, url) {
        const printData = await this.request(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ ...reportData }),
            showLoading: true
        });
        if(printData.status === 200) {
            const blob = await printData.blob();
            const objectUrl = (window.URL || window.webkitURL).createObjectURL(blob);
            if(blob.size > 0){
                let iframe = document.createElement('iframe');
                iframe.style.display = 'none';
                iframe.src = objectUrl;
                document.body.appendChild(iframe);
                iframe.contentWindow.focus();
                iframe.contentWindow.print();
                window.URL.revokeObjectURL(objectUrl);
            }
        }
    },
};

/**
 * API 에러 클래스
 */
class APIError extends Error {
    /**
     * @param {string} type - 에러 타입 (예: 'VALIDATION_FAILED')
     * @param {string} message - 사용자에게 표시할 메시지
     * @param {string} code - 백엔드 에러 코드 (예: 'USER_001')
     * @param {number} status - HTTP 상태 코드
     * @param {object} data - 원본 에러 데이터 (ErrorResponse 전체)
     * @param {Error} originalError - 원본 에러 객체
     */
    constructor(type, message, code = null, status = null, data = null, originalError = null) {
        super(message);
        this.name = 'APIError';
        this.type = type;
        this.code = code;
        this.status = status;
        this.data = data;
        this.originalError = originalError;

        // 유효성 검증 에러 정보 추출
        if (data && data.validationErrors) {
            this.validationErrors = data.validationErrors;
        }

        // 상세 정보 추출
        if (data && data.details) {
            this.details = data.details;
        }
    }

    /**
     * 에러 타입 확인 메서드
     */
    isAuthError() {
        return this.status === 401 || this.type === 'UNAUTHORIZED';
    }

    isForbiddenError() {
        return this.status === 403 || this.type === 'FORBIDDEN';
    }

    isNotFoundError() {
        return this.status === 404 || this.type === 'NOT_FOUND';
    }

    isValidationError() {
        return this.code === 'VALID_001' ||
               this.type === 'VALIDATION_FAILED' ||
               (this.validationErrors && this.validationErrors.length > 0);
    }

    isServerError() {
        return this.status >= 500;
    }

    isNetworkError() {
        return this.type === 'NETWORK_ERROR';
    }

    isSessionConflict() {
        return this.status === 409 || this.type === 'SESSION_CONFLICT';
    }

    isBusinessError() {
        return this.code && this.code.match(/^(USER|ROLE|AUTH|COMMON)_/);
    }

    /**
     * 에러 코드 확인
     */
    hasErrorCode(code) {
        return this.code === code;
    }

    /**
     * 사용자 친화적 에러 메시지 반환
     */
    getUserMessage() {
        if (this.isValidationError() && this.validationErrors) {
            return API.formatValidationErrors(this.validationErrors);
        }
        return this.message;
    }

    /**
     * 전체 에러 정보를 콘솔에 출력 (디버깅용)
     */
    logDetails() {
    }
}

// 전역 객체로 노출
window.API = API;
window.APIError = APIError;