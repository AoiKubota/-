/**
 * PlanVista - Schedule Form JavaScript
 * スケジュール登録・更新フォームの機能を管理
 */

/**
 * タスク選択時に推定時間を取得して表示
 */
function setupTaskEstimation() {
    const taskSelect = document.getElementById('task');
    if (!taskSelect) return;
    
    taskSelect.addEventListener('change', function() {
        const taskName = this.value;
        if (taskName) {
            fetchEstimatedTime(taskName);
        } else {
            document.getElementById('estimatedTime').textContent = '--:--';
        }
    });
}

/**
 * タスクの推定時間をAPIから取得
 * @param {string} taskName - タスク名
 */
function fetchEstimatedTime(taskName) {
    fetch('/api/estimated_time?taskName=' + encodeURIComponent(taskName))
        .then(response => response.json())
        .then(data => {
            if (data.estimatedTime) {
                document.getElementById('estimatedTime').textContent = data.estimatedTime;
            }
        })
        .catch(error => console.error('Error:', error));
}

/**
 * 新規タスク入力欄を表示
 */
function showNewTaskInput() {
    document.getElementById('newTaskInput').style.display = 'block';
}

/**
 * 新規タスクを追加
 */
function addNewTask() {
    const taskName = document.getElementById('newTaskName').value.trim();
    if (!taskName) {
        alert('タスク名を入力してください');
        return;
    }

    fetch('/task_add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'taskName=' + encodeURIComponent(taskName)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // セレクトボックスに追加
            const select = document.getElementById('task');
            const option = document.createElement('option');
            option.value = data.task.taskName;
            option.text = data.task.taskName;
            option.selected = true;
            select.add(option);
            
            // 入力欄を非表示にしてリセット
            document.getElementById('newTaskInput').style.display = 'none';
            document.getElementById('newTaskName').value = '';
            
            // 推定時間を更新（更新ページの場合）
            if (typeof updateEstimatedTime === 'function') {
                updateEstimatedTime(data.task.taskName);
            }
            
            alert(data.message);
        } else {
            alert(data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('タスクの追加に失敗しました');
    });
}

/**
 * 推定時間を更新（schedule_update.html専用）
 * @param {string} taskName - タスク名
 */
function updateEstimatedTime(taskName) {
    if (taskName) {
        fetch('/api/estimated_time?taskName=' + encodeURIComponent(taskName))
            .then(response => response.json())
            .then(data => {
                if (data.estimatedTime) {
                    document.getElementById('estimatedTime').textContent = data.estimatedTime;
                }
            })
            .catch(error => console.error('Error:', error));
    } else {
        document.getElementById('estimatedTime').textContent = '--:--';
    }
}

/**
 * schedule_update.html用の初期化
 */
function initScheduleUpdate() {
    const taskSelect = document.getElementById('task');
    if (taskSelect && taskSelect.value) {
        updateEstimatedTime(taskSelect.value);
    }
    
    // タスク選択変更時のイベントリスナーを設定
    if (taskSelect) {
        taskSelect.addEventListener('change', function() {
            updateEstimatedTime(this.value);
        });
    }
}

// DOMContentLoaded時の初期化
document.addEventListener('DOMContentLoaded', function() {
    setupTaskEstimation();
    
    // schedule_update.htmlの場合は追加の初期化を実行
    if (document.getElementById('estimatedTime') && 
        document.getElementById('estimatedTime').textContent !== '--:--') {
        initScheduleUpdate();
    }
});