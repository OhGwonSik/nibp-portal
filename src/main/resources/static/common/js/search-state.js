/**
 * SearchState - 검색조건 저장/복원 공통 유틸리티
 *
 * 목록 페이지에서 상세 페이지 이동 후 복귀 시 이전 검색조건을 유지하기 위한 유틸리티.
 * sessionStorage 기반으로 탭 단위 격리, 탭 닫으면 자동 삭제.
 *
 * 복원 조건:
 * - Nav 메뉴 클릭으로 진입 (navClick 플래그 있음) → 초기화
 * - F5 새로고침 (reload) → 복원
 * - 상세→목록 복귀 (navigate, navClick 플래그 없음) → 복원
 * - 브라우저 뒤로가기 (back_forward) → 복원
 *
 * 사용법:
 *   // 저장 (fetchList/getListData 등 API 호출 시)
 *   SearchState.saveForm('admin601', '.search_wrap', { pageIndex: this.state.pageIndex });
 *
 *   // 복원 (init 시, datepicker 초기화 이후에 호출)
 *   const restored = SearchState.restoreForm('admin601', '.search_wrap');
 *   if (restored) {
 *       this.state.pageIndex = restored._extra.pageIndex || 1;
 *   } else {
 *       // 기본값 설정 (datepicker 등)
 *   }
 *
 *   // 삭제 (등록 완료 후 목록 복귀 시)
 *   SearchState.clear('admin601');
 */
