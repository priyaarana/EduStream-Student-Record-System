package com.studentcrud;

import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/adminAttendance")
public class AdminAttendanceServlet extends HttpServlet {

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

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println(Layout.head("Attendance"));
        out.println(Layout.adminSidebar("attendance"));
        out.println("<div class='main-content'>");

        try (Connection con = DBconnection.getConnection()) {

            if (studentId <= 0) {
                // Step 1: choose a student
                out.println("<div class='topbar'><div><h1>Attendance</h1><p>Select a student to update attendance.</p></div></div>");
                out.println("<table><tr><th>ID</th><th>Name</th><th>Course</th><th>Overall</th><th>Action</th></tr>");
                ResultSet rs = con.createStatement().executeQuery(
                        "SELECT id, name, course, attendance FROM students ORDER BY name");
                while (rs.next()) {
                    int id = rs.getInt("id");
                    out.println("<tr><td>" + id + "</td><td>" + Layout.esc(rs.getString("name")) + "</td><td>"
                            + Layout.esc(rs.getString("course")) + "</td><td>" + Layout.esc(rs.getString("attendance"))
                            + "</td><td><a href='adminAttendance?id=" + id + "'>Update Attendance</a></td></tr>");
                }
                out.println("</table>");

            } else {
                // Step 2: one row per subject of the student's course
                PreparedStatement sp = con.prepareStatement(
                        "SELECT name, course, attendance FROM students WHERE id=?");
                sp.setInt(1, studentId);
                ResultSet sr = sp.executeQuery();

                if (!sr.next()) {
                    out.println("<div class='topbar'><div><h1>Attendance</h1></div></div>");
                    out.println("<p>Student not found. <a href='adminAttendance'>Back</a></p>");
                } else {
                    String name = sr.getString("name");
                    String course = sr.getString("course");
                    String overall = sr.getString("attendance");

                    out.println("<div class='topbar'><div><h1>Attendance: " + Layout.esc(name) + "</h1><p>"
                            + Layout.esc(course) + " &nbsp;|&nbsp; Overall: " + Layout.esc(overall)
                            + " &nbsp;|&nbsp; <a href='adminAttendance'>&larr; All students</a></p></div></div>");

                    String msg = request.getParameter("msg");
                    if ("saved".equals(msg)) {
                        out.println("<div class='notice ok'>Attendance saved.</div>");
                    } else if ("invalid".equals(msg)) {
                        out.println("<div class='notice err'>Invalid input. Enter whole numbers, and attended classes cannot be more than classes held.</div>");
                    }

                    // attendance already recorded for this student
                    Map<String, int[]> existing = new HashMap<>();
                    PreparedStatement ap = con.prepareStatement(
                            "SELECT subject_name, classes_held, classes_attended FROM attendance WHERE student_id=?");
                    ap.setInt(1, studentId);
                    ResultSet ar = ap.executeQuery();
                    while (ar.next()) {
                        existing.put(ar.getString("subject_name"),
                                new int[]{ar.getInt("classes_held"), ar.getInt("classes_attended")});
                    }

                    PreparedStatement subPs = con.prepareStatement(
                            "SELECT subject_name FROM subjects WHERE course=? ORDER BY subject_name");
                    subPs.setString(1, course);
                    ResultSet subRs = subPs.executeQuery();

                    StringBuilder rows = new StringBuilder();
                    int i = 0;
                    while (subRs.next()) {
                        String sub = subRs.getString("subject_name");
                        int[] v = existing.getOrDefault(sub, new int[]{0, 0});
                        String pct = v[0] > 0 ? String.format(Locale.US, "%.1f%%", v[1] * 100.0 / v[0]) : "-";
                        rows.append("<tr><td>").append(Layout.esc(sub))
                                .append("<input type='hidden' name='subject_").append(i).append("' value='")
                                .append(Layout.esc(sub)).append("'></td>")
                                .append("<td><input type='text' name='held_").append(i).append("' value='").append(v[0]).append("'></td>")
                                .append("<td><input type='text' name='att_").append(i).append("' value='").append(v[1]).append("'></td>")
                                .append("<td>").append(pct).append("</td></tr>");
                        i++;
                    }

                    if (i == 0) {
                        out.println("<div class='notice err'>No subjects are defined for this course yet.</div>");
                    } else {
                        out.println("<form action='adminAttendance' method='post'>");
                        out.println("<input type='hidden' name='id' value='" + studentId + "'>");
                        out.println("<input type='hidden' name='count' value='" + i + "'>");
                        out.println("<table><tr><th>Subject</th><th>Classes held</th><th>Classes attended</th><th>Current %</th></tr>");
                        out.println(rows);
                        out.println("</table>");
                        out.println("<input type='submit' value='Save Attendance'>");
                        out.println("</form>");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<div class='notice err'>Something went wrong while loading this page.</div>");
        }

        out.println("</div></div></body></html>");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            response.sendRedirect("login.html");
            return;
        }

        int studentId = Layout.toInt(request.getParameter("id"), -1);
        if (studentId <= 0) {
            response.sendRedirect("adminAttendance");
            return;
        }

        String result = "invalid";
        try {
            int count = Layout.toInt(request.getParameter("count"), 0);
            if (count < 1 || count > 100) {
                throw new IllegalArgumentException("Bad subject count");
            }

            // read and validate every row before saving anything
            String[] subjects = new String[count];
            int[] held = new int[count];
            int[] attended = new int[count];
            for (int i = 0; i < count; i++) {
                subjects[i] = request.getParameter("subject_" + i);
                held[i] = Integer.parseInt(request.getParameter("held_" + i).trim());
                attended[i] = Integer.parseInt(request.getParameter("att_" + i).trim());
                if (subjects[i] == null || held[i] < 0 || attended[i] < 0 || attended[i] > held[i]) {
                    throw new IllegalArgumentException("Bad row " + i);
                }
            }

            try (Connection con = DBconnection.getConnection()) {
                con.setAutoCommit(false);
                try {
                    String sql = "INSERT INTO attendance(student_id, subject_name, classes_held, classes_attended) "
                            + "VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE "
                            + "classes_held=VALUES(classes_held), classes_attended=VALUES(classes_attended)";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        for (int i = 0; i < count; i++) {
                            ps.setInt(1, studentId);
                            ps.setString(2, subjects[i]);
                            ps.setInt(3, held[i]);
                            ps.setInt(4, attended[i]);
                            ps.executeUpdate();
                        }
                    }
                    updateOverall(con, studentId);
                    con.commit();
                    result = "saved";
                } catch (Exception e) {
                    con.rollback();
                    throw e;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.sendRedirect("adminAttendance?id=" + studentId + "&msg=" + result);
    }

    /** Recalculates the overall percentage and stores it in students.attendance. */
    private void updateOverall(Connection con, int studentId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT SUM(classes_held), SUM(classes_attended) FROM attendance WHERE student_id=?")) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long held = rs.getLong(1);
                long attended = rs.getLong(2);
                String value = held > 0
                        ? String.format(Locale.US, "%.1f%%", attended * 100.0 / held)
                        : "Not Marked";
                try (PreparedStatement up = con.prepareStatement("UPDATE students SET attendance=? WHERE id=?")) {
                    up.setString(1, value);
                    up.setInt(2, studentId);
                    up.executeUpdate();
                }
            }
        }
    }
}