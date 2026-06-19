/**
 * 세션 관리 모듈
 *
 * 사용자 활동 감지 기반 토큰 자동 갱신
 * - API 호출 없이 페이지에서 활동 중일 때 세션 유지
 * - 3분 간격으로 throttle하여 서버 부하 최소화
 * - 토큰 만료 시각 기반 세션 타이머 표시 (localStorage 활용)
 */

const SessionManager = {
    /**
     * 설정
     */
    config: {
        refreshInterval: 3 * 60 * 1000,  // 3분 (ms) - 활동 감지 시 갱신 간격
        refreshEndpoint: '/auth/refresh',
        timerElementId: 'sessionTimer',
        storageKey: 'nibp_token_expiry',

        // 세션 만료 시간 (서버에서 전달, 초기 로드 시 fallback용)
        get sessionExpiry() {
            return window.sessionExpiry || 30 * 60 * 1000;
        }
    },

    /**
     * 상태
     */
    state: {
        lastRefreshTime: Date.now(),
        isRefreshing: false,
        isInitialized: false,
        timerInterval: null
    },

    /**
     * 감지할 사용자 활동 이벤트
     */
    activityEvents: ['mousemove', 'keydown', 'click', 'scroll', 'touchstart'],

    /**
     * 초기화
     */
    init() {
        // 이미 초기화되었으면 무시
        if (this.state.isInitialized) return;

        // 로그인 상태가 아니면 종료
        if (!this.isLoggedIn()) {
            return;
        }

        // localStorage에 저장된 만료 시각이 없거나 이미 만료된 경우 sessionExpiry로 초기 설정
        var storedExpiry = localStorage.getItem(this.config.storageKey);
        if (!storedExpiry || parseInt(storedExpiry, 10) <= Date.now()) {
            var expiryTimestamp = Date.now() + this.config.sessionExpiry;
            localStorage.setItem(this.config.storageKey, expiryTimestamp.toString());
        }

        // 이벤트 리스너 등록
        this.bindActivityEvents();

        // 페이지 visibility 변경 감지 (탭 전환)
        this.bindVisibilityChange();

        // 타이머 시작
        this.startTimer();

        this.state.isInitialized = true;
    },

    /**
     * 로그인 상태 확인
     * 서버에서 전달한 window.isAuthenticated 값 사용
     * @returns {boolean}
     */
    isLoggedIn() {
        return window.isAuthenticated === true;
    },

    /**
     * 사용자 활동 이벤트 바인딩
     */
    bindActivityEvents() {
        // throttle된 핸들러 생성
        const throttledHandler = this.throttle(
            () => this.onActivity(),
            1000  // 이벤트 자체는 1초 throttle (갱신 여부는 내부에서 3분 체크)
        );

        // 각 이벤트에 리스너 등록
        this.activityEvents.forEach(eventType => {
            document.addEventListener(eventType, throttledHandler, { passive: true });
        });
    },

    /**
     * 페이지 visibility 변경 감지
     * 탭 전환 후 복귀 시 토큰 갱신
     */
    bindVisibilityChange() {
        document.addEventListener('visibilitychange', () => {
            if (document.visibilityState === 'visible') {
                // 탭이 다시 활성화되면 갱신 시도
                this.onActivity();
            }
        });
    },

    /**
     * 사용자 활동 감지 시 호출
     */
    onActivity() {
        // 로그인 상태가 아니면 무시
        if (!this.isLoggedIn()) return;

        const now = Date.now();
        const elapsed = now - this.state.lastRefreshTime;

        // refreshInterval(3분) 경과했으면 갱신
        if (elapsed >= this.config.refreshInterval) {
            this.refreshToken();
        }
    },

    /**
     * 토큰 갱신 요청
     */
    async refreshToken() {
        // 이미 갱신 중이면 무시
        if (this.state.isRefreshing) return;

        // API 모듈에서 갱신 중이면 무시 (중복 방지)
        if (typeof API !== 'undefined' && API._isRefreshing) return;

        this.state.isRefreshing = true;

        try {
            const response = await fetch(
                (window.contextPath || '') + this.config.refreshEndpoint,
                {
                    method: 'POST',
                    credentials: 'same-origin',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }
            );

            if (response.ok) {
                var expiresIn = null;

                // 응답에서 expiresIn 추출
                var contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    try {
                        var data = await response.json();
                        expiresIn = data.data ? data.data.expiresIn : data.expiresIn;
                    } catch (e) {
                        console.warn('Failed to parse refresh response:', e);
                    }
                }

                this.state.lastRefreshTime = Date.now();
                this.resetTimer(expiresIn);
            } else if (response.status === 401) {
                // 갱신 실패 (RF 만료) - 로그인 상태 해제
                window.isAuthenticated = false;
                this.stopTimer();
                localStorage.removeItem(this.config.storageKey);
            }
        } catch (error) {
            // 네트워크 오류 등 - 다음 API 호출 시 401로 처리됨
            console.warn('Session refresh failed:', error);
        } finally {
            this.state.isRefreshing = false;
        }
    },

    /**
     * 타이머 시작
     */
    startTimer() {
        // 기존 타이머가 있으면 정리
        this.stopTimer();

        // 즉시 한 번 업데이트
        this.updateTimerDisplay();

        // 1초마다 업데이트
        this.state.timerInterval = setInterval(() => {
            this.updateTimerDisplay();
        }, 1000);
    },

    /**
     * 타이머 중지
     */
    stopTimer() {
        if (this.state.timerInterval) {
            clearInterval(this.state.timerInterval);
            this.state.timerInterval = null;
        }

        // 타이머 요소 숨기기
        const timerElement = document.getElementById(this.config.timerElementId);
        if (timerElement) {
            timerElement.textContent = '';
        }
    },

    /**
     * 타이머 리셋 (갱신 시 호출)
     * api.js에서도 호출 가능
     * @param {number} [expiresIn] - 토큰 만료까지 남은 시간 (초). 없으면 표시만 갱신.
     */
    resetTimer(expiresIn) {
        if (expiresIn) {
            var expiryTimestamp = Date.now() + (expiresIn * 1000);
            localStorage.setItem(this.config.storageKey, expiryTimestamp.toString());
        }
        this.state.lastRefreshTime = Date.now();
        this.updateTimerDisplay();
    },

    /**
     * 타이머 표시 업데이트 (localStorage의 만료 시각 기준)
     */
    updateTimerDisplay() {
        const timerElement = document.getElementById(this.config.timerElementId);
        if (!timerElement) return;

        if (!this.isLoggedIn()) {
            timerElement.textContent = '';
            return;
        }

        var expiryStr = localStorage.getItem(this.config.storageKey);
        if (!expiryStr) {
            timerElement.textContent = '';
            return;
        }

        var remaining = parseInt(expiryStr, 10) - Date.now();

        if (remaining <= 0) {
            timerElement.textContent = '(세션 만료)';
            return;
        }

        const hours = Math.floor(remaining / 3600000);
        const minutes = Math.floor((remaining % 3600000) / 60000);
        const seconds = Math.floor((remaining % 60000) / 1000);

        // 1시간 이상이면 시:분:초, 미만이면 분:초
        if (hours > 0) {
            timerElement.textContent = `(${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')})`;
        } else {
            timerElement.textContent = `(${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')})`;
        }
    },

    /**
     * Throttle 유틸리티
     * @param {Function} func - 실행할 함수
     * @param {number} limit - 제한 시간 (ms)
     * @returns {Function}
     */
    throttle(func, limit) {
        let inThrottle = false;
        return function(...args) {
            if (!inThrottle) {
                func.apply(this, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    },

    /**
     * 세션 관리 중지 (로그아웃 시 호출)
     */
    destroy() {
        this.stopTimer();
        this.state.isInitialized = false;
        window.isAuthenticated = false;
        localStorage.removeItem(this.config.storageKey);
    }
};

// DOM 로드 완료 후 초기화
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => SessionManager.init());
} else {
    SessionManager.init();
}

// 전역 객체로 노출
window.SessionManager = SessionManager;
