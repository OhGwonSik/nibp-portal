const SurveyRenderer = {

    state: {
        basePath: '',
        allowedExtensions: '',
        maxFileSize: 10,
        contextPath: ''
    },

    /**
     * 초기 설정을 위한 함수
     * @param {object} options - { basePath, allowedExtensions, maxFileSize, contextPath }
     */
    config: function(options = {}) {
        this.state.basePath = options.basePath || '';
        this.state.allowedExtensions = options.allowedExtensions || 'jpg,jpeg,png,gif,bmp';
        this.state.maxFileSize = options.maxFileSize || 10;
        this.state.contextPath = options.contextPath || '';
    },

    /**
     * 설문조사 전체를 렌더링하는 메인 함수
     * @param {object} targets - { header, description, content } DOM element
     * @param {object} surveyData - API에서 받아온 설문 데이터
     */
    render: function(targets, surveyData) {
        if (!targets || !targets.content) {
            console.error("Target container for survey content is not defined.");
            return;
        }

        // 초기화
        if(targets.header) targets.header.innerHTML = "";
        if(targets.description) targets.description.innerHTML = "";
        targets.content.innerHTML = "";

        // 헤더 렌더링
        if(targets.header) {
            this.fnRenderHeader(surveyData, targets.header);
        }

        // 설명 렌더링
        if(targets.description) {
            targets.description.innerHTML = surveyData.srvyCn || "";
        }

        // 응답자 정보 렌더링
        this.fnRenderUserInfo(surveyData, targets.content);

        // 문항 렌더링
        if (surveyData.qstList && surveyData.qstList.length > 0) {
            this.fnRenderQuestions(surveyData.qstList, targets.content);
        } else {
            targets.content.insertAdjacentHTML('beforeend', '<div class="no_data">등록된 문항이 없습니다.</div>');
        }

        // 이벤트 바인딩
        this.fnBindEvents(targets.content);
    },

    fnRenderHeader: function(data, $container) {
        const today = new Date().toISOString().split('T')[0];
        let statusText = "대기";
        if (data.srvyBgngDt <= today && data.srvyEndDt >= today) statusText = "진행";
        else if (data.srvyEndDt < today) statusText = "마감";

        const qCount = data.qstList ? data.qstList.filter(q => q.level !== 0).length : 0;
        const regDate = data.regDt ? data.regDt.substring(0, 10) : data.srvyBgngDt;

        $container.innerHTML = `
            <h4 class="title">${data.srvyTtl}</h4>
            <em class="board_date"><b>등록일</b> ${regDate}</em>
            <em class="board_date pl20"><b>상태</b> ${statusText}</em>
            <em class="board_date pl20"><b>문항수</b> ${qCount}</em>
        `;
    },

    fnRenderUserInfo: function(data, $container) {
        const isCollect = (
            data.nmClctYn === 'Y' || data.gndrClctYn === 'Y' ||
            data.instClctYn === 'Y' || data.mpnoClctYn === 'Y' ||
            data.emlClctYn === 'Y' || data.addrClctYn === 'Y'
        );

        if (!isCollect) return;

        const reqHtml = '<span>*</span><i class="hidden">필수 입력</i>';
        let html = `
            <h4 class="chapter"><span class="lb">기본정보</span>응답자 기본 정보</h4>
            <dl>
        `;

        if (data.nmClctYn === 'Y') {
            html += `
                <dd>
                    <div class="checkbox_id"><label for="userNm">${reqHtml} 성명</label></div>
                    <input type="text" id="userNm" title="성명 입력" placeholder="성명을 입력해주세요. ">
                </dd>`;
        }
        if (data.gndrClctYn === 'Y') {
            html += `
                <dd>
                    <div class="checkbox_id"><label>${reqHtml} 성별</label></div>
                    <div class="radio-box" style="display:inline-block; margin-right:20px;">
                        <label><input type="radio" name="userInfoGender" value="M"> 남성</label>
                    </div>
                    <div class="radio-box" style="display:inline-block;">
                        <label><input type="radio" name="userInfoGender" value="F"> 여성</label>
                    </div>
                </dd>`;
        }
        if (data.instClctYn === 'Y') {
            html += `
                <dd>
                    <div class="checkbox_id"><label for="userOrg">${reqHtml} 소속</label></div>
                    <input type="text" id="userOrg" title="소속 입력" placeholder="소속을 입력해주세요.">
                </dd>`;
        }
        if (data.mpnoClctYn === 'Y') {
            html += `
                <dd>
                    <div class="checkbox_id"><label for="userMpno">${reqHtml} 휴대전화 번호</label></div>
                    <input type="text" id="userMpno" title="휴대전화 번호 입력" placeholder="'-'없이 숫자만 입력" oninput="this.value=this.value.replace(/[^0-9]/g,'')">
                </dd>`;
        }
        if (data.emlClctYn === 'Y') {
            html += `
                <dd>
                    <div class="checkbox_id"><label for="userEml">${reqHtml} 이메일</label></div>
                    <input type="text" id="userEml" title="이메일 입력" placeholder="이메일을 입력해주세요.">
                </dd>`;
        }
        if (data.addrClctYn === 'Y') {
            html += `
                <dd>
                    <div class="checkbox_id"><label for="userAddr">${reqHtml} 주소</label></div>
                    <input type="text" id="userAddr" title="주소 입력" placeholder="주소를 입력해주세요.">
                </dd>`;
        }

        html += `</dl><hr class="hr_line_s">`;
        $container.insertAdjacentHTML('beforeend', html);
    },

    fnRenderQuestions: function(qstList, $container) {
        let html = '';
        const basePath = this.state.basePath;

        qstList.forEach((q, index) => {
            if (q.level === 0) {
                if (index > 0) html += `<hr class="hr_line_s">`;
                let chapterTitle = q.displayQNo ? q.displayQNo.replace(/CH/i, '챕터 ') + '.' : '챕터.';
                html += `
                    <h4 class="chapter" data-qst-no="${q.srvyQitemOid}">
                        <span class="lb">${chapterTitle}</span>${q.srvyQitemTtl}
                    </h4>`;
                return;
            }

            html += `<dl data-qst-no="${q.srvyQitemOid}" data-q-no="${q.displayQNo}" data-required="${q.esntlYn}" data-type="${q.srvyQitemType}" data-title="${q.srvyQitemTtl}" data-limit="${q.srvyQitemLmt}">`;

            let reqMark = (q.esntlYn === 'Y') ? '<span>*</span><i class="hidden">필수 입력</i>' : '';
            let limitInfo = "";
            if ((q.srvyQitemType === 'MULTI' || q.srvyQitemType === 'IMG_SEL') && q.plrlChcYn === 'Y' && q.srvyQitemLmt > 0) {
                limitInfo = ` (최대 ${q.srvyQitemLmt}개)`;
            }

            html += `<dt>${q.displayQNo}.${reqMark} ${q.srvyQitemTtl} ${limitInfo}</dt>`;

			// 첨부파일 처리 (이미지는 표시, 그 외 파일은 다운로드 링크)
			if (q.surveyQstAttach && q.surveyQstAttach.length > 0) {
				html += `<dd class="qst_img_area">`;
				q.surveyQstAttach.forEach(file => {

					const filePath = `${basePath}${file.strgFilePath}/${file.strgFileNm}`;

					// 파일 확장자 추출
					const fileExt = file.strgFileNm.split('.').pop().toLowerCase();

					// 이미지 확장자 목록
					const imageExts = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'];

					// 이미지 파일인 경우 - 이미지 표시
					if (imageExts.includes(fileExt)) {
						html += `
							<p class="imgarea" style="text-align: center; margin: 10px 0;">
								<img src="${filePath}" alt="${file.orgnlFileNm}" 
									 style="max-width: 300px; width: 100%; height: auto; border: 1px solid #ddd;"
									 onerror="this.src='${contextPath}/portal/images/vod_03.png';">
							</p>`;
					}
					// 이미지가 아닌 파일인 경우 - 다운로드 링크 표시 (notice_post 참고)
					else {
						html += `
							<div class="attachment" style="margin: 10px 0; padding: 10px; background: #f9f9f9; border: 1px solid #ddd; border-radius: 4px;">
								<a href="${filePath}" download="${file.orgnlFileNm}" class="file_link" style="display: flex; align-items: center; text-decoration: none; color: #333;">
									<i class="ico_file">첨부아이콘</i>
									<span style="color: #0066cc; text-decoration: underline; margin-left: 5px;">${file.orgnlFileNm}</span>
								</a>
							</div>`;
					}
				});
				html += `</dd>`;
			}

            // ... (The rest of the rendering logic for each question type) ...
            // This part is very long, so I'll just put a placeholder here, but the full logic from edu501-1.html should be copied.
            html += this.fnRenderQuestionByType(q);

            html += `</dl>`;
        });

        $container.insertAdjacentHTML('beforeend', html);
    },

    fnRenderQuestionByType: function(q) {
        let html = '';
        const basePath = this.state.basePath;

        // A. 주관식 (LONG / SHORT)
        if (q.srvyQitemType === 'LONG' || q.srvyQitemType === 'SHORT') {
            html += `<dd>`;
            const maxLen = q.srvyQitemLmt || 100;
            const formattedLimit = maxLen.toLocaleString();
            let ph = `${formattedLimit}자 이내로 작성해주세요.`;
            let attrScript = "";
            if (q.srvyQitemType === 'SHORT' && q.lmtYn === 'Y') {
                ph = `숫자만 입력해주세요. (${formattedLimit}자 이내)`;
                attrScript = `oninput="this.value = this.value.replace(/[^0-9]/g, '')" pattern="[0-9]*" inputmode="numeric"`;
            }
            html += `<textarea name="ans_${q.srvyQitemOid}" title="답변 입력" placeholder="${ph}" maxlength="${maxLen}" ${attrScript}></textarea>`;
            html += `</dd>`;
        }

        // B. 객관식 (MULTI / IMG_SEL)
        else if (q.srvyQitemType === 'MULTI' || q.srvyQitemType === 'IMG_SEL') {
            html += `<dd>`;
            if (q.optList && q.optList.length > 0) {
                q.optList.forEach(opt => {
                    const isLimitOne = (q.srvyQitemLmt == 1);
                    const isRealMulti = (q.plrlChcYn === 'Y' && !isLimitOne);
                    const type = isRealMulti ? 'checkbox' : 'radio';
                    const boxClass = isRealMulti ? 'checkbox_id' : 'radio-box';
                    const groupName = isRealMulti ? `group_${q.srvyQitemOid}` : `ans_${q.srvyQitemOid}`;
                    const limitAttr = (isRealMulti && q.srvyQitemLmt > 0) ? `data-limit="${q.srvyQitemLmt}"` : "";

                    let fileNo = "";
                    let imgSrc = "";

                    if (opt.optionAttach && opt.optionAttach.length > 0) {
                        const file = opt.optionAttach[0];
                        imgSrc = `${basePath}${file.strgFilePath}/${file.strgFileNm}`;
                        fileNo = file.fileOid;
                    } else if (opt.optFilePath) {
                        imgSrc = opt.optFilePath;
                    }

                    html += `<div class="opt-item mb10">
                                <div class="${boxClass}">
                                    <label>
                                        <input type="${type}" name="${groupName}" value="${opt.srvyQitemOptOid}" data-is-etc="${opt.etcOptYn}" data-file-no="${fileNo}" ${limitAttr}>
                                        ${opt.srvyQitemOptTxt}
                                    </label>
                                </div>`;

                    if (q.srvyQitemType === 'IMG_SEL' && imgSrc) {
                        html += `<div class="opt_img_area mt5" style="padding-left: 24px;">
                                    <img src="${imgSrc}" alt="${opt.srvyQitemOptTxt}" onclick="this.closest('.opt-item').querySelector('input').click();" style="max-width:300px; height:auto; border:1px solid #e0e0e0; cursor:pointer; display:block; border-radius:4px;" onerror="this.style.display='none';"> 
                                 </div>`;
                    }

                    if (opt.etcOptYn === 'Y') {
                        html += `<div style="padding-left: 24px;">
                                     <textarea name="ans_etc_${q.srvyQitemOid}_${opt.srvyQitemOptOid}" class="input_etc mt5" title="기타 입력" disabled></textarea>
                                 </div>`;
                    }
                    html += `</div>`;
                });
            }
            html += `</dd>`;
        }

        // C. 리커트 (LIKERT)
        else if (q.srvyQitemType === 'LIKERT') {
            const min = q.likertMin || 1;
            const max = q.likertMax || 5;
            html += `<dd>
                        <div class="order_box col_${max} mb10"><ul>`;
            for (let i = min; i <= max; i++) {
                let labelText = "";
                if (q.optList && q.optList.length > 0) {
                    const matchedOpt = q.optList.find(opt => opt.srvyQitemOptSeq === i);
                    if (matchedOpt && matchedOpt.srvyQitemOptTxt) labelText = matchedOpt.srvyQitemOptTxt;
                }
                html += `<li><span>${i}) ${labelText}</span></li>`;
            }
            html += `</ul></div>
                     <div class="select_box mt10">
                        <select name="ans_${q.srvyQitemOid}" class="wp100">
                            <option value="">점수 선택</option>`;
            for (let i = min; i <= max; i++) {
                html += `<option value="${i}">${i}점</option>`;
            }
            html += `</select></div></dd>`;
        }

        // F. 비율 (RATIO)
        else if (q.srvyQitemType === 'RATIO') {
            const min = q.likertMin || 1;
            const max = q.likertMax || 5;
            html += `<dd>`;
            if (q.optList && q.optList.length > 0) {
                q.optList.forEach(opt => {
                    html += `<div class="mb15">
                                <p class="mb5"><b>${opt.srvyQitemOptTxt}</b></p> 
                                <div class="select_box">
                                    <select name="ans_${q.srvyQitemOid}_${opt.srvyQitemOptOid}" class="wp100">
                                        <option value="">점수 선택</option>`;
                    for (let i = min; i <= max; i++) {
                        html += `<option value="${i}">${i}</option>`;
                    }
                    html += `</select></div></div>`;
                });
            }
            html += `</dd>`;
        }

        // D. 순위형 (RANK)
        else if (q.srvyQitemType === 'RANK') {
            html += `<dd>`;
            if (q.optList && q.optList.length > 0) {
                const count = q.optList.length;
                html += `<div class="order_box mb15"><ul>`;
                for(let i=1; i <= count; i++) {
                    html += `<li>
                                <span>${i}순위</span>
                                <input type="text" name="ans_rank_${q.srvyQitemOid}_${i}" class="rank-input" data-qst-no="${q.srvyQitemOid}" data-rank="${i}" placeholder="아래 항목을 선택하세요" readonly style="cursor:pointer; background-color: #f9f9f9;">
                             </li>`;
                }
                html += `</ul></div>`;
                q.optList.forEach(opt => {
                    html += `<div class="checkbox_id">
                                <label>
                                    <input type="checkbox" name="chk_rank_${q.srvyQitemOid}" class="rank-check" value="${opt.srvyQitemOptOid}" data-qst-no="${q.srvyQitemOid}" data-txt="${opt.srvyQitemOptTxt}"> 
                                    ${opt.srvyQitemOptTxt}
                                </label>
                             </div>`;
                });
            }
            html += `</dd>`;
        }

        // E. 이미지 응답형
        else if (q.srvyQitemType === 'IMG_RESP') {
            html += `<dd>
                        <div class="file_upload_group" data-qst-no="${q.srvyQitemOid}">
                            <div class="file_upload">
                                <label for="ans_file_${q.srvyQitemOid}"><span>파일찾기</span></label>
                                <input type="file" id="ans_file_${q.srvyQitemOid}" name="ans_file_${q.srvyQitemOid}" class="upload-hidden" data-qst-no="${q.srvyQitemOid}" title="파일찾기" accept="image/jpeg, image/png, image/gif, image/bmp">
                                <span class="file_txt">파일을 첨부해주세요</span>
                            </div>
                            <div id="file_view_${q.srvyQitemOid}" class="file_list flex_col mt10"></div>
                        </div>`;
            if (q.srvyQitemLmt && q.srvyQitemLmt > 0) {
                const ph = `${q.srvyQitemLmt.toLocaleString()}자 이내로 작성해주세요.`;
                html += `<div class="mt10">
                            <textarea name="ans_txt_${q.srvyQitemOid}" title="설명 입력" placeholder="${ph}" maxlength="${q.srvyQitemLmt}" style="width:100%; height:100px; margin-top:10px;"></textarea>
                         </div>`;
            }
            html += `</dd>`;
        }
        return html;
    },

    fnBindEvents: function($container) {
        const _this = this;
        
        const inputs = $container.querySelectorAll('input[type="radio"], input[type="checkbox"]:not(.rank-check)');
        inputs.forEach(input => {
            input.addEventListener('change', function() {
                if(this.disabled) return; 

                const limit = this.getAttribute('data-limit');
                if (this.type === 'checkbox' && limit && this.checked) {
                    const maxCount = parseInt(limit);
                    const groupName = this.name; 
                    const checkedCount = $container.querySelectorAll(`input[name="${groupName}"]:checked`).length;
                    if (checkedCount > maxCount) {
                        if(typeof uiCommon !== 'undefined') uiCommon.fnShowAlertModal(`최대 ${maxCount}개까지만 선택 가능합니다.`);
                        else alert(`최대 ${maxCount}개까지만 선택 가능합니다.`);
                        this.checked = false; 
                        return;
                    }
                }
                
                const optItem = this.closest('.opt-item');
                const myEtcTextarea = optItem ? optItem.querySelector('.input_etc') : null;
                const isEtc = this.getAttribute('data-is-etc');

                if (isEtc === 'Y' && myEtcTextarea) {
                    myEtcTextarea.disabled = !this.checked;
                    if (!this.checked) myEtcTextarea.value = '';
                }

                if (this.type === 'radio' && this.checked) {
                    const dl = this.closest('dl');
                    if (dl) {
                        const allEtcAreas = dl.querySelectorAll('.input_etc');
                        allEtcAreas.forEach(area => {
                            if (area !== myEtcTextarea) {
                                area.disabled = true;
                                area.value = '';
                            }
                        });
                    }
                }
            });
        });
        
        const rankChecks = $container.querySelectorAll('.rank-check');
        rankChecks.forEach(chk => {
            chk.addEventListener('change', function() {
                const srvyQitemOid = this.getAttribute('data-qst-no');
                const txt = this.getAttribute('data-txt');
                const val = this.value;
                const rankInputs = $container.querySelectorAll(`.rank-input[data-qst-no="${srvyQitemOid}"]`);

                if (this.checked) {
                    let placed = false;
                    for (let input of rankInputs) {
                        if (input.value === "") {
                            input.value = txt;
                            input.dataset.srvyQitemOptOid = val;
                            placed = true;
                            break; 
                        }
                    }
                    if (!placed) {
                        this.checked = false; 
                        if(typeof uiCommon !== 'undefined') uiCommon.fnShowAlertModal("모든 순위가 지정되었습니다.");
                        else alert("모든 순위가 지정되었습니다.");
                    }
                } else {
                    rankInputs.forEach(input => {
                        if (input.dataset.srvyQitemOptOid === val) {
                            input.value = "";
                            input.dataset.srvyQitemOptOid = "";
                        }
                    });
                }
            });
        });

        const rankInputs = $container.querySelectorAll('.rank-input');
        rankInputs.forEach(input => {
            input.addEventListener('click', function() {
                if (this.value === "") return; 
                const srvyQitemOid = this.getAttribute('data-qst-no');
                const optNo = this.dataset.srvyQitemOptOid;
                this.value = "";
                this.dataset.srvyQitemOptOid = "";
                const relatedChk = $container.querySelector(`.rank-check[data-qst-no="${srvyQitemOid}"][value="${optNo}"]`);
                if (relatedChk) relatedChk.checked = false;
            });
        });
        
        const fileInputs = $container.querySelectorAll('input[type="file"]');
        fileInputs.forEach(input => {
            input.addEventListener('change', function() {
                const qstNo = this.getAttribute('data-qst-no');
                if (this.files && this.files.length > 0) {
                    const file = this.files[0];
                    if (!_this.validateFile(file)) {
                        _this.resetFileUI(qstNo, $container);
                        return;
                    }
                    const fileListContainer = $container.querySelector(`#file_view_${qstNo}`);
                    const fileTxt = this.closest('.file_upload').querySelector('.file_txt');
                    if (fileTxt) fileTxt.textContent = file.name;
                    fileListContainer.innerHTML = `<p>${file.name} <a href="javascript:;" class="icon_del" data-qst-no="${qstNo}">파일삭제</a></p>`;
                    const delBtn = fileListContainer.querySelector('.icon_del');
                    delBtn.addEventListener('click', (e) => {
                        e.preventDefault();
                        _this.resetFileUI(qstNo, $container);
                    });
                } 
            });
        });
    },
    
    validateFile: function (file) {
           if (!file) return false;
           const fileName = file.name;
           const fileExt = fileName.split('.').pop().toLowerCase();
           const systemAllowed = this.state.allowedExtensions.split(',').map(s => s.trim().toLowerCase());
           
           if (!systemAllowed.includes(fileExt)) {
               const msg = `이미지 파일만 업로드 가능합니다. (허용: ${this.state.allowedExtensions})`;
               if(typeof uiCommon !== 'undefined') uiCommon.fnShowAlertModal(msg); else alert(msg);
               return false;
           }

           const maxMb = parseInt(this.state.maxFileSize) || 10;
           const maxSizeInBytes = maxMb * 1024 * 1024;
           if (file.size > maxSizeInBytes) {
               const msg = `최대 ${maxMb}MB까지만 업로드 가능합니다.`;
               if(typeof uiCommon !== 'undefined') uiCommon.fnShowAlertModal(msg); else alert(msg);
               return false;
           }
           return true;
    },

    resetFileUI: function(qstNo, container) {
        const $container = container || document;
        const fileInput = $container.querySelector(`#ans_file_${qstNo}`);
        const fileListContainer = $container.querySelector(`#file_view_${qstNo}`);
        const fileTxt = fileInput ? fileInput.closest('.file_upload').querySelector('.file_txt') : null;

        if (fileInput) fileInput.value = ''; 
        if (fileListContainer) fileListContainer.innerHTML = '';
        if (fileTxt) fileTxt.textContent = "파일을 첨부해주세요";
    }        
};
