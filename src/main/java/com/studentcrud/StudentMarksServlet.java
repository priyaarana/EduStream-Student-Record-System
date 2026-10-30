package com.studentcrud;

import java.io.*;
import java.sql.*;
import java.util.Locale;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/studentMarks")
public class StudentMarksServlet extends HttpServlet {

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

        out.println(Layout.head("My Marks"));
        out.println(Layout.studentSidebar("marks"));
        out.println("<div class='main-content'>");
        out.println("<div class='topbar'><div><h1>My Marks</h1><p>Subject-wise results and overall performance.</p></div></div>");

        StringBuilder rows = new StringBuilder();
        double totalObtained = 0, totalMax = 0;
        boolean failedAny = false;
        int count = 0;

        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT subject_name, marks_obtained, max_marks FROM marks "
                             + "WHERE student_id=? ORDER BY subject_name")) {

            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int obtained = rs.getInt("marks_obtained");
                int max = rs.getInt("max_marks");
                double pct = max > 0 ? obtained * 100.0 / max : 0;

                totalObtained += obtained;
                totalMax += max;
                count++;
                if (pct < 40) failedAny = true;

                rows.append("<tr><td>").append(Layout.esc(rs.getString("subject_name"))).append("</td><td>")
                        .append(obtained).append("</td><td>").append(max).append("</td><td>")
                        .append(String.format(Locale.US, "%.1f%%", pct)).append("</td><td>")
                        .append(Layout.grade(pct)).append("</td></tr>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (count > 0 && totalMax > 0) {
            double overall = totalObtained * 100.0 / totalMax;
            out.println("<div class='stat-cards'>");
            out.println("<div class='stat-card'><div class='num'>" + (int) totalObtained + " / " + (int) totalMax
                    + "</div><div class='label'>Total marks</div></div>");
            out.println("<div class='stat-card'><div class='num'>" + String.format(Locale.US, "%.1f%%", overall)
                    + "</div><div class='label'>Percentage</div></div>");
            out.println("<div class='stat-card'><div class='num'>" + Layout.grade(overall)
                    + "</div><div class='label'>Overall grade</div></div>");
            out.println("<div class='stat-card'><div class='num'>" + (failedAny ? "<span class='low'>Reappear</span>" : "<span class='good'>Pass</span>")
                    + "</div><div class='label'>Result (pass mark 40%)</div></div>");
            out.println("</div>");
        }

        out.println("<table><tr><th>Subject</th><th>Marks obtained</th><th>Maximum</th><th>Percentage</th><th>Grade</th></tr>");
        if (rows.length() == 0) {
            out.println("<tr><td colspan='5'>No marks have been recorded yet.</td></tr>");
        } else {
            out.println(rows);
        }
        out.println("</table>");

        out.println("</div></div></body></html>");
    }
}