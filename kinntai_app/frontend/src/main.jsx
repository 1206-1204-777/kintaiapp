import React from 'react'
import ReactDOM from 'react-dom/client'
import TodayAttendance from './components/TodayAttendance'

const todayAttendanceElement = document.getElementById('today-attendance-react')
if (todayAttendanceElement) {
  const root = ReactDOM.createRoot(todayAttendanceElement)
  root.render(<TodayAttendance />)
}