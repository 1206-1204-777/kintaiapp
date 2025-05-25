// グローバル変数
let refreshTimer = null;

// 認証チェック
document.addEventListener('DOMContentLoaded', function() {
    try {
        const userId = localStorage.getItem('userId');
        const username = localStorage.getItem('username');

        if (!userId || !username) {
            window.location.href = 'login.html';
            return;
        }

        const userDisplayElement = document.getElementById('userDisplay');
        if (userDisplayElement) {
            userDisplayElement.textContent = username;
        }

        // 時計の初期化と更新
        updateClock();
        setInterval(updateClock, 1000);

        // 自動更新タイマーの設定（30秒ごとに状態確認）
        startAutoRefresh();

        // 初期データ読み込み
        initializeApp();

        const monthSelectElement = document.getElementById('monthSelect');
        if (monthSelectElement) {
            monthSelectElement.value = getCurrentMonth();
        }

        setupEventListeners();
    } catch (error) {
        console.error('初期化エラー:', error);
        showError('ページの初期化中にエラーが発生しました。ページをリロードしてください。');
    }
});

// アプリケーション初期化
async function initializeApp() {
    try {
        await Promise.all([
            loadTodayAttendance(),
            loadAttendanceHistory(getCurrentMonth()),
            checkAttendanceStatus(),
            loadScheduledTime()
        ]);
    } catch (error) {
        console.error('アプリケーション初期化エラー:', error);
        showError('データの読み込み中にエラーが発生しました。');
    }
}

// 自動更新タイマーの開始
function startAutoRefresh() {
    stopAutoRefresh(); // 既存のタイマーがあれば停止
    
    refreshTimer = setInterval(async () => {
        try {
            await checkAttendanceStatus();
            await loadTodayAttendance();
        } catch (error) {
            console.error('自動更新エラー:', error);
            // エラーがあってもタイマーは続行
        }
    }, 30000); // 30秒ごとに更新
}

// 自動更新タイマーの停止
function stopAutoRefresh() {
    if (refreshTimer) {
        clearInterval(refreshTimer);
        refreshTimer = null;
    }
}

// 現在時刻更新
function updateClock() {
    try {
        const now = new Date();
        const timeString = now.toLocaleTimeString('ja-JP');
        const currentTimeElement = document.getElementById('currentTime');
        if (currentTimeElement) {
            currentTimeElement.textContent = timeString;
        }
    } catch (error) {
        console.error('時刻更新エラー:', error);
    }
}

// 現在の年月を取得（YYYY-MM形式）
function getCurrentMonth() {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
}

// 今日の勤怠情報を読み込む
async function loadTodayAttendance() {
    try {
        const userId = localStorage.getItem('userId');
        if (!userId) return;

        const today = new Date().toISOString().split('T')[0];
        const attendanceResponse = await fetchWithTimeout(`/api/attendance/${userId}/date/${today}`);

        if (attendanceResponse.ok) {
            const attendanceData = await attendanceResponse.json();

            if (attendanceData) {
                document.getElementById('todayClockIn').textContent = formatTime(attendanceData.clockIn);
                document.getElementById('todayClockOut').textContent = formatTime(attendanceData.clockOut);

                if (attendanceData.clockIn && attendanceData.clockOut) {
                    const workHours = calculateWorkHours(attendanceData.clockIn, attendanceData.clockOut);
                    document.getElementById('todayWorkHours').textContent = workHours;
                } else {
                    document.getElementById('todayWorkHours').textContent = '-';
                }

                // 今日の休憩時間合計のAPI呼び出し
                try {
                    const breakTotalResponse = await fetchWithTimeout(`/api/break/total/${userId}/today`);
                    if(breakTotalResponse.ok) {
                        const breakTotalData = await breakTotalResponse.json();
                        if(breakTotalData && breakTotalData.totalMinutes !== undefined && breakTotalData.totalMinutes !== null) {
                            document.getElementById('todayTotalBreakHours').textContent = formatMinutesToHoursAndMinutes(breakTotalData.totalMinutes);
                        } else {
                            document.getElementById('todayTotalBreakHours').textContent = '0分';
                        }
                    } else {
                        console.warn('今日の休憩時間合計取得APIエラー:', breakTotalResponse.status, breakTotalResponse.statusText);
                        document.getElementById('todayTotalBreakHours').textContent = '-';
                    }
                } catch (breakError) {
                    console.error('今日の休憩時間合計取得エラー:', breakError);
                    document.getElementById('todayTotalBreakHours').textContent = '-';
                }
            } else {
                resetTodayAttendanceDisplay();
            }
        } else {
            console.error('今日の勤怠データ取得エラー:', attendanceResponse.status, attendanceResponse.statusText);
            resetTodayAttendanceDisplay();
        }
    } catch (error) {
        console.error('今日の勤怠データ取得エラー:', error);
        resetTodayAttendanceDisplay();
    }
}

