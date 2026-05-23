package StudentGradeTracker;



import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *  DatabaseManager
 *  Handles: Student serialization (.dat)
 *           User serialization  (.usr)
 */
public class Databasemanager {

    private static final String STUDENT_FILE = "students.dat";
    private static final String USER_FILE    = "users.usr";

    // ══════════════════════════════════════════════════════════════════════════
    //  STUDENTS
    // ══════════════════════════════════════════════════════════════════════════

    public static void saveStudents(List<Student> students) throws IOException {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(STUDENT_FILE))) {
            oos.writeObject(new ArrayList<>(students));
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Student> loadStudents() throws IOException, ClassNotFoundException {
        File f = new File(STUDENT_FILE);
        if (!f.exists()) return new ArrayList<>();
        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(f))) {
            return (ArrayList<Student>) ois.readObject();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  USERS
    // ══════════════════════════════════════════════════════════════════════════

    public static void saveUsers(List<User> users) throws IOException {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(USER_FILE))) {
            oos.writeObject(new ArrayList<>(users));
        }
    }

    @SuppressWarnings("unchecked")
    public static List<User> loadUsers() throws IOException, ClassNotFoundException {
        File f = new File(USER_FILE);
        if (!f.exists()) return new ArrayList<>();
        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(f))) {
            return (ArrayList<User>) ois.readObject();
        }
    }

    /**
     *  First-run: create default admin + teacher accounts and persist them.
     */
    public static List<User> createDefaultUsers() {
        List<User> defaults = new ArrayList<>();
        defaults.add(new User("admin",   "admin123",  User.Role.ADMIN,   "Administrator"));
        defaults.add(new User("teacher", "teach123",  User.Role.TEACHER, "Mr. Teacher"));
        try { saveUsers(defaults); } catch (IOException ignored) {}
        return defaults;
    }

    /** Load users; if file missing, seed defaults */
    public static List<User> loadOrCreateUsers() {
        try {
            List<User> users = loadUsers();
            return users.isEmpty() ? createDefaultUsers() : users;
        } catch (Exception e) {
            return createDefaultUsers();
        }
    }

    /** Authenticate; returns User on success, null on failure */
    public static User authenticate(List<User> users, String username, String password) {
        return users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username)
                        && u.getPassword().equals(password))
                .findFirst().orElse(null);
    }
}