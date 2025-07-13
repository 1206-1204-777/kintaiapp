window.currentUserId = 7 // 開発用の固定ユーザーID

import { useState, useEffect, useMemo } from 'react'

function AttendanceTable() {
  const [attendanceData, setAttendanceData] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [currentMonth, setCurrentMonth] = useState(new Date().toISOString().slice(0, 7))
  const [sortConfig, setSortConfig] = useState({ key: 'date', direction: 'desc' })
  const [pageSize, setPageSize] = useState(20)
  const [currentPage, setCurrentPage] = useState(1)
  const [searchTerm, setSearchTerm] = useState('')
  const [lastUpdated, setLastUpdated] = useState(new Date())

  // データ取得
  useEffect(() => {
    loadAttendanceData()
  }, [currentMonth])

  // 既存システムからの更新通知を受信
  useEffect(() => {
    const handleUpdate = () => {
      loadAttendanceData()
      setLastUpdated(new Date())
    }
    
    window.addEventListener('attendance-updated', handleUpdate)
    
    // 定期更新（5分間隔）
    const interval = setInterval(handleUpdate, 5 * 60 * 1000)
    
    return () => {
      window.removeEventListener('attendance-updated', handleUpdate)
      clearInterval(interval)
    }
  }, [])

  const loadAttendanceData = async () => {
    try {
      setLoading(true)
      setError(null)
      
      // 固定ユーザーID（開発用）
      const userId = window.currentUserId || 1
      
      const response = await fetch(`/api/attendance/monthly/${userId}?month=${currentMonth}`)
      
      if (response.ok) {
        const data = await response.json()
        setAttendanceData(Array.isArray(data) ? data : [])
      } else if (response.status === 404) {
        // データがない場合
        setAttendanceData([])
      } else {
        throw new Error(`HTTP ${response.status}`)
      }
    } catch (err) {
      console.error('勤怠履歴取得エラー:', err)
      setError('データの取得に失敗しました')
      // 開発環境用のテストデータ
      if (process.env.NODE_ENV === 'development') {
        setAttendanceData(generateTestData())
      }
    } finally {
      setLoading(false)
    }
  }

  // 開発用テストデータ生成
  const generateTestData = () => {
    const testData = []
    const year = parseInt(currentMonth.split('-')[0])
    const month = parseInt(currentMonth.split('-')[1])
    const daysInMonth = new Date(year, month, 0).getDate()
    
    for (let day = 1; day <= daysInMonth; day++) {
      if (Math.random() > 0.3) { // 70%の確率で勤怠データを生成
        const date = `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}`
        const clockIn = `09:${Math.floor(Math.random() * 60).toString().padStart(2, '0')}`
        const clockOut = Math.random() > 0.1 ? `18:${Math.floor(Math.random() * 60).toString().padStart(2, '0')}` : null
        
        testData.push({
          id: day,
          date,
          clockIn,
          clockOut,
          totalWorkMin: clockOut ? 480 + Math.floor(Math.random() * 120) : null,
          overtimeMinutes: Math.random() > 0.7 ? Math.floor(Math.random() * 120) : 0,
          totalBreakMin: 60,
          status: clockOut ? 'completed' : 'working'
        })
      }
    }
    return testData.reverse()
  }

  // ソート・フィルター・ページネーション
  const filteredAndSortedData = useMemo(() => {
    let filtered = attendanceData.filter(record => 
      record.date.includes(searchTerm) ||
      (record.status && record.status.includes(searchTerm))
    )

    // ソート
    filtered.sort((a, b) => {
      const aValue = a[sortConfig.key]
      const bValue = b[sortConfig.key]
      
      if (!aValue && !bValue) return 0
      if (!aValue) return 1
      if (!bValue) return -1
      
      if (aValue < bValue) return sortConfig.direction === 'asc' ? -1 : 1
      if (aValue > bValue) return sortConfig.direction === 'asc' ? 1 : -1
      return 0
    })

    return filtered
  }, [attendanceData, searchTerm, sortConfig])

  // ページネーション
  const paginatedData = useMemo(() => {
    const startIndex = (currentPage - 1) * pageSize
    return filteredAndSortedData.slice(startIndex, startIndex + pageSize)
  }, [filteredAndSortedData, currentPage, pageSize])

  const totalPages = Math.ceil(filteredAndSortedData.length / pageSize)

  // ソートハンドラー
  const handleSort = (key) => {
    setSortConfig(prevConfig => ({
      key,
      direction: prevConfig.key === key && prevConfig.direction === 'asc' ? 'desc' : 'asc'
    }))
  }

  // ソートアイコン取得
  const getSortIcon = (key) => {
    if (sortConfig.key !== key) return '↕️'
    return sortConfig.direction === 'asc' ? '⬆️' : '⬇️'
  }

  // CSV出力
  const exportToCSV = () => {
    const headers = ['日付', '出勤時刻', '退勤時刻', '実労働時間', '残業時間', '休憩時間', '状態']
    const csvData = [
      headers,
      ...filteredAndSortedData.map(record => [
        record.date,
        formatTime(record.clockIn),
        formatTime(record.clockOut),
        formatMinutesToHours(record.totalWorkMin),
        formatMinutesToHours(record.overtimeMinutes),
        formatMinutesToHours(record.totalBreakMin),
        getStatusText(record)
      ])
    ]
    
    const csvContent = csvData.map(row => row.join(',')).join('\n')
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    
    const link = document.createElement('a')
    link.href = url
    link.download = `attendance_${currentMonth}.csv`
    link.click()
    
    URL.revokeObjectURL(url)
  }

  // ユーティリティ関数
  const formatTime = (timeStr) => {
    if (!timeStr) return '-'
    try {
      // ISO形式の場合
      if (timeStr.includes('T')) {
        const date = new Date(timeStr)
        return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
      }
      // HH:mm形式の場合
      const parts = timeStr.split(':')
      return `${parts[0].padStart(2, '0')}:${parts[1].padStart(2, '0')}`
    } catch (e) {
      return timeStr
    }
  }

  const formatMinutesToHours = (minutes) => {
    if (minutes === undefined || minutes === null || isNaN(minutes)) return '-'
    if (minutes === 0) return '0分'
    
    const hours = Math.floor(minutes / 60)
    const mins = minutes % 60
    
    if (hours === 0) return `${mins}分`
    if (mins === 0) return `${hours}時間`
    return `${hours}時間${mins}分`
  }

  const getStatusText = (record) => {
    if (record.clockIn && record.clockOut) return '完了'
    if (record.clockIn) return '勤務中'
    return '未打刻'
  }

  const getStatusColor = (record) => {
    if (record.clockIn && record.clockOut) return '#4CAF50'
    if (record.clockIn) return '#FFC107'
    return '#6c757d'
  }

  // ページ変更時に先頭に戻る
  useEffect(() => {
    if (currentPage > totalPages && totalPages > 0) {
      setCurrentPage(1)
    }
  }, [totalPages, currentPage])

  if (loading) {
    return (
      <div style={{ 
        textAlign: 'center', 
        padding: '2rem',
        backgroundColor: 'white',
        borderRadius: '8px',
        boxShadow: '0 2px 5px rgba(0,0,0,0.1)',
        margin: '1rem 0'
      }}>
        <div style={{ fontSize: '1.1rem', color: '#666' }}>
          📊 勤怠履歴を読み込み中...
        </div>
      </div>
    )
  }

  return (
    <div style={{
      backgroundColor: 'white',
      borderRadius: '8px',
      boxShadow: '0 2px 5px rgba(0,0,0,0.1)',
      margin: '1rem 0',
      overflow: 'hidden'
    }}>
      <div style={{ padding: '1.5rem' }}>
        <h2 style={{
          color: '#4a6da7',
          borderBottom: '1px solid #eee',
          paddingBottom: '0.5rem',
          marginTop: 0,
          marginBottom: '1.5rem'
        }}>
          勤怠履歴
        </h2>

        {/* 操作パネル */}
        <div style={{
          display: 'flex',
          flexWrap: 'wrap',
          gap: '1rem',
          marginBottom: '1.5rem',
          alignItems: 'center'
        }}>
          <div>
            <label style={{ display: 'block', marginBottom: '0.25rem', fontSize: '0.9rem', fontWeight: 'bold' }}>
              年月:
            </label>
            <input
              type="month"
              value={currentMonth}
              onChange={(e) => setCurrentMonth(e.target.value)}
              style={{
                padding: '0.5rem',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '0.9rem'
              }}
            />
          </div>
          
          <div>
            <label style={{ display: 'block', marginBottom: '0.25rem', fontSize: '0.9rem', fontWeight: 'bold' }}>
              検索:
            </label>
            <input
              type="text"
              placeholder="日付や状態で検索..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              style={{
                padding: '0.5rem',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '0.9rem',
                minWidth: '200px'
              }}
            />
          </div>
          
          <div>
            <label style={{ display: 'block', marginBottom: '0.25rem', fontSize: '0.9rem', fontWeight: 'bold' }}>
              表示件数:
            </label>
            <select
              value={pageSize}
              onChange={(e) => {
                setPageSize(Number(e.target.value))
                setCurrentPage(1)
              }}
              style={{
                padding: '0.5rem',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '0.9rem'
              }}
            >
              <option value={10}>10件</option>
              <option value={20}>20件</option>
              <option value={50}>50件</option>
            </select>
          </div>
          
          <div style={{ marginLeft: 'auto' }}>
            <button
              onClick={exportToCSV}
              disabled={filteredAndSortedData.length === 0}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: '#28a745',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '0.9rem',
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem'
              }}
            >
              📊 CSV出力
            </button>
          </div>
        </div>

        {error && (
          <div style={{
            backgroundColor: '#f8d7da',
            color: '#721c24',
            padding: '1rem',
            borderRadius: '4px',
            marginBottom: '1rem',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem'
          }}>
            <span>⚠️</span>
            {error}
            <button
              onClick={loadAttendanceData}
              style={{
                marginLeft: 'auto',
                padding: '0.25rem 0.5rem',
                backgroundColor: '#dc3545',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '0.8rem'
              }}
            >
              再試行
            </button>
          </div>
        )}

        {/* テーブル */}
        <div style={{ overflowX: 'auto' }}>
          <table style={{
            width: '100%',
            borderCollapse: 'collapse',
            minWidth: '700px'
          }}>
            <thead>
              <tr style={{ backgroundColor: '#4a6da7' }}>
                <th 
                  onClick={() => handleSort('date')}
                  style={{
                    padding: '1rem 0.75rem',
                    color: 'white',
                    cursor: 'pointer',
                    userSelect: 'none',
                    textAlign: 'left',
                    fontSize: '0.9rem'
                  }}
                >
                  日付 {getSortIcon('date')}
                </th>
                <th 
                  onClick={() => handleSort('clockIn')}
                  style={{
                    padding: '1rem 0.75rem',
                    color: 'white',
                    cursor: 'pointer',
                    userSelect: 'none',
                    textAlign: 'left',
                    fontSize: '0.9rem'
                  }}
                >
                  出勤時刻 {getSortIcon('clockIn')}
                </th>
                <th 
                  onClick={() => handleSort('clockOut')}
                  style={{
                    padding: '1rem 0.75rem',
                    color: 'white',
                    cursor: 'pointer',
                    userSelect: 'none',
                    textAlign: 'left',
                    fontSize: '0.9rem'
                  }}
                >
                  退勤時刻 {getSortIcon('clockOut')}
                </th>
                <th style={{ padding: '1rem 0.75rem', color: 'white', textAlign: 'left', fontSize: '0.9rem' }}>
                  実労働時間
                </th>
                <th style={{ padding: '1rem 0.75rem', color: 'white', textAlign: 'left', fontSize: '0.9rem' }}>
                  残業時間
                </th>
                <th style={{ padding: '1rem 0.75rem', color: 'white', textAlign: 'left', fontSize: '0.9rem' }}>
                  休憩時間
                </th>
                <th style={{ padding: '1rem 0.75rem', color: 'white', textAlign: 'left', fontSize: '0.9rem' }}>
                  状態
                </th>
                <th style={{ padding: '1rem 0.75rem', color: 'white', textAlign: 'left', fontSize: '0.9rem' }}>
                  操作
                </th>
              </tr>
            </thead>
            <tbody>
              {paginatedData.length === 0 ? (
                <tr>
                  <td colSpan="8" style={{
                    padding: '2rem',
                    textAlign: 'center',
                    color: '#666',
                    fontStyle: 'italic'
                  }}>
                    {searchTerm ? '検索条件に一致するデータがありません' : '勤怠データがありません'}
                  </td>
                </tr>
              ) : (
                paginatedData.map((record, index) => (
                  <AttendanceRow
                    key={record.id || `${record.date}-${index}`}
                    record={record}
                    onUpdate={loadAttendanceData}
                  />
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* ページネーション */}
        {totalPages > 1 && (
          <div style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginTop: '1.5rem',
            padding: '1rem',
            backgroundColor: '#f8f9fa',
            borderRadius: '4px'
          }}>
            <button
              disabled={currentPage === 1}
              onClick={() => setCurrentPage(currentPage - 1)}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: currentPage === 1 ? '#6c757d' : '#4a6da7',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: currentPage === 1 ? 'not-allowed' : 'pointer',
                fontSize: '0.9rem'
              }}
            >
              ← 前へ
            </button>
            
            <span style={{ fontSize: '0.9rem', color: '#666' }}>
              {currentPage} / {totalPages} ページ（全 {filteredAndSortedData.length} 件）
            </span>
            
            <button
              disabled={currentPage === totalPages}
              onClick={() => setCurrentPage(currentPage + 1)}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: currentPage === totalPages ? '#6c757d' : '#4a6da7',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: currentPage === totalPages ? 'not-allowed' : 'pointer',
                fontSize: '0.9rem'
              }}
            >
              次へ →
            </button>
          </div>
        )}

        {/* 更新情報 */}
        <div style={{
          marginTop: '1rem',
          textAlign: 'right',
          fontSize: '0.8rem',
          color: '#666'
        }}>
          最終更新: {lastUpdated.toLocaleTimeString()}
          <button
            onClick={loadAttendanceData}
            style={{
              marginLeft: '0.5rem',
              padding: '0.25rem 0.5rem',
              backgroundColor: 'transparent',
              border: '1px solid #ddd',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '0.8rem'
            }}
            title="手動更新"
          >
            🔄
          </button>
        </div>
      </div>
    </div>
  )
}

