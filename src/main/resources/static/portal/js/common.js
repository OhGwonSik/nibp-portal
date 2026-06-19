//GNB
$(function () {
    const $items = $('.gnb_wrap > ul > li');
    const close = () => $items.removeClass('on').find('.depth_2').hide();

    // 클릭 시 아코디언 토글 (menu01~menu06 공통)
    $items.children('a').on('mouseenter focus click', function (e) {
        e.preventDefault();
        const $li = $(this).parent();

        if ($li.hasClass('on')) {
            // 이미 열려 있으면 닫기
            $li.removeClass('on').find('.depth_2').hide();
        } else {
            // 다른 메뉴들 닫고, 현재 메뉴만 열기
            close();
            $li.addClass('on').find('.depth_2').show();
        }
    });

    // 영역 벗어나면 전부 닫기
    $('.gnb_wrap').on('mouseleave', close);

    // menu06 depth_2 마지막 a에서 Tab → 모두 닫기 (접근성)
    $('.menu06 .depth_2 a:last').on('keydown', e => {
        if (e.key === 'Tab' && !e.shiftKey) close();
    });
});

// 서브메뉴
$(function () {
    const selector = '.sub_depth1 > a, .sub_depth2 > div > a, .sub_depth3 > div > a';

    // 메뉴 열기/닫기
    $(selector).on('click', function (e) {
        e.preventDefault();

        const $ul = $(this).next('ul');

        // 다른 메뉴 닫기
        $('.sub_nav ul').not($ul).slideUp();

        // 현재 메뉴만 토글
        $ul.slideToggle();
    });

    // 포커스가 sub_nav 영역을 벗어나면 닫기
    $('.sub_nav').on('focusout', function (e) {
        setTimeout(() => {
            // 새로 포커스된 요소
            const active = document.activeElement;

            // sub_nav 내부를 벗어난 경우만 닫기
            if (!$(active).closest('.sub_nav').length) {
                $('.sub_nav ul').slideUp();
            }
        }, 0);
    });

    // 마우스로 sub_nav 밖 클릭 시 닫기
    $(document).on('click', function (e) {
        if (!$(e.target).closest('.sub_nav').length) {
            $('.sub_nav ul').slideUp();
        }
    });
});

// 모바일 메뉴
$('.m_menu').click(function () {
    $(this).toggleClass('off');
    $('.gnb_wrap').slideToggle(function () {
        // gnb가 완전히 닫힌 경우
        if (!$(this).is(':visible')) {
            $('body').css('overflow', 'auto');
        }
    });

    // gnb 열릴 때는 스크롤 막기
    if (!$(this).hasClass('off')) {
        $('body').css('overflow', 'hidden');
    }
});

// 사이즈 초기화
$(window).resize(function () {
    $('.gnb_wrap').removeAttr("style");
    $('.m_menu').removeClass("off");
    $('body').css("overflow", "auto"); // 스크롤 복원
});

//검색영역
$(function () {

    // search 버튼 클릭 → 열기
    $('.search > a').on('click', function (e) {
        e.stopPropagation();
        $('.search_layer_wrap').slideDown(200);
    });

    // close_search 클릭/Enter → 닫고 포커스 다시 search 버튼으로
    $('.close_search').on('click keydown', function (e) {
        if (e.type === 'click' || e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            $('.search_layer_wrap').slideUp(200, function () {
                $('.search > a').focus();
            });
        }
    });

    // 레이어 내부 클릭 시 닫힘 방지
    $('.search_layer_wrap').on('click', function (e) {
        e.stopPropagation();
    });

});

// 전체메뉴
$(function () {

    // 전체메뉴 열기
    $('.all_menu > a').on('click', function (e) {
        e.stopPropagation();
        $(".all_menu_layer").css('display', 'flex');
        $('body').css("overflow", "hidden");
    });

    // 닫기 버튼
    $('.close_sitemap').on('click keydown', function (e) {

        // Enter 또는 Space로 닫은 경우
        if (e.type === 'click' || e.key === 'Enter' || e.key === ' ') {

            e.preventDefault();

            // 레이어 닫기
            $(".all_menu_layer").css('display', 'none');
            $('body').css("overflow", "auto");

            // 포커스를 다시 전체메뉴 버튼으로 이동
            $('.all_menu > a').focus();
        }
    });

});

