package com.studentcrud;

import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/view")
public class ViewServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.html");
            return;
        }

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<html><head><title>Student Records</title>");
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
        out.println("<div class='topbar'><div><h1>Student Records</h1></div></div>");

        out.println("<form class='search-row' action='search' method='get'>");
        out.println("<input type='text' name='keyword' placeholder='Search by name'>");
        out.println("<select name='course'>");
        out.println("<option value=''>All Courses</option>");
        out.println("<option value='MCA'>MCA</option>");
        out.println("<option value='BCA'>BCA</option>");
        out.println("<option value='BTech'>BTech</option>");
        out.println("<option value='MTech'>MTech</option>");
        out.println("</select>");
        out.println("<input type='submit' value='Search'>");
        out.println("</form>");

        out.println("<table>");
        out.println("<tr><th>ID</th><th>Name</th><th>Course</th><th>Dept</th><th>Email</th><th>Attendance</th><th>Clubs</th><th>Actions</th></tr>");

        try {
            Connection con = DBconnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM students");

            while (rs.next()) {
                int id = rs.getInt("id");
                String clubs = rs.getString("clubs");
                if (clubs == null) clubs = "-";
                out.println("<tr>");
                out.println("<td>" + id + "</td>");
                out.println("<td>" + rs.getString("name") + "</td>");
                out.println("<td>" + rs.getString("course") + "</td>");
                out.println("<td>" + rs.getString("department") + "</td>");
                out.println("<td>" + rs.getString("email") + "</td>");
                out.println("<td>" + rs.getString("attendance") + "</td>");
                out.println("<td>" + clubs + "</td>");
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
        out.println("<p style='margin-bottom:10px;'><a href='exportCsv'>Download student list (CSV)</a></p>");
        out.println("</table></div></div></body></html>");
    }
}