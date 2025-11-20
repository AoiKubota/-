/**
 * PlanVista - Members Calendar Page JavaScript
 * メンバーカレンダーページの機能を管理
 */

// グローバル変数
let targetUserId;
let isViewOnly = true;
let currentYear, currentMonth;
let selectedDate = new Date();
let schedules = [];

/**
 * カレンダーを初期化（メンバーカレンダー用）
 * @param {number} userId - 表示対象のユーザーID
 * @param {boolean} viewOnly - 閲覧専用モードかどうか
 */
function initCalendar(userId, viewOnly = true) {
    console.log(`=== メンバーカレンダー初期化 ===`);
    console.log(`ユーザーID: ${userId}, 閲覧専用: ${viewOnly}`);
    
    targetUserId = userId;
    isViewOnly = viewOnly;
    
    const today = new Date();
    currentYear = today.getFullYear();
    currentMonth = today.getMonth();
    selectedDate = new Date(today);
    
    // イベントリスナーを設定
    setupEventListeners();
    
    // カレンダーを描画
    generateCalendar();
    updateDailySchedule();
}

/**
 * イベントリスナーを設定
 */
function setupEventListeners() {
    // 月ナビゲーション
    document.getElementById('prevMonth')?.addEventListener('click', () => {
        currentMonth--;
        if (currentMonth < 0) {
            currentMonth = 11;
            currentYear--;
        }
        generateCalendar();
        updateDailySchedule();
    });
    
    document.getElementById('nextMonth')?.addEventListener('click', () => {
        currentMonth++;
        if (currentMonth > 11) {
            currentMonth = 0;
            currentYear++;
        }
        generateCalendar();
        updateDailySchedule();
    });
    
    // 日ナビゲーション
    document.getElementById('prevDay')?.addEventListener('click', () => {
        selectedDate.setDate(selectedDate.getDate() - 1);
        if (selectedDate.getMonth() !== currentMonth) {
            currentMonth = selectedDate.getMonth();
            currentYear = selectedDate.getFullYear();
            generateCalendar();
        }
        updateDailySchedule();
    });
    
    document.getElementById('nextDay')?.addEventListener('click', () => {
        selectedDate.setDate(selectedDate.getDate() + 1);
        if (selectedDate.getMonth() !== currentMonth) {
            currentMonth = selectedDate.getMonth();
            currentYear = selectedDate.getFullYear();
            generateCalendar();
        }
        updateDailySchedule();
    });
}

/**
 * 月次カレンダーを生成
 */
function generateCalendar() {
    // 年月を取得
    const yearMonth = `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}`;
    
    // 月の表示を更新
    updateMonthDisplay();
    
    // APIからスケジュールを取得
    fetchMonthSchedules(yearMonth);
}

/**
 * 月のスケジュールを取得
 */