//탭메뉴
$(function () {

    /* =========================================
       공통 탭 클릭 (main / press / history)
       ========================================= */
    $('.main_tab_wrap a, .press_tab_wrap a, .history_tab_wrap a').on('click', function (e) {
        e.preventDefault();

        // 현재 클릭된 탭이 속한 그룹(랩퍼) 찾기
        var $group = $(this).closest('.main_tab_wrap, .press_tab_wrap, .history_tab_wrap');

        // ▶ history_tab_wrap만 index 기반으로 처리
        if ($group.hasClass('history_tab_wrap')) {

            var $tabs = $group.find('a[class^="tab"]');                 // tab01, tab02...
            var $panels = $group.find('.history_tab_inner .history_cont');
            var idx = $tabs.index(this);

            $tabs.removeClass('on');
            $tabs.removeAttr('title');
            $panels.hide();

            $(this).addClass('on');
            $(this).attr('title', '선택됨');
            if (idx > -1) {
                $panels.eq(idx).show();
            }

        } else {
            // ▶ 기존 main_tab_wrap / press_tab_wrap 처리
            $group.find('a').removeClass('on');
            $group.find('a').removeAttr('title');
            $group.find('.tab_cont_inner, .press_cont').hide();

            $(this).addClass('on');
            $(this).attr('title', '선택됨');
            $(this).siblings('.tab_cont_inner, .press_cont').show();
        }
    });

    /* =========================================
       640px 이하: history 탭 스크롤 박스 생성
       ========================================= */
    function initHistoryTabScroll() {
        $('.history_tab_wrap').each(function () {
            var $wrap = $(this);

            // 이미 모바일 모드 초기화 되어 있으면 패스
            if ($wrap.data('scroll-init') === 'Y') return;

            var $scroll = $('<div class="history_tab_scroll"></div>');
            $wrap.prepend($scroll);

            // 각 history_tab_inner 에서 탭 <a>만 떼어내어 스크롤 박스로 이동
            $wrap.find('.history_tab_inner').each(function () {
                var $inner = $(this);
                var $tab = $inner.children('a').first();  // tab01, tab02...

                if ($tab.length) {
                    // 원래 부모(history_tab_inner)를 기억해둠
                    $tab.data('orig-parent', $inner);
                    $scroll.append($tab);
                }
            });

            enableDragScroll($scroll);
            $wrap.data('scroll-init', 'Y'); // 모바일 모드 적용됨 표시
        });
    }

    /* =========================================
       640px 이상: 원상복귀 (스크롤 박스 제거)
       ========================================= */
    function destroyHistoryTabScroll() {
        $('.history_tab_wrap').each(function () {
            var $wrap = $(this);

            // 모바일 모드가 아니면 할 일 없음
            if ($wrap.data('scroll-init') !== 'Y') return;

            var $scroll = $wrap.children('.history_tab_scroll');

            // 스크롤 박스 안의 탭들을 다시 원래 부모에게 되돌림
            $scroll.find('a').each(function () {
                var $tab = $(this);
                var $parent = $tab.data('orig-parent');

                if ($parent && $parent.length) {
                    // history_tab_inner 안에서 history_cont보다 앞에 오게 prepend
                    $parent.prepend($tab);
                }
                $tab.removeData('orig-parent');
            });

            $scroll.remove();          // 스크롤 박스 제거
            $wrap.removeData('scroll-init');
        });
    }

    /* =========================================
       드래그로 가로 스크롤 (터치 + 마우스)
       ========================================= */
    function enableDragScroll($el) {
        var isDown = false;
        var startX;
        var scrollLeft;

        $el.on('mousedown touchstart', function (e) {
            isDown = true;
            var pageX = e.pageX || (e.originalEvent.touches && e.originalEvent.touches[0].pageX);
            startX = pageX - this.offsetLeft;
            scrollLeft = this.scrollLeft;
        });

        $el.on('mouseleave mouseup touchend', function () {
            isDown = false;
        });

        $el.on('mousemove touchmove', function (e) {
            if (!isDown) return;
            e.preventDefault();
            var pageX = e.pageX || (e.originalEvent.touches && e.originalEvent.touches[0].pageX);
            var x = pageX - this.offsetLeft;
            var walk = (startX - x);
            this.scrollLeft = scrollLeft + walk;
        });
    }

    /* =========================================
       화면 크기에 따라 레이아웃 적용/해제
       ========================================= */
    var mql = window.matchMedia('(max-width: 640px)');

    function applyHistoryLayout() {
        if (mql.matches) {
            // 640px 이하 → 스크롤 박스 생성
            initHistoryTabScroll();
        } else {
            // 640px 이상 → 원상복귀
            destroyHistoryTabScroll();
        }
    }

    // 최초 1번 적용
    applyHistoryLayout();

    // matchMedia 지원 브라우저
    if (mql.addEventListener) {
        mql.addEventListener('change', applyHistoryLayout);
    } else if (mql.addListener) {
        // 구형 브라우저 대응
        mql.addListener(applyHistoryLayout);
    } else {
        // 최후의 수단: resize 감시
        $(window).on('resize', applyHistoryLayout);
    }

    /* =========================================
       페이지 로딩 시 history 탭 초기 패널 표시
       ========================================= */
    $('.history_tab_wrap').each(function () {
        var $wrap = $(this);
        var $tabs = $wrap.find('a[class^="tab"]');
        var $panels = $wrap.find('.history_tab_inner .history_cont');
        var $onTab = $tabs.filter('.on').first();
        var idx = $tabs.index($onTab);

        if (idx < 0) idx = 0;

        $tabs.removeClass('on');
        $panels.hide();

        $tabs.eq(idx).addClass('on');
        $panels.eq(idx).show();
    });

});

