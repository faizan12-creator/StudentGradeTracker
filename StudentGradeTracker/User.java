package StudentGradeTracker;

import java.io.Serializable;

/**
 *  User – login credentials
 *  Role : ADMIN  (full access)
 *         TEACHER (no user-management)
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Role { ADMIN, TEACHER }

    private String username;
    private String password;        // plain-text (demo project)
    private Role   role;
    private String displayName;

    public User(String username, String password, Role role, String displayName) {
        this.username    = username;
        this.password    = password;
        this.role        = role;
        this.displayName = displayName;
    }

    public String getUsername()    { return username; }
    public String getPassword()    { return password; }
    public Role   getRole()        { return role; }
    public String getDisplayName() { return displayName; }

    public void setPassword(String p)    { this.password = p; }
    public void setRole(Role r)          { this.role = r; }
    public void setDisplayName(String d) { this.displayName = d; }

    public boolean isAdmin() { return role == Role.ADMIN; }

    @Override public String toString() {
        return "[" + role + "] " + displayName + " (" + username + ")";
    }
}