// 今日の勤怠表示をリセット
function resetTodayAttendanceDisplay() {
    document.getElementById('todayClockIn').textContent = '-';
    document.getElementById('todayClockOut').textContent = '-';
    document.getElementById('todayWorkHours').textContent = '-';
    document.getElementById('todayTotalBreakHours').textContent = '-';
}

// 勤怠履歴を読み込む
async function loadAttendanceHistory(month) {
    try {
        const userId = localStorage.getItem('userId');
        if (!userId) return;

        const tableLoadingElement = document.getElementById('tableLoading');
        if (tableLoadingElement) {
            tableLoadingElement.style.display = 'block';
        }

        console.log(`勤怠履歴取得: userId=${userId}, month=${month}`);

        const response = await fetchWithTimeout(`/api/attendance/monthly/${userId}?month=${month}`);

        if (response.ok) {
            const data = await response.json();

            const attendanceTableElement = document.getElementById('attendanceTable');
            if (!attendanceTableElement) {
                console.error('要素が見つかりません: attendanceTable');
                return;
            }

            if (data && data.length > 0) {
                renderAttendanceTable(data);
            } else {
                attendanceTableElement.innerHTML =
                    '<tr><td colspan="6" style="text-align: center;">記録がありません</td></tr>';
            }
        } else {
            console.error('勤怠履歴APIエラー:', response.status, response.statusText);
            document.getElementById('attendanceTable').innerHTML =
                `<tr><td colspan="6" style="text-align: center;">データの取得に失敗しました (${response.status})</td></tr>`;
        }
    } catch (error) {
        console.error('勤怠履歴取得エラー:', error);
        const attendanceTableElement = document.getElementById('attendanceTable');
        if (attendanceTableElement) {
            attendanceTableElement.innerHTML =
                '<tr><td colspan="6" style="text-align: center;">データの取得に失敗しました</td></tr>';
        }
    } finally {
        const tableLoadingElement = document.getElementById('tableLoading');
        if (tableLoadingElement) {
            tableLoadingElement.style.display = 'none';
        }
    }
}

// 勤怠テーブルを描画
function renderAttendanceTable(attendances) {
    try {
        const tableBody = document.getElementById('attendanceTable');
        if (!tableBody) {
            console.error('要素が見つかりません: attendanceTable');
            return;
        }

        tableBody.innerHTML = '';

        attendances.forEach(item => {
            const row = document.createElement('tr');

            const dateCell = document.createElement('td');
            const date = new Date(item.date);
            dateCell.textContent = date.toLocaleDateString('ja-JP');
            row.appendChild(dateCell);

            const clockInCell = document.createElement('td');
            clockInCell.textContent = formatTime(item.clockIn);
            row.appendChild(clockInCell);

            const clockOutCell = document.createElement('td');
            clockOutCell.textContent = formatTime(item.clockOut);
            row.appendChild(clockOutCell);

            const workHoursCell = document.createElement('td');
            workHoursCell.textContent = item.workHours !== undefined && item.workHours !== null ?
                                        formatMinutesToHoursAndMinutes(item.workHours) :
                                        (item.clockIn && item.clockOut ? calculateWorkHours(item.clockIn, item.clockOut) : '-');
            row.appendChild(workHoursCell);

            const totalBreakHoursCell = document.createElement('td');
            totalBreakHoursCell.textContent = item.totalBreakMinutes !== undefined && item.totalBreakMinutes !== null ?
                                            formatMinutesToHoursAndMinutes(item.totalBreakMinutes) : '-';
            row.appendChild(totalBreakHoursCell);


            const statusCell = document.createElement('td');
            if (item.clockIn && item.clockOut) {
                statusCell.innerHTML = '<span style="color: #4CAF50;">完了</span>';
            } else if (item.clockIn) {
                statusCell.innerHTML = '<span style="color: #FFC107;">勤務中</span>';
            } else {
                statusCell.innerHTML = '<span style="color: #6c757d;">未打刻</span>';
            }
            row.appendChild(statusCell);

            tableBody.appendChild(row);
        });
    } catch (error) {
        console.error('テーブル描画エラー:', error);
    }
}