/*관련사이트*/
$(function () {

    const $box = $(".related_link");
    const $btn = $box.find("> a");
    const $panel = $box.find("> span");

    // 버튼 클릭 → 열고 닫기
    $btn.on("click", function (e) {
        e.preventDefault();
        $panel.slideToggle();
    });

    // 영역 밖 클릭 → 닫기
    $(document).on("click", function (e) {
        if (!$(e.target).closest($box).length) $panel.slideUp();
    });

    // Tab 이동 시 패널 밖으로 나가면 닫기
    $box.find("a, button, input, span, [tabindex]").on("keydown", function (e) {
        if (e.key === "Tab" && !e.shiftKey) {
            const $focusables = $box.find("a,button,input,[tabindex]:not([tabindex='-1'])");
            if (e.target === $focusables.last()[0]) $panel.slideUp();
        }
    });

});


// 하단 소셜링크 (API 기반 동적 렌더링)
$(function () {
  const $wrap = $(".mark_wrap");
  if (!$wrap.length) return;

  // 플랫폼별 아이콘/라벨 설정
  const platformConfig = {
    YOUTUBE:   { icon: '/portal/images/ico_youtube.png',    alt: '유튜브',       label: '유튜브',   listTitle: '유튜브 채널 목록 열기' },
    INSTAGRAM: { icon: '/portal/images/ico_instargram.png', alt: 'instagram',    label: 'Instagram' },
    FACEBOOK:  { icon: '/portal/images/ico_facebook.png',   alt: 'facebook',     label: '페이스북', listTitle: '페이스북 채널 목록 열기' },
    BLOG:      { icon: '/portal/images/ico_blog.png',       alt: '네이버 블로그', label: '블로그',   listTitle: '네이버 블로그 채널 목록 열기' }
  };

  // SNS 데이터 로드 및 렌더링
  API.get('/api/common/portal/sns/list', {}, {showLoading: false})
    .then(function (data) {
      if (!data || !Array.isArray(data) || data.length === 0) return;

      // 플랫폼별 그룹화 (displayOrder 순서 유지)
      const grouped = {};
      const platformOrder = [];
      data.forEach(function (sns) {
        if (!grouped[sns.pltfmType]) {
          grouped[sns.pltfmType] = [];
          platformOrder.push(sns.pltfmType);
        }
        grouped[sns.pltfmType].push(sns);
      });

      // HTML 생성
      let html = '';
      platformOrder.forEach(function (platform) {
        const channels = grouped[platform];
        const config = platformConfig[platform];
        if (!config) return;

        if (channels.length === 1) {
          // 단일 채널: 직접 링크
          const ch = channels[0];
          html += '<a href="' + ch.chnlUrl + '" target="_blank" title="' + ch.chnlNm + ' ' + config.label + ' 새창열림">'
                + '<img src="' + config.icon + '" alt="' + ch.chnlNm + ' ' + config.alt + '"></a>';
        } else {
          // 복수 채널: 드롭다운
          html += '<span>';
          html += '<a href="javascript:void(0);" title="' + config.listTitle + '"><img src="' + config.icon + '" alt="' + config.alt + '"></a>';
          html += '<div class="sosial_wrap ' + platform?.toLowerCase() + '">';
          channels.forEach(function (ch) {
            html += '<a href="' + ch.chnlUrl + '" target="_blank" title="' + ch.chnlNm + ' 새창열림">' + ch.chnlNm + ' ' + config.label + '</a>';
          });
          html += '</div></span>';
        }
      });

      $wrap.html(html);

      // 이벤트 바인딩
      fnBindFooterSnsEvents($wrap);
    })
    .catch(function (error) {
      console.error('Footer SNS 데이터 조회 실패:', error);
    });

  // 이벤트 바인딩 함수
  function fnBindFooterSnsEvents($wrap) {
    // 드롭다운 토글
    $wrap.on("click", "> span > a", function (e) {
      e.preventDefault();
      var $span  = $(this).closest("span");
      var $panel = $span.find(".sosial_wrap");
      $wrap.find(".sosial_wrap").not($panel).slideUp(150);
      $panel.stop(true, true).slideToggle(150);
    });

    // 바깥 클릭 시 닫기
    $(document).on("click", function (e) {
      if (!$(e.target).closest(".mark_wrap").length) {
        $wrap.find(".sosial_wrap").slideUp(150);
      }
    });

    // 키보드 접근성
    $wrap.find("span").each(function () {
      var $span = $(this);
      $span.on("keydown", "a, button, [tabindex]", function (e) {
        if (e.key !== "Tab") return;
        var $focusables = $span.find("a, button, [tabindex]:not([tabindex='-1'])");
        var first = $focusables.first()[0];
        var last  = $focusables.last()[0];
        if (!e.shiftKey && e.target === last) {
          $span.find(".sosial_wrap").slideUp(150);
        }
        if (e.shiftKey && e.target === first) {
          $span.find(".sosial_wrap").slideUp(150);
        }
      });
    });
  }
});

