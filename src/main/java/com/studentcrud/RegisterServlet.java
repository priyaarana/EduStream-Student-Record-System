package com.studentcrud;

import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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
                out.println("</head><body><div class='login-wrap'><div class='login-card'>");
                out.println("<h2>⚠ Already Registered</h2>");
                out.println("<p>An account with this email or username already exists.</p>");
                out.println("<a href='register.html'>Go Back</a>");
                out.println("</div></div></body></html>");
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

            response.setContentType("text/html; charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<html><head><meta charset='UTF-8'>");
            out.println("<link rel='stylesheet' type='text/css' href='style.css'>");
            out.println("</head><body><div class='login-wrap'><div class='login-card'>");
            out.println("<h2>✅ Registration Successful</h2>");
            out.println("<p>You can now log in with your new username and password.</p>");
            out.println("<a href='studentlogin.html' style='display:block; text-align:center; background:#4a3aff; color:white; padding:13px; border-radius:8px; font-weight:600; text-decoration:none; margin-top:16px;'>Go to Student Login</a>");
            out.println("</div></div></body></html>");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}