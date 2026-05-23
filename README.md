# *StudentGradeTracker*
🎓 A professional Student Grade Tracker built with Java &amp; JavaFX. Features login/register system, role-based access (Admin/Teacher), subject-wise marks, grade charts, leaderboard, CSV export &amp; report cards. Dark neon UI.
✨ Features
🔐 Authentication System

Animated Login Page with shake animation on wrong password
Register New User — with role selection (Teacher / Admin)
Admin secret key protection for admin accounts
Logout button to return to login screen
Persistent user accounts saved to users.usr

# 👤 User Roles
   # Role       Permissions
🛡  ADMIN       Full access + User Management
👨‍🏫 TEACHER     Student management only

📋 Student Management

Add students with Name, Roll No, Section, Marks, Attendance
Edit existing student records
Delete with confirmation dialog
Search by name or roll number
Sort by Name / Marks / Attendance
Subject-wise marks entry (Math, English, Science, Urdu, Computer, Physics, Chemistry)

📊 Statistics & Analytics

Real-time stats: Total, Average, Highest, Lowest
Pass / Fail count summary
Pie Chart — Grade distribution
Bar Chart — Students per grade
🏅 Top 3 Leaderboard with medal rankings

🖨️ Reports & Export

Report Card per student (formatted, printable)
Save as TXT — individual student report
Export CSV — all students with all fields

💾 Data Persistence

Java Serialization (.dat file) — no external database needed
Save / Load / Clear all records
