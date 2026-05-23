package StudentGradeTracker;


import java.util.*;

/**
 *  StatsCalculator – pure static helpers for grade stats
 */
public class StatsCalculator {

    public record Stats(int total, double avg, double highest, double lowest,
                        int pass, int fail, List<Student> top3) {}

    public static Stats compute(List<Student> students) {
        if (students == null || students.isEmpty())
            return new Stats(0, 0, 0, 0, 0, 0, Collections.emptyList());

        double sum = 0, high = Double.MIN_VALUE, low = Double.MAX_VALUE;
        int pass = 0, fail = 0;

        for (Student s : students) {
            double m = s.getMarks();
            sum += m;
            if (m > high) high = m;
            if (m < low)  low  = m;
            if (m >= 50) pass++; else fail++;
        }

        List<Student> sorted = new ArrayList<>(students);
        sorted.sort(Comparator.comparingDouble(Student::getMarks).reversed());
        List<Student> top3 = sorted.subList(0, Math.min(3, sorted.size()));

        return new Stats(students.size(), sum / students.size(),
                high, low, pass, fail, top3);
    }

    /** Count students per grade label */
    public static Map<String, Integer> gradeDistribution(List<Student> students) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (String g : new String[]{"A+","A","B+","B","C","F"}) map.put(g, 0);
        for (Student s : students) map.merge(s.getGrade(), 1, Integer::sum);
        return map;
    }
}