// 勤怠状態を確認する
async function checkAttendanceStatus() {
    try {
        const userId = localStorage.getItem('userId');
        if (!userId) return;

        const statusElement = document.getElementById('attendanceStatus');
        const clockInBtn = document.getElementById('clockInButton');
        const clockOutBtn = document.getElementById('clockOutButton');
        const breakStartBtn = document.getElementById('breakStartButton');
        const breakEndBtn = document.getElementById('breakEndButton');

        if (!statusElement || !clockInBtn || !clockOutBtn || !breakStartBtn || !breakEndBtn) {
            console.error('勤怠状態UI要素が見つかりません');
            // エラー時は全ての打刻ボタンを無効化
            disableAllButtons();
            return;
        }

        // デフォルトで全てのボタンを無効化しておく
        disableAllButtons();
        statusElement.textContent = '状態確認中...';
        statusElement.className = 'attendance-status'; // クラスをリセット

        let isWorking = false;
        let isOngoingBreak = false;

        // 並行して両方のAPIを呼び出し
        const [attendanceStatusResponse, breakStatusResponse] = await Promise.allSettled([
            fetchWithTimeout(`/api/attendance/${userId}/status`),
            fetchWithTimeout(`/api/break/status/${userId}/today`)
        ]);

        // 勤怠ステータスの処理
        if (attendanceStatusResponse.status === 'fulfilled' && attendanceStatusResponse.value.ok) {
            const attendanceStatusData = await attendanceStatusResponse.value.json();
            isWorking = attendanceStatusData.working;
        } else {
            console.warn('勤怠状態確認APIエラー:', 
                attendanceStatusResponse.status === 'fulfilled' 
                    ? `${attendanceStatusResponse.value.status}: ${attendanceStatusResponse.value.statusText}` 
                    : attendanceStatusResponse.reason);
        }

        // 休憩ステータスの処理
        if (breakStatusResponse.status === 'fulfilled' && breakStatusResponse.value.ok) {
            const breakStatusData = await breakStatusResponse.value.json();
            isOngoingBreak = breakStatusData.ongoingBreak;
        } else {
            console.warn('休憩状態確認APIエラー:', 
                breakStatusResponse.status === 'fulfilled' 
                    ? `${breakStatusResponse.value.status}: ${breakStatusResponse.value.statusText}` 
                    : breakStatusResponse.reason);
        }

        // APIの結果に基づいてボタンの有効/無効を決定
        updateUIBasedOnStatus(isWorking, isOngoingBreak, statusElement, clockInBtn, clockOutBtn, breakStartBtn, breakEndBtn);

    } catch (error) {
        console.error('勤怠状態確認エラー (最終catch):', error);
        const statusElement = document.getElementById('attendanceStatus');
        if (statusElement) {
            statusElement.textContent = 'ステータス取得エラー';
            statusElement.className = 'attendance-status';
        }
        disableAllButtons();
    }
}

// 状態に基づいてUIを更新
function updateUIBasedOnStatus(isWorking, isOngoingBreak, statusElement, clockInBtn, clockOutBtn, breakStartBtn, breakEndBtn) {
    if (isOngoingBreak) { // 休憩中の場合
        statusElement.textContent = '休憩中';
        statusElement.className = 'attendance-status status-breaking';
        breakEndBtn.disabled = false; // 休憩中は休憩終了できる
    } else if (isWorking) { // 勤務中の場合 (休憩中でない)
        statusElement.textContent = '勤務中';
        statusElement.className = 'attendance-status status-working';
        clockOutBtn.disabled = false;
        breakStartBtn.disabled = false; // 勤務中なら休憩開始できる
    } else { // 退勤済み、または未出勤の場合
        statusElement.textContent = '退勤中'; // もしくは「未出勤」
        statusElement.className = 'attendance-status status-off';
        clockInBtn.disabled = false; // 出勤ボタン有効
    }
}

