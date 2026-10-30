package com.studentcrud;

import java.io.*;
import java.sql.*;
import java.util.Locale;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/studentHome")
public class StudentHomeServlet extends HttpServlet {

    // Styles for this page only, so style.css does not need to change.
    private static final String PAGE_CSS =
        "<style>"
        + ".sh-hero{display:flex;align-items:center;gap:18px;margin-bottom:26px}"
        + ".sh-avatar{width:64px;height:64px;border-radius:50%;background:#4a3aff;color:#fff;"
        +   "display:flex;align-items:center;justify-content:center;font-size:24px;font-weight:700;flex-shrink:0}"
        + ".sh-hero h1{margin:0 0 4px 0}.sh-hero p{margin:0;color:#777}"
        + ".sh-cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(190px,1fr));gap:18px;margin-bottom:26px}"
        + ".sh-card{background:#fff;border-radius:16px;padding:20px 22px;box-shadow:0 4px 14px rgba(0,0,0,.06)}"
        + ".sh-card .v{font-size:26px;font-weight:700;color:#4a3aff;line-height:1.2}"
        + ".sh-card .l{color:#777;font-size:14px;margin-top:4px}"
        + ".sh-card .s{color:#999;font-size:12px;margin-top:2px}"
        + ".sh-panel{background:#fff;border-radius:16px;padding:22px 26px;box-shadow:0 4px 14px rgba(0,0,0,.06);margin-bottom:26px}"
        + ".sh-panel h2{margin:0 0 16px 0;font-size:18px}"
        + ".sh-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:18px 28px}"
        + ".sh-grid .k{color:#888;font-size:13px;margin-bottom:3px}"
        + ".sh-grid .d{font-weight:600;word-break:break-word}"
        + ".sh-more{display:inline-block;margin-top:12px;color:#4a3aff;font-weight:600}"
        + "</style>";

    private static String initials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        String s = parts[0].substring(0, 1);
        if (parts.length > 1) s += parts[1].substring(0, 1);
        return s.toUpperCase();
    }

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

        out.println(Layout.head("My Profile"));
        out.println(Layout.studentSidebar("profile"));
        out.println("<div class='main-content'>");
        out.println(PAGE_CSS);

        try (Connection con = DBconnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement("SELECT * FROM students WHERE id=?");
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                out.println("<div class='notice err'>Your record could not be found.</div>");
            } else {
                String name = rs.getString("name");
                String course = rs.getString("course");
                String department = rs.getString("department");
                String email = rs.getString("email");
                String attendance = rs.getString("attendance");
                String clubs = rs.getString("clubs");
                if (clubs == null || clubs.trim().isEmpty()) clubs = "Not enrolled in any club";
                if (attendance == null || attendance.trim().isEmpty()) attendance = "Not Marked";

                // number of subjects in this student's course
                int totalSubjects = 0;
                PreparedStatement sp = con.prepareStatement("SELECT COUNT(*) FROM subjects WHERE course=?");
                sp.setString(1, course);
                ResultSet sr = sp.executeQuery();
                if (sr.next()) totalSubjects = sr.getInt(1);

                // marks summary
                int markedSubjects = 0;
                long gotSum = 0, maxSum = 0;
                PreparedStatement mp = con.prepareStatement(
                        "SELECT COUNT(*), SUM(marks_obtained), SUM(max_marks) FROM marks WHERE student_id=?");
                mp.setInt(1, studentId);
                ResultSet mr = mp.executeQuery();
                if (mr.next()) {
                    markedSubjects = mr.getInt(1);
                    gotSum = mr.getLong(2);
                    maxSum = mr.getLong(3);
                }
                boolean hasMarks = markedSubjects > 0 && maxSum > 0;
                double avg = hasMarks ? gotSum * 100.0 / maxSum : 0;

                // ---- welcome header ----
                out.println("<div class='sh-hero'>");
                out.println("<div class='sh-avatar'>" + Layout.esc(initials(name)) + "</div>");
                out.println("<div><h1>Welcome back, " + Layout.esc(name) + "</h1>"
                        + "<p>" + Layout.esc(course) + " &nbsp;|&nbsp; " + Layout.esc(department) + "</p></div>");
                out.println("</div>");

                // ---- summary cards ----
                out.println("<div class='sh-cards'>");
                out.println("<div class='sh-card'><div class='v'>" + totalSubjects
                        + "</div><div class='l'>Subjects enrolled</div></div>");
                out.println("<div class='sh-card'><div class='v'>" + markedSubjects + " / " + totalSubjects
                        + "</div><div class='l'>Subjects with marks</div></div>");
                if (hasMarks) {
                    out.println("<div class='sh-card'><div class='v'>"
                            + String.format(Locale.US, "%.1f%%", avg) + "</div><div class='l'>Overall average</div>"
                            + "<div class='s'>Grade " + Layout.grade(avg) + "</div></div>");
                } else {
                    out.println("<div class='sh-card'><div class='v'>&mdash;</div>"
                            + "<div class='l'>Overall average</div><div class='s'>No marks yet</div></div>");
                }
                out.println("<div class='sh-card'><div class='v' style='font-size:20px'>" + Layout.esc(attendance)
                        + "</div><div class='l'>Attendance</div></div>");
                out.println("</div>");

                // ---- profile details ----
                out.println("<div class='sh-panel'><h2>Profile details</h2><div class='sh-grid'>");
                out.println("<div><div class='k'>Name</div><div class='d'>" + Layout.esc(name) + "</div></div>");
                out.println("<div><div class='k'>Email</div><div class='d'>" + Layout.esc(email) + "</div></div>");
                out.println("<div><div class='k'>Course</div><div class='d'>" + Layout.esc(course) + "</div></div>");
                out.println("<div><div class='k'>Department</div><div class='d'>" + Layout.esc(department) + "</div></div>");
                out.println("<div><div class='k'>Clubs</div><div class='d'>" + Layout.esc(clubs) + "</div></div>");
                out.println("</div></div>");

                // ---- recent marks ----
                out.println("<div class='sh-panel'><h2>Recent marks</h2>");
                PreparedStatement rp = con.prepareStatement(
                        "SELECT subject_name, marks_obtained, max_marks FROM marks "
                        + "WHERE student_id=? ORDER BY id DESC LIMIT 5");
                rp.setInt(1, studentId);
                ResultSet rr = rp.executeQuery();
                boolean any = false;
                StringBuilder rows = new StringBuilder();
                while (rr.next()) {
                    any = true;
                    int got = rr.getInt("marks_obtained");
                    int max = rr.getInt("max_marks");
                    double pct = max > 0 ? got * 100.0 / max : 0;
                    rows.append("<tr><td>").append(Layout.esc(rr.getString("subject_name"))).append("</td><td>")
                        .append(got).append(" / ").append(max).append("</td><td>")
                        .append(String.format(Locale.US, "%.1f%%", pct)).append("</td><td>")
                        .append(Layout.grade(pct)).append("</td></tr>");
                }
                if (any) {
                    out.println("<table><tr><th>Subject</th><th>Marks</th><th>Percentage</th><th>Grade</th></tr>");
                    out.println(rows);
                    out.println("</table>");
                    out.println("<a class='sh-more' href='studentMarks'>View all marks &rarr;</a>");
                } else {
                    out.println("<p>No marks have been recorded for you yet.</p>");
                }
                out.println("</div>");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<div class='notice err'>Something went wrong while loading your profile.</div>");
        }

        out.println("</div></div></body></html>");
    }
}