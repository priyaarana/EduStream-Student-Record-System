package com.studentcrud;

import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/exportCsv")
public class ExportCsvServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.html");
            return;
        }

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"students.csv\"");
        PrintWriter out = response.getWriter();

        out.print("\uFEFF"); // lets Excel read the file as UTF-8
        out.println("ID,Name,Course,Department,Email,Attendance,Clubs");

        try (Connection con = DBconnection.getConnection();
             Statement st = con.createStatement()) {

            ResultSet rs = st.executeQuery(
                    "SELECT id, name, course, department, email, attendance, clubs FROM students ORDER BY id");
            while (rs.next()) {
                out.println(rs.getInt("id") + "," + csv(rs.getString("name")) + "," + csv(rs.getString("course"))
                        + "," + csv(rs.getString("department")) + "," + csv(rs.getString("email"))
                        + "," + csv(rs.getString("attendance")) + "," + csv(rs.getString("clubs")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String csv(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}