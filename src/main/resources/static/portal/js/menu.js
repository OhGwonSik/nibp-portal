let menu = {

    /**
     * 상태값
     * - 서버에서 조회한 메뉴 목록을 보관
     */
    state: {
        menus: []
    },

    /**
     * 1depth 메뉴별 영문명 매핑
     * - menuNm을 기준으로 매핑
     */
    depth1Config: {
        'KBPMENU100': {
            enName: 'Introduction'
        },
        'KBPMENU200': {
            enName: 'Business'
        },
        'KBPMENU300': {
            enName: 'Notices'
        },
        'KBPMENU400': {
            enName: 'Information'
        },
        'KBPMENU500': {
            enName: 'Data Room'
        },
        'KBPMENU600': {
            enName: 'Communication'
        }
    },






    /**
     * 초기 진입 시점
     * - 공통 이벤트 바인딩
     * - 메뉴 데이터 로드
     */
    init: function () {
        this.initEvent();
        this.fnLoadMenu();
    },

    /**
     * 공통 이벤트 바인딩
     * - 전체메뉴(사이트맵) 열기 / 닫기 버튼 처리
     */
    initEvent: function () {
        // 전체메뉴 열기
        document.addEventListener('click', function (e) {
            const openBtn = e.target.closest('.all_menu > a');
            if (openBtn) {
                e.preventDefault();
                const layer = document.querySelector('.all_menu_layer');
                if (layer) layer.style.display = 'block';
            }
        });

        // 전체메뉴 닫기
        document.addEventListener('click', function (e) {
            const closeBtn = e.target.closest('.close_sitemap');
            if (closeBtn) {
                e.preventDefault();
                const layer = document.querySelector('.all_menu_layer');
                if (layer) layer.style.display = 'none';
            }
        });
    },

    /**
     * 메뉴 데이터 조회
     * - /api/common/menu 호출
     * - ApiResponse 래핑/배열 리턴 모두 대응
     * - 조회 후 GNB / 사이트맵 렌더링
     */
    fnLoadMenu: async function () {
        try {
            const res = await API.get('/api/common/menu', {} ,{showLoading: false});

            const menus = Array.isArray(res)
                ? res
                : (res && res.data)
                    ? res.data
                    : [];

            this.state.menus = menus || [];

            this.fnRenderGnb();
            this.fnRenderSitemap();
            this.fnRenderSubNav();
            this.fnSetActiveMenuByUrl();


        } catch (error) {
            console.error('헤더 메뉴 조회 실패', error);
            if (window.uiCommon && uiCommon.fnShowAlertModal) {
                uiCommon.fnShowAlertModal('메뉴 정보를 불러오는데 실패했습니다.');
            } else {
                uiCommon.fnShowAlertModal('메뉴 정보를 불러오는데 실패했습니다.');
            }
        }
    },

    /**
     * 메뉴 트리 구성
     * - depth1 (1뎁스 대메뉴) 목록
     * - childrenMap (parentMenuNo → 자식 메뉴 배열)
     * - menuType=HIDDEN이 아니고 useYn = Y 인 것만 노출
     */
    buildTree: function (skipMenuType = false) {
        const menus = this.state.menus || [];
        // camelCase 필드 기준 유틸
        const getLevel = (m) => Number(m.menuLv || 0);
        const getParentNo = (m) => m.upMenuOid || null;
        const getOrder = (m) => Number(m.menuSeq || 0);
        const isVisible = (m) => {
            const type = String(m.menuType || '').toUpperCase();
            const use = String(m.useYn || 'Y').toUpperCase();
            return (skipMenuType ? true : type !== 'HIDDEN') && use === 'Y';
        };

        // 1뎁스(대메뉴)만 필터링
        const depth1 = menus.filter(m =>
            getLevel(m) === 1 &&
            isVisible(m)
        );

        // parentMenuNo 기준 children 맵
        const childrenMap = {};
        menus.forEach(m => {
            if (!isVisible(m)) return;

            const parent = getParentNo(m);
            if (!parent) return;

            if (!childrenMap[parent]) {
                childrenMap[parent] = [];
            }
            childrenMap[parent].push(m);
        });

        // 메뉴 순서 정렬
        depth1.sort((a, b) => getOrder(a) - getOrder(b));
        Object.values(childrenMap).forEach(list =>
            list.sort((a, b) => getOrder(a) - getOrder(b))
        );

        return {depth1, childrenMap};
    },

    /**
     * 특정 depth부터 이후의 모든 서브 네비게이션 컨테이너 비우기
     * @param {number} startDepth - 시작 depth (이 depth부터 비움)
     */
    clearDepthContainersFrom: function(startDepth) {
        let depth = startDepth;
        while (true) {
            const container = document.getElementById(`sub_nav_depth${depth}`);
            if (!container) break;
            container.innerHTML = '';
            depth++;
        }
    },

    /**
     * 재귀적으로 메뉴 트리 렌더링 (GNB/Sitemap용)
     * @param {Object} menu - 현재 메뉴 객체
     * @param {Object} childrenMap - 자식 메뉴 맵
     * @returns {string} HTML 문자열
     */
    renderMenuRecursive: function(menu, childrenMap) {
        const children = childrenMap[menu.menuOid] || [];

        let html = `<li>`;

        // npagYn이 'Y'인 경우 open_window 클래스 및 target, title 추가
        const isNewWindow = String(menu.npagYn || '').toUpperCase() === 'Y';
        const linkClass = isNewWindow ? ' class="open_window"' : '';
        const linkTarget = isNewWindow ? ' target="_blank" title="새창열림"' : '';

        html += `<a href="${menu.menuUrl || 'javascript:;'}"${linkClass}${linkTarget}>${menu.menuNm}</a>`;

        // 자식이 있으면 재귀적으로 렌더링
        if (children.length > 0) {
            html += `<ul>`;
            children.forEach(child => {
                html += this.renderMenuRecursive(child, childrenMap);
            });
            html += `</ul>`;
        }

        html += `</li>`;
        return html;
    },

    /**
     * 상단 GNB 영역 렌더링 (재귀 버전)
     * - #gnbMenu 기준으로 동적 HTML 구성
     * - depth 제한 없이 재귀적으로 렌더링
     * - 대메뉴 hover 시 하위 메뉴 열고, GNB 밖으로 나가면 닫기 처리
     */
    fnRenderGnb: function () {
        const gnbUl = document.getElementById('gnbMenu');
        if (!gnbUl) return;

        const {depth1, childrenMap} = this.buildTree();
        if (!depth1.length) {
            gnbUl.innerHTML = '';
            return;
        }

        let html = '';

        depth1.forEach((l1, idx) => {
            const menuClass = 'menu' + String(idx + 1).padStart(2, '0');
            const children = childrenMap[l1.menuOid] || [];

            // 영문명 및 이미지 정보 가져오기
            const config = this.depth1Config[l1.menuCd] || {};
            const enName = config.enName || '';

            // npagYn이 'Y'인 경우 open_window 클래스 및 target, title 추가
            const isNewWindow = String(l1.npagYn || '').toUpperCase() === 'Y';
            const linkClass = isNewWindow ? ' class="open_window"' : '';
            const linkTarget = isNewWindow ? ' target="_blank" title="새창열림"' : '';

            html += `
            <li class="${menuClass}">
                <a href="${l1.menuUrl || 'javascript:;'}"${linkClass}${linkTarget}>${l1.menuNm}</a>
        `;

            if (children.length > 0) {
                html += `
                <div class="depth_2" style="display:none;">
                    <div class="depth_2_in">
            `;

                // depth_2_tit 추가 (영문명과 이미지가 있을 때만)
                if (enName) {
                    html += `
                        <div class="depth_2_tit">
                            <strong>${enName}</strong>
                        </div>
                    `;
                }

                html += `
                        <ul>
                `;
                children.forEach(child => {
                    html += this.renderMenuRecursive(child, childrenMap);
                });
                html += `
                        </ul>
                    </div>
                </div>
            `;
            }

            html += `</li>`;
        });

        gnbUl.innerHTML = html;

        /**
         * depth_2 열고 닫기 제어
         * - 대메뉴(li)에 마우스 올리면 해당 depth_2만 열기
         * - 키보드 포커스 시에도 해당 depth_2 열기
         * - gnb_wrap 영역을 벗어나면 전체 depth_2 닫기
         */

            // 모든 depth_2를 숨기는 유틸
        const hideAllDepth2 = () => {
                document.querySelectorAll('.gnb_wrap .depth_2').forEach(d => {
                    d.style.display = 'none';
                });
            };

        // 모든 1depth li에서 on 클래스를 제거하는 유틸
        const removeAllOn = () => {
            document.querySelectorAll('.gnb_wrap > ul > li').forEach(item => {
                item.classList.remove('on');
            });
        };

        // 하위 메뉴를 여는 공통 함수
        const showDepth2 = (li) => {
            const depth2 = li.querySelector('.depth_2');
            hideAllDepth2(); // 다른 메뉴들 닫기
            removeAllOn(); // 다른 메뉴 on 제거
            li.classList.add('on'); // 현재 메뉴에 on 추가
            if (!depth2) {
                // 하위 메뉴가 없는 경우, 열려 있던 다른 메뉴가 닫히기만 함.
                return;
            }
            depth2.style.display = 'block'; // 현재 것만 열기
        };

        // GNB 전체 영역에서 마우스가 벗어나면 모두 닫기
        const gnbWrap = document.querySelector('.gnb_wrap');
        if (gnbWrap) {
            gnbWrap.addEventListener('mouseleave', () => {
                hideAllDepth2();
                removeAllOn();
            });
        }

        // 각 대메뉴(li)에 마우스 및 키보드 이벤트 바인딩
        document.querySelectorAll('.gnb_wrap > ul > li').forEach(li => {
            // 1. 마우스 호버 이벤트
            li.addEventListener('mouseenter', () => {
                showDepth2(li);
            });

            // --- 키보드 접근성 처리 ---

            // 2. 대메뉴 링크에 포커스가 오면 하위 메뉴를 연다.
            const topLink = li.querySelector('a');
            if (topLink) {
                topLink.addEventListener('focus', () => {
                    showDepth2(li);
                });
            }

            const depth2 = li.querySelector('.depth_2');
            if (!depth2) {
                return; // 하위 메뉴 없으면 키보드 관련 추가 처리 불필요
            }

            // 3. (핵심 수정) 하위 메뉴의 링크에 포커스가 올 경우 (Shift+Tab으로 진입 시)
            //    현재 메뉴를 열린 상태로 유지하고 다른 메뉴는 닫는다.
            depth2.querySelectorAll('a').forEach(subLink => {
                subLink.addEventListener('focus', () => {
                    showDepth2(li);
                });
            });

            // 4. 하위 메뉴의 마지막 링크에서 Tab 키로 나갈 경우, 모든 메뉴를 닫는다.
            const allLinks = depth2.querySelectorAll('a');
            const lastLink = allLinks[allLinks.length - 1];
            if (lastLink) {
                lastLink.addEventListener('keydown', (e) => {
                    if (e.key === 'Tab' && !e.shiftKey) {
                        hideAllDepth2();
                    }
                });
            }
        });
    },

    /**
     * 전체메뉴(사이트맵) 렌더링 (재귀 버전)
     * - 헤더 우측 "전체메뉴" 레이어 내부 #sitemapMenu 구성
     * - depth 제한 없이 재귀적으로 렌더링
     */
    fnRenderSitemap: function () {
        const sitemapUl = document.getElementById('sitemapMenu');
        if (!sitemapUl) return;

        const {depth1, childrenMap} = this.buildTree();
        if (!depth1.length) {
            sitemapUl.innerHTML = '';
            return;
        }

        let html = '';
        depth1.forEach(menu => {
            html += this.renderMenuRecursive(menu, childrenMap);
        });

        sitemapUl.innerHTML = html;
    },

    /**
     * 서브 네비게이션 렌더링(파란색)
     */
    fnRenderSubNav: function () {
        const _this = this;

        const depth1Container = document.getElementById('sub_nav_depth1');
        const depth2Container = document.getElementById('sub_nav_depth2');
        const depth3Container = document.getElementById('sub_nav_depth3');

        if (!depth1Container || !depth2Container || !depth3Container) return;

        const {depth1, childrenMap} = this.buildTree();
        if (!depth1.length) {
            depth1Container.innerHTML = '';
            depth2Container.innerHTML = '';
            depth3Container.innerHTML = '';
            return;
        }

        // sessionStorage에서 마지막 선택 메뉴 루트 확인
        let defaultDepth1Idx = 0;
        const lastDepth1MenuOid = sessionStorage.getItem('lastActiveDepth1MenuOid');
        if (lastDepth1MenuOid) {
            const savedIdx = depth1.findIndex(m => String(m.menuOid) === lastDepth1MenuOid);
            if (savedIdx >= 0) {
                defaultDepth1Idx = savedIdx;
            }
        }

        const defaultMenu = depth1[defaultDepth1Idx];

        // 1뎁스 렌더링 - 모든 대메뉴 표시
        let depth1Html = '';

        // 기본 메뉴의 npagYn 확인
        const defaultIsNewWindow = String(defaultMenu.npagYn || '').toUpperCase() === 'Y';
        const defaultLinkClass = defaultIsNewWindow ? ' class="open_window"' : '';
        const defaultLinkTarget = defaultIsNewWindow ? ' target="_blank" title="새창열림"' : '';

        depth1Html += `
        <a href="${defaultMenu.menuUrl || 'javascript:;'}" aria-label="메뉴 열기" data-menu-oid="${defaultMenu.menuOid}" data-depth1-idx="${defaultDepth1Idx}"${defaultLinkClass}${defaultLinkTarget}>${defaultMenu.menuNm}</a>
        <ul style="display:none;">
    `;

        depth1.forEach((menu, idx) => {
            // npagYn이 'Y'인 경우 open_window 클래스 및 target, title 추가
            const isNewWindow = String(menu.npagYn || '').toUpperCase() === 'Y';
            const linkClass = isNewWindow ? ' class="open_window"' : '';
            const linkTarget = isNewWindow ? ' target="_blank" title="새창열림"' : '';

            depth1Html += `<li><a href="${menu.menuUrl || 'javascript:;'}" data-menu-oid="${menu.menuOid}" data-depth1-idx="${idx}"${linkClass}${linkTarget}>${menu.menuNm}</a></li>`;
        });

        depth1Html += `</ul>`;

        depth1Container.innerHTML = depth1Html;

        // 마지막 메뉴에서 Tab 시 닫기
        const d1Links = depth1Container.querySelectorAll('ul li a');
        const lastD1Link = d1Links[d1Links.length - 1];
        if (lastD1Link) {
            lastD1Link.addEventListener('keydown', function(e) {
                if (e.key === 'Tab' && !e.shiftKey) {
                    $(depth1Container.querySelector('ul')).slideUp();
                }
            });
        }

        // 메뉴 전체에서 포커스가 완전히 나갈 때 닫기
        depth1Container.addEventListener('focusout', function(e) {
            if (!depth1Container.contains(e.relatedTarget)) {
                $(depth1Container.querySelector('ul')).slideUp();
            }
        });

        // 1뎁스 ul 내 메뉴 클릭 이벤트
        depth1Container.querySelectorAll('ul li a').forEach(link => {
            link.addEventListener('click', function(e) {
                e.preventDefault();

                const menuOid = this.getAttribute('data-menu-oid');
                const depth1Idx = this.getAttribute('data-depth1-idx');
                const menuNm = this.textContent;
                const menuUrl = this.getAttribute('href');

                const depth1Link = depth1Container.querySelector('a');
                if (depth1Link) {
                    depth1Link.textContent = menuNm;
                    depth1Link.setAttribute('href', menuUrl || 'javascript:;');
                    depth1Link.setAttribute('data-menu-oid', menuOid);
                    depth1Link.setAttribute('data-depth1-idx', depth1Idx);
                }

                $(depth1Container.querySelector('ul')).slideUp();

                if (menuUrl && menuUrl !== 'javascript:;') {
                    window.location.href = menuUrl;
                    return;
                }

                // URL 없고 하위 메뉴 있을 때만 렌더링
                const {childrenMap} = _this.buildTree();
                if (childrenMap[menuOid] && childrenMap[menuOid].length > 0) {
                    _this.fnRenderSubNavDepth2(menuOid, depth1Idx);
                } else {
                    depth2Container.innerHTML = '';
                    depth3Container.innerHTML = '';
                }
            });
        });



        // 기본 메뉴의 하위 메뉴 자동 렌더링
        if (defaultMenu && childrenMap[defaultMenu.menuOid] && childrenMap[defaultMenu.menuOid].length > 0) {
            _this.fnRenderSubNavDepth2(defaultMenu.menuOid, defaultDepth1Idx);
        } else {
            // 하위 메뉴가 없으면 비움
            depth2Container.innerHTML = '';
            depth3Container.innerHTML = '';
        }

        _this.bindSubNavToggleEvent();
    },

    /**
     * 서브 네비게이션 depth별 렌더링 (동적 depth 버전)
     * @param {string|number} parentMenuOid - 부모 메뉴 번호
     * @param {number} depth - 현재 depth (2, 3, 4, ...)
     * @param {object} indices - depth별 인덱스 정보 {depth1Idx, depth2Idx, ...}
     */
    fnRenderSubNavDepth: function(parentMenuOid, depth, indices = {}) {
        const _this = this;
        const containerId = `sub_nav_depth${depth}`;
        const container = document.getElementById(containerId);

        if (!container) return;

        const {childrenMap} = this.buildTree();
        // expsrYn=N 메뉴 제외
        const menuList = (childrenMap[parentMenuOid] || []).filter(m => {
            const show = String(m.expsrYn || 'Y').toUpperCase();
            return show !== 'N';
        });

        if (menuList.length === 0) {
            container.innerHTML = '';
            // 다음 depth들도 비우기
            this.clearDepthContainersFrom(depth + 1);
            return;
        }

        // sessionStorage에서 이 depth의 마지막 선택 메뉴 확인
        let defaultMenuIdx = 0;
        const savedDepthMenuOid = sessionStorage.getItem(`lastActiveDepth${depth}MenuOid`);
        if (savedDepthMenuOid) {
            const savedIdx = menuList.findIndex(m => String(m.menuOid) === savedDepthMenuOid);
            if (savedIdx >= 0) {
                defaultMenuIdx = savedIdx;
            }
        }

        const firstMenu = menuList[defaultMenuIdx];

        // data attributes 생성
        let dataAttrs = '';
        Object.keys(indices).forEach(key => {
            dataAttrs += ` data-${key}="${indices[key]}"`;
        });
        dataAttrs += ` data-depth${depth}-idx="${defaultMenuIdx}"`;

        // npagYn이 'Y'인 경우 open_window 클래스 및 target, title 추가
        const isNewWindow = String(firstMenu.npagYn || '').toUpperCase() === 'Y';
        const linkClass = isNewWindow ? ' class="open_window"' : '';
        const linkTarget = isNewWindow ? ' target="_blank" title="새창열림"' : '';

        let html = `
            <div style="display: block;">
                <a href="javascript:;" aria-label="메뉴 열기" data-menu-oid="${firstMenu.menuOid}"${dataAttrs}${linkClass}${linkTarget}>
                    ${firstMenu.menuNm}
                </a>
                <ul style="display:none;">
        `;

        menuList.forEach((menu, idx) => {
            let itemDataAttrs = '';
            Object.keys(indices).forEach(key => {
                itemDataAttrs += ` data-${key}="${indices[key]}"`;
            });
            itemDataAttrs += ` data-depth${depth}-idx="${idx}"`;

            // npagYn이 'Y'인 경우 open_window 클래스 및 target, title 추가
            const isNewWindow = String(menu.npagYn || '').toUpperCase() === 'Y';
            const linkClass = isNewWindow ? ' class="open_window"' : '';
            const linkTarget = isNewWindow ? ' target="_blank" title="새창열림"' : '';

            html += `
                <li>
                    <a href="${menu.menuUrl || 'javascript:;'}"
                       data-menu-oid="${menu.menuOid}"${itemDataAttrs}${linkClass}${linkTarget}>
                        ${menu.menuNm}
                    </a>
                </li>
            `;
        });

        html += `</ul></div>`;
        container.innerHTML = html;

        // 마지막 메뉴에서 Tab 시 닫기
        const links = container.querySelectorAll('ul li a');
        const lastLink = links[links.length - 1];
        if (lastLink) {
            lastLink.addEventListener('keydown', function(e) {
                if (e.key === 'Tab' && !e.shiftKey) {
                    $(container.querySelector('ul')).slideUp();
                }
            });
        }

        // 메뉴 전체에서 포커스가 완전히 나갈 때 닫기
        container.addEventListener('focusout', function(e) {
            if (!container.contains(e.relatedTarget)) {
                $(container.querySelector('ul')).slideUp();
            }
        });

        // ul 내 메뉴 클릭 이벤트
        container.querySelectorAll('ul li a').forEach(link => {
            link.addEventListener('click', function(e) {
                e.preventDefault();

                const menuOid = this.getAttribute('data-menu-oid');
                const menuNm = this.textContent;
                const menuUrl = this.getAttribute('href');

                // 현재 depth의 인덱스들 수집
                const newIndices = {};
                for (let i = 1; i <= depth; i++) {
                    const idx = this.getAttribute(`data-depth${i}-idx`);
                    if (idx !== null) {
                        newIndices[`depth${i}Idx`] = idx;
                    }
                }

                const depthLink = container.querySelector('div > a');
                if (depthLink) {
                    depthLink.textContent = menuNm;
                    depthLink.setAttribute('href', menuUrl || 'javascript:;');
                    depthLink.setAttribute('data-menu-oid', menuOid);

                    // data attributes 업데이트
                    Object.keys(newIndices).forEach(key => {
                        depthLink.setAttribute(`data-${key}`, newIndices[key]);
                    });
                }

                $(container.querySelector('ul')).slideUp();

                if (menuUrl && menuUrl !== 'javascript:;') {
                    window.location.href = menuUrl;
                    return;
                }

                const {childrenMap} = _this.buildTree();
                if (childrenMap[menuOid] && childrenMap[menuOid].length > 0) {
                    _this.fnRenderSubNavDepth(menuOid, depth + 1, newIndices);
                } else {
                    // 하위 메뉴 없으면 다음 depth들 비우기
                    _this.clearDepthContainersFrom(depth + 1);
                }
            });
        });

        // 기본 메뉴의 하위 메뉴 자동 렌더링
        if (firstMenu && childrenMap[firstMenu.menuOid] && childrenMap[firstMenu.menuOid].length > 0) {
            const newIndices = {...indices};
            newIndices[`depth${depth}Idx`] = defaultMenuIdx;
            _this.fnRenderSubNavDepth(firstMenu.menuOid, depth + 1, newIndices);
        } else {
            // 하위 메뉴가 없으면 다음 depth들 비우기
            _this.clearDepthContainersFrom(depth + 1);
        }

        _this.bindSubNavToggleEvent();
    },

    /**
     * 2뎁스 렌더링 (하위 호환성 유지)
     */
    fnRenderSubNavDepth2: function(parentMenuOid, depth1Idx) {
        this.fnRenderSubNavDepth(parentMenuOid, 2, {depth1Idx});
    },

    /**
     * 3뎁스 렌더링 (하위 호환성 유지)
     */
    fnRenderSubNavDepth3: function(parentMenuOid, depth1Idx, depth2Idx) {
        this.fnRenderSubNavDepth(parentMenuOid, 3, {depth1Idx, depth2Idx});
    },

    /**
     * 서브메뉴 토글 이벤트 바인딩
     */
    bindSubNavToggleEvent: function() {
        const selector = '#sub_nav_depth1 > a, #sub_nav_depth2 > div > a, #sub_nav_depth3 > div > a';

        // 기존 이벤트 제거 (중복 방지)
        $(selector).off('click');

        // 메뉴 열기/닫기
        $(selector).on('click', function (e) {
            e.preventDefault();

            const $ul = $(this).next('ul');

            // 다른 메뉴 닫기
            $('.sub_nav ul').not($ul).slideUp();

            // 현재 메뉴만 토글
            $ul.slideToggle();
        });
    },

    /**
     * 페이지 로딩 후 현재 URL 기준으로 선택 메뉴 세팅 (동적 depth 버전)
     */
    fnSetActiveMenuByUrl: function () {
        const currentUrl = location.pathname + location.search;
        const { depth1, childrenMap } = this.buildTree(true);
        const flatList = this.state.menus;

        // 경로에서 "-숫자" 패턴 제거 (예: /page/portal604-2 -> /page/portal604)
        const normalizedPathname = location.pathname.replace(/-\d+$/, '');
        const normalizedUrl = normalizedPathname + location.search;

        // 1) 현재 Path와 menuUrl이 일치하는 가장 하위 메뉴 찾기
        const currentMenu = flatList
            .filter(m => {
                if (!m.menuUrl) {
                    return false;
                }

                // Split URL into pathname and query string
                const [menuPath, menuQuery] = m.menuUrl.split('?');
                const [currentPath, currentQuery] = normalizedUrl.split('?');

                // Pathname must match
                if (menuPath !== currentPath) {
                    return false;
                }

                // If menu has no query params, pathname match is enough
                if (!menuQuery) {
                    return true;
                }

                // If menu has query params, check if all of them exist in current URL
                const menuParams = new URLSearchParams(menuQuery);
                const currentParams = new URLSearchParams(currentQuery || '');

                for (const [key, value] of menuParams.entries()) {
                    if (currentParams.get(key) !== value) {
                        return false;
                    }
                }

                return true;
            })
            .sort((a, b) => {
                // Sort by number of query params (more specific first)
                const aParamCount = (a.menuUrl.split('?')[1] || '').split('&').filter(p => p).length;
                const bParamCount = (b.menuUrl.split('?')[1] || '').split('&').filter(p => p).length;

                if (bParamCount !== aParamCount) {
                    return bParamCount - aParamCount;
                }

                // Then by menu level (deeper first)
                return Number(b.menuLv || 0) - Number(a.menuLv || 0);
            })
            [0]; // 첫 번째 메뉴를 선택 (가장 구체적이고 하위인 메뉴)

        if (!currentMenu) {
            // 현재 URL과 일치하는 메뉴가 없는 경우
            // fnRenderSubNav에서 sessionStorage 기반으로 이미 기본 메뉴를 렌더링했으므로 추가 처리 불필요
            return;
        }

        // 2) 부모 메뉴들을 역추적하여 메뉴 체인 구성
        const menuChain = [];
        let currentItem = currentMenu;

        while (currentItem) {
            menuChain.unshift(currentItem); // 앞에 추가 (역순으로)
            if (currentItem.upMenuOid) {
                currentItem = flatList.find(m => m.menuOid === currentItem.upMenuOid);
            } else {
                break;
            }
        }

        if (menuChain.length === 0) return;

        // 마지막 선택 메뉴 체인을 sessionStorage에 저장
        for (let d = 0; d < menuChain.length && d < 3; d++) {
            sessionStorage.setItem(`lastActiveDepth${d + 1}MenuOid`, String(menuChain[d].menuOid));
        }
        // 체인에 없는 하위 depth는 제거
        for (let d = menuChain.length + 1; d <= 3; d++) {
            sessionStorage.removeItem(`lastActiveDepth${d}MenuOid`);
        }

        // 3) 각 depth별로 렌더링 및 선택값 반영
        const indices = {};

        for (let i = 0; i < menuChain.length; i++) {
            const currentDepth = i + 1;
            const menu = menuChain[i];
            const containerId = `sub_nav_depth${currentDepth}`;
            const container = document.getElementById(containerId);

            if (!container) break; // 컨테이너가 없으면 중단

            // depth1은 특별 처리
            if (currentDepth === 1) {
                const depth1Idx = depth1.findIndex(m => m.menuOid === menu.menuOid);

                if (depth1Idx < 0) break;

                indices.depth1Idx = depth1Idx;

                let depth1Link = document.querySelector('#sub_nav_depth1 > a');

                // expsrYn=N이면 다음 depth들 모두 비우고 중단
                if('N' === String(menuChain[i].expsrYn || 'Y').toUpperCase()){
                    this.clearDepthContainersFrom(2);
                    break;
                }

                // depth1Link가 없으면 생성
                if (!depth1Link && container) {
                    depth1Link = document.createElement('a');
                    depth1Link.href = 'javascript:;';
                    depth1Link.setAttribute('aria-label', '메뉴 열기');
                    container.insertBefore(depth1Link, container.firstChild);
                }

                if (depth1Link) {
                    depth1Link.textContent = menu.menuNm;
                    depth1Link.href = currentUrl || 'javascript:;';
                    depth1Link.dataset.menuOid = menu.menuOid;
                    depth1Link.dataset.depth1Idx = depth1Idx;

                    // npagYn이 'Y'인 경우 open_window 클래스 및 target, title 추가
                    const isNewWindow = String(menu.npagYn || '').toUpperCase() === 'Y';
                    if (isNewWindow) {
                        depth1Link.classList.add('open_window');
                        depth1Link.setAttribute('target', '_blank');
                        depth1Link.setAttribute('title', '새창열림');
                    } else {
                        depth1Link.classList.remove('open_window');
                        depth1Link.removeAttribute('target');
                        depth1Link.removeAttribute('title');
                    }

                    // 다음 depth 렌더링 (다음 메뉴가 있을 때만)
                    if (i < menuChain.length - 1) {
                        this.fnRenderSubNavDepth(menu.menuOid, 2, {depth1Idx});
                    } else {
                        // 1depth만 있으면 2depth 이상 모두 비우기
                        this.clearDepthContainersFrom(2);
                    }
                }
            } else {
                // depth2 이상
                const parentMenu = menuChain[i - 1];
                const siblingList = childrenMap[parentMenu.menuOid] || [];
                const menuIdx = siblingList.findIndex(m => m.menuOid === menu.menuOid);

                if (menuIdx < 0) break;
                indices[`depth${currentDepth}Idx`] = menuIdx;

                let depthLink = document.querySelector(`#sub_nav_depth${currentDepth} > div > a`);

                // expsrYn=N이면 다음 depth들 모두 비우고 중단
                if('N' === String(menuChain[i].expsrYn || 'Y').toUpperCase()){
                    this.clearDepthContainersFrom(currentDepth + 1);
                    break;
                }

                // depthLink가 없으면 생성
                if (!depthLink && container) {
                    const div = document.createElement('div');
                    div.style.display = 'block';

                    depthLink = document.createElement('a');
                    depthLink.href = 'javascript:;';
                    depthLink.setAttribute('aria-label', '메뉴 열기');

                    div.appendChild(depthLink);
                    container.appendChild(div);
                }

                if (depthLink) {
                    depthLink.textContent = menu.menuNm;
                    depthLink.href = currentUrl || 'javascript:;';
                    depthLink.dataset.menuOid = menu.menuOid;

                    // data attributes 설정
                    Object.keys(indices).forEach(key => {
                        const idxValue = indices[key];
                        depthLink.setAttribute(`data-${key}`, idxValue);
                    });

                    // npagYn이 'Y'인 경우 open_window 클래스 및 target, title 추가
                    const isNewWindow = String(menu.npagYn || '').toUpperCase() === 'Y';
                    if (isNewWindow) {
                        depthLink.classList.add('open_window');
                        depthLink.setAttribute('target', '_blank');
                        depthLink.setAttribute('title', '새창열림');
                    } else {
                        depthLink.classList.remove('open_window');
                        depthLink.removeAttribute('target');
                        depthLink.removeAttribute('title');
                    }

                    // 다음 depth 렌더링 (다음 메뉴가 있을 때만)
                    if (i < menuChain.length - 1) {
                        this.fnRenderSubNavDepth(menu.menuOid, currentDepth + 1, {...indices});
                    } else {
                        // 마지막 depth면 다음 depth들 모두 비우기
                        this.clearDepthContainersFrom(currentDepth + 1);
                    }
                }
            }
        }
    },

};
document.addEventListener("DOMContentLoaded", function () {
    menu.init();
});