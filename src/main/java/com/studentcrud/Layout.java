package com.studentcrud;

public class Layout {

    // Change the app name here and every page that uses Layout updates.
    private static final String BRAND = "🎓 EduStream";

    public static String head(String title) {
        return "<html><head><title>" + esc(title) + "</title>"
                + "<meta charset='UTF-8'>"
                + "<link rel='stylesheet' type='text/css' href='style.css'>"
                + "</head><body><div class='app-shell'>";
    }

    private static String link(String href, String label, String key, String active, boolean logout) {
        String cls = key.equals(active) ? "active" : "";
        if (logout) cls = (cls + " logout").trim();
        return "<a href='" + href + "'" + (cls.isEmpty() ? "" : " class='" + cls + "'") + ">" + label + "</a>";
    }

    public static String adminSidebar(String active) {
        return "<div class='sidebar'><div class='brand'>" + BRAND + "</div>"
                + link("home", "Dashboard", "home", active, false)
                + link("index.html", "Add Student", "add", active, false)
                + link("view", "View Students", "view", active, false)
                + link("adminMarks", "Marks", "marks", active, false)
                + link("adminAttendance", "Attendance", "attendance", active, false)
                + link("logout", "Logout", "logout", active, true)
                + "</div>";
    }

    public static String studentSidebar(String active) {
        return "<div class='sidebar'><div class='brand'>" + BRAND + "</div>"
                + link("studentHome", "My Profile", "profile", active, false)
                + link("studentSubjects", "Subjects", "subjects", active, false)
                + link("studentAttendance", "Attendance", "attendance", active, false)
                + link("studentMarks", "Marks", "marks", active, false)
                + link("studentTimetable", "Timetable", "timetable", active, false)
                + link("logout", "Logout", "logout", active, true)
                + "</div>";
    }

    /** Escapes text so it is safe to place inside HTML. */
    public static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    /** Turns text into a quoted JavaScript string literal. */
    public static String js(String s) {
        if (s == null) s = "";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("<", "\\u003c").replace("\n", " ").replace("\r", " ") + "\"";
    }

    public static String grade(double pct) {
        if (pct >= 90) return "A+";
        if (pct >= 80) return "A";
        if (pct >= 70) return "B+";
        if (pct >= 60) return "B";
        if (pct >= 50) return "C";
        if (pct >= 40) return "D";
        return "F";
    }

    public static int toInt(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }
}