// 全てのボタンを無効化
function disableAllButtons() {
    const clockInBtn = document.getElementById('clockInButton');
    const clockOutBtn = document.getElementById('clockOutButton');
    const breakStartBtn = document.getElementById('breakStartButton');
    const breakEndBtn = document.getElementById('breakEndButton');
    
    if (clockInBtn) clockInBtn.disabled = true;
    if (clockOutBtn) clockOutBtn.disabled = true;
    if (breakStartBtn) breakStartBtn.disabled = true;
    if (breakEndBtn) breakEndBtn.disabled = true;
}

// 出勤処理
async function clockIn() {
    try {
        const userId = localStorage.getItem('userId');
        if (!userId) {
            showError('ユーザー情報が見つかりません。再度ログインしてください。');
            return;
        }

        showLoading(true);
        clearMessages(); // 既存のメッセージをクリア
        disableAllButtons(); // 処理中は全ボタンを無効化

        const response = await fetchWithTimeout(`/api/attendance/clock-in/${userId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            showSuccess('出勤打刻が完了しました');
            await refreshDataAfterAction();
        } else {
            const errorData = await response.json().catch(() => null);
            if (errorData && errorData.message) {
                showError(errorData.message);
            } else {
                showError(`出勤打刻に失敗しました (${response.status})`);
            }
            await checkAttendanceStatus(); // エラーでもUI状態を更新
        }
    } catch (error) {
        console.error('出勤打刻エラー:', error);
        showError('サーバーとの通信中にエラーが発生しました');
        await checkAttendanceStatus(); // エラーでもUI状態を更新
    } finally {
        showLoading(false);
    }
}

// 退勤処理
async function clockOut() {
    try {
        const userId = localStorage.getItem('userId');
        if (!userId) {
            showError('ユーザー情報が見つかりません。再度ログインしてください。');
            return;
        }

        showLoading(true);
        clearMessages(); // 既存のメッセージをクリア
        disableAllButtons(); // 処理中は全ボタンを無効化

        const response = await fetchWithTimeout(`/api/attendance/clock-out/${userId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            showSuccess('退勤打刻が完了しました');
            await refreshDataAfterAction();
        } else {
            const errorData = await response.json().catch(() => null);
            if (errorData && errorData.message) {
                showError(errorData.message);
            } else {
                showError(`退勤打刻に失敗しました (${response.status})`);
            }
            await checkAttendanceStatus(); // エラーでもUI状態を更新
        }
    } catch (error) {
        console.error('退勤打刻エラー:', error);
        showError('サーバーとの通信中にエラーが発生しました');
        await checkAttendanceStatus(); // エラーでもUI状態を更新
    } finally {
        showLoading(false);
    }
}

// 休憩開始処理
async function startBreak() {
    try {
        const userId = localStorage.getItem('userId');
        if (!userId) {
            showError('ユーザー情報が見つかりません。再度ログインしてください。');
            return;
        }

        showLoading(true);
        clearMessages(); // 既存のメッセージをクリア
        disableAllButtons(); // 処理中は全ボタンを無効化

        const response = await fetchWithTimeout(`/api/break/start/${userId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            showSuccess('休憩開始を打刻しました');
            await refreshDataAfterAction();
        } else {
            const errorData = await response.json().catch(() => null);
            if (errorData && errorData.message) {
                showError(errorData.message);
            } else {
                showError(`休憩開始に失敗しました (${response.status})`);
            }
            await checkAttendanceStatus(); // エラーでもUI状態を更新
        }
    } catch (error) {
        console.error('休憩開始打刻エラー:', error);
        showError('サーバーとの通信中にエラーが発生しました');
        await checkAttendanceStatus(); // エラーでもUI状態を更新
    } finally {
        showLoading(false);
    }
}

// 休憩終了処理
async function endBreak() {
    try {
        const userId = localStorage.getItem('userId');
        if (!userId) {
            showError('ユーザー情報が見つかりません。再度ログインしてください。');
            return;
        }

        showLoading(true);
        clearMessages(); // 既存のメッセージをクリア
        disableAllButtons(); // 処理中は全ボタンを無効化

        const response = await fetchWithTimeout(`/api/break/end/${userId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            showSuccess('休憩終了を打刻しました');
            await refreshDataAfterAction();
        } else {
            const errorData = await response.json().catch(() => null);
            if (errorData && errorData.message) {
                showError(errorData.message);
            } else {
                showError(`休憩終了に失敗しました (${response.status})`);
            }
            await checkAttendanceStatus(); // エラーでもUI状態を更新
        }
    } catch (error) {
        console.error('休憩終了打刻エラー:', error);
        showError('サーバーとの通信中にエラーが発生しました');
        await checkAttendanceStatus(); // エラーでもUI状態を更新
    } finally {
        showLoading(false);
    }
}

// 操作後のデータ更新
async function refreshDataAfterAction() {
    try {
        await Promise.all([
            loadTodayAttendance(),
            checkAttendanceStatus(),
            loadAttendanceHistory(getCurrentMonth())
        ]);
    } catch (error) {
        console.error('データ更新エラー:', error);
    }
}

// 時間のフォーマット
function formatLocalTime(timeStr) {
    if (!timeStr) return '-';

    try {
        const date = new Date(timeStr);
        if (isNaN(date.getTime())) {
            const parts = timeStr.split(':');
            if (parts.length >= 2) {
                return `${parts[0]}:${parts[1]}`;
            }
            return timeStr;
        }
        return date.toLocaleTimeString('ja-JP', { hour: '2-digit', minute: '2-digit' });

    } catch (e) {
        console.error('ローカル時刻フォーマットエラー:', e);
        return '-';
    }
}

// 定時時刻の取得
async function loadScheduledTime() {
    try {
        const userId = localStorage.getItem('userId');
        if (!userId) return;
        
        const response = await fetchWithTimeout(`/api/users/${userId}/info`);
        if (response.ok) {
            const data = await response.json();
            const start = data.startTime ? formatLocalTime(data.startTime) : '-';
            const end = data.endTime ? formatLocalTime(data.endTime) : '-';
            document.getElementById('scheduledTime').textContent = `${start} ～ ${end}`;
        } else {
            console.error('定時時刻取得APIエラー:', response.status, response.statusText);
            document.getElementById('scheduledTime').textContent = '-';
        }
    } catch (e) {
        console.error('定時時刻取得エラー:', e);
        document.getElementById('scheduledTime').textContent = '-';
    }
}

// イベントリスナーの設定
function setupEventListeners() {
    try {
        const clockInButton = document.getElementById('clockInButton');
        if (clockInButton) {
            clockInButton.addEventListener('click', clockIn);
        }

        const clockOutButton = document.getElementById('clockOutButton');
        if (clockOutButton) {
            clockOutButton.addEventListener('click', clockOut);
        }

        const breakStartButton = document.getElementById('breakStartButton');
        if (breakStartButton) {
            breakStartButton.addEventListener('click', startBreak);
        }

        const breakEndButton = document.getElementById('breakEndButton');
        if (breakEndButton) {
            breakEndButton.addEventListener('click', endBreak);
        }

        const monthSelectElement = document.getElementById('monthSelect');
        if (monthSelectElement) {
            monthSelectElement.addEventListener('change', function() {
                loadAttendanceHistory(this.value);
            });
        }

        const logoutBtn = document.getElementById('logoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', function(e) {
                e.preventDefault();
                
                // 自動更新を停止
                stopAutoRefresh();

                localStorage.removeItem('userId');
                localStorage.removeItem('username');

                window.location.href = 'login.html';
            });
        }
        
        // ウィンドウフォーカス時にデータをリフレッシュ
        window.addEventListener('focus', async () => {
            try {
                await Promise.all([
                    checkAttendanceStatus(),
                    loadTodayAttendance()
                ]);
            } catch (error) {
                console.error('フォーカス時リフレッシュエラー:', error);
            }
        });
    } catch (error) {
        console.error('イベントリスナー設定エラー:', error);
    }
}

// タイムアウト付きのfetch関数
async function fetchWithTimeout(url, options = {}, timeout = 10000) {
    const controller = new AbortController();
    const id = setTimeout(() => controller.abort(), timeout);
    
    try {
        const response = await fetch(url, {
            ...options,
            signal: controller.signal
        });
        return response;
    } finally {
        clearTimeout(id);
    }
}

// ユーティリティ関数
function formatTime(timeStr) {
     if (!timeStr) return '-';

     try {
         const date = new Date(timeStr);
         if (isNaN(date.getTime())) {
             const parts = timeStr.split(':');
             if (parts.length >= 2) {
                  return `${parts[0]}:${parts[1]}`;
             }
             return timeStr;
         }
         return date.toLocaleTimeString('ja-JP', { hour: '2-digit', minute: '2-digit' });

     } catch (e) {
         console.error('時刻フォーマットエラー:', e);
         return '-';
     }
 }

 function calculateWorkHours(startTime, endTime) {
     if (!startTime || !endTime) return '-';

     try {
         const start = new Date(startTime).getTime();
         const end = new Date(endTime).getTime();
         const diffMs = end - start;

         if (isNaN(diffMs) || diffMs < 0) return '-';

         const hours = Math.floor(diffMs / (1000 * 60 * 60));
         const minutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));

         return `${hours}時間${minutes}分`;
     } catch (e) {
         console.error('勤務時間計算エラー:', e);
         return '-';
     }
 }

 function formatMinutesToHoursAndMinutes(totalMinutes) {
      if (totalMinutes === undefined || totalMinutes === null || isNaN(totalMinutes)) return '-';
      if (totalMinutes < 0) totalMinutes = 0;

      const hours = Math.floor(totalMinutes / 60);
      const minutes = totalMinutes % 60;

      if (hours === 0 && minutes === 0) return '0分';
      if (hours === 0) return `${minutes}分`;
      if (minutes === 0) return `${hours}時間`;
      return `${hours}時間${minutes}分`;
 }

