package com.studentcrud;

import java.io.*;
import java.sql.*;
import java.util.Locale;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/studentAttendance")
public class StudentAttendanceServlet extends HttpServlet {

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

        out.println(Layout.head("My Attendance"));
        out.println(Layout.studentSidebar("attendance"));
        out.println("<div class='main-content'>");
        out.println("<div class='topbar'><div><h1>My Attendance</h1><p>Subject-wise attendance. The minimum required is 75%.</p></div></div>");

        StringBuilder rows = new StringBuilder();
        long totalHeld = 0, totalAttended = 0;

        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT subject_name, classes_held, classes_attended FROM attendance "
                             + "WHERE student_id=? ORDER BY subject_name")) {

            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int held = rs.getInt("classes_held");
                int attended = rs.getInt("classes_attended");
                totalHeld += held;
                totalAttended += attended;

                double pct = held > 0 ? attended * 100.0 / held : 0;
                String status = held == 0 ? "-" : (pct >= 75 ? "<span class='good'>Satisfactory</span>" : "<span class='low'>Shortage</span>");

                rows.append("<tr><td>").append(Layout.esc(rs.getString("subject_name"))).append("</td><td>")
                        .append(held).append("</td><td>").append(attended).append("</td><td>")
                        .append(held > 0 ? String.format(Locale.US, "%.1f%%", pct) : "-").append("</td><td>")
                        .append(status).append("</td></tr>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (totalHeld > 0) {
            double overall = totalAttended * 100.0 / totalHeld;
            out.println("<div class='stat-cards'>");
            out.println("<div class='stat-card'><div class='num'>" + String.format(Locale.US, "%.1f%%", overall)
                    + "</div><div class='label'>Overall attendance</div></div>");
            out.println("<div class='stat-card'><div class='num'>" + totalAttended + " / " + totalHeld
                    + "</div><div class='label'>Classes attended</div></div>");
            out.println("</div>");
            if (overall < 75) {
                out.println("<div class='notice err'>Your overall attendance is below the required 75%.</div>");
            }
        }

        out.println("<table><tr><th>Subject</th><th>Classes held</th><th>Classes attended</th><th>Percentage</th><th>Status</th></tr>");
        if (rows.length() == 0) {
            out.println("<tr><td colspan='5'>Attendance has not been recorded yet.</td></tr>");
        } else {
            out.println(rows);
        }
        out.println("</table>");

        out.println("</div></div></body></html>");
    }
}