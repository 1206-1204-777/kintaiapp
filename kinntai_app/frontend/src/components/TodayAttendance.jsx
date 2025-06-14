window.currentUserId = 7;

import { useState, useEffect } from 'react'

function TodayAttendance() {
  const [attendance, setAttendance] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [lastUpdated, setLastUpdated] = useState(null)

  useEffect(() => {
    loadData()
    
    // 既存のJavaScriptからの更新通知を受信
    const handleUpdate = () => {
      console.log('出勤・退勤の更新通知を受信')
      loadData()
    }
    
    window.addEventListener('attendance-updated', handleUpdate)
    
    // 5分ごとに自動更新
    const interval = setInterval(loadData, 5 * 60 * 1000)
    
    return () => {
      window.removeEventListener('attendance-updated', handleUpdate)
      clearInterval(interval)
    }
  }, [])

  const loadData = async () => {
    try {
      setLoading(true)
      setError(null)
      
      const userId = 7;
      
      const today = new Date().toISOString().split('T')[0]
      console.log(`今日の勤怠データを取得中: ${today}, ユーザー: ${window.currentUserId}`)
      
      // 既存のfetchWithTimeout関数があれば使用、なければ通常のfetch
      const fetchFunction = window.fetchWithTimeout || fetch
      const response = await fetchFunction(`/api/attendance/${window.currentUserId}/date/${today}`)
      
      if (response.ok) {
        const data = await response.json()
        console.log('勤怠データ取得成功:', data)
        setAttendance(data)
        setLastUpdated(new Date())
      } else if (response.status === 404) {
        // 今日の勤怠データがまだない場合
        console.log('今日の勤怠データはまだありません')
        setAttendance(null)
        setLastUpdated(new Date())
      } else {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      }
    } catch (err) {
      console.error('API呼び出しエラー:', err)
      setError('データの取得に失敗しました')
      
      // 開発時用のフォールバック（APIが利用できない場合）
      if (process.env.NODE_ENV === 'development') {
        console.log('開発環境用のテストデータを使用')
        setTimeout(() => {
          setAttendance({
            clockIn: '09:00:00',
            clockOut: null,
            totalBreakMinutes: 60,
            overtimeMinutes: 0
          })
          setError(null)
          setLastUpdated(new Date())
        }, 1000)
      }
    } finally {
      setLoading(false)
    }
  }

  // 既存のユーティリティ関数を活用
  const formatTime = (timeStr) => {

    if (!timeStr) return '-'
    console.log('formatTime引数:', timeStr, typeof timeStr);
    
   const date = new Date(String(timeStr));
   const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    
    return `${hours}:${minutes}`
  }

  const calculateWorkHours = () => {
  if (!attendance?.clockIn || !attendance?.clockOut) return '-';

  try {
    // clockIn, clockOut が ISO形式（日付＋時刻）ならそのまま使う
    const clockIn = new Date(attendance.clockIn);
    const clockOut = new Date(attendance.clockOut);

    // 時間差（分単位）
    const diffMs = clockOut - clockIn;
    if (isNaN(diffMs)) return '-'; // ← ここで安全確認

    const diffMinutes = Math.floor(diffMs / (1000 * 60));
    const breakMinutes = attendance.totalBreakMinutes || 60;
    const totalMinutes = diffMinutes - breakMinutes;

    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;

    return `${hours}時間${minutes}分`;
  } catch (e) {
    return '-';
  }
};



  const formatBreakTime = () => {
    if (attendance?.totalBreakMinutes != null && window.formatMinutesToHoursAndMinutes) {
      return window.formatMinutesToHoursAndMinutes(attendance.totalBreakMinutes)
    }
    
    // フォールバック
    if (attendance?.totalBreakMinutes != null) {
      const hours = Math.floor(attendance.totalBreakMinutes / 60)
      const minutes = attendance.totalBreakMinutes % 60
      return hours > 0 ? `${hours}時間${minutes}分` : `${minutes}分`
    }
    
    return '60分（自動）'
  }

  const formatOvertimeHours = () => {
    if (attendance?.overtimeMinutes != null && window.formatMinutesToHoursAndMinutes) {
      return window.formatMinutesToHoursAndMinutes(attendance.overtimeMinutes)
    }
    
    // フォールバック
    if (attendance?.overtimeMinutes != null && attendance.overtimeMinutes > 0) {
      const hours = Math.floor(attendance.overtimeMinutes / 60)
      const minutes = attendance.overtimeMinutes % 60
      return hours > 0 ? `${hours}時間${minutes}分` : `${minutes}分`
    }
    
    return '-'
  }

  const getWorkStatus = () => {
    if (!attendance) return 'まだ出勤していません'
    if (attendance.clockIn && !attendance.clockOut) return '勤務中'
    if (attendance.clockIn && attendance.clockOut) return '退勤済み'
    return '状態不明'
  }

  const getStatusColor = () => {
    if (!attendance) return '#gray'
    if (attendance.clockIn && !attendance.clockOut) return '#green'
    if (attendance.clockIn && attendance.clockOut) return '#blue'
    return '#gray'
  }

  if (loading) {
    return (
      <div className="attendance-card">
        <h3>今日の勤怠</h3>
        <div className="loading" style={{ textAlign: 'center', padding: '20px', color: '#666' }}>
          <div style={{ marginBottom: '10px' }}>📊</div>
          読み込み中...
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="attendance-card">
        <h3>今日の勤怠</h3>
        <div className="error-message" style={{ textAlign: 'center', padding: '20px' }}>
          <div style={{ color: '#e74c3c', marginBottom: '10px' }}>⚠️ {error}</div>
          <button 
            onClick={loadData} 
            className="retry-button"
            style={{ 
              padding: '8px 16px', 
              backgroundColor: '#3498db', 
              color: 'white', 
              border: 'none', 
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            🔄 再試行 
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="attendance-card">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
        <h3 style={{ margin: 0 }}>今日の勤怠</h3>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <span 
            style={{ 
              padding: '4px 8px', 
              backgroundColor: getStatusColor(), 
              color: 'white', 
              borderRadius: '12px', 
              fontSize: '12px',
              fontWeight: 'bold'
            }}
          >
            {getWorkStatus()}
          </span>
          <button 
            onClick={loadData} 
            style={{ 
              background: 'none', 
              border: 'none', 
              cursor: 'pointer', 
              fontSize: '16px',
              padding: '4px'
            }}
            title="手動更新"
          >
            🔄
          </button>
        </div>
      </div>
      
      <div className="attendance-info">
        <div className="time-row" style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
          <span className="label" style={{ fontWeight: 'bold' }}>出勤時刻:</span>
          <span className="value" style={{ 
            color: attendance?.clockIn ? '#2ecc71' : '#95a5a6',
            fontWeight: attendance?.clockIn ? 'bold' : 'normal'
          }}>
            {formatTime(attendance?.clockIn)}
          </span>
        </div>
        
        <div className="time-row" style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
          <span className="label" style={{ fontWeight: 'bold' }}>退勤時刻:</span>
          <span className="value" style={{ 
            color: attendance?.clockOut ? '#3498db' : '#95a5a6',
            fontWeight: attendance?.clockOut ? 'bold' : 'normal'
          }}>
            {formatTime(attendance?.clockOut)}
          </span>
        </div>
        
        <div className="time-row" style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
          <span className="label" style={{ fontWeight: 'bold' }}>勤務時間:</span>
          <span className="value" style={{ 
            color: attendance?.clockIn && attendance?.clockOut ? '#8e44ad' : '#95a5a6'
          }}>
            {calculateWorkHours()}
          </span>
        </div>
        
        <div className="time-row" style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
          <span className="label" style={{ fontWeight: 'bold' }}>休憩時間:</span>
          <span className="value">{formatBreakTime()}</span>
        </div>
        
        <div className="time-row" style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
          <span className="label" style={{ fontWeight: 'bold' }}>残業時間:</span>
          <span className="value" style={{ 
            color: attendance?.overtimeMinutes > 0 ? '#e67e22' : '#95a5a6'
          }}>
            {formatOvertimeHours()}
          </span>
        </div>
      </div>
      
      {lastUpdated && (
        <div className="update-info" style={{ 
          textAlign: 'center', 
          marginTop: '15px', 
          paddingTop: '10px', 
          borderTop: '1px solid #ecf0f1',
          fontSize: '12px',
          color: '#7f8c8d'
        }}>
          最終更新: {lastUpdated.toLocaleTimeString()}
        </div>
      )}
    </div>
  )
}

export default TodayAttendance