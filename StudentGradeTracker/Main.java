package StudentGradeTracker;

import javafx.application.Application;

/**
 *  Main – application entry point
 *  Launches LoginPage first; on success opens StudentGradeTracker.
 *
 *  Default credentials (first run, saved to users.usr):
 *    admin   / admin123    → ADMIN   (full access + user management)
 *    teacher / teach123    → TEACHER (student management only)
 */
public class Main {
    public static void main(String[] args) {
        Application.launch(Loginpage.class, args);
    }
}