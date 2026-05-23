package StudentGradeTracker;




import java.io.*;
        import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 *  ExportManager
 *  exportCSV()     – all students → students_export.csv
 *  exportReport()  – single student → <Name>_Report.txt
 */
public class Exportmanager {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ── CSV ──────────────────────────────────────────────────────────────────
    public static String exportCSV(List<Student> students) throws IOException {
        String fn = "students_export.csv";
        try (PrintWriter pw = new PrintWriter(new FileWriter(fn))) {
            pw.println("Roll No,Name,Section,Overall Marks,Grade,Status,Attendance %,Subjects");
            for (Student s : students) {
                StringBuilder subs = new StringBuilder();
                s.getSubjects().forEach((k, v) ->
                        subs.append(k).append(":").append(String.format("%.1f", v)).append(";"));
                pw.printf("\"%s\",\"%s\",\"%s\",%.1f,%s,%s,%.1f,\"%s\"%n",
                        s.getRollNo(), s.getName(), s.getSection(),
                        s.getMarks(), s.getGrade(),
                        s.getStatus().replace("✔ ","").replace("✘ ",""),
                        s.getAttendance(), subs);
            }
        }
        return fn;
    }

    // ── Report Card TXT ──────────────────────────────────────────────────────
    public static String buildReportText(Student s) {
        String line = "╠══════════════════════════════════════════╣\n";
        String top  = "╔══════════════════════════════════════════╗\n";
        String bot  = "╚══════════════════════════════════════════╝\n";

        StringBuilder sb = new StringBuilder();
        sb.append(top);
        sb.append("║         STUDENT GRADE REPORT CARD        ║\n");
        sb.append("║   Generated: ").append(String.format("%-29s", LocalDateTime.now().format(FMT))).append("║\n");
        sb.append(line);
        sb.append(String.format("║  Roll No  :  %-29s║\n", s.getRollNo()));
        sb.append(String.format("║  Name     :  %-29s║\n", s.getName()));
        sb.append(String.format("║  Section  :  %-29s║\n", s.getSection()));
        sb.append(line);
        sb.append(String.format("║  Marks    :  %-29s║\n", String.format("%.1f / 100", s.getMarks())));
        sb.append(String.format("║  Grade    :  %-29s║\n", s.getGrade()));
        sb.append(String.format("║  Status   :  %-29s║\n", s.getStatus()));
        sb.append(String.format("║  Attend.  :  %-29s║\n",
                String.format("%.1f%%  (%s)", s.getAttendance(), s.getAttendanceStatus())));

        if (!s.getSubjects().isEmpty()) {
            sb.append(line);
            sb.append("║  SUBJECT BREAKDOWN                       ║\n");
            sb.append(line);
            s.getSubjects().forEach((k, v) ->
                    sb.append(String.format("║  %-13s :   %-25s║\n",
                            k, String.format("%.1f / 100", v))));
        }
        sb.append(bot);
        return sb.toString();
    }

    public static String saveReportTXT(Student s) throws IOException {
        String fn = s.getName().replaceAll("\\s+", "_") + "_Report.txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(fn))) {
            pw.print(buildReportText(s));
        }
        return fn;
    }
}