function fetchMonthSchedules(yearMonth) {
    console.log(`月間スケジュール取得: ${yearMonth}`);
    
    fetch(`/api/members_calendar/${targetUserId}/schedules?yearMonth=${yearMonth}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTPエラー: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('取得したスケジュール:', data);
            schedules = data.schedules || [];
            drawMonthCalendar();
        })
        .catch(error => {
            console.error('スケジュール取得エラー:', error);
            schedules = [];
            drawMonthCalendar();
        });
}

/**
 * 月カレンダーを描画
 */
function drawMonthCalendar() {
    const calendarGrid = document.getElementById('monthCalendar');
    if (!calendarGrid) {
        console.error('monthCalendarエレメントが見つかりません');
        return;
    }
    
    // 既存のコンテンツをクリア
    calendarGrid.innerHTML = '';
    
    // 曜日ヘッダーを追加
    const dayHeaders = ['日', '月', '火', '水', '木', '金', '土'];
    dayHeaders.forEach(day => {
        const header = document.createElement('div');
        header.className = 'calendar-day-header';
        header.textContent = day;
        calendarGrid.appendChild(header);
    });
    
    // 月の初日と最終日を取得
    const firstDay = new Date(currentYear, currentMonth, 1);
    const lastDay = new Date(currentYear, currentMonth + 1, 0);
    const firstDayOfWeek = firstDay.getDay();
    const daysInMonth = lastDay.getDate();
    
    // 前月の日付を埋める
    const prevMonthLastDay = new Date(currentYear, currentMonth, 0).getDate();
    for (let i = firstDayOfWeek - 1; i >= 0; i--) {
        const day = prevMonthLastDay - i;
        const dayElement = document.createElement('div');
        dayElement.className = 'calendar-day other-month';
        dayElement.innerHTML = `<div class="day-number">${day}</div>`;
        calendarGrid.appendChild(dayElement);
    }
    
    // 当月の日付
    for (let day = 1; day <= daysInMonth; day++) {
        const date = new Date(currentYear, currentMonth, day);
        const isToday = date.toDateString() === new Date().toDateString();
        const isSelected = date.toDateString() === selectedDate.toDateString();
        const dateStr = formatDate(date);
        
        // その日のスケジュール数をカウント
        const daySchedules = schedules.filter(s => {
            if (!s.startTime) return false;
            const scheduleDate = new Date(s.startTime);
            return scheduleDate.getFullYear() === currentYear &&
                   scheduleDate.getMonth() === currentMonth &&
                   scheduleDate.getDate() === day;
        });
        
        const dayElement = document.createElement('div');
        dayElement.className = 'calendar-day';
        
        if (isToday) dayElement.classList.add('today');
        if (isSelected) dayElement.classList.add('selected');
        
        const dayOfWeek = date.getDay();
        if (dayOfWeek === 0) dayElement.classList.add('sunday');
        if (dayOfWeek === 6) dayElement.classList.add('saturday');
        
        const dayNumber = document.createElement('div');
        dayNumber.className = 'day-number';
        dayNumber.textContent = day;
        dayElement.appendChild(dayNumber);
        
        // スケジュールがある場合
        if (daySchedules.length > 0) {
            const dayEvents = document.createElement('div');
            dayEvents.className = 'day-events';
            
            // 最大2件まで表示
            daySchedules.slice(0, 2).forEach(schedule => {
                const eventTitle = document.createElement('div');
                eventTitle.style.fontSize = '10px';
                eventTitle.style.overflow = 'hidden';
                eventTitle.style.textOverflow = 'ellipsis';
                eventTitle.style.whiteSpace = 'nowrap';
                eventTitle.style.color = '#0d6efd';
                eventTitle.textContent = schedule.title;
                dayEvents.appendChild(eventTitle);
            });
            
            // 3件以上ある場合は「+n件」を表示
            if (daySchedules.length > 2) {
                const moreText = document.createElement('div');
                moreText.style.fontSize = '9px';
                moreText.style.color = '#6c757d';
                moreText.textContent = `+${daySchedules.length - 2}件`;
                dayEvents.appendChild(moreText);
            }
            
            dayElement.appendChild(dayEvents);
        }
        
        // クリックイベント
        dayElement.onclick = function() {
            selectDate(dateStr);
        };
        
        calendarGrid.appendChild(dayElement);
    }
    
    // 次月の日付を埋める
    const remainingCells = 42 - (firstDayOfWeek + daysInMonth);
    for (let day = 1; day <= remainingCells; day++) {
        const dayElement = document.createElement('div');
        dayElement.className = 'calendar-day other-month';
        dayElement.innerHTML = `<div class="day-number">${day}</div>`;
        calendarGrid.appendChild(dayElement);
    }
    
    console.log('月カレンダー描画完了');
}

/**
 * 日別スケジュールを更新
 */
function updateDailySchedule() {
    const dateStr = formatDate(selectedDate);
    
    // 日付の表示を更新
    updateDayDisplay();
    
    // APIから日別スケジュールを取得
    console.log(`日別スケジュール取得: ${dateStr}`);
    
    fetch(`/api/members_calendar/${targetUserId}/schedules/day?date=${dateStr}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTPエラー: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('取得した日別スケジュール:', data);
            drawTimeline(data.schedules || []);
        })
        .catch(error => {
            console.error('日別スケジュール取得エラー:', error);
            drawTimeline([]);
        });
}

/**
 * タイムラインを描画
 */