// 個別行コンポーネント
function AttendanceRow({ record, onUpdate }) {
  const [editing, setEditing] = useState(false)
  const [editData, setEditData] = useState(record)

  const handleEdit = () => {
    setEditData(record)
    setEditing(true)
  }

  const handleCancel = () => {
    setEditData(record)
    setEditing(false)
  }

  const handleSave = async () => {
    try {
      // TODO: 実際のAPI呼び出し
      console.log('勤怠データ更新:', editData)
      
      // デモ用の成功処理
      setEditing(false)
      if (window.showAttendanceSuccess) {
        window.showAttendanceSuccess('勤怠データを更新しました')
      }
      onUpdate()
    } catch (error) {
      console.error('更新エラー:', error)
      if (window.showAttendanceError) {
        window.showAttendanceError('更新に失敗しました')
      }
    }
  }

  const formatTime = (timeStr) => {
    if (!timeStr) return '-'
    try {
      if (timeStr.includes('T')) {
        const date = new Date(timeStr)
        return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
      }
      const parts = timeStr.split(':')
      return `${parts[0].padStart(2, '0')}:${parts[1].padStart(2, '0')}`
    } catch (e) {
      return timeStr
    }
  }

  const formatMinutesToHours = (minutes) => {
    if (minutes === undefined || minutes === null || isNaN(minutes)) return '-'
    if (minutes === 0) return '0分'
    
    const hours = Math.floor(minutes / 60)
    const mins = minutes % 60
    
    if (hours === 0) return `${mins}分`
    if (mins === 0) return `${hours}時間`
    return `${hours}時間${mins}分`
  }

  const getStatusText = () => {
    if (record.clockIn && record.clockOut) return '完了'
    if (record.clockIn) return '勤務中'
    return '未打刻'
  }

  const getStatusColor = () => {
    if (record.clockIn && record.clockOut) return '#4CAF50'
    if (record.clockIn) return '#FFC107'
    return '#6c757d'
  }

  const formatDateDisplay = (dateStr) => {
    const date = new Date(dateStr)
    const weekdays = ['日', '月', '火', '水', '木', '金', '土']
    const weekday = weekdays[date.getDay()]
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    return `${month}/${day}(${weekday})`
  }

  return (
    <tr style={{
      borderBottom: '1px solid #ddd',
      backgroundColor: editing ? '#fff3cd' : (record.index % 2 === 0 ? '#f8f9fa' : 'white')
    }}>
      <td style={{ padding: '0.75rem', fontSize: '0.9rem' }}>
        {formatDateDisplay(record.date)}
      </td>
      <td style={{ padding: '0.75rem', fontSize: '0.9rem' }}>
        {editing ? (
          <input
            type="time"
            value={editData.clockIn || ''}
            onChange={(e) => setEditData({...editData, clockIn: e.target.value})}
            style={{
              padding: '0.25rem',
              border: '1px solid #ddd',
              borderRadius: '4px',
              fontSize: '0.9rem',
              width: '100px'
            }}
          />
        ) : (
          formatTime(record.clockIn)
        )}
      </td>
      <td style={{ padding: '0.75rem', fontSize: '0.9rem' }}>
        {editing ? (
          <input
            type="time"
            value={editData.clockOut || ''}
            onChange={(e) => setEditData({...editData, clockOut: e.target.value})}
            style={{
              padding: '0.25rem',
              border: '1px solid #ddd',
              borderRadius: '4px',
              fontSize: '0.9rem',
              width: '100px'
            }}
          />
        ) : (
          formatTime(record.clockOut)
        )}
      </td>
      <td style={{ padding: '0.75rem', fontSize: '0.9rem' }}>
        {formatMinutesToHours(record.totalWorkMin)}
      </td>
      <td style={{ padding: '0.75rem', fontSize: '0.9rem' }}>
        {formatMinutesToHours(record.overtimeMinutes)}
      </td>
      <td style={{ padding: '0.75rem', fontSize: '0.9rem' }}>
        {formatMinutesToHours(record.totalBreakMin)}
      </td>
      <td style={{ padding: '0.75rem', fontSize: '0.9rem' }}>
        <span style={{
          display: 'inline-block',
          padding: '0.25rem 0.5rem',
          borderRadius: '12px',
          fontSize: '0.8rem',
          fontWeight: 'bold',
          color: 'white',
          backgroundColor: getStatusColor()
        }}>
          {getStatusText()}
        </span>
      </td>
      <td style={{ padding: '0.75rem', fontSize: '0.9rem' }}>
        {editing ? (
          <div style={{ display: 'flex', gap: '0.25rem' }}>
            <button
              onClick={handleSave}
              style={{
                padding: '0.25rem 0.5rem',
                backgroundColor: '#28a745',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '0.8rem'
              }}
            >
              保存
            </button>
            <button
              onClick={handleCancel}
              style={{
                padding: '0.25rem 0.5rem',
                backgroundColor: '#6c757d',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '0.8rem'
              }}
            >
              キャンセル
            </button>
          </div>
        ) : (
          <button
            onClick={handleEdit}
            style={{
              padding: '0.25rem 0.5rem',
              backgroundColor: '#17a2b8',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '0.8rem'
            }}
          >
            編集
          </button>
        )}
      </td>
    </tr>
  )
}

export default AttendanceTable