package com.studentcrud;

import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/studentSubjects")
public class StudentSubjectsServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("student") == null) {
            response.sendRedirect("studentlogin.html");
            return;
        }

        int studentId = (int) session.getAttribute("student");

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<html><head><title>My Subjects</title>");
        out.println("<meta charset='UTF-8'>");
        out.println("<link rel='stylesheet' type='text/css' href='style.css'>");
        out.println("</head><body>");

        out.println("<div class='app-shell'>");
        out.println("<div class='sidebar'>");
        out.println("<div class='brand'>🎓 StudentCRUD</div>");
        out.println("<a href='studentHome'>My Profile</a>");
        out.println("<a href='studentSubjects' class='active'>Subjects</a>");
        out.println("<a href='studentTimetable'>Timetable</a>");
        out.println("<a href='studentAttendance'>Attendance</a>");
        out.println("<a href='studentMarks'>Marks</a>");
        out.println("<a href='logout' class='logout'>Logout</a>");
        out.println("</div>");

        out.println("<div class='main-content'>");
        out.println("<div class='topbar'><div><h1>My Subjects</h1><p>Subjects for your enrolled course.</p></div></div>");

        try {
            Connection con = DBconnection.getConnection();

            PreparedStatement coursePs = con.prepareStatement("SELECT course FROM students WHERE id=?");
            coursePs.setInt(1, studentId);
            ResultSet courseRs = coursePs.executeQuery();
            String course = "";
            if (courseRs.next()) course = courseRs.getString("course");

            out.println("<table>");
            out.println("<tr><th>Subject</th><th>Credits</th></tr>");

            PreparedStatement ps = con.prepareStatement("SELECT * FROM subjects WHERE course=?");
            ps.setString(1, course);
            ResultSet rs = ps.executeQuery();

            boolean any = false;
            while (rs.next()) {
                any = true;
                out.println("<tr><td>" + rs.getString("subject_name") + "</td><td>" + rs.getInt("credits") + "</td></tr>");
            }
            if (!any) {
                out.println("<tr><td colspan='2'>No subjects found for your course yet.</td></tr>");
            }
            out.println("</table>");

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        out.println("</div></div></body></html>");
    }
}