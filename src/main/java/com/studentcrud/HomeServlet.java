package com.studentcrud;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private String jsArray(List<String> items) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(Layout.js(items.get(i)));
        }
        return sb.append("]").toString();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.html");
            return;
        }

        int totalStudents = 0, totalCourses = 0;
        double avgMarks = -1;
        List<String> courseLabels = new ArrayList<>();
        List<Integer> courseCounts = new ArrayList<>();
        List<String> subjectLabels = new ArrayList<>();
        List<Double> subjectAverages = new ArrayList<>();
        List<String[]> lowAttendance = new ArrayList<>();
        List<String[]> topStudents = new ArrayList<>();

        try (Connection con = DBconnection.getConnection();
             Statement st = con.createStatement()) {

            ResultSet rs = st.executeQuery("SELECT COUNT(*), COUNT(DISTINCT course) FROM students");
            if (rs.next()) {
                totalStudents = rs.getInt(1);
                totalCourses = rs.getInt(2);
            }

            rs = st.executeQuery("SELECT AVG(marks_obtained * 100.0 / max_marks) FROM marks WHERE max_marks > 0");
            if (rs.next()) {
                double v = rs.getDouble(1);
                if (!rs.wasNull()) avgMarks = v;
            }

            rs = st.executeQuery("SELECT course, COUNT(*) FROM students GROUP BY course ORDER BY course");
            while (rs.next()) {
                courseLabels.add(rs.getString(1));
                courseCounts.add(rs.getInt(2));
            }

            rs = st.executeQuery("SELECT subject_name, AVG(marks_obtained * 100.0 / max_marks) "
                    + "FROM marks WHERE max_marks > 0 GROUP BY subject_name ORDER BY subject_name");
            while (rs.next()) {
                subjectLabels.add(rs.getString(1));
                subjectAverages.add(Math.round(rs.getDouble(2) * 10) / 10.0);
            }

            rs = st.executeQuery("SELECT s.name, s.course, SUM(a.classes_attended) * 100.0 / SUM(a.classes_held) AS pct "
                    + "FROM students s JOIN attendance a ON a.student_id = s.id "
                    + "GROUP BY s.id, s.name, s.course HAVING pct < 75 ORDER BY pct");
            while (rs.next()) {
                lowAttendance.add(new String[]{rs.getString("name"), rs.getString("course"),
                        String.format(Locale.US, "%.1f%%", rs.getDouble("pct"))});
            }

            rs = st.executeQuery("SELECT s.name, s.course, AVG(m.marks_obtained * 100.0 / m.max_marks) AS avg_pct "
                    + "FROM students s JOIN marks m ON m.student_id = s.id WHERE m.max_marks > 0 "
                    + "GROUP BY s.id, s.name, s.course ORDER BY avg_pct DESC LIMIT 5");
            while (rs.next()) {
                topStudents.add(new String[]{rs.getString("name"), rs.getString("course"),
                        String.format(Locale.US, "%.1f%%", rs.getDouble("avg_pct"))});
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println(Layout.head("Dashboard"));
        out.println(Layout.adminSidebar("home"));
        out.println("<div class='main-content'>");
        out.println("<div class='topbar'><div><h1>Dashboard</h1><p>Overview of students, results and attendance.</p></div></div>");

        // summary boxes
        String avgText = avgMarks < 0 ? "N/A" : String.format(Locale.US, "%.1f%%", avgMarks);
        out.println("<div class='stat-cards'>");
        out.println("<div class='stat-card'><div class='num'>" + totalStudents + "</div><div class='label'>Total students</div></div>");
        out.println("<div class='stat-card'><div class='num'>" + totalCourses + "</div><div class='label'>Courses</div></div>");
        out.println("<div class='stat-card'><div class='num'>" + avgText + "</div><div class='label'>Average marks</div></div>");
        out.println("<div class='stat-card'><div class='num'>" + lowAttendance.size() + "</div><div class='label'>Below 75% attendance</div></div>");
        out.println("</div>");

        // charts
        out.println("<div class='chart-grid'>");
        out.println("<div class='chart-box'><h3>Students per course</h3>");
        if (courseLabels.isEmpty()) {
            out.println("<p class='muted'>No students yet.</p>");
        } else {
            out.println("<div class='cv'><canvas id='courseChart'></canvas></div>");
        }
        out.println("</div>");
        out.println("<div class='chart-box'><h3>Average marks per subject (%)</h3>");
        if (subjectLabels.isEmpty()) {
            out.println("<p class='muted'>No marks recorded yet.</p>");
        } else {
            out.println("<div class='cv'><canvas id='subjectChart'></canvas></div>");
        }
        out.println("</div>");
        out.println("</div>");

        // top students and attendance shortage
        out.println("<div class='chart-grid'>");
        out.println("<div><h2>Top performers</h2><table><tr><th>Name</th><th>Course</th><th>Average</th></tr>");
        if (topStudents.isEmpty()) {
            out.println("<tr><td colspan='3'>No marks recorded yet.</td></tr>");
        }
        for (String[] r : topStudents) {
            out.println("<tr><td>" + Layout.esc(r[0]) + "</td><td>" + Layout.esc(r[1]) + "</td><td>" + r[2] + "</td></tr>");
        }
        out.println("</table></div>");

        out.println("<div><h2>Attendance shortage (below 75%)</h2><table><tr><th>Name</th><th>Course</th><th>Attendance</th></tr>");
        if (lowAttendance.isEmpty()) {
            out.println("<tr><td colspan='3'>No students below 75%.</td></tr>");
        }
        for (String[] r : lowAttendance) {
            out.println("<tr><td>" + Layout.esc(r[0]) + "</td><td>" + Layout.esc(r[1]) + "</td><td><span class='low'>" + r[2] + "</span></td></tr>");
        }
        out.println("</table></div>");
        out.println("</div>");

        // quick links
        out.println("<h2>Quick actions</h2>");
        out.println("<div class='action-cards'>");
        out.println("<a href='index.html' class='action-card'><h3>Add Student</h3><p>Register a new student record.</p></a>");
        out.println("<a href='view' class='action-card'><h3>View Students</h3><p>Browse, search and manage records.</p></a>");
        out.println("<a href='adminMarks' class='action-card'><h3>Enter Marks</h3><p>Record or correct subject marks.</p></a>");
        out.println("<a href='adminAttendance' class='action-card'><h3>Mark Attendance</h3><p>Update classes held and attended.</p></a>");
        out.println("</div>");

        // chart scripts (Chart.js is loaded from a CDN, so an internet connection is needed)
        if (!courseLabels.isEmpty() || !subjectLabels.isEmpty()) {
            out.println("<script src='https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js'></script>");
            out.println("<script>");
            out.println("var navy = '#14284b', gold = '#b8902f';");
            if (!courseLabels.isEmpty()) {
                out.println("new Chart(document.getElementById('courseChart'), {type: 'bar', data: {labels: "
                        + jsArray(courseLabels) + ", datasets: [{label: 'Students', data: " + courseCounts
                        + ", backgroundColor: navy}]}, options: {maintainAspectRatio: false, plugins: {legend: {display: false}}, "
                        + "scales: {y: {beginAtZero: true, ticks: {precision: 0}}}}});");
            }
            if (!subjectLabels.isEmpty()) {
                out.println("new Chart(document.getElementById('subjectChart'), {type: 'bar', data: {labels: "
                        + jsArray(subjectLabels) + ", datasets: [{label: 'Average %', data: " + subjectAverages
                        + ", backgroundColor: gold}]}, options: {maintainAspectRatio: false, plugins: {legend: {display: false}}, "
                        + "scales: {y: {beginAtZero: true, max: 100}}}});");
            }
            out.println("</script>");
        }

        out.println("</div></div></body></html>");
    }
}