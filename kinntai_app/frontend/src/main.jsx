import React from 'react'
import ReactDOM from 'react-dom/client'
import TodayAttendance from './components/TodayAttendance'
import AttendanceTable from './components/AttendanceTable'

// 今日の勤怠データを表示する要素を取得
//       setError(err.message)
const todayAttendanceElement = document.getElementById('today-attendance-react')
if (todayAttendanceElement) {
  const root = ReactDOM.createRoot(todayAttendanceElement)
  root.render(<TodayAttendance />)
}
// 勤怠テーブルの要素を取得 
const tableElements = document.getElementById('attendance-table-react')
if(tableElements){
  const root = ReactDOM.createRoot(tableElements)
  root.render(<AttendanceTable />)
}
//debug用のログ出力
console.log('✅ React components initialized:', {
  todayAttendance: !!todayAttendanceElement,
  attendanceTable: !!tableElement
})