package com.studentcrud;

import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/update")
public class UpdateServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.html");
            return;
        }

        int id = Integer.parseInt(request.getParameter("id"));
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            Connection con = DBconnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM students WHERE id=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                out.println("<html><head><title>Edit Student</title>");
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
                out.println("<div class='topbar'><div><h1>Edit Student</h1></div></div>");
                out.println("<form class='panel' action='update' method='post'>");
                out.println("<input type='hidden' name='id' value='" + id + "'>");
                out.println("<p class='field-label'>Name</p>");
                out.println("<input type='text' name='name' value='" + rs.getString("name") + "'>");
                out.println("<p class='field-label'>Course</p>");
                out.println("<input type='text' name='course' value='" + rs.getString("course") + "'>");
                out.println("<p class='field-label'>Email</p>");
                out.println("<input type='text' name='email' value='" + rs.getString("email") + "'>");
                out.println("<input type='submit' value='Update'>");
                out.println("</form></div></div></body></html>");
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.html");
            return;
        }

        int id = Integer.parseInt(request.getParameter("id"));
        String name = request.getParameter("name");
        String course = request.getParameter("course");
        String email = request.getParameter("email");

        try {
            Connection con = DBconnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "UPDATE students SET name=?, course=?, email=? WHERE id=?"
            );
            ps.setString(1, name);
            ps.setString(2, course);
            ps.setString(3, email);
            ps.setInt(4, id);
            ps.executeUpdate();
            con.close();

            response.sendRedirect("view");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}