const SearchState = {

    _prefix: 'searchState_',
    _navClickKey: 'searchState_navClick',

    /**
     * 검색조건 삭제
     * @param {string} pageId - 페이지 식별자
     */
    clear: function (pageId) {
        sessionStorage.removeItem(this._prefix + pageId);
    },

    /**
     * 현재 pageId 외의 모든 searchState key 삭제
     * @param {string} currentPageId - 유지할 페이지 식별자
     */
    _clearOthers: function (currentPageId) {
        let keysToRemove = [];
        for (let i = 0; i < sessionStorage.length; i++) {
            let key = sessionStorage.key(i);
            if (key && key.indexOf(this._prefix) === 0
                && key !== this._prefix + currentPageId
                && key !== this._navClickKey) {
                keysToRemove.push(key);
            }
        }
        for (let i = 0; i < keysToRemove.length; i++) {
            sessionStorage.removeItem(keysToRemove[i]);
        }
    },

    /**
     * search_wrap 컨테이너 내 모든 입력 요소를 자동 수집하여 저장
     * @param {string} pageId - 페이지 식별자
     * @param {string} containerSelector - 검색 영역 CSS 선택자 (기본: '.search_wrap')
     * @param {object} [extra] - 추가 저장 데이터 (예: { pageIndex: 1 })
     */
    saveForm: function (pageId, containerSelector, extra) {
        let container = document.querySelector(containerSelector || '.search_wrap');
        if (!container) return;

        // 다른 페이지의 검색조건 정리
        this._clearOthers(pageId);

        let data = { _inputs: {}, _radios: {}, _selects: {}, _extra: extra || {} };

        // text input 수집 (datepicker 포함)
        let inputs = container.querySelectorAll('input[type="text"][id]');
        for (let i = 0; i < inputs.length; i++) {
            data._inputs[inputs[i].id] = inputs[i].value;
        }

        // radio 수집 (name 기준, 체크된 값)
        let radios = container.querySelectorAll('input[type="radio"]:checked');
        for (let i = 0; i < radios.length; i++) {
            if (radios[i].name) {
                data._radios[radios[i].name] = radios[i].value;
            }
        }

        // select 수집
        let selects = container.querySelectorAll('select[id]');
        for (let i = 0; i < selects.length; i++) {
            data._selects[selects[i].id] = selects[i].value;
        }

        try {
            sessionStorage.setItem(this._prefix + pageId, JSON.stringify(data));
        } catch (e) {
            console.warn('[SearchState] saveForm failed:', e);
        }
    },

    /**
     * 저장된 검색조건을 search_wrap 컨테이너에 자동 복원
     *
     * @param {string} pageId - 페이지 식별자
     * @param {string} containerSelector - 검색 영역 CSS 선택자 (기본: '.search_wrap')
     * @returns {object|null} 복원된 데이터 (._extra에 추가 데이터 포함) 또는 null (저장된 상태 없음)
     */
    restoreForm: function (pageId, containerSelector) {
        // nav 클릭 플래그 확인 및 제거
        let isNavClick = sessionStorage.getItem(this._navClickKey) === 'Y';
        sessionStorage.removeItem(this._navClickKey);

        // nav 메뉴 클릭으로 진입한 경우 → 초기화
        if (isNavClick) {
            this.clear(pageId);
            return null;
        }

        let data;
        try {
            let raw = sessionStorage.getItem(this._prefix + pageId);
            if (!raw) return null;
            data = JSON.parse(raw);
        } catch (e) {
            console.warn('[SearchState] restoreForm failed:', e);
            return null;
        }

        let container = document.querySelector(containerSelector || '.search_wrap');
        if (!container) return data;

        // text input 복원 (datepicker는 별도 처리)
        let datepickIds = {};
        let datepicks = container.querySelectorAll('.datepick[id]');
        for (let i = 0; i < datepicks.length; i++) {
            datepickIds[datepicks[i].id] = true;
        }

        if (data._inputs) {
            for (let id in data._inputs) {
                if (datepickIds[id]) {
                    // datepicker는 jQuery UI API로 복원
                    try {
                        let val = data._inputs[id];
                        if (val && val !== '시작일' && val !== '종료일') {
                            $('#' + id).datepicker('setDate', val);
                        }
                    } catch (e) {
                        // datepicker 미초기화 시 무시
                    }
                } else {
                    let el = document.getElementById(id);
                    if (el) el.value = data._inputs[id];
                }
            }
        }

        // radio 복원
        if (data._radios) {
            for (let name in data._radios) {
                let radio = container.querySelector('input[name="' + name + '"][value="' + data._radios[name] + '"]');
                if (radio) radio.checked = true;
            }
        }

        // select 복원
        if (data._selects) {
            for (let id in data._selects) {
                let el = document.getElementById(id);
                if (el) el.value = data._selects[id];
            }
        }

        return data;
    },

    /**
     * URL 쿼리 파라미터에서 검색조건 복원
     *
     * URL에 검색 파라미터가 있으면 해당 값으로 폼 필드를 채우고 페이지 번호를 반환.
     * URL에 파라미터가 없으면 null을 반환하여 기존 sessionStorage 복원 로직으로 폴백.
     *
     * @param {object} fieldMap - URL 파라미터명 → DOM 요소 ID 매핑
     *   예: { keyword: 'board_keyword', searchType: 'board_search_type', ctgry: 'search_category' }
     * @param {string} [pageKey='page'] - URL에서 페이지 번호를 가져올 파라미터명
     * @returns {number|null} - 복원된 페이지 번호 또는 null (URL 파라미터 없음)
     *
     * 사용법:
     *   const urlPage = SearchState.restoreFromUrl({ keyword: 'board_keyword', searchType: 'board_search_type' });
     *   if (urlPage !== null) {
     *       this.state.pageIndex = urlPage;
     *   } else {
     *       const saved = SearchState.restoreForm('pageId', '.board_search');
     *       if (saved) this.state.pageIndex = saved._extra.pageIndex || 1;
     *   }
     */
    restoreFromUrl: function (fieldMap, pageKey) {
        let urlParams = new URLSearchParams(window.location.search);
        let pk = pageKey || 'page';

        // URL에 검색 관련 파라미터가 있는지 확인
        let hasParams = urlParams.has(pk);
        if (!hasParams && fieldMap) {
            for (let urlKey in fieldMap) {
                if (fieldMap.hasOwnProperty(urlKey) && urlParams.has(urlKey)) {
                    hasParams = true;
                    break;
                }
            }
        }
        if (!hasParams) return null;

        // URL 복원 성공 시 navClick 플래그 제거 (뒤로가기로 돌아온 경우 stale 플래그 제거)
        sessionStorage.removeItem(this._navClickKey);

        // 폼 필드 복원
        if (fieldMap) {
            for (let urlKey in fieldMap) {
                if (!fieldMap.hasOwnProperty(urlKey)) continue;
                let el = document.getElementById(fieldMap[urlKey]);
                if (el && urlParams.has(urlKey)) {
                    el.value = urlParams.get(urlKey);
                }
            }
        }

        return parseInt(urlParams.get(pk)) || 1;
    },

    /**
     * 현재 URL 쿼리스트링을 업데이트 (검색/페이징 시 URL 반영)
     * pushState로 히스토리에 추가하여 뒤로가기 시 이전 검색 상태로 복원 가능
     *
     * @param {URLSearchParams|string} params - 쿼리 파라미터
     *
     * 사용법:
     *   const param = new URLSearchParams({ page: 1, keyword: '검색어' });
     *   SearchState.updateUrl(param);
     */
    updateUrl: function (params) {
        let qs = (typeof params === 'string') ? params : params.toString();
        let newUrl = window.location.pathname + '?' + qs;

        // 현재 URL과 동일하면 히스토리 중복 방지
        if (newUrl === window.location.pathname + window.location.search) return;

        history.pushState(null, '', newUrl);
    },

    /**
     * 브라우저 뒤로가기/앞으로가기 시 URL 파라미터로 검색조건 복원 및 목록 재조회
     *
     * @param {object} fieldMap - URL 파라미터명 → DOM 요소 ID 매핑
     * @param {string} [pageKey='page'] - 페이지 파라미터명
     * @param {function} callback - 복원된 페이지 번호를 받아 목록 재조회하는 콜백
     *
     * 사용법 (init에서 호출):
     *   SearchState.handlePopState(
     *       { keyword: 'board_keyword', searchType: 'board_search_type' },
     *       'page',
     *       function(page) { _this.state.pageIndex = page; _this.getListData(); }
     *   );
     */
    handlePopState: function (fieldMap, pageKey, callback) {
        let _self = this;
        window.addEventListener('popstate', function () {
            let page = _self.restoreFromUrl(fieldMap, pageKey);
            if (page === null) {
                // URL에 파라미터가 없으면 (최초 진입 URL로 복귀) 폼 초기화
                if (fieldMap) {
                    for (let urlKey in fieldMap) {
                        if (!fieldMap.hasOwnProperty(urlKey)) continue;
                        let el = document.getElementById(fieldMap[urlKey]);
                        if (el) {
                            el.tagName === 'SELECT' ? (el.selectedIndex = 0) : (el.value = '');
                        }
                    }
                }
                page = 1;
            }
            callback(page);
        });
    }
};

/**
 * Nav 메뉴 클릭 자동 감지 (세션 초기화 시 사용)
 * admin sidebar, portal GNB, portal sub_nav 의 링크 클릭 시 navClick 플래그 설정
 */
document.addEventListener('click', function (e) {
    let navLink = e.target.closest('#admin_sidebar a, #gnbMenu a, .sub_nav a');
    if (!navLink) return;

    let href = navLink.getAttribute('href');
    if (href && href !== 'javascript:;' && href !== '#none') {
        sessionStorage.setItem(SearchState._navClickKey, 'Y');
    }
});