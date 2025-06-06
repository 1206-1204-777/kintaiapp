import { useState, useEffect } from 'react'

function TodayAttendance() {
  const [attendance, setAttendance] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    loadData()
    
    const handleUpdate = () => loadData()
    window.addEventListener('attendance-updated', handleUpdate)
    
    return () => {
      window.removeEventListener('attendance-updated', handleUpdate)
    }
  }, [])

  const loadData = async () => {
    try {
      setLoading(true)
      setError(null)
      
      // テスト用の固定データ（後でAPI接続に変更）
      setTimeout(() => {
        setAttendance({
          clockIn: '09:00',
          clockOut: '18:00',
          workHours: '8時間0分',
          breakTime: '1時間0分',
          overtime: '-'
        })
        setLoading(false)
      }, 1000)
      
    } catch (err) {
      console.error('データ取得エラー:', err)
      setError('通信エラーが発生しました')
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="attendance-card">
        <h3>今日の勤怠</h3>
        <div className="loading">読み込み中...</div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="attendance-card">
        <h3>今日の勤怠</h3>
        <div className="error">{error}</div>
      </div>
    )
  }

  return (
    <div className="attendance-card">
      <h3>今日の勤怠（React版）</h3>
      <div className="attendance-info">
        <div className="time-row">
          <span className="label">出勤時刻:</span>
          <span className="value">{attendance?.clockIn || '-'}</span>
        </div>
        <div className="time-row">
          <span className="label">退勤時刻:</span>
          <span className="value">{attendance?.clockOut || '-'}</span>
        </div>
        <div className="time-row">
          <span className="label">勤務時間:</span>
          <span className="value">{attendance?.workHours || '-'}</span>
        </div>
        <div className="time-row">
          <span className="label">休憩時間:</span>
          <span className="value">{attendance?.breakTime || '-'}</span>
        </div>
        <div className="time-row">
          <span className="label">残業時間:</span>
          <span className="value">{attendance?.overtime || '-'}</span>
        </div>
      </div>
    </div>
  )
}

export default TodayAttendance