// メッセージ表示関連の関数
function clearMessages() {
    const errorElement = document.getElementById('errorMessage');
    const successElement = document.getElementById('successMessage');
    
    if (errorElement) errorElement.style.display = 'none';
    if (successElement) successElement.style.display = 'none';
}

function showError(message) {
    try {
        if (!message) return;
        
        const errorElement = document.getElementById('errorMessage');
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.style.display = 'block';

            setTimeout(() => {
                errorElement.style.display = 'none';
            }, 5000);
        } else {
            console.error('エラー表示要素が見つかりません:', message);
            alert(message);
        }
    } catch (error) {
        console.error('エラー表示中にエラー発生:', error);
        alert(message);
    }
}

function showSuccess(message) {
    try {
        if (!message) return;
        
        const successElement = document.getElementById('successMessage');
        if (successElement) {
            successElement.textContent = message;
            successElement.style.display = 'block';
            
            setTimeout(() => {
                successElement.style.display = 'none';
            }, 5000);
        } else {
            alert(message);
        }
    } catch (error) {
        console.error('成功メッセージ表示エラー:', error);
        alert(message);
    }
}

function showLoading(isLoading) {
    const loadingElement = document.getElementById('loading');
    if (loadingElement) {
        loadingElement.style.display = isLoading ? 'block' : 'none';
    }
}

// グローバルエラーハンドリング
window.addEventListener('error', function(event) {
    console.error('グローバルエラー:', event.error);
    showError('予期せぬエラーが発生しました。ページをリロードしてください。');
});

// ネットワーク状態の監視
window.addEventListener('online', function() {
    console.log('ネットワーク接続が復旧しました');
    initializeApp(); // アプリを再初期化
});

window.addEventListener('offline', function() {
    console.warn('ネットワーク接続が切断されました');
    showError('ネットワーク接続が切断されました。接続が復旧したら自動的に更新されます。');
});