// 통합검색
$(function () {
    const $searchInput = $('#totalSearchKeyword');
    const $searchBtn = $('#totalSearchBtn');

    // 통합 검색 실행
    const executeSearch = () => {
        const keyword = $('#totalSearchKeyword').val().trim();

        if (keyword.length < 2) {
            uiCommon.fnShowAlertModal('검색어는 2글자 이상 입력해 주세요.');
            return;
        }

        // 동적 폼 생성 및 제출 (페이지 이동 발생)
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/api/common/search';

        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'keyword';
        input.value = keyword;

        form.appendChild(input);
        document.body.appendChild(form);

        form.submit();
    };

    // 버튼 클릭시 이벤트
    $searchBtn.on('click', executeSearch);

    // 검색창에서 엔터키 입력시
    $searchInput.on('keydown', function (e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            executeSearch();
        }
    });

    // 통합 검색 닫기 버튼 클릭시 이벤트
    $('.close_search').on('click keydown', function (e) {
        if (e.type === 'click' || e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            $('#totalSearchKeyword').val('');
        }
    });
})

//테이블 스크롤
$('.scroll_img').on('click', function () {
    $('.scroll_img').css('display', 'none');
});

////////////////////////////////////////////////////////////////////////////////////// 팝업 시작

let rootOpenerBtn = null;   // 처음 팝업을 띄운 버튼 저장

