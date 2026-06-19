const common = {
    /**
     * 아이디 형식 검증 (6~20자리 영문, 숫자)
     * @param {string} id - 검증할 아이디
     * @returns {boolean} - 유효한 아이디 형식이면 true, 아니면 false
     */
    regId: function(id) {
        if (!id || typeof id !== 'string') return false;
        const idRegex = /^[a-zA-Z0-9]{6,20}$/;
        return idRegex.test(id);
    },

    /**
     * 생년월일 형식 검증 (YYYYMMDD)
     * @param {string} text - 검증할 생년월일 문자열 (예: '19810101')
     * @returns {boolean} - 유효한 생년월일 형식이면 true, 아니면 false
     */
    regBirth: function(text) {
        // 숫자만 허용하고 8자리인지 확인
        if (!/^\d{8}$/.test(text)) {
            return false;
        }
        
        // 년, 월, 일 추출
        const year = parseInt(text.substring(0, 4), 10);
        const month = parseInt(text.substring(4, 6), 10) - 1; // 월은 0부터 시작
        const day = parseInt(text.substring(6, 8), 10);
        
        // 유효한 날짜인지 확인
        const date = new Date(year, month, day);
        return (
            date.getFullYear() === year &&
            date.getMonth() === month &&
            date.getDate() === day
        );
    },
    
    /**
     * 이메일 형식 검증
     * @param {string} email - 검증할 이메일 주소
     * @returns {boolean} - 유효한 이메일 형식이면 true, 아니면 false
     */
    regEmail: function(email) {
        if (!email || typeof email !== 'string' || email.length > 254) return false;
        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;
        return emailRegex.test(email);
    },
    /**
     * 이메일 도메인 검증
     * @param {string} emailDomain - 검증할 이메일 도메인
     * @returns {boolean} - 유효한 이메일 도메인이면 true, 아니면 false
     */
    regEmailDomain: function(emailDomain) {
        if (!emailDomain || typeof emailDomain !== 'string' || emailDomain.length > 253) return false;
        const domainRegex = /^(?!-)[a-zA-Z0-9-]{1,63}(?<!-)(\.[a-zA-Z0-9-]{1,63})*\.[a-zA-Z]{2,}$/;
        return domainRegex.test(emailDomain);
    },

    /**
     * 휴대폰 번호 형식 검증
     * @param {string} phoneNumber - 검증할 휴대폰 번호
     * @returns {boolean} - 유효한 휴대폰 번호 형식이면 true, 아니면 false
     */
    regMobileNumber: function(prefix, middle, suffix) {
        // 앞자리: 010, 011, 016, 017, 018, 019
        const prefixRegex = /^(010|011|016|017|018|019)$/;
        // 중간자리: 숫자 4자리
        const middleRegex = /^\d{4}$/;
        // 끝자리: 숫자 4자리
        const suffixRegex = /^\d{4}$/;
        
        return prefixRegex.test(prefix) && 
               middleRegex.test(middle) && 
               suffixRegex.test(suffix);
    },
    
    /**
     * 휴대폰 번호 형식 검증 (01X-XXXX-XXXX 또는 01XXXXXXXX)
     * @param {string} phoneNumber - 검증할 휴대폰 번호 (예: "010-1234-5678" 또는 "01012345678")
     * @returns {boolean} - 유효한 휴대폰 번호 형식이면 true, 아니면 false
     */
    regMobileNumberString: function(phoneNumber) {
        if (!phoneNumber || typeof phoneNumber !== 'string') return false;
        
        // 하이픈이 있으면 반드시 두 개 모두 있어야 함
        if (phoneNumber.includes('-')) {
            return /^01[0-9]-\d{3,4}-\d{4}$/.test(phoneNumber);
        }
        // 하이픈이 없으면 숫자만 10~11자리
        return /^01[0-9]\d{7,8}$/.test(phoneNumber);
    },
    
    /**
     * 문자열 최소 길이 검증
     * @param {string} str - 검증할 문자열
     * @param {number} min - 최소 길이
     * @returns {boolean} - 문자열이 최소 길이 이상이면 true, 아니면 false
     */
    checkMinString: function(str, min) {
        return str && typeof str === 'string' && str.length >= min;
    },
    
    /**
     * 문자열 최대 길이 검증
     * @param {string} str - 검증할 문자열
     * @param {number} max - 최대 길이
     * @returns {boolean} - 문자열이 최대 길이 이하면 true, 아니면 false
     */
    checkMaxString: function(str, max) {
        return str && typeof str === 'string' && str.length <= max;
    },
    
    /**
     * 문자열 길이 범위 검증
     * @param {string} str - 검증할 문자열
     * @param {number} min - 최소 길이
     * @param {number} max - 최대 길이
     * @returns {boolean} - 문자열이 지정된 길이 범위 내에 있으면 true, 아니면 false
     */
    checkStringLength: function(str, min, max) {
        return str && typeof str === 'string' && str.length >= min && str.length <= max;
    },

    regPassword: function(password) {
        // At least 8 characters, and at least 3 of: lowercase, uppercase, digit, special character
        const hasLower = /[a-z]/.test(password);
        const hasUpper = /[A-Z]/.test(password);
        const hasDigit = /\d/.test(password);
        const hasSpecial = /[@$!%*?&]/.test(password);
        const validChars = /^[A-Za-z\d@$!%*?&]+$/;
        
        const conditionsMet = [hasLower, hasUpper, hasDigit, hasSpecial].filter(Boolean).length >= 3;
        return password.length >= 8 && validChars.test(password) && conditionsMet;
    },

    /**
     * 날짜와 시간을 "YYYY-MM-DD HH:MM:SS" 형식으로 변환
     * @param {string|Date} dateTime - 입력값(날짜 & 시간)
     * @returns {string} 결과(YYYY-MM-DD HH:MM:SS), 입력값 없으면 '-'
     */
    formatDateTime: function(dateTime) {
        if (!dateTime) return '-';

        const d = new Date(dateTime);
        const pad = (n) => n.toString().padStart(2, '0');

        const year = d.getFullYear();
        const month = pad(d.getMonth() + 1); // 0~11
        const day = pad(d.getDate());
        const hours = pad(d.getHours());
        const minutes = pad(d.getMinutes());
        const seconds = pad(d.getSeconds());

        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    },

    /**
     * 날짜를 "YYYY-MM-DD" 형식으로 변환
     * @param {string|Date} date - 입력값(날짜)
     * @returns {string} 결과(YYYY-MM-DD), 입력값 없으면 '-'
     */
    formatDate: function(date) {
        if (!date) return '';

        let d;
        if (/^\d{8}$/.test(date)) { // YYYYMMDD 형태 (생년월일 등)
            d = new Date(`${date.substring(0,4)}-${date.substring(4,6)}-${date.substring(6,8)}`);
        } else {
            d = new Date(date); // (등록일시 등)
        }

        const pad = (n) => n.toString().padStart(2, '0');
        return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
    },

    /**
     * 기준 날짜에서 n일 전/후 날짜를 YYYY-MM-DD 문자열로 반환
     * @param {number} offset - 음수: 과거, 양수: 미래
     * @param {Date} baseDate - 기준 날짜 (생략 시 오늘)
     * @returns {string} YYYY-MM-DD
     */
    formatDateOffset: function(offset = 0, baseDate = new Date()) {
        const d = new Date(baseDate);      // 기준 날짜 복사
        d.setDate(d.getDate() + offset);   // offset 적용
        return d.toISOString().slice(0, 10);
    },

    /**
     * 기준 날짜에서 n일 전/후 날짜를 YYYY-MM-DD HH:MM:SS 문자열로 반환
     * @param {number} offset - 음수: 과거, 양수: 미래
     * @param {Date} baseDate - 기준 날짜 (생략 시 현재 시간)
     * @returns {string} YYYY-MM-DD HH:MM:SS
     */
    formatDateTimeOffset: function(offset = 0, baseDate = new Date()) {
        const d = new Date(baseDate);      // 기준 날짜 복사
        d.setDate(d.getDate() + offset);   // offset 적용

        const pad = (n) => n.toString().padStart(2, '0');

        return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
    },

    /**
     * 현재 날짜 경로 생성 (/YYYY/MM/DD/)
     */
    fnGetDatePath: function () {
        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');
        const day = String(now.getDate()).padStart(2, '0'); // 일(day) 추가
        return '/' + year + '/' + month + '/' + day + '/';
    },

    /**
     * 숫자만 있는 전화번호 문자열을 "010-1234-5678" 형식으로 변환
     * @param {string} number - 입력값(01012341234)
     * @returns {string} 결과(010-1234-1234), 11자리가 아니면 입력값 반환
     */
    formatPhoneNumber: function(number) {
        if (!number) return '';

        const digits = number.replace(/\D/g, '');

        if (digits.length !== 11) return number;

        return `${digits.substring(0,3)}-${digits.substring(3,7)}-${digits.substring(7)}`;
    },

    /**
     * 숫자만 있는 전화번호 문자열을 "02-123-4567" 형식으로 변환
     * @param {string} number - 입력값(021234567)
     * @returns {string} 결과(02-123-4567), 9자리가 아니면 입력값 반환
     */
    formatTelNo: function(number) {
        if (!number) return '';

        const digits = number.replace(/\D/g, '');

        if (digits.length !== 9) return number;

        return `${digits.substring(0,2)}-${digits.substring(2,5)}-${digits.substring(5)}`;
    },

    /**
     * 전화번호 세 부분을 받아 하이픈으로 연결(팩스번호 연결 시에도 사용 가능)
     * @param {string} part1 - 입력값1 (예: 010)
     * @param {string} part2 - 입력값2 (예: 1234)
     * @param {string} part3 - 입력값3 (예: 5678)
     * @returns {string} 결과(010-1234-1234), 입력값 없으면 '-'
     */
    joinPhoneNumber: function(part1, part2, part3) {
        if (!part1 || !part2 || !part3) return '-';
        return `${part1}-${part2}-${part3}`;
    },

    /**
     * 전화번호를 세 부분으로 나누어 배열로 반환(팩스번호 나눌 때도 사용 가능)
     * @param {string} phone - 입력값 (예: '010-1234-5678')
     * @returns {string[]} [앞자리, 중간자리, 끝자리] 형태의 배열
     *                      유효하지 않은 경우 ['', '', ''] 반환
     */
    splitPhoneNumber: function(phone) {
        if (!phone) return ['', '', ''];
        // 숫자만 추출
        const digits = phone.replace(/\D/g, '');
        let part1 = '', part2 = '', part3 = '';
        if (digits.length === 9) {
            // 예: 02-123-4567
            part1 = digits.substring(0, 2);
            part2 = digits.substring(2, 5);
            part3 = digits.substring(5);
        } else if (digits.length === 10) {
            if (digits.startsWith('02')) {
                // 예: 02-1234-5678
                part1 = digits.substring(0, 2);
                part2 = digits.substring(2, 6);
                part3 = digits.substring(6);
            } else {
                // 예: 031-123-4567
                part1 = digits.substring(0, 3);
                part2 = digits.substring(3, 6);
                part3 = digits.substring(6);
            }
        } else if (digits.length === 11) {
            // 예: 010-1234-5678
            part1 = digits.substring(0, 3);
            part2 = digits.substring(3, 7);
            part3 = digits.substring(7);
        } else if (digits.length === 12) {
            // 예: 0505-1234-5678
            part1 = digits.substring(0, 4);
            part2 = digits.substring(4, 8);
            part3 = digits.substring(8);
        }

        return [part1, part2, part3];
    },

    /**
     * 이메일 아이디와 도메인 @로 연결
     * @param {string} localPart - 이메일 아이디
     * @param {string} domain - 이메일 도메인
     * @returns {string} 결과, 입력값 없으면 '-'
     */
    joinEmail: function(localPart, domain) {
        if (!localPart || !domain) return '-';
        return `${localPart}@${domain}`;
    },

    /**
     * useYn(사용 여부)값 포맷
     * 'Y' : '사용', 'N' : '미사용', 그 외 : '-'
     *
     * @param {string} value - 사용 여부 값
     * @returns {string} 변환된 문자열
     */
    formatUseYn: function(value) {
        if (value === 'Y') return '사용';
        if (value === 'N') return '미사용';
        return '-';
    },

    /**
     * useYn(사용 여부)값 포맷
     * 'Y' : '활성', 'N' : '비활성', 그 외 : '-'
     *
     * @param {string} value - 사용 여부 값
     * @returns {string} 변환된 문자열
     */
    formatActiveYn: function(value) {
        if (value === 'Y') return '활성';
        if (value === 'N') return '비활성';
        return '-';
    },

    /**
     * 여러 값 중 비어있지 않은 값이 하나라도 존재하면 값을 공백으로 연결하여 반환,
     * 모두 비어있으면 '-' 반환
     * 한 요소 내 여러 값이 빈 값일 경우 '-' 이 다중 표출되는 경우 방지용
     *
     * @param  {...any} args - 확인할 값들
     * @returns {string} 연결된 문자열 또는 '-'
     */
    formatValues: function(...args) {
        // 존재하는 값만 필터링
        const nonEmpty = args.filter(v => v !== null && v !== undefined && v !== '');
        if (nonEmpty.length === 0) return '-';
        return nonEmpty.join(' ');
    },

    /**
     * 지정한 input 요소들에 숫자만 입력되도록 이벤트 바인딩
     * @param  {...string} ids - input 요소의 id
     */
    bindNumericOnly: function(...ids) {
        ids.forEach(id => {
            const el = document.getElementById(id);
            if (!el) return;
            el.addEventListener("input", function () {
                this.value = this.value.replace(/[^0-9]/g, '');
            });
        });
    },
    /**
     * 사업자등록번호 입력(input)에 숫자만 허용하고 자동으로 000-00-00000 포맷 적용
     * @param  {...string} ids - input 요소의 id
     */
    bindBizRegNo: function(...ids) {
        ids.forEach(id => {
            const el = document.getElementById(id);
            if (!el) return;

            el.addEventListener("input", function () {
                // 숫자만 추출
                let value = this.value.replace(/[^0-9]/g, '');

                // 최대 10자리까지만 허용
                if (value.length > 10) value = value.substring(0, 10);

                // 000-00-00000 포맷 적용
                let formattedValue = '';
                if (value.length > 0) formattedValue = value.substring(0, 3);
                if (value.length > 3) formattedValue += '-' + value.substring(3, 5);
                if (value.length > 5) formattedValue += '-' + value.substring(5, 10);

                this.value = formattedValue;
            });
        });
    },

    /**
     * 바이트(Byte)를 메가바이트(MB)로 변환
     * @param {number} bytes - 바이트 값
     * @param {number} decimals - 소수점 자리수 (기본 2)
     * @returns {string} MB 단위 문자열
     */
    bytesToMB: function (bytes, decimals = 2) {
        if (isNaN(bytes) || bytes < 0) return '0 MB';
        const mb = bytes / (1024 * 1024);
        return `${mb.toFixed(decimals)} MB`;
    },

    /**
     * snake_case 객체 key를 camelCase로 변환
     * @param {object} obj - 변환할 원본 객체
     * @returns {object} camelCase로 변환된 객체
     */
    convertSnakeToCamel: function (obj) {
        const result = {};

        Object.keys(obj).forEach(function (key) {
            const camelKey = key.replace(/_([a-z])/g, function (match, p1) {
                return p1.toUpperCase();
            });
            result[camelKey] = obj[key];
        });

        return result;
    },

    /**
     * HTML에서 특수문자를 안전하게 출력할 수 있도록 이스케이프 처리
     * @param {any} val - 변환할 값
     * @returns {string} 변환된 문자열, null/undefined는 '-'
     */
    escapeHtml: function(val) {
        if (val === null || val === undefined) return '-';
        return String(val)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    },

    /**
     * HTML 태그 제거 (순수 텍스트만 추출)
     * @param {string} htmlString - HTML 태그가 포함된 문자열
     * @returns {string} HTML 태그가 제거된 순수 텍스트
     * @example
     * common.stripHtmlTags('<p>안녕하세요</p>') // "안녕하세요"
     * common.stripHtmlTags('<div><span>테스트</span></div>') // "테스트"
     */
    stripHtmlTags: function(htmlString) {
        if (!htmlString || typeof htmlString !== 'string') return '';
        return htmlString.replace(/<[^>]*>/g, '').trim();
    },

    /**
     * 이미지 확장자인지 판단
     * @param {string} fileName - 파일명 (확장자 포함)
     * @returns {boolean} 이미지 확장자이면 true, 아니면 false
     * @example
     * common.isImageFile('test.png') // true
     * common.isImageFile('document.pdf') // false
     */
    isImageFile: function(fileName) {
        if (!fileName || typeof fileName !== 'string') return false;

        // 허용할 이미지 확장자 (소문자)
        const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'];

        // 마지막 점(.) 기준으로 확장자 추출
        const ext = fileName.split('.').pop().toLowerCase();

        return imageExtensions.includes(ext);
    },

    changeToThumbnailPath: function (url) {
        if (!url || typeof url !== 'string') return '';
        return url.replace(/(\.[^/.]+)$/, '_s$1');
    },

    changeToMiddleImagePath: function (url) {
        if (!url || typeof url !== 'string') return '';
        return url.replace(/(\.[^/.]+)$/, '_m$1');
    },

    /**
     * .post-content 내 테이블 HTML 속성을 inline style로 변환.
     * CSS 리셋 환경에서도 테이블이 원본대로 표시되도록 보장한다.
     * 페이지 로드 시 자동 실행됨.
     */
    applyTableStyles: function(container) {
        var root = container || document;
        var tables = root.querySelectorAll('.post-content table');
        for (var i = 0; i < tables.length; i++) {
            var tbl = tables[i];
            var border = tbl.getAttribute('border');
            var cellpadding = tbl.getAttribute('cellpadding');
            var cellspacing = tbl.getAttribute('cellspacing');
            var align = tbl.getAttribute('align');

            if (border && border !== '0') {
                if (!tbl.style.border) tbl.style.border = border + 'px solid black';
                if (!tbl.style.borderCollapse) tbl.style.borderCollapse = 'collapse';
            }
            if (cellspacing) {
                if (cellspacing === '0') {
                    if (!tbl.style.borderCollapse) tbl.style.borderCollapse = 'collapse';
                } else {
                    tbl.style.borderCollapse = 'separate';
                    if (!tbl.style.borderSpacing) tbl.style.borderSpacing = cellspacing + 'px';
                }
            }
            if (align === 'center') {
                if (!tbl.style.marginLeft) { tbl.style.marginLeft = 'auto'; tbl.style.marginRight = 'auto'; }
            } else if (align === 'right') {
                if (!tbl.style.marginLeft) { tbl.style.marginLeft = 'auto'; tbl.style.marginRight = '0'; }
            }

            var cells = tbl.querySelectorAll('tr, th, td');
            for (var j = 0; j < cells.length; j++) {
                var cell = cells[j];
                var tag = cell.tagName.toLowerCase();

                if ((tag === 'th' || tag === 'td') && border && border !== '0' && !cell.style.border) {
                    cell.style.border = border + 'px solid black';
                }
                if ((tag === 'th' || tag === 'td') && cellpadding && !cell.style.padding) {
                    cell.style.padding = cellpadding + 'px';
                }
                var cAlign = cell.getAttribute('align');
                if (cAlign && !cell.style.textAlign) cell.style.textAlign = cAlign;
                var cValign = cell.getAttribute('valign');
                if (cValign && !cell.style.verticalAlign) cell.style.verticalAlign = cValign;
                var cHeight = cell.getAttribute('height');
                if (cHeight && !cell.style.height) cell.style.height = (/^\d+$/.test(cHeight) ? cHeight + 'px' : cHeight);
                var cWidth = cell.getAttribute('width');
                if ((tag === 'th' || tag === 'td') && cWidth && !cell.style.width) cell.style.width = (/^\d+$/.test(cWidth) ? cWidth + 'px' : cWidth);
                var cBg = cell.getAttribute('bgcolor');
                if (cBg && !cell.style.backgroundColor) cell.style.backgroundColor = cBg;
            }
        }
    }
};

// .post-content 내 테이블 HTML 속성 → inline style 자동 변환
document.addEventListener('DOMContentLoaded', function() { common.applyTableStyles(); });

/**
 * 페이지 로드 시 현재 URL을 sessionStorage에 자동 저장
 * 로그인 후 원래 페이지로 돌아가기 위한 로직
 */
(function() {
    // 페이지 로드 시 실행
    const currentPath = window.location.pathname;

    // 로그인 페이지가 아닌 경우에만 현재 URL 저장
    if (!currentPath.includes('/login')) {
        // 쿼리스트링 포함한 전체 URL 저장
        const fullUrl = window.location.href.replace(window.location.origin, '');
        sessionStorage.setItem('returnUrl', fullUrl);
    }
})();