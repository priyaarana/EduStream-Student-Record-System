package com.studentcrud;

import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/adminStudentProfile")
public class AdminStudentProfileServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.html");
            return;
        }

        int studentId = Integer.parseInt(request.getParameter("id"));

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<html><head><title>Student Profile</title>");
        out.println("<meta charset='UTF-8'>");
        out.println("<link rel='stylesheet' type='text/css' href='style.css'>");
        out.println("</head><body>");

        out.println("<div class='app-shell'>");
        out.println("<div class='sidebar'>");
        out.println("<div class='brand'>🎓 StudentCRUD</div>");
        out.println("<a href='home'>Dashboard</a>");
        out.println("<a href='index.html'>Add Student</a>");
        out.println("<a href='view' class='active'>View Students</a>");
        out.println("<a href='adminMarks'>Marks</a>");
        out.println("<a href='adminAttendance'>Attendance</a>");
        out.println("<a href='logout' class='logout'>Logout</a>");
        out.println("</div>");

        out.println("<div class='main-content'>");

        String course = "";
        try {
            Connection con = DBconnection.getConnection();

            // --- Basic details ---
            PreparedStatement ps = con.prepareStatement("SELECT * FROM students WHERE id=?");
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                course = rs.getString("course");
                out.println("<div class='topbar'><div><h1>" + rs.getString("name") + "</h1><p>Full profile overview (admin view)</p></div></div>");

                out.println("<div class='stat-cards'>");
                out.println("<div class='stat-card'><div class='num' style='font-size:16px;'>" + rs.getString("course") + "</div><div class='label'>Course</div></div>");
                out.println("<div class='stat-card'><div class='num' style='font-size:16px;'>" + rs.getString("department") + "</div><div class='label'>Department</div></div>");
                out.println("<div class='stat-card'><div class='num' style='font-size:16px;'>" + rs.getString("email") + "</div><div class='label'>Email</div></div>");
                out.println("<div class='stat-card'><div class='num' style='font-size:16px;'>" + rs.getString("attendance") + "</div><div class='label'>Attendance</div></div>");
                String clubs = rs.getString("clubs");
                if (clubs == null || clubs.isEmpty()) clubs = "Not enrolled";
                out.println("<div class='stat-card'><div class='num' style='font-size:16px;'>" + clubs + "</div><div class='label'>Clubs</div></div>");
                out.println("</div>");

                out.println("<p style='margin:20px 0 10px;'><a href='update?id=" + studentId + "'>Edit this student</a> &nbsp;|&nbsp; <a href='view'>&larr; Back to all students</a></p>");
            }

            // --- Subjects ---
            out.println("<h2 style='margin-top:30px; font-size:18px;'>Subjects</h2>");
            out.println("<table>");
            out.println("<tr><th>Subject</th><th>Credits</th></tr>");
            PreparedStatement subPs = con.prepareStatement("SELECT * FROM subjects WHERE course=?");
            subPs.setString(1, course);
            ResultSet subRs = subPs.executeQuery();
            boolean anySub = false;
            while (subRs.next()) {
                anySub = true;
                out.println("<tr><td>" + subRs.getString("subject_name") + "</td><td>" + subRs.getInt("credits") + "</td></tr>");
            }
            if (!anySub) out.println("<tr><td colspan='2'>No subjects found for this course.</td></tr>");
            out.println("</table>");

            // --- Marks ---
            out.println("<h2 style='margin-top:30px; font-size:18px;'>Marks</h2>");
            out.println("<table>");
            out.println("<tr><th>Subject</th><th>Marks Obtained</th><th>Max Marks</th></tr>");
            PreparedStatement markPs = con.prepareStatement("SELECT * FROM marks WHERE student_id=?");
            markPs.setInt(1, studentId);
            ResultSet markRs = markPs.executeQuery();
            boolean anyMark = false;
            while (markRs.next()) {
                anyMark = true;
                out.println("<tr><td>" + markRs.getString("subject_name") + "</td><td>"
                    + markRs.getInt("marks_obtained") + "</td><td>" + markRs.getInt("max_marks") + "</td></tr>");
            }
            if (!anyMark) out.println("<tr><td colspan='3'>No marks recorded yet.</td></tr>");
            out.println("</table>");

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // --- Timetable (same static pattern as student side) ---
        out.println("<h2 style='margin-top:30px; font-size:18px;'>Timetable</h2>");
        out.println("<table>");
        out.println("<tr><th>Day</th><th>9-11 AM</th><th>11-1 PM</th><th>2-4 PM</th></tr>");
        out.println("<tr><td>Monday</td><td>Core Subject 1</td><td>Core Subject 2</td><td>Lab</td></tr>");
        out.println("<tr><td>Tuesday</td><td>Core Subject 3</td><td>Elective</td><td>Core Subject 1</td></tr>");
        out.println("<tr><td>Wednesday</td><td>Lab</td><td>Core Subject 2</td><td>Core Subject 3</td></tr>");
        out.println("<tr><td>Thursday</td><td>Core Subject 1</td><td>Elective</td><td>Core Subject 2</td></tr>");
        out.println("<tr><td>Friday</td><td>Core Subject 3</td><td>Lab</td><td>Seminar</td></tr>");
        out.println("</table>");

        out.println("</div></div></body></html>");
    }
}