// 팝업 열기
$('.open_popup').on('click', function (e) {
    e.preventDefault();

    const popupId = $(this).data('popup');
    const $popup = $('#' + popupId);
    const $wrap = $popup.find('.popup_wrap');

    // 처음 팝업만 root 저장
    if (!rootOpenerBtn) {
        rootOpenerBtn = this;
    }

    // popup_wrap에 tabindex 자동 생성
    if (!$wrap.attr('tabindex')) {
        $wrap.attr('tabindex', '-1');
    }

    // 팝업 open
    $popup.fadeIn(200, function () {
        $wrap.focus(); // popup_wrap에 바로 포커스 이동
    });

    $('.bg_overlay').fadeIn(200);
});

// 팝업 닫기 → 모든 팝업 닫기
$('.pop_close, .my_close').on('click', function (e) {
    e.preventDefault();

    // 열려있는 모든 팝업 닫기
    $('.layer_popup:visible').fadeOut(200);
    $('.bg_overlay').fadeOut(200);

    // 처음 팝업을 열었던 버튼으로 포커스 복귀
    if (rootOpenerBtn) {
        $(rootOpenerBtn).focus();
        rootOpenerBtn = null;  // 초기화(다음 팝업 시퀀스를 위해)
    }
});

////////////////////////////////////////////////////////////////////////////////////// 팝업 닫기

////////////////////////////////////////////////////////////////////////////////////// 파일 업로드

document.addEventListener('DOMContentLoaded', function () {

    document.querySelectorAll('.file_upload').forEach(function (wrap) {

        const fileInput = wrap.querySelector('input[type="file"]');
        const fileTxt = wrap.querySelector('.file_txt');
        const fileLabel = wrap.querySelector('label'); // ★ label 요소

        // 파일 선택 시 이름 표시
        fileInput.addEventListener('change', function () {
            if (this.files && this.files.length > 0) {
                fileTxt.textContent = this.files[0].name;
            } else {
                fileTxt.textContent = '파일첨부';
            }
        });

        //label에 .focus 추가
        fileInput.addEventListener('focus', function () {
            fileLabel.classList.add('focus');
        });

        //label에서 .focus 제거
        fileInput.addEventListener('blur', function () {
            fileLabel.classList.remove('focus');
        });

    });

})

/////////////////////////////////////////////////////////////접속시 텝이 중앙으로 이동하는 스크립트
document.addEventListener('DOMContentLoaded', function () {
    const tabMenu = document.querySelector('.sub_tab_menu ul');
    const activeItem = document.querySelector('.sub_tab_menu ul li.on');

    function scrollToActive() {
        if (!tabMenu || !activeItem) return;

        const tabRect = tabMenu.getBoundingClientRect();
        const activeRect = activeItem.getBoundingClientRect();

        // 활성 탭이 보이지 않으면 중앙에 위치하도록 스크롤 이동
        const offset = (activeRect.left + activeRect.width / 2) - (tabRect.left + tabRect.width / 2);
        tabMenu.scrollTo({
            left: tabMenu.scrollLeft + offset,
            behavior: 'smooth'
        });
    }

    // 초기 로드시 실행
    scrollToActive();

    // 브라우저 리사이즈 시 재확인 (모바일 <-> PC 전환 시)
    let resizeTimer;
    window.addEventListener('resize', function () {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(() => {
            scrollToActive();
        }, 200);
    });
});