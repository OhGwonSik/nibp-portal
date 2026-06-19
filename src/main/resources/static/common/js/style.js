window.uiCommon = {
    /**
     * ==================================================================================
     * 일반 모달 Show/Hide, 공통 모달(Alert, Confirm, Prompt) Show/Hide, 로딩 관련 스크립트
     * ==================================================================================
     *
     * 일반 모달은 Alert, Confirm, Prompt 모달을 제외한 모든 모달을 의미합니다.
     *
     * - fnShowModal(popupId): 일반 모달 Show
     * - fnHideModal(popupId): 일반 모달 Hide
     * - fnHideAllModals(): 모든 일반 모달 Hide
     * - fnShowAlertModal(message, callback): Alert 모달 Show
     * - fnHideAlertModal(): Alert 모달 Hide
     * - fnShowConfirmModal(message, okCallback, cancelCallback): Confirm 모달 Show
     * - fnHideConfirmModal(): Confirm 모달 Hide
     * - fnShowPromptModal(message, options): Prompt 모달 Show (Promise 반환)
     * - fnHidePromptModal(): Prompt 모달 Hide
     * - fnShowLoading(message): 로딩 오버레이 Show
     * - fnHideLoading(): 로딩 오버레이 Hide
     *
     **/

    /**
     * modal 상태
     */
    modal: {
        firstPopupID: null,           // 첫 번째로 열린 모달 ID
        zIndexCounter: 1000,          // z-index 카운터
        openedPopups: [],             // 열려있는 모달 ID 스택
        triggerElements: {}           // 각 모달을 연 트리거 요소 저장
    },

    /**
     * 모달 내부의 첫 번째 포커스 가능한 요소를 찾아 포커스
     * @param {jQuery} $modal - 모달 요소
     */
    fnFocusFirstElement: function($modal) {
        // 포커스 가능한 요소 셀렉터
        const focusableSelectors =
            'button:not([disabled]), ' +
            'a[href], ' +
            'input:not([disabled]), ' +
            'select:not([disabled]), ' +
            'textarea:not([disabled]), ' +
            '[tabindex]:not([tabindex="-1"])';

        // 모달 내부의 첫 번째 포커스 가능한 요소 찾기
        const $focusable = $modal.find(focusableSelectors).filter(':visible').first();

        if ($focusable.length > 0) {
            // focus-visible 적용
            try {
                $focusable[0].focus({ focusVisible: true });
            } catch (e) {
                // 구형 브라우저는 기본 focus 사용
                $focusable.focus();
            }
        }
    },

    /**
     * modal show
     * @param {string} popupId - 모달 ID
     */
    fnShowModal: function (popupId) {
        const _this = this;
        const modalElement = $('#' + popupId);
        const bgOverLay = $('#bg_overlay');
        const popupWrap = modalElement.find('.popup_wrap');

        if (!modalElement.length) {
            console.error(`모달을 찾을 수 없습니다: #${popupId}`);
            return;
        }

        // 현재 포커스된 요소(트리거 버튼)를 저장
        const activeElement = document.activeElement;
        if (activeElement && activeElement !== document.body) {
            _this.modal.triggerElements[popupId] = activeElement;
        }

        // 첫 번째 열리는 모달이 없다면 기록
        if (!_this.modal.firstPopupID) {
            _this.modal.firstPopupID = popupId;
        }

        // 열린 모달 스택에 추가
        if (!_this.modal.openedPopups.includes(popupId)) {
            _this.modal.openedPopups.push(popupId);
        }

        // 새 모달의 z-index를 할당하여 이후 모달들이 위로 쌓이게 함
        modalElement.css('z-index', _this.modal.zIndexCounter++);
        modalElement.fadeIn(function() {
            // popup_wrap에 tabindex 설정
            if (!popupWrap.attr('tabindex')) {
                popupWrap.attr('tabindex', '-1');
            }
            popupWrap.focus();
        });
        bgOverLay.fadeIn();
    },

    /**
     * modal hide
     * @param {string} popupId - 모달 ID
     */
    fnHideModal: function (popupId) {
        const _this = this;
        const modalElement = $('#' + popupId);
        const bgOverLay = $('#bg_overlay');

        if (!modalElement.length) {
            console.error(`모달을 찾을 수 없습니다: #${popupId}`);
            return;
        }

        // 모달 닫기 애니메이션
        modalElement.fadeOut(function(){
            // 포커스 복원: 저장된 트리거 요소로 포커스 이동
            const triggerElement = _this.modal.triggerElements[popupId];
            if (triggerElement && triggerElement.focus) {
                try {
                    triggerElement.focus({ focusVisible: true });
                } catch (e) {
                    triggerElement.focus();
                }
                delete _this.modal.triggerElements[popupId];
            }

            // 열린 모달 스택에서 제거
            const index = _this.modal.openedPopups.indexOf(popupId);
            if (index > -1) {
                _this.modal.openedPopups.splice(index, 1);
            }

            // 만약 닫은 모달이 첫 번째로 열린 모달이고, 더 이상 열린 모달이 없다면 오버레이도 닫는다
            if (popupId === _this.modal.firstPopupID && _this.modal.openedPopups.length === 0) {
                bgOverLay.fadeOut();
                _this.modal.firstPopupID = null;
                _this.modal.zIndexCounter = 1000; // 카운터 초기화
            }
        });
    },

    /**
     * 공통 Alert 모달 Show
     * @param {string} message - 모달에 표시할 메시지
     * @param {function} callback - 확인 버튼 클릭 콜백함수 (선택 사항)
     */
    fnShowAlertModal: function (message, callback = null, duration = 0) {
        const _this = this;
        const modalElement = $('#commonAlertModal');
        const alertMessage = modalElement.find('#commonAlertMessage');
        alertMessage.text(message);

        // 현재 포커스된 요소(트리거 버튼)를 저장
        const activeElement = document.activeElement;
        if (activeElement && activeElement !== document.body) {
            _this.modal.triggerElements['commonAlertModal'] = activeElement;
        }

        let bgOverLay = $('#commonAlertOverlay');
        if (bgOverLay.length === 0) {
            $('body').append('<div id="commonAlertOverlay" class="bg_overlay" display="none" style="z-index:9998;"></div>');
            bgOverLay = $('#commonAlertOverlay');
        }

        bgOverLay.fadeIn();
        modalElement.css('z-index', 9999);

        const okBtn = modalElement.find('#commonAlertConfirmBtn');
        const closeBtn = modalElement.find('#commonAlertCloseBtn');
        const popupWrap = modalElement.find('.popup_wrap');

        modalElement.fadeIn(function() {
            // popup_wrap에 tabindex 설정
            if (!popupWrap.attr('tabindex')) {
                popupWrap.attr('tabindex', '-1');
            }
            popupWrap.focus();
        });

        const handler = function(e) {
            e.preventDefault();
            // 타이머가 설정된 경우, 버튼 클릭 시 즉시 숨기지 않고 타이머에 맡길 수 있음
            // 여기서는 버튼 클릭 시 항상 닫히도록 처리
            if (typeof callback === 'function') {
                // 콜백이 있으면 모달을 먼저 닫고 콜백 실행 or 콜백 안에서 닫기
                // 현재 구조에서는 콜백 안에서 닫는 것이 더 유연함
                callback();
            } else {
                uiCommon.fnHideAlertModal();
            }
        };

        okBtn.off('click').on('click', handler);
        closeBtn.off('click').on('click', () => uiCommon.fnHideAlertModal()); // 닫기 버튼은 항상 모달을 닫음

        // 자동 닫힘 로직
        if (duration > 0) {
            setTimeout(() => {
                uiCommon.fnHideAlertModal();
            }, duration);
        }
    },

    /**
     * 공통 Alert 모달 Hide
     */
    fnHideAlertModal: function () {
        const _this = this;
        const modalElement = $('#commonAlertModal');
        const bgOverLay = $('#commonAlertOverlay');

        modalElement.fadeOut(function() {
            // 포커스 복원: 저장된 트리거 요소로 포커스 이동
            const triggerElement = _this.modal.triggerElements['commonAlertModal'];
            if (triggerElement && triggerElement.focus) {
                try {
                    triggerElement.focus({ focusVisible: true });
                } catch (e) {
                    triggerElement.focus();
                }
                delete _this.modal.triggerElements['commonAlertModal'];
            }
        });
        bgOverLay.remove();
    },

    /**
     * 공통 Confirm 모달 Show
     * @param {string} message - 모달에 표시할 메시지
     * @param {function} okCallback - 확인 버튼 클릭 콜백함수 (선택 사항)
     * @param {function} cancelCallback - 취소 버튼 클릭 콜백함수 (선택 사항)
     */
    fnShowConfirmModal: function (message, okCallback = null, cancelCallback = null) {
        const _this = this;
        const modalElement = $('#commonConfirmModal');
        const confirmMessage = modalElement.find('#commonConfirmMessage');
        confirmMessage.text(message);

        // 현재 포커스된 요소(트리거 버튼)를 저장
        const activeElement = document.activeElement;
        if (activeElement && activeElement !== document.body) {
            _this.modal.triggerElements['commonConfirmModal'] = activeElement;
        }

        let bgOverLay = $('#commonConfirmOverlay');
        if (bgOverLay.length === 0) {
            $('body').append('<div id="commonConfirmOverlay" class="bg_overlay" style="z-index:9998;"></div>');
            bgOverLay = $('#commonConfirmOverlay');
        }

        bgOverLay.fadeIn();
        modalElement.css('z-index', 9999);

        const okBtn = modalElement.find('#commonConfirmOkBtn');
        const cancelBtn = modalElement.find('#commonConfirmCancelBtn');
        const popupWrap = modalElement.find('.popup_wrap');

        modalElement.fadeIn(function() {
            if (!popupWrap.attr('tabindex')) {
                popupWrap.attr('tabindex', '-1');
            }
            popupWrap.focus();
        });

        okBtn.off('click').on('click', function(e) {
            e.preventDefault();
            typeof okCallback === 'function'
                ? okCallback()
                : uiCommon.fnHideConfirmModal();
        });

        cancelBtn.off('click').on('click', function(e) {
            e.preventDefault();
            typeof cancelCallback === 'function'
                ? cancelCallback()
                : uiCommon.fnHideConfirmModal();
        });
    },

    /**
     * 공통 Confirm 모달 Hide
     */
    fnHideConfirmModal: function (popupId = 'commonConfirmModal') {
        const _this = this;
        const modalElement = $('#commonConfirmModal');
        const bgOverLay = $('#commonConfirmOverlay');

        modalElement.fadeOut(function() {
            // 포커스 복원: 저장된 트리거 요소로 포커스 이동
            const triggerElement = _this.modal.triggerElements['commonConfirmModal'];
            if (triggerElement && triggerElement.focus) {
                try {
                    triggerElement.focus({ focusVisible: true });
                } catch (e) {
                    triggerElement.focus();
                }
                delete _this.modal.triggerElements['commonConfirmModal'];
            }
        });
        bgOverLay.remove();
    },

    /**
     * 공통 Prompt 모달 Show (Promise 반환)
     * @param {string} message - 모달에 표시할 메시지
     * @param {object} options - 추가 옵션 (선택 사항)
     * @param {string} options.title - 모달 제목 (기본값: '입력')
     * @param {string} options.defaultValue - 입력 필드 기본값 (기본값: '')
     * @param {string} options.placeholder - 입력 필드 placeholder (기본값: '')
     * @returns {Promise<string|null>} 확인 시 입력값, 취소 시 null
     *
     * @example
     * // 기본 사용
     * const name = await uiCommon.fnShowPromptModal('이름을 입력하세요');
     * if (name !== null) {
     *     console.log('입력값:', name);
     * }
     *
     * // 옵션 사용
     * const value = await uiCommon.fnShowPromptModal('값을 입력하세요', {
     *     title: '설정',
     *     defaultValue: '기본값',
     *     placeholder: '여기에 입력'
     * });
     */
    fnShowPromptModal: function (message, options = {}) {
        const _this = this;

        return new Promise((resolve) => {
            const modalElement = $('#commonPromptModal');
            const promptMessage = modalElement.find('#commonPromptMessage');
            const promptInput = modalElement.find('#commonPromptInput');
            const promptTitle = modalElement.find('#commonPromptTitle');

            // 옵션 적용
            const title = options.title || '입력';
            const defaultValue = options.defaultValue || '';
            const placeholder = options.placeholder || '';

            promptTitle.text(title);
            promptMessage.text(message);
            promptInput.val(defaultValue);
            promptInput.attr('placeholder', placeholder);

            // 현재 포커스된 요소(트리거 버튼)를 저장
            const activeElement = document.activeElement;
            if (activeElement && activeElement !== document.body) {
                _this.modal.triggerElements['commonPromptModal'] = activeElement;
            }

            let bgOverLay = $('#commonPromptOverlay');
            if (bgOverLay.length === 0) {
                $('body').append('<div id="commonPromptOverlay" class="bg_overlay" style="z-index:9998;"></div>');
                bgOverLay = $('#commonPromptOverlay');
            }

            bgOverLay.fadeIn();
            modalElement.css('z-index', 9999);

            const okBtn = modalElement.find('#commonPromptOkBtn');
            const cancelBtn = modalElement.find('#commonPromptCancelBtn');
            const closeBtn = modalElement.find('#commonPromptCloseBtn');
            const popupWrap = modalElement.find('.popup_wrap');

            // 이벤트 정리 함수
            const cleanup = () => {
                okBtn.off('click.prompt');
                cancelBtn.off('click.prompt');
                closeBtn.off('click.prompt');
                promptInput.off('keydown.prompt');
            };

            modalElement.fadeIn(function() {
                if (!popupWrap.attr('tabindex')) {
                    popupWrap.attr('tabindex', '-1');
                }
                // 입력 필드에 포커스
                promptInput.focus();
            });

            // 확인 버튼 클릭
            okBtn.off('click.prompt').on('click.prompt', function(e) {
                e.preventDefault();
                const inputValue = promptInput.val();
                cleanup();
                uiCommon.fnHidePromptModal();
                resolve(inputValue);
            });

            // 취소 버튼 클릭
            cancelBtn.off('click.prompt').on('click.prompt', function(e) {
                e.preventDefault();
                cleanup();
                uiCommon.fnHidePromptModal();
                resolve(null);
            });

            // 닫기 버튼 클릭
            closeBtn.off('click.prompt').on('click.prompt', function(e) {
                e.preventDefault();
                cleanup();
                uiCommon.fnHidePromptModal();
                resolve(null);
            });

            // Enter 키로 확인
            promptInput.off('keydown.prompt').on('keydown.prompt', function(e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    okBtn.trigger('click.prompt');
                }
            });
        });
    },

    /**
     * 공통 Prompt 모달 Hide
     */
    fnHidePromptModal: function () {
        const _this = this;
        const modalElement = $('#commonPromptModal');
        const bgOverLay = $('#commonPromptOverlay');
        const promptInput = modalElement.find('#commonPromptInput');

        // 입력 필드 초기화
        promptInput.val('');

        modalElement.fadeOut(function() {
            // 포커스 복원: 저장된 트리거 요소로 포커스 이동
            const triggerElement = _this.modal.triggerElements['commonPromptModal'];
            if (triggerElement && triggerElement.focus) {
                try {
                    triggerElement.focus({ focusVisible: true });
                } catch (e) {
                    triggerElement.focus();
                }
                delete _this.modal.triggerElements['commonPromptModal'];
            }
        });
        bgOverLay.remove();
    },

    /**
     * 로딩 오버레이 표시
     * @param {string} [message='처리 중입니다...'] - 로딩 메시지 (선택 사항)
     */
    fnShowLoading: function (message= '처리 중입니다...') {
        const $loadingOverlay = $('#commonLoadingOverlay');
        const $loadingMessage = $('#commonLoadingMessage');
        if ($loadingOverlay.length === 0) return;

        if ($loadingMessage.length > 0) {
            $loadingMessage.text(message);
        }
        $loadingOverlay.css({
            'display': 'flex',
            'visibility': 'visible',
            'zIndex': 9999
        });
        setTimeout(() => {
            $('body').css('overflowY','hidden');
        }, 300);
    },

    /**
     * 로딩 오버레이 숨김
     */
    fnHideLoading: function () {
        const $loadingOverlay = $('#commonLoadingOverlay');
        if ($loadingOverlay.length) {
            // 모든 애니메이션 즉시 중단
            $loadingOverlay.stop(true, true);

            // 브라우저 렌더링 큐를 사용하여 확실하게 숨김
            const overlay = $loadingOverlay[0];

            // 먼저 visibility를 hidden으로 설정 (즉시 보이지 않게)
            overlay.style.visibility = 'hidden';

            // requestAnimationFrame을 사용하여 다음 프레임에서 완전히 제거
            requestAnimationFrame(() => {
                overlay.style.display = 'none';
                overlay.style.zIndex = '-1';
                overlay.style.opacity = '1';
                overlay.style.height = '100vh';
            });
        }
        setTimeout(() => {
            $('body').css('overflowY','auto');
        }, 300);
    },

    initEvent: function () {
        const LNB = {
            selectors: {
                wrap: '.lnb_wrap',
                topMenu: '.lnb_wrap > ul > li',
                subMenu: '.lnb_wrap > ul > li > ul > li > a',
            },
            storageKeys: {
                menu: 'activeLnbMenu',
                url: 'activeLnbUrl'
            },
            init() {
                this.restoreState();
                this.bindEvents();
            },
            getActiveState() {
                return {
                    menu: localStorage.getItem(this.storageKeys.menu),
                    url: localStorage.getItem(this.storageKeys.url)
                };
            },
            saveState(menuClass, url) {
                if (menuClass) localStorage.setItem(this.storageKeys.menu, menuClass);
                if (url) localStorage.setItem(this.storageKeys.url, url);
            },
            clearState() {
                localStorage.removeItem(this.storageKeys.menu);
                localStorage.removeItem(this.storageKeys.url);
            },
            getMenuClass($el) {
                return $el.attr('class')?.split(' ').find(cls => cls.startsWith('menu')) || null;
            },
            restoreState() {
                const { menu, url } = this.getActiveState();
                const currentPath = window.location.pathname;
                if (!menu || !url) return;

                const $currentMenu = $(`${this.selectors.wrap} ul a[href="${currentPath}"]`);
                if (currentPath !== url && !$currentMenu.length) {
                    this.clearState();
                    return;
                }

                const $activeLi = $(`${this.selectors.topMenu}.${menu}`);
                $activeLi.addClass('on').find('> a').siblings('ul').show();

                if (url) {
                    $activeLi.find('ul a').removeClass('on');
                    $activeLi.find(`ul a[href="${url}"]`).addClass('on');
                }
            },
            bindEvents() {
                // 상위 메뉴 클릭
                $(document).on('click', `${this.selectors.topMenu} > a`, e => {
                    e.preventDefault();
                    const $parent = $(e.currentTarget).parent();
                    const menuClass = this.getMenuClass($parent);

                    if ($parent.hasClass('on')) {
                        $parent.removeClass('on').find('ul').slideUp(400);
                        this.clearState();
                    } else {
                        $(this.selectors.topMenu).not($parent).removeClass('on').find('ul').slideUp(400);
                        $parent.addClass('on').find('ul').slideDown(400);
                        this.saveState(menuClass);
                    }
                });

                // 하위 메뉴 클릭
                $(document).on('click', this.selectors.subMenu, e => {
                    const $target = $(e.currentTarget);
                    const href = $target.attr('href');

                    if (!href || href === '#none' || href.startsWith('javascript:')) {
                        e.preventDefault();
                        return;
                    }

                    const $parentLi = $target.closest(this.selectors.topMenu);
                    const menuClass = this.getMenuClass($parentLi);
                    this.saveState(menuClass, href);

                    $parentLi.find('ul a').removeClass('on');
                    $target.addClass('on');
                });
            }
        };

        LNB.init();

        // 깊이 메뉴 토글
        // 초기 상태: 모두 닫힘, 트리거 텍스트는 '열림'
        $('.depth > a').each(function () {
            $(this).removeClass('off').text('열림');
        });

        // 열림/닫힘 토글
        $(document).on('click', '.Depth > a', function (e) {
            e.preventDefault();
            const $trigger = $(this);
            const targetId = this.id;
            const $targets = $('tr.t_Depth[data-tg="' + targetId + '"]');
            const isOpen = $trigger.hasClass('off');

            if (isOpen) {
                // 닫기
                closeMenu(targetId);
                $trigger.removeClass('off').text('열림');
            } else {
                // 열기
                const $directChild = $('tr.t_Depth[data-tg="' + targetId + '"]');
                $directChild.stop(true, true).slideDown(150);
                $trigger.addClass('off').text('닫힘');
            }
        });

        function closeMenu(parentId) {
            const $children = $('tr.t_Depth[data-tg="' + parentId + '"]');

            $children.each(function() {
                const $child = $(this);
                const childId = $child.attr('data-id');

                // 하위 메뉴 닫기
                $child.stop(true, true).slideUp(150);

                // 토글 버튼 초기화
                $child.find('.depth > a, .Depth > a').removeClass('off').text('열림');

                // 재귀: 이 자식의 하위 메뉴도 닫기
                closeMenu(childId);
            });
        }

        // 퍼블 스크립트
        //모바일 메뉴
        $('.m_menu').click(function () {
            $(this).toggleClass('close');
            $('.left_wrap').slideToggle();

        });
        $(window).resize(function () {
            $('.left_wrap').removeAttr("style");
        });

        //조직도
        $('.group_list > ul > li > a, .group_list > ul > li > ul > li > a').click(function () {
            var $parent = $(this).parent();

            if ($parent.hasClass("on")) {
                $parent.removeClass("on");
                $parent.children('ul').slideUp(400);
            } else {
                $parent.siblings().removeClass("on").find('ul').slideUp(400);
                $parent.addClass("on");
                $parent.children('ul').slideDown(400);
            }
            return false; // 링크의 기본 동작을 방지합니다.
        });

        //테이블 스크롤
        $('.scroll_img').click(function () {
            $(this).hide();
        });

        /* QUICK 버튼 클릭 → 열기 */
        $('.quick_sise_wrap .quick_side_inner .quick_box > a').on('click', function (e) {
            e.preventDefault();
            $(this).closest('.quick_box').addClass('active');
        });

        /* QUICK 마우스오버 → 열기 */
        $('.quick_sise_wrap .quick_side_inner .quick_box').on('mouseenter', function () {
            $(this).addClass('active');
        });

        /* QUICK 마우스아웃 → 닫기 */
        $('.quick_sise_wrap .quick_side_inner .quick_box').on('mouseleave', function () {
            $(this).removeClass('active');
        });

        /* QUICK 내부 포커스 → 열기, 포커스 이탈 → 닫기 */
        $('.quick_sise_wrap .quick_side_inner .quick_box').on('focusin', function () {
            $(this).addClass('active');
        }).on('focusout', function () {
            var $box = $(this);
            setTimeout(function () {
                if (!$box.find(':focus').length) {
                    $box.removeClass('active');
                }
            }, 0);
        });

        /* 닫기 버튼 클릭 → 닫기 */
        $('.quick_sise_wrap .quick_side_inner .quick_close').on('click', function (e) {
            e.preventDefault();
            $(this).closest('.quick_box').removeClass('active');
        });
    },

    /**
     * 페이지네이션 공통
     */
    pagination: {
        /**
         * 페이지네이션 렌더링
         * @param {{containerSelector: string, totalPages: number, onPageChange: *, currentPage: number}} options - 페이지네이션 옵션
         * @param {number} options.currentPage - 현재 페이지 번호
         * @param {number} options.totalPages - 전체 페이지 수
         * @param {string} options.containerSelector - 페이지 번호를 렌더링할 컨테이너 셀렉터 (기본값: '#paging_wrap')
         * @param {number} options.pageGroupSize - 한 번에 보여줄 페이지 번호 개수 (기본값: 10)
         * @param {Function} options.onPageChange - 페이지 변경 시 호출될 콜백 함수
         */
        render: function (options) {
            const {
                currentPage,
                totalPages,
                containerSelector = '#paging_wrap',
                pageGroupSize = 10,
                onPageChange
            } = options;

            const $container = $(containerSelector);
            const $pg = $container.find(".pagination_container"); // 페이지 번호 영역
            $pg.empty();

            if (totalPages <= 0) {
                $container.hide();
                return;
            }

            $container.show();

            // 페이지가 1개인 경우 숫자만 표시하고 화살표 버튼 숨김
            if (totalPages === 1) {
                $pg.append(`<strong title="현재페이지">1</strong>`);
                $container.find(".page_first, .page_prev, .page_next, .page_last").hide();
                return;
            }

            // 화살표 버튼 다시 보이기 (페이지가 2개 이상인 경우)
            $container.find(".page_first, .page_prev, .page_next, .page_last").show();

            // 페이지 그룹 계산
            const currentGroup = Math.ceil(currentPage / pageGroupSize);
            const startPage = (currentGroup - 1) * pageGroupSize + 1;
            const endPage = Math.min(currentGroup * pageGroupSize, totalPages);

            // 페이지 번호 렌더링
            for (let p = startPage; p <= endPage; p++) {
                if (p === currentPage) {
                    $pg.append(`<strong title="현재페이지">${p}</strong>`);
                } else {
                    $pg.append(`<a href="javascript:" data-page="${p}">${p}</a>`);
                }
            }

            // 버튼 활성화/비활성화 및 숨김 처리
            const $first = $container.find(".page_first");
            const $prev = $container.find(".page_prev");
            const $next = $container.find(".page_next");
            const $last = $container.find(".page_last");

            const handleBtn = ($btn, shouldHide) => {
                if (shouldHide) {
                    $btn.hide();
                } else {
                    $btn.show().removeClass("disabled").css({
                        "pointer-events": "auto",
                        "opacity": "1"
                    });
                }
            };

            // 첫 페이지인 경우 이전 버튼 숨김
            handleBtn($first, currentPage === 1);
            handleBtn($prev, currentPage === 1);

            // 마지막 페이지인 경우 다음 버튼 숨김
            handleBtn($next, currentPage === totalPages);
            handleBtn($last, currentPage === totalPages);

            // 이벤트 바인딩
            if (typeof onPageChange === "function") {
                this.bindEvents($container, totalPages, onPageChange);
            }
        },

        /**
         * 이벤트 바인딩
         */
        bindEvents: function ($container, totalPages, onPageChange) {
            // 페이지 번호 클릭
            $container.find(".pagination_container a").off("click").on("click", function () {
                const page = Number($(this).data("page"));
                if (!isNaN(page)) onPageChange(page);
            });

            // 첫 페이지
            $container.find(".page_first").off("click").on("click", function () {
                if (!$(this).hasClass("disabled")) onPageChange(1);
            });

            // 이전 페이지
            $container.find(".page_prev").off("click").on("click", function () {
                if (!$(this).hasClass("disabled")) {
                    const currentPage = Number($container.find(".pagination_container strong").text());
                    if (currentPage > 1) onPageChange(currentPage - 1);
                }
            });

            // 다음 페이지
            $container.find(".page_next").off("click").on("click", function () {
                if (!$(this).hasClass("disabled")) {
                    const currentPage = Number($container.find(".pagination_container strong").text());
                    if (currentPage < totalPages) onPageChange(currentPage + 1);
                }
            });

            // 마지막 페이지
            $container.find(".page_last").off("click").on("click", function () {
                if (!$(this).hasClass("disabled")) onPageChange(totalPages);
            });
        }
    },
    /**
     * ==================================================================================
     * Datepicker 공통 설정
     * ==================================================================================
     *
     * 1. 자동 초기화: datepick 클래스를 가진 모든 요소에 자동으로 적용
     * 2. 수동 초기화: 특정 요소에 대해 uiCommon.datepicker.init(selector, options) 호출
     *
     * 사용 예시:
     * - 자동: <input type="text" class="datepicker" />
     * - 수동: uiCommon.datepicker.init('#myDatepicker', {
     *           onSelect: function(dateText, inst) {
     *               console.log('선택된 날짜:', dateText);
     *           }
     *       });
     */

    /**
     * 탭 메뉴 접근성 속성 업데이트
     */
    updateTabAccessibility: function(container) {
        const $container = container ? $(container) : $('.sub_tab_menu');

        $container.each(function() {
            const $tabMenu = $(this);
            const $links = $tabMenu.find('li a');

            $links.each(function() {
                const $link = $(this);
                const $li = $link.closest('li');

                // 외부 링크(target="_blank")는 기존 title 유지
                if ($link.attr('target') === '_blank') return;

                if ($li.hasClass('on')) {
                    $link.attr('title', '선택됨');
                } else {
                    $link.removeAttr('title');
                }
            });
        });
    },

    datepicker: {
        /**
         * 기본 datepicker 옵션
         */
        getDefaultOptions: function() {
            return {
                dateFormat: 'yy-mm-dd',
                showOtherMonths: true,
                showMonthAfterYear: true,
                changeYear: true,
                changeMonth: true,
                showOn: "focus",  // 인풋 클릭 시에만 달력 표시
                buttonImage: "images/ico_date.png",
                buttonImageOnly: true,
                buttonText: "선택",
                yearSuffix: "년",
                monthNamesShort: ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월'],
                monthNames: ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월'],
                dayNamesMin: ['일','월','화','수','목','금','토'],
                dayNames: ['일요일','월요일','화요일','수요일','목요일','금요일','토요일'],
                minDate: "-20Y",
                maxDate: "+10y"
            };
        },

        /**
         * 특정 요소에 datepicker 초기화 (커스텀 옵션 사용 가능)
         * @param {string|jQuery} selector - datepicker를 적용할 요소 셀렉터
         * @param {object} customOptions - 추가/덮어쓸 옵션 (onSelect 등)
         */
        init: function(selector, customOptions = {}) {
            const $elements = $(selector);
            if ($elements.length === 0) return;

            const options = $.extend({}, this.getDefaultOptions(), customOptions);
            $elements.each(function() {
                const $this = $(this);
                // 이미 datepicker가 적용된 경우 제거 후 재적용
                // jQuery UI는 적용 시 hasDatepicker 클래스를 자동으로 추가함
                try {
                    if ($this.hasClass('hasDatepicker')) {
                        $this.datepicker('destroy');
                    }
                } catch (e) {
                    // datepicker가 없는 경우 무시
                }
                $this.datepicker(options);
            });
        },

        /**
         * datepick 클래스를 가진 모든 요소에 자동으로 datepicker 적용
         */
        bindEvents: function() {
            this.init('.datepick');

            // 하드코딩된 datepicker 버튼 이미지 클릭 시 input에 포커스
            $(document).on('click', '.ui-datepicker-trigger', function() {
                $(this).prev('input.datepick').focus();
            });
        }
    },

    /**
     * ==================================================================================
     * Month & Year Picker 공통 설정 (Air Datepicker 사용)
     * ==================================================================================
     * * [설명]
     * - 기본 일자 선택(Date)은 기존 jQuery UI를 사용하고,
     * - 월(Month) 및 연도(Year) 선택은 Air Datepicker 라이브러리를 사용합니다.
     *
     * [사용 예시]
     * - 자동(클래스): <input type="text" class="monthpick" />
     * - 자동(클래스): <input type="text" class="yearpick" />
     * - 수동(JS): uiCommon.monthpicker.init('#ID', { onClose: function(val) { console.log(val); } });
     */
    monthpicker: {
        /**
         * 타입별 기본 옵션 생성
         * @param {string} type - 'month' | 'year'
         */
        getDefaultOptions: function(type = 'month') {
            return {
                locale: {
                    days: ['일요일', '월요일', '화요일', '수요일', '목요일', '금요일', '토요일'],
                    daysShort: ['일', '월', '화', '수', '목', '금', '토'],
                    daysMin: ['일', '월', '화', '수', '목', '금', '토'],
                    months: ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'],
                    monthsShort: ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'],
                    today: '오늘',
                    clear: '초기화',
                    dateFormat: type === 'month' ? 'yyyy-MM' : 'yyyy',
                    firstDay: 0
                },
                view: type === 'month' ? 'months' : 'years',     // 시작 뷰 모드
                minView: type === 'month' ? 'months' : 'years',  // 최소 선택 단위
                autoClose: true,                                 // 선택 시 창 자동 닫힘
                navTitles: {
                    days: 'MMMM <i>yyyy</i>',
                    months: 'yyyy',
                    years: 'yyyy1 - yyyy2'
                }
            };
        },

        /**
         * Month/Year Picker 초기화
         * @param {string|jQuery} selector - 대상 요소 셀렉터
         * @param {object} customOptions - 사용자 정의 옵션 (onClose 등)
         */
        init: function(selector, customOptions = {}) {
            const _this = this;
            const $elements = $(selector);

            if ($elements.length === 0) return;

            $elements.each(function() {
                const $el = $(this);

                // 클래스에 따라 month 또는 year 타입 결정
                const type = $el.hasClass('yearpick') ? 'year' : 'month';
                const defaultOptions = _this.getDefaultOptions(type);

                // Air Datepicker의 onHide를 jQuery UI의 onClose처럼 동작하게 매핑
                const options = $.extend(true, {}, defaultOptions, customOptions);

                if (typeof customOptions.onClose === 'function') {
                    options.onHide = function(isFinished) {
                        // 창이 완전히 닫히는 시점(isFinished: true)에 콜백 실행
                        if (isFinished) {
                            customOptions.onClose.call($el[0], $el.val());
                        }
                    };
                }

                // 기존 인스턴스가 있다면 파괴 후 재생성
                if (this._airDatepicker) {
                    this._airDatepicker.destroy();
                }
                this._airDatepicker = new AirDatepicker(this, options);
            });
        },

        /**
         * 페이지 로드 시 클래스 기반 자동 바인딩
         */
        bindEvents: function() {
            // 월 선택기 자동 바인딩
            this.init('.monthpick');

            // 연도 선택기 자동 바인딩
            this.init('.yearpick');
        }
    }
}

$(function() {
    uiCommon.initEvent();
    uiCommon.datepicker.bindEvents();
    uiCommon.monthpicker.bindEvents();
    uiCommon.updateTabAccessibility();
});