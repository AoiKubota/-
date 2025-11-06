/**
 * PlanVista - Calendar Page JavaScript
 * カレンダーページの全機能を管理
 */

// グローバル変数
let currentYear, currentMonth;
let selectedDate = new Date();
let schedules = [];
let records = [];
let currentScheduleId = null;
let currentIsSchedule = true;

/**
 * 初期化処理
 */
function initCalendar() {
    const today = new Date();
    currentYear = today.getFullYear();
    currentMonth = today.getMonth();
    selectedDate = new Date(today);
    
    // Thymeleafから渡されたイベントデータを処理
    if (typeof eventsData !== 'undefined') {
        processEventsData(eventsData);
    }
    
    generateCalendar();
    updateDailySchedule();
}

/**
 * サーバーから渡されたイベントデータを処理
 * @param {Array} data - イベントデータ配列
 */
function processEventsData(data) {
    schedules = [];
    records = [];
    
    data.forEach(item => {
        const processedItem = {
            id: item.id,
            title: item.title,
            date: item.date,
            startHour: item.startHour,
            startMinute: item.startMinute,
            endHour: item.endHour,
            endMinute: item.endMinute,
            startTime: String(item.startHour).padStart(2, '0') + ':' + String(item.startMinute).padStart(2, '0'),
            endTime: String(item.endHour).padStart(2, '0') + ':' + String(item.endMinute).padStart(2, '0'),
            memo: item.memo || '',
            task: item.task || '',
            isSyncedFromGoogle: item.isSyncedFromGoogle || false,
            isSchedule: item.type === 'schedule'
        };
        
        if (item.type === 'schedule') {
            schedules.push(processedItem);
        } else if (item.type === 'record') {
            records.push(processedItem);
        }
    });
}

/**
 * 月次カレンダーを生成
 */
function generateCalendar() {
    const calendarGrid = document.getElementById('calendarGrid');
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
        const dayElement = createDayElement(day, true, false);
        calendarGrid.appendChild(dayElement);
    }
    
    // 当月の日付
    for (let day = 1; day <= daysInMonth; day++) {
        const date = new Date(currentYear, currentMonth, day);
        const isToday = date.toDateString() === new Date().toDateString();
        const isSelected = date.toDateString() === selectedDate.toDateString();
        const dayElement = createDayElement(day, false, isToday, isSelected, date);
        calendarGrid.appendChild(dayElement);
    }
    
    // 次月の日付を埋める
    const remainingCells = 42 - (firstDayOfWeek + daysInMonth);
    for (let day = 1; day <= remainingCells; day++) {
        const dayElement = createDayElement(day, true, false);
        calendarGrid.appendChild(dayElement);
    }
    
    updateMonthDisplay();
}

/**
 * 日付要素を作成
 */
function createDayElement(day, isOtherMonth, isToday, isSelected, date) {
    const dayElement = document.createElement('div');
    dayElement.className = 'calendar-day';
    
    if (isOtherMonth) {
        dayElement.classList.add('other-month');
    }
    if (isToday) {
        dayElement.classList.add('today');
    }
    if (isSelected) {
        dayElement.classList.add('selected');
    }
    
    if (date) {
        const dayOfWeek = date.getDay();
        if (dayOfWeek === 0) {
            dayElement.classList.add('sunday');
        } else if (dayOfWeek === 6) {
            dayElement.classList.add('saturday');
        }
        
        dayElement.onclick = function() {
            selectedDate = new Date(date);
            generateCalendar();
            updateDailySchedule();
        };
    }
    
    const dayNumber = document.createElement('div');
    dayNumber.className = 'day-number';
    dayNumber.textContent = day;
    dayElement.appendChild(dayNumber);
    
    // イベントインジケーターを追加
    if (date && !isOtherMonth) {
        const dateStr = formatDate(date);
        const dayEvents = schedules.filter(s => s.date === dateStr);
        const dayRecords = records.filter(r => r.date === dateStr);
        
        if (dayEvents.length > 0 || dayRecords.length > 0) {
            const indicator = document.createElement('div');
            indicator.className = 'event-indicator';
            dayElement.appendChild(indicator);
        }
    }
    
    return dayElement;
}

/**
 * 月表示を更新
 */
function updateMonthDisplay() {
    document.getElementById('currentYear').textContent = currentYear + '年';
    document.getElementById('currentMonth').textContent = getMonthName(currentMonth);
}

/**
 * 日次スケジュールを更新
 */