function drawTimeline(daySchedules) {
    const timeLabels = document.getElementById('timeLabels');
    const timeline = document.getElementById('scheduleTimeline');
    
    if (!timeLabels || !timeline) {
        console.error('タイムライン要素が見つかりません');
        return;
    }
    
    // 時間ラベルを生成
    timeLabels.innerHTML = '';
    for (let hour = 0; hour < 24; hour++) {
        const slot = document.createElement('div');
        slot.className = 'time-slot';
        
        const label = document.createElement('div');
        label.className = 'time-label';
        label.textContent = String(hour).padStart(2, '0') + ':00';
        
        slot.appendChild(label);
        timeLabels.appendChild(slot);
    }
    
    // タイムラインをクリア
    timeline.innerHTML = '';
    
    // グリッド線を描画
    for (let hour = 0; hour <= 24; hour++) {
        const line = document.createElement('div');
        line.className = 'grid-line';
        line.style.top = (hour * 60) + 'px';
        timeline.appendChild(line);
    }
    
    // スケジュールを描画
    daySchedules.forEach(schedule => {
        const startTime = new Date(schedule.startTime);
        const endTime = new Date(schedule.endTime);
        
        const startMinutes = startTime.getHours() * 60 + startTime.getMinutes();
        const endMinutes = endTime.getHours() * 60 + endTime.getMinutes();
        const duration = endMinutes - startMinutes;
        
        const topPosition = (startMinutes / 60) * 60;
        const height = (duration / 60) * 60;
        
        const startTimeStr = formatTime(startTime);
        const endTimeStr = formatTime(endTime);
        
        const item = document.createElement('div');
        item.className = 'calendar-item';
        
        if (schedule.isSyncedFromGoogle) {
            item.classList.add('google-item');
        } else {
            item.classList.add('schedule-item');
        }
        
        item.style.top = topPosition + 'px';
        item.style.height = height + 'px';
        
        // 30分未満の場合は小さいアイテムとして扱う
        const isSmall = height < 30;
        if (isSmall) {
            item.classList.add('small-item');
        }
        
        // ツールチップの内容を作成
        let tooltipContent = '<strong>' + escapeHtml(schedule.title) + '</strong><br>' + 
                            startTimeStr + ' ~ ' + endTimeStr;
        if (schedule.task) {
            tooltipContent += '<br>' + escapeHtml(schedule.task);
        }
        if (schedule.memo) {
            tooltipContent += '<br>' + escapeHtml(schedule.memo);
        }
        
        // アイテムの内容
        item.innerHTML = '<div class="item-content">' +
                       '<div class="item-title">' + escapeHtml(schedule.title) + '</div>' +
                       '<div class="item-time">' + startTimeStr + ' ~ ' + endTimeStr + '</div>' +
                       '</div>' +
                       '<div class="item-tooltip">' + tooltipContent + '</div>';
        
        timeline.appendChild(item);
    });
    
    console.log('タイムライン描画完了:', daySchedules.length + '件');
}

/**
 * 月の表示を更新
 */
function updateMonthDisplay() {
    const monthElement = document.getElementById('currentMonth');
    if (monthElement) {
        const monthNames = ['1月', '2月', '3月', '4月', '5月', '6月', 
                           '7月', '8月', '9月', '10月', '11月', '12月'];
        monthElement.textContent = `${currentYear}年 ${monthNames[currentMonth]}`;
    }
}

/**
 * 日の表示を更新
 */
function updateDayDisplay() {
    const dayElement = document.getElementById('currentDay');
    if (dayElement) {
        const weekdays = ['日', '月', '火', '水', '木', '金', '土'];
        const weekday = weekdays[selectedDate.getDay()];
        dayElement.textContent = 
            `${selectedDate.getMonth() + 1}/${selectedDate.getDate()} (${weekday})`;
    }
}

/**
 * 日付を選択
 */
function selectDate(dateStr) {
    selectedDate = new Date(dateStr);
    
    // 月が変わった場合はカレンダーを再生成
    if (selectedDate.getMonth() !== currentMonth || 
        selectedDate.getFullYear() !== currentYear) {
        currentMonth = selectedDate.getMonth();
        currentYear = selectedDate.getFullYear();
        generateCalendar();
    } else {
        // 月が同じ場合は選択状態だけ更新
        drawMonthCalendar();
    }
    
    updateDailySchedule();
}

/**
 * 日付をフォーマット (YYYY-MM-DD)
 */
function formatDate(date) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

/**
 * 時刻をフォーマット (HH:MM)
 */
function formatTime(date) {
    return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
}

/**
 * HTMLエスケープ
 */
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}