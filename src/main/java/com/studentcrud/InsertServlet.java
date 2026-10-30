package com.studentcrud;

import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/insert")
public class InsertServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.html");
            return;
        }

        String name = request.getParameter("name");
        String course = request.getParameter("course");
        String department = request.getParameter("department");
        String email = request.getParameter("email");
        String clubs = request.getParameter("clubs");
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try {
            Connection con = DBconnection.getConnection();

            PreparedStatement checkPs = con.prepareStatement(
                "SELECT COUNT(*) FROM students WHERE email=? OR username=?"
            );
            checkPs.setString(1, email);
            checkPs.setString(2, username);
            ResultSet rs = checkPs.executeQuery();
            rs.next();

            if (rs.getInt(1) > 0) {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.println("<html><head><meta charset='UTF-8'>");
                out.println("<link rel='stylesheet' type='text/css' href='style.css'>");
                out.println("</head><body><div class='container'>");
                out.println("<h2>⚠ Duplicate Email or Username</h2>");
                out.println("<p>A student with this email or username already exists.</p>");
                out.println("<a href='index.html'>Go Back</a>");
                out.println("</div></body></html>");
                con.close();
                return;
            }

            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO students(name, course, department, email, clubs, username, password, attendance) " +
                "VALUES(?,?,?,?,?,?,?,'Not Marked')"
            );
            ps.setString(1, name);
            ps.setString(2, course);
            ps.setString(3, department);
            ps.setString(4, email);
            ps.setString(5, clubs);
            ps.setString(6, username);
            ps.setString(7, password);
            ps.executeUpdate();
            con.close();

            response.sendRedirect("view");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}