/**
 * PlanVista - 共通JavaScript関数
 * カレンダー、メインページ、スケジュールフォームで共有される関数
 */

/**
 * 時間ラベルを生成（0:00～23:00）
 */
function generateTimeLabels() {
    const timeLabels = document.getElementById('timeLabels');
    if (!timeLabels) return;
    
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
}

/**
 * 日時を "HH:MM" 形式にフォーマット
 * @param {Date} date - フォーマットする日付オブジェクト
 * @returns {string} フォーマットされた時刻文字列
 */
function formatTime(date) {
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return hours + ':' + minutes;
}

/**
 * 日付を "YYYY-MM-DD" 形式にフォーマット
 * @param {Date} date - フォーマットする日付オブジェクト
 * @returns {string} フォーマットされた日付文字列
 */
function formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

/**
 * カレンダーアイテム（スケジュールまたはレコード）を作成
 * @param {Object} data - アイテムデータ
 * @param {string} type - 'schedule' または 'record'
 * @returns {HTMLElement} 作成されたアイテム要素
 */
function createCalendarItem(data, type) {
    const item = document.createElement('div');
    item.className = 'calendar-item ' + (type === 'schedule' ? 'schedule-item' : 'record-item');
    
    const startMinutes = data.startHour * 60 + data.startMinute;
    const endMinutes = data.endHour * 60 + data.endMinute;
    const duration = endMinutes - startMinutes;
    
    const topPosition = (startMinutes / 60) * 60;
    const height = (duration / 60) * 60;
    
    item.style.top = topPosition + 'px';
    item.style.height = height + 'px';

    const startTime = String(data.startHour).padStart(2, '0') + ':' + String(data.startMinute).padStart(2, '0');
    const endTime = String(data.endHour).padStart(2, '0') + ':' + String(data.endMinute).padStart(2, '0');

    const isSmall = height < 30;
    if (isSmall) {
        item.classList.add('small-item');
    }

    let tooltipContent = '<strong>' + data.title + '</strong><br>' + startTime + ' ~ ' + endTime;
    if (data.memo) {
        tooltipContent += '<br>' + data.memo;
    }

    item.innerHTML = '<div class="item-content">' +
                   '<div class="item-title">' + data.title + '</div>' +
                   '<div class="item-time">' + startTime + ' ~ ' + endTime + '</div>' +
                   '</div>' +
                   '<div class="item-tooltip">' + tooltipContent + '</div>';

    return item;
}

/**
 * グリッドライン（横線）を生成
 * @param {HTMLElement} container - グリッドラインを追加するコンテナ要素
 */
function generateGridLines(container) {
    if (!container) return;
    
    for (let i = 0; i <= 24; i++) {
        const line = document.createElement('div');
        line.className = 'grid-line';
        line.style.top = (i * 60) + 'px';
        container.appendChild(line);
    }
}

/**
 * 現在時刻を更新
 */
function updateCurrentTime() {
    const currentTimeElement = document.getElementById('currentTime');
    if (!currentTimeElement) return;
    
    const now = new Date();
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    currentTimeElement.textContent = hours + ':' + minutes;
}

/**
 * 曜日名を取得
 * @param {number} dayIndex - 曜日インデックス（0=日曜、6=土曜）
 * @returns {string} 曜日の略称
 */
function getWeekdayName(dayIndex) {
    const weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    return weekdays[dayIndex];
}

/**
 * 月名を取得
 * @param {number} monthIndex - 月インデックス（0=1月、11=12月）
 * @returns {string} 月名
 */
function getMonthName(monthIndex) {
    const monthNames = ['1月', '2月', '3月', '4月', '5月', '6月',
                       '7月', '8月', '9月', '10月', '11月', '12月'];
    return monthNames[monthIndex];
}