function updateDailySchedule() {
    const month = selectedDate.getMonth() + 1;
    const day = selectedDate.getDate();
    const weekday = getWeekdayName(selectedDate.getDay());
    
    document.getElementById('selectedDate').textContent = `${month}/${day} ${weekday}`;

    generateTimeLabels();
    
    const scheduleArea = document.getElementById('scheduleArea');
    const recordArea = document.getElementById('recordArea');
    
    scheduleArea.innerHTML = '';
    recordArea.innerHTML = '';

    // グリッドラインを生成
    generateGridLines(scheduleArea);
    generateGridLines(recordArea);
    
    const dateStr = formatDate(selectedDate);
    
    // スケジュールを表示
    schedules.filter(s => s.date === dateStr).forEach(schedule => {
        const item = createCalendarItem(schedule, 'schedule');
        item.onclick = function(e) {
            e.stopPropagation();
            showScheduleDetails(schedule);
        };
        scheduleArea.appendChild(item);
    });

    // レコードを表示
    records.filter(r => r.date === dateStr).forEach(record => {
        const item = createCalendarItem(record, 'record');
        item.onclick = function(e) {
            e.stopPropagation();
            showRecordDetail(record);
        };
        recordArea.appendChild(item);
    });
}

/**
 * 前月へ移動
 */
function prevMonth() {
    currentMonth--;
    if (currentMonth < 0) {
        currentMonth = 11;
        currentYear--;
    }
    generateCalendar();
}

/**
 * 次月へ移動
 */
function nextMonth() {
    currentMonth++;
    if (currentMonth > 11) {
        currentMonth = 0;
        currentYear++;
    }
    generateCalendar();
}

/**
 * 前日へ移動
 */
function prevDay() {
    selectedDate.setDate(selectedDate.getDate() - 1);
    if (selectedDate.getMonth() !== currentMonth) {
        currentMonth = selectedDate.getMonth();
        currentYear = selectedDate.getFullYear();
        generateCalendar();
    } else {
        updateDailySchedule();
        generateCalendar();
    }
}

/**
 * 次日へ移動
 */
function nextDay() {
    selectedDate.setDate(selectedDate.getDate() + 1);
    if (selectedDate.getMonth() !== currentMonth) {
        currentMonth = selectedDate.getMonth();
        currentYear = selectedDate.getFullYear();
        generateCalendar();
    } else {
        updateDailySchedule();
        generateCalendar();
    }
}

/**
 * スケジュール詳細モーダルを表示
 */
function showScheduleDetails(schedule) {
    const modalElement = document.getElementById('scheduleDetailModal');
    if (!modalElement) {
        console.error('Modal element not found!');
        return;
    }

    document.getElementById('modal-title').textContent = schedule.title;
    document.getElementById('scheduleDate').textContent = schedule.date;
    document.getElementById('scheduleTime').textContent = 
        `${schedule.startTime} ~ ${schedule.endTime}`;
    document.getElementById('scheduleTask').textContent = schedule.task || 'なし';
    document.getElementById('scheduleMemo').textContent = schedule.memo || 'なし';
    
    currentScheduleId = schedule.id;
    currentIsSchedule = schedule.isSchedule;
 
    const btnEdit = document.getElementById('btnEditSchedule');
    const btnDelete = document.getElementById('btnDeleteSchedule');
    
    // 手動登録スケジュールのみ編集・削除可能
    if (schedule.isSchedule && !schedule.isSyncedFromGoogle) {
        btnEdit.style.display = 'inline-block';
        btnDelete.style.display = 'inline-block';
    } else {
        btnEdit.style.display = 'none';
        btnDelete.style.display = 'none';
    }

    const modal = new bootstrap.Modal(modalElement);
    modal.show();
}

/**
 * スケジュールを編集
 */
function editSchedule() {
    if (currentScheduleId) {
        window.location.href = `/schedule_update?id=${currentScheduleId}`;
    }
}

/**
 * スケジュールを削除
 */
function deleteSchedule() {
    if (!currentScheduleId) return;
    
    if (confirm('このスケジュールを削除しますか?')) {
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/schedule_delete';
        
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'scheduleId';
        input.value = currentScheduleId;
        
        form.appendChild(input);
        document.body.appendChild(form);
        form.submit();
    }
}

/**
 * レコード詳細モーダルを表示
 */
function showRecordDetail(data) {
    document.getElementById('recordDate').textContent = data.date;
    
    const startTime = `${String(data.startHour).padStart(2, '0')}:${String(data.startMinute).padStart(2, '0')}`;
    const endTime = `${String(data.endHour).padStart(2, '0')}:${String(data.endMinute).padStart(2, '0')}`;
    document.getElementById('recordTime').textContent = `${startTime}～${endTime}`;
    
    document.getElementById('recordTask').textContent = data.task;
    document.getElementById('recordMemo').textContent = data.memo || 'メモなし';
    
    const modal = new bootstrap.Modal(document.getElementById('recordDetailModal'));
    modal.show();
}

/**
 * 新しいタスクを追加
 */
function addNewTask() {
    const newTaskName = document.getElementById('newTaskName').value.trim();
    
    if (newTaskName === '') {
        alert('タスク名を入力してください');
        return;
    }
    
    const modal = bootstrap.Modal.getInstance(document.getElementById('addTaskModal'));
    modal.hide();
    
    document.getElementById('newTaskName').value = '';
    alert('タスク「' + newTaskName + '」を追加しました');
}

// DOMContentLoaded時の初期化
document.addEventListener('DOMContentLoaded', function() {
    initCalendar();
});