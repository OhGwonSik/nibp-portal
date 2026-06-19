const menu = {
    menuData: [],

    baseMenu: ['/admin/main', '/admin/login'],

    async loadMenuData() {
        if (this.menuData.length > 0) {
            return this.menuData;
        }

        try {
            this.menuData = await API.get('/api/admin/menu/my_menu', {}, {showLoading: false});
            return this.menuData;
        } catch (error) {
            console.error('메뉴 로드 실패:', error);
            return [];
        }
    }
};

const sidebar = {

    /**
     * 초기 로드
     */
    async init() {
        await this.fnLoadMenu();
        this.initEvent();
    },

    /**
     * 메뉴 데이터 조회
     */
    async fnLoadMenu() {
        const data = await menu.loadMenuData();
        this.renderSideBar(data);
    },

    /**
     * 현재 URL과 일치하는 메뉴 활성화
     */
    fnSetActiveMenu() {
        const currentPath = window.location.pathname;
        const currentSearch = window.location.search;
        const currentFullPath = currentPath + currentSearch;
        let foundMatch = false;
        let bestMatch = null;
        let bestMatchScore = 0; // 매칭 점수 (높을수록 정확한 매칭)

        // 모든 링크 검색
        $('#admin_sidebar a').each((index, element) => {
            const $a = $(element);
            const href = $a.attr('href');

            if (href && href !== 'javascript:;' && href !== '#none') {
                let matchScore = 0;

                // 1순위: 전체 경로 완전 일치 (쿼리스트링 포함)
                if (href === currentFullPath) {
                    matchScore = 100;
                }
                // 2순위: pathname 일치 + 쿼리스트링 부분 일치
                else {
                    const hrefPath = href.split('?')[0];
                    const hrefSearch = href.includes('?') ? href.split('?')[1] : '';

                    if (hrefPath === currentPath) {
                        matchScore = 50; // 기본 점수

                        // 쿼리스트링이 있는 경우 추가 점수
                        if (hrefSearch && currentSearch) {
                            const hrefParams = new URLSearchParams(hrefSearch);
                            const currentParams = new URLSearchParams(currentSearch);

                            // 공통 파라미터 개수만큼 점수 추가
                            let commonParams = 0;
                            hrefParams.forEach((value, key) => {
                                if (currentParams.get(key) === value) {
                                    commonParams++;
                                }
                            });
                            matchScore += commonParams * 10;
                        }
                    }
                }

                // 더 높은 점수의 매칭 발견 시 업데이트
                if (matchScore > bestMatchScore) {
                    bestMatchScore = matchScore;
                    bestMatch = $a;
                    foundMatch = true;
                }
            }
        });

        // 가장 잘 매칭된 메뉴 활성화
        if (foundMatch && bestMatch) {
            bestMatch.addClass('on');

            // 현재 링크의 가장 가까운 li 찾기
            const $currentLi = bestMatch.closest('li');

            // 1레벨 메뉴 찾기
            const $level1Li = bestMatch.closest('#admin_sidebar > ul > li');
            $level1Li.addClass('on').find('> ul').show();

            // 부모 체인을 따라가며 모든 부모 ul 열기
            let $tempLi = $currentLi;
            while ($tempLi.length > 0 && $tempLi[0] !== $level1Li[0]) {
                const $parentUl = $tempLi.parent();
                if ($parentUl.prop('tagName') === 'UL') {
                    $parentUl.show();
                }
                $tempLi = $parentUl.closest('li');
            }

            // 2레벨 메뉴인 경우 같은 1레벨의 모든 2레벨과 3레벨 메뉴 열기
            const $level2Ul = $currentLi.find('> ul');
            if ($level2Ul.length > 0) {
                // 같은 1레벨 아래의 모든 2레벨 메뉴 열기
                $level1Li.find('> ul > li > a').each(function() {
                    $(this).closest('li').find('> ul').show();
                });
            }

            const menuClass = $level1Li.attr('class')?.split(' ').find(cls => cls.startsWith('menu'));
            if (menuClass) {
                localStorage.setItem('activeLnbMenu', menuClass);
                localStorage.setItem('activeLnbUrl', bestMatch.attr('href'));
            }
        }

        // 정확한 경로 매칭이 없으면 저장된 정보로 메뉴 열기
        if (!foundMatch) {
            const savedMenuClass = localStorage.getItem('activeLnbMenu');
            const savedUrl = localStorage.getItem('activeLnbUrl');

            if (savedMenuClass && savedUrl) {
                const $activeLi = $(`#admin_sidebar .${savedMenuClass}`);
                if ($activeLi.length > 0) {
                    $activeLi.addClass('on').find('> ul').show();

                    // 저장된 URL과 일치하는 2레벨 메뉴 찾아서 그것의 3레벨만 열기
                    $activeLi.find('> ul > li > a').each(function() {
                        const $link = $(this);
                        if ($link.attr('href') === savedUrl) {
                            // 이 2레벨의 3레벨 메뉴 열기
                            const $level2Li = $link.closest('li');
                            const $level3Ul = $level2Li.find('> ul');
                            if ($level3Ul.length > 0) {
                                $level3Ul.show();
                            }
                        }
                    });
                }
            }
        }
    },

    /**
     * 메뉴 데이터 맵 생성
     */
    fnBuildMenuMap(menuData) {
        const menuMap = {};

        // 1레벨 메뉴 맵 생성
        menuData.forEach(menu => {
            if (menu.menuLv === 1 && menu.useYn === 'Y') {
                menuMap[menu.menuOid] = { ...menu, subMenus: [] };
            }
        });

        // 2레벨 메뉴 추가
        menuData.forEach(menu => {
            if (menu.useYn === 'Y' && menu.menuLv === 2 && menuMap[menu.upMenuOid]) {
                menuMap[menu.upMenuOid].subMenus.push({ ...menu, subMenus: [] });
            }
        });

        // 3레벨 메뉴 추가
        menuData.forEach(menu => {
            if (menu.useYn === 'Y' && menu.menuLv === 3) {
                // 2레벨 메뉴 찾기
                for (const level1Menu of Object.values(menuMap)) {
                    const level2Menu = level1Menu.subMenus.find(m => m.menuOid === menu.upMenuOid);
                    if (level2Menu) {
                        level2Menu.subMenus.push(menu);
                        break;
                    }
                }
            }
        });

        // 1레벨 메뉴 배열 생성 및 정렬
        return Object.values(menuMap).sort((a, b) => a.menuSeq - b.menuSeq);
    },

    /**
     * 1레벨 메뉴 요소 생성
     */
    fnCreateMenuItem(menu, index) {
        const menuClass = `menu${String(index + 1).padStart(2, '0')}`;
        const $li = $(`<li class="${menuClass}"></li>`);

        const menuLink = menu.menuUrl && menu.subMenus.length === 0 ? menu.menuUrl : 'javascript:;';
        const $a = $(`<a href="${menuLink}"><span>${menu.menuNm}</span></a>`);
        $li.append($a);

        if (menu.subMenus.length > 0) {
            const $subUl = this.fnCreateSubMenu(menu.subMenus);
            $li.append($subUl);
        }

        return $li;
    },

    /**
     * 2레벨 메뉴 요소 생성
     */
    fnCreateSubMenu(subMenus) {
        const $subUl = $('<ul style="display:none;"></ul>');

        subMenus.sort((a, b) => a.menuSeq - b.menuSeq).forEach(subMenu => {
            const menuLink = subMenu.menuUrl || '#none';
            const $subLi = $(`<li><a href="${menuLink}"><span>${subMenu.menuNm}</span></a></li>`);

            // 3레벨 메뉴가 있으면 추가
            if (subMenu.subMenus && subMenu.subMenus.length > 0) {
                const $thirdUl = this.fnCreateThirdLevelMenu(subMenu.subMenus);
                $subLi.append($thirdUl);
            }

            $subUl.append($subLi);
        });

        return $subUl;
    },

    /**
     * 3레벨 메뉴 요소 생성
     */
    fnCreateThirdLevelMenu(thirdMenus) {
        const $thirdUl = $('<ul style="display:none;"></ul>');

        thirdMenus.sort((a, b) => a.menuSeq - b.menuSeq).forEach(thirdMenu => {
            const menuLink = thirdMenu.menuUrl || '#none';
            const $thirdLi = $(`<li><a href="${menuLink}"><span>${thirdMenu.menuNm}</span></a></li>`);
            $thirdUl.append($thirdLi);
        });

        return $thirdUl;
    },

    /**
     * 메뉴 생성
     */
    renderSideBar(menuData) {
        const $sidebar = $('#admin_sidebar > ul');

        if ($sidebar.length === 0) {
            console.error('사이드바 요소를 찾을 수 없습니다.');
            return;
        }

        $sidebar.empty();

        const menuMap = this.fnBuildMenuMap(menuData);

        menuMap.forEach((menu, index) => {
            const $menuItem = this.fnCreateMenuItem(menu, index);
            $sidebar.append($menuItem);
        });

        this.fnSetActiveMenu();
        this.initMenuToggle();
    },

    /**
     * 메뉴 클릭 토글 이벤트 초기화
     */
    initMenuToggle() {
        $('#admin_sidebar').on('click', 'a', function(e) {
            const $a = $(this);
            const $li = $a.closest('li');
            const $subUl = $li.find('> ul');

            // 하위 메뉴가 있는 경우
            if ($subUl.length > 0) {
                // 링크가 'javascript:;' 또는 '#none'인 경우만 토글
                const href = $a.attr('href');
                if (href === 'javascript:;' || href === '#none') {
                    e.preventDefault();
                    $subUl.slideToggle(100);
                } else {
                    // 실제 URL이 있는 경우 서브메뉴 열기만 함
                    $subUl.slideDown(100);
                }
            }
        });
    },

    initEvent() {
        document.getElementById("admin_sidebar_search").addEventListener("keyup", function (e) {
            sidebar.search();
        });

        document.getElementById("admin_sidebar_search_icon").addEventListener("click", function (e) {
            sidebar.search();
        });
    },

    search() {
        const menuDom = document.getElementById("admin_sidebar");
        const menuList = menuDom.querySelectorAll("nav#admin_sidebar ul > li");
        const searchValue = document.getElementById("admin_sidebar_search").value;
        
        menuList.forEach(menu => {
            const menuText = menu.textContent.toLowerCase();
            if (menuText.includes(searchValue.toLowerCase())) {
                menu.style.display = "block";
            } else {
                menu.style.display = "none";
            }
        });
    }
};

const location_menu = {

    /**
     * 초기 로드
     */
    async init() {
        await this.fnLoadMenu();
    },

    /**
     * 메뉴 데이터 조회
     */
    async fnLoadMenu() {
        const data = await menu.loadMenuData();
        // this.fnSetLocation(data);
    },

    /**
     * 경로 생성
     */
    fnSetLocation(menuData) {
        const currentPath = window.location.pathname;
        const $locationUl = $('#admin_location');

        if ($locationUl.length === 0) {
            console.error('location 요소를 찾을 수 없습니다.');
            return;
        }

        // 홈 제외하고 초기화
        $locationUl.find('li:not(.home)').remove();

        // 현재 경로와 일치하는 메뉴 찾기
        let currentMenu = menuData.find(menu => menu.menuUrl === currentPath);

        // 메뉴 조회 결과가 없거나 기본 메뉴가 아닌 경우 3레벨 메뉴로 간주
        if (!currentMenu && !menu.baseMenu.includes(currentPath)) {
            // URL에서 페이지 ID 추출 (예: /admin/page/admin502-1 → admin502-1)
            const pageId = currentPath.split('/').pop();

            // - 기준으로 부모 페이지 ID 추출 (예: admin502-1 → admin502)
            if (pageId.includes('-')) {
                const parentPageId = pageId.split('-')[0];

                // 부모 메뉴 URL 구성
                const parentPath = currentPath.substring(0, currentPath.lastIndexOf('/') + 1) + parentPageId;

                // 부모 메뉴 찾기
                const parentMenu = menuData.find(menu => menu.menuUrl === parentPath);

                if (parentMenu) {
                    // 부모 메뉴 breadcrumb 생성
                    const locations = this.fnBuildLocations(parentMenu, menuData);

                    // 현재 페이지 정보 추가
                    const currentPageTitle = this.fnGetCurrentPageTitle();
                    locations.push({
                        name: currentPageTitle,
                        url: currentPath,
                        level: 3
                    });

                    this.renderLocation(locations);
                    return;
                }
            }

            return;
        }

        // 부모 메뉴들을 찾아서 breadcrumb 생성
        const locations = this.fnBuildLocations(currentMenu, menuData);
        this.renderLocation(locations);
    },

    /**
     * 현재 페이지 제목 가져오기 (title 태그에서 추출)
     */
    fnGetCurrentPageTitle() {
        const titleTag = document.title;
        if (titleTag && titleTag.includes('>')) {
            const titleParts = titleTag.split('>');
            return titleParts.pop().trim();
        }
        return '상세정보';
    },

    /**
     * location 요소 만들기
     */
    fnBuildLocations(currentMenu, menuData) {
        const locations = [];
        let menu = currentMenu;

        // 현재 메뉴부터 최상위 부모까지 역순으로 추적
        while (menu) {
            locations.unshift({
                name: menu.menuNm,
                url: menu.menuUrl,
                level: menu.menuLv
            });

            // 부모 메뉴 찾기
            if (menu.upMenuOid) {
                menu = menuData.find(m => m.menuOid === menu.upMenuOid);
            } else {
                menu = null;
            }
        }

        return locations;
    },

    /**
     * location 요소 생성
     */
    renderLocation(locations) {
        const $locationUl = $('#admin_location');

        locations.forEach((item, index) => {
            const isLast = index === locations.length - 1;

            if (isLast) {
                // 마지막 항목(현재 페이지)은 링크 없이
                $locationUl.append(`<li>${item.name}</li>`);
            } else {
                // 부모 메뉴는 링크 포함
                const href = item.url || 'javascript:;';
                $locationUl.append(`<li><a href="${href}">${item.name}</a></li>`);
            }
        });
    }
};

$(async function() {
    await sidebar.init();
    await location_menu.init();
});