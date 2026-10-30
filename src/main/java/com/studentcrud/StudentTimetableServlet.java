package com.studentcrud;

import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/studentTimetable")
public class StudentTimetableServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("student") == null) {
            response.sendRedirect("studentlogin.html");
            return;
        }

        int studentId = (int) session.getAttribute("student");
        String course = "";

        try {
            Connection con = DBconnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT course FROM students WHERE id=?");
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) course = rs.getString("course");
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<html><head><title>My Timetable</title>");
        out.println("<meta charset='UTF-8'>");
        out.println("<link rel='stylesheet' type='text/css' href='style.css'>");
        out.println("</head><body>");

        out.println("<div class='app-shell'>");
        out.println("<div class='sidebar'>");
        out.println("<div class='brand'>🎓 StudentCRUD</div>");
        out.println("<a href='studentHome'>My Profile</a>");
        out.println("<a href='studentSubjects'>Subjects</a>");
        out.println("<a href='studentTimetable' class='active'>Timetable</a>");
        out.println("<a href='studentAttendance'>Attendance</a>");
        out.println("<a href='studentMarks'>Marks</a>");
        out.println("<a href='logout' class='logout'>Logout</a>");
        out.println("</div>");

        out.println("<div class='main-content'>");
        out.println("<div class='topbar'><div><h1>My Timetable</h1><p>Weekly schedule for " + course + ".</p></div></div>");

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