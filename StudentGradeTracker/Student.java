package StudentGradeTracker;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 *  Student – core model
 *  Fields  : name, rollNo, section, marks, attendance, subjects
 */
public class Student implements Serializable {
    private static final long serialVersionUID = 3L;

    private String name;
    private String rollNo;
    private String section;
    private double marks;
    private double attendance;                              // percentage 0-100
    private LinkedHashMap<String, Double> subjects = new LinkedHashMap<>();

    // ── Constructors ──────────────────────────────────────────────────────────
    public Student(String name, String rollNo, String section, double marks) {
        this.name      = name;
        this.rollNo    = rollNo;
        this.section   = section;
        this.marks     = marks;
        this.attendance = 100.0;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public String getName()       { return name; }
    public String getRollNo()     { return rollNo; }
    public String getSection()    { return section; }
    public double getAttendance() { return attendance; }

    public void setName(String n)        { this.name = n; }
    public void setRollNo(String r)      { this.rollNo = r; }
    public void setSection(String s)     { this.section = s; }
    public void setAttendance(double a)  { this.attendance = a; }
    public void setSubjects(LinkedHashMap<String, Double> s) { this.subjects = s; }

    public LinkedHashMap<String, Double> getSubjects() { return subjects; }

    /** If subjects exist → average of subjects; else manual marks */
    public double getMarks() {
        if (!subjects.isEmpty())
            return subjects.values().stream().mapToDouble(d -> d).average().orElse(marks);
        return marks;
    }
    public void setMarks(double m) { this.marks = m; }

    // ── Derived fields ────────────────────────────────────────────────────────
    public String getGrade() {
        double m = getMarks();
        if (m >= 90) return "A+";
        if (m >= 80) return "A";
        if (m >= 70) return "B+";
        if (m >= 60) return "B";
        if (m >= 50) return "C";
        return "F";
    }

    public String getStatus()           { return getMarks() >= 50 ? "✔ Pass" : "✘ Fail"; }
    public String getAttendanceStatus() { return attendance >= 75  ? "✔ OK"   : "⚠ Low";  }

    @Override
    public String toString() {
        return String.format("Student{name=%s, roll=%s, sec=%s, marks=%.1f, att=%.1f}",
                name, rollNo, section, getMarks(), attendance);
    }
}