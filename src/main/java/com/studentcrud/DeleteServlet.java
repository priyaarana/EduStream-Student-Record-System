package com.studentcrud;

import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/delete")
public class DeleteServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.html");
            return;
        }

        int id = Layout.toInt(request.getParameter("id"), -1);

        if (id > 0) {
            try (Connection con = DBconnection.getConnection()) {
                con.setAutoCommit(false);
                try {
                    // child rows first, then the student
                    String[] statements = {
                        "DELETE FROM marks WHERE student_id=?",
                        "DELETE FROM attendance WHERE student_id=?",
                        "DELETE FROM students WHERE id=?"
                    };
                    for (String sql : statements) {
                        try (PreparedStatement ps = con.prepareStatement(sql)) {
                            ps.setInt(1, id);
                            ps.executeUpdate();
                        }
                    }
                    con.commit();
                } catch (Exception e) {
                    con.rollback();
                    throw e;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        response.sendRedirect("view");
    }
}