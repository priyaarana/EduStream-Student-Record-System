package com.studentcrud;

import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/search")
public class SearchServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.html");
            return;
        }

        String keyword = request.getParameter("keyword");
        String course = request.getParameter("course");
        if (keyword == null) keyword = "";
        if (course == null) course = "";

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<html><head><title>Search Results</title>");
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
        out.println("<div class='topbar'><div><h1>Search Results</h1></div></div>");

        out.println("<table>");
        out.println("<tr><th>ID</th><th>Name</th><th>Course</th><th>Email</th><th>Actions</th></tr>");

        try {
            Connection con = DBconnection.getConnection();
            String sql = "SELECT * FROM students WHERE name LIKE ?";
            if (!course.isEmpty()) sql += " AND course = ?";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
            if (!course.isEmpty()) ps.setString(2, course);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                out.println("<tr>");
                out.println("<td>" + id + "</td>");
                out.println("<td>" + rs.getString("name") + "</td>");
                out.println("<td>" + rs.getString("course") + "</td>");
                out.println("<td>" + rs.getString("email") + "</td>");
                out.println("<td>"
                    + "<a href='update?id=" + id + "'>Edit</a> | "
                    + "<a href='delete?id=" + id + "'>Delete</a>"
                    + "</td>");
                out.println("</tr>");
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        out.println("</table></div></div></body></html>");
    }
}