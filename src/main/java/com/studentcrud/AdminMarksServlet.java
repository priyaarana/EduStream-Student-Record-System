package com.studentcrud;

import java.io.*;
import java.sql.*;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/adminMarks")
public class AdminMarksServlet extends HttpServlet {

    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("user") != null;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            response.sendRedirect("login.html");
            return;
        }

        int studentId = Layout.toInt(request.getParameter("id"), -1);
        int markId = Layout.toInt(request.getParameter("del"), -1);

        // Delete one marks entry, then return to the same student
        if (studentId > 0 && markId > 0) {
            try (Connection con = DBconnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("DELETE FROM marks WHERE id=? AND student_id=?")) {
                ps.setInt(1, markId);
                ps.setInt(2, studentId);
                ps.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
            response.sendRedirect("adminMarks?id=" + studentId + "&msg=deleted");
            return;
        }

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println(Layout.head("Marks"));
        out.println(Layout.adminSidebar("marks"));
        out.println("<div class='main-content'>");

        try (Connection con = DBconnection.getConnection()) {

            if (studentId <= 0) {
                // Step 1: choose a student
                out.println("<div class='topbar'><div><h1>Marks</h1><p>Select a student to view, edit or delete marks.</p></div></div>");
                out.println("<table><tr><th>ID</th><th>Name</th><th>Course</th><th>Action</th></tr>");
                ResultSet rs = con.createStatement().executeQuery("SELECT id, name, course FROM students ORDER BY name");
                while (rs.next()) {
                    int id = rs.getInt("id");
                    out.println("<tr><td>" + id + "</td><td>" + Layout.esc(rs.getString("name")) + "</td><td>"
                            + Layout.esc(rs.getString("course")) + "</td><td><a href='adminMarks?id=" + id
                            + "'>Manage Marks</a></td></tr>");
                }
                out.println("</table>");

            } else {
                // Step 2: marks for the chosen student
                PreparedStatement sp = con.prepareStatement("SELECT name, course FROM students WHERE id=?");
                sp.setInt(1, studentId);
                ResultSet sr = sp.executeQuery();

                if (!sr.next()) {
                    out.println("<div class='topbar'><div><h1>Marks</h1></div></div>");
                    out.println("<p>Student not found. <a href='adminMarks'>Back</a></p>");
                } else {
                    String name = sr.getString("name");
                    String course = sr.getString("course");
                    int editId = Layout.toInt(request.getParameter("edit"), -1);

                    out.println("<div class='topbar'><div><h1>Marks: " + Layout.esc(name) + "</h1><p>"
                            + Layout.esc(course) + " &nbsp;|&nbsp; <a href='adminMarks'>&larr; All students</a></p></div></div>");

                    String msg = request.getParameter("msg");
                    if ("saved".equals(msg)) {
                        out.println("<div class='notice ok'>Marks saved.</div>");
                    } else if ("deleted".equals(msg)) {
                        out.println("<div class='notice ok'>Marks deleted.</div>");
                    } else if ("invalid".equals(msg)) {
                        out.println("<div class='notice err'>Invalid input. Marks must be a whole number between 0 and the maximum marks.</div>");
                    }

                    // ---- 1. Load recorded marks (table rows + data for the edit form) ----
                    StringBuilder rows = new StringBuilder();
                    Set<String> recorded = new HashSet<>();
                    String editSubject = null;
                    int editMarks = 0;
                    int editMax = 100;

                    PreparedStatement mp = con.prepareStatement(
                            "SELECT id, subject_name, marks_obtained, max_marks FROM marks WHERE student_id=? ORDER BY subject_name");
                    mp.setInt(1, studentId);
                    ResultSet mr = mp.executeQuery();
                    while (mr.next()) {
                        int mid = mr.getInt("id");
                        String subj = mr.getString("subject_name");
                        int obtained = mr.getInt("marks_obtained");
                        int max = mr.getInt("max_marks");
                        double pct = max > 0 ? obtained * 100.0 / max : 0;

                        recorded.add(subj);
                        if (mid == editId) {
                            editSubject = subj;
                            editMarks = obtained;
                            editMax = max;
                        }

                        rows.append("<tr><td>").append(Layout.esc(subj)).append("</td><td>").append(obtained)
                                .append("</td><td>").append(max).append("</td><td>")
                                .append(String.format(Locale.US, "%.1f%%", pct)).append("</td><td>")
                                .append(Layout.grade(pct)).append("</td><td>")
                                .append("<a href='adminMarks?id=").append(studentId).append("&amp;edit=").append(mid)
                                .append("#marks-form'>Edit</a> &nbsp;|&nbsp; ")
                                .append("<a href='adminMarks?id=").append(studentId).append("&amp;del=").append(mid)
                                .append("' onclick=\"return confirm('Delete marks for this subject?')\">Delete</a>")
                                .append("</td></tr>");
                    }

                    // ---- 2. Marks table (shown first) ----
                    out.println("<h2>Recorded marks</h2>");
                    out.println("<table><tr><th>Subject</th><th>Marks</th><th>Max</th><th>Percentage</th><th>Grade</th><th>Action</th></tr>");
                    if (rows.length() == 0) {
                        out.println("<tr><td colspan='6'>No marks recorded yet.</td></tr>");
                    } else {
                        out.println(rows);
                    }
                    out.println("</table>");

                    // ---- 3. Edit form (if Edit was clicked) or Add form ----
                    if (editSubject != null) {
                        String subjEsc = Layout.esc(editSubject);
                        out.println("<h2 id='marks-form'>Edit marks</h2>");
                        out.println("<form class='panel' action='adminMarks' method='post'>");
                        out.println("<input type='hidden' name='id' value='" + studentId + "'>");
                        out.println("<input type='hidden' name='subject' value='" + subjEsc + "'>");
                        out.println("<p class='field-label'>Subject</p>");
                        out.println("<input type='text' value='" + subjEsc + "' readonly>");
                        out.println("<p class='field-label'>Marks obtained</p>");
                        out.println("<input type='text' name='marks' value='" + editMarks + "' required>");
                        out.println("<p class='field-label'>Maximum marks</p>");
                        out.println("<input type='text' name='max' value='" + editMax + "' required>");
                        out.println("<input type='submit' value='Update Marks'>");
                        out.println("<p><a href='adminMarks?id=" + studentId + "'>Cancel</a></p>");
                        out.println("</form>");
                    } else {
                        // only offer subjects that don't have marks yet
                        PreparedStatement subPs = con.prepareStatement(
                                "SELECT subject_name FROM subjects WHERE course=? ORDER BY subject_name");
                        subPs.setString(1, course);
                        ResultSet subRs = subPs.executeQuery();
                        StringBuilder options = new StringBuilder();
                        while (subRs.next()) {
                            String raw = subRs.getString("subject_name");
                            if (recorded.contains(raw)) {
                                continue;
                            }
                            String sub = Layout.esc(raw);
                            options.append("<option value='").append(sub).append("'>").append(sub).append("</option>");
                        }

                        out.println("<h2 id='marks-form'>Add marks</h2>");
                        if (options.length() == 0) {
                            out.println("<div class='notice ok'>Marks are recorded for every subject of this course. Use Edit above to change them.</div>");
                        } else {
                            out.println("<form class='panel' action='adminMarks' method='post'>");
                            out.println("<input type='hidden' name='id' value='" + studentId + "'>");
                            out.println("<p class='field-label'>Subject</p>");
                            out.println("<select name='subject'>" + options + "</select>");
                            out.println("<p class='field-label'>Marks obtained</p>");
                            out.println("<input type='text' name='marks' required>");
                            out.println("<p class='field-label'>Maximum marks</p>");
                            out.println("<input type='text' name='max' value='100' required>");
                            out.println("<input type='submit' value='Save Marks'>");
                            out.println("</form>");
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<div class='notice err'>Something went wrong while loading this page.</div>");
        }

        out.println("</div></div></body></html>");
    }

    // doPost is unchanged: it updates the row if the subject already has marks, otherwise inserts.
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            response.sendRedirect("login.html");
            return;
        }

        int studentId = Layout.toInt(request.getParameter("id"), -1);
        if (studentId <= 0) {
            response.sendRedirect("adminMarks");
            return;
        }

        String result = "invalid";
        try {
            String subject = request.getParameter("subject");
            int marks = Integer.parseInt(request.getParameter("marks").trim());
            int max = Integer.parseInt(request.getParameter("max").trim());

            if (subject != null && !subject.isEmpty() && max > 0 && marks >= 0 && marks <= max) {
                try (Connection con = DBconnection.getConnection()) {
                    PreparedStatement chk = con.prepareStatement(
                            "SELECT id FROM marks WHERE student_id=? AND subject_name=?");
                    chk.setInt(1, studentId);
                    chk.setString(2, subject);
                    ResultSet rs = chk.executeQuery();

                    if (rs.next()) {
                        PreparedStatement up = con.prepareStatement(
                                "UPDATE marks SET marks_obtained=?, max_marks=? WHERE id=?");
                        up.setInt(1, marks);
                        up.setInt(2, max);
                        up.setInt(3, rs.getInt("id"));
                        up.executeUpdate();
                    } else {
                        PreparedStatement ins = con.prepareStatement(
                                "INSERT INTO marks(student_id, subject_name, marks_obtained, max_marks) VALUES(?,?,?,?)");
                        ins.setInt(1, studentId);
                        ins.setString(2, subject);
                        ins.setInt(3, marks);
                        ins.setInt(4, max);
                        ins.executeUpdate();
                    }
                    result = "saved";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.sendRedirect("adminMarks?id=" + studentId + "&msg=" + result);
    }
}