package StudentGradeTracker;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.Duration;

import java.util.List;

public class Loginpage extends Application {

    private List<User>    users;
    private TextField     userField;
    private PasswordField passField;
    private Label         errLabel;
    private Stage         loginStage;

    @Override
    public void start(Stage primaryStage) {
        this.loginStage = primaryStage;
        this.users      = Databasemanager.loadOrCreateUsers();
        primaryStage.initStyle(StageStyle.UNDECORATED);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color:" + Uihelper.BG_DARK + ";");
        root.getChildren().addAll(buildAnimatedBg(), buildLoginCard());

        primaryStage.setScene(new Scene(root, 520, 680));
        primaryStage.centerOnScreen();
        primaryStage.setTitle("Login");
        primaryStage.show();
    }

    private Pane buildAnimatedBg() {
        Pane pane = new Pane(); pane.setPrefSize(520, 680);
        for (int i = 0; i < 45; i++) {
            Rectangle dot = new Rectangle(2, 2, Color.web("#1a1a66"));
            dot.setX(Math.random() * 520); dot.setY(Math.random() * 680);
            FadeTransition ft = new FadeTransition(Duration.millis(1500 + Math.random() * 2500), dot);
            ft.setFromValue(0.1); ft.setToValue(0.6);
            ft.setAutoReverse(true); ft.setCycleCount(Animation.INDEFINITE);
            ft.setDelay(Duration.millis(Math.random() * 2000)); ft.play();
            pane.getChildren().add(dot);
        }
        return pane;
    }

    private VBox buildLoginCard() {
        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36));
        card.setMaxWidth(390);
        card.setStyle(
                "-fx-background-color:#0d0d2e;" +
                        "-fx-border-color:#2a2aff;" +
                        "-fx-border-width:2;" +
                        "-fx-border-radius:16;-fx-background-radius:16;");
        card.setEffect(new DropShadow(30, Color.web("#2a2aff")));

        Button closeBtn = Uihelper.actionBtn("✕", "#1a0000","#440000", Uihelper.RED);
        closeBtn.setOnAction(e -> loginStage.close());
        HBox topBar = new HBox(closeBtn); topBar.setAlignment(Pos.TOP_RIGHT);

        Label icon  = new Label("🎓"); icon.setFont(Font.font(48));
        Label title = new Label("GRADE TRACKER");
        title.setFont(Font.font(Uihelper.FONT, FontWeight.EXTRA_BOLD, 22));
        title.setTextFill(Color.web(Uihelper.CYAN));
        title.setStyle("-fx-effect:dropshadow(gaussian," + Uihelper.CYAN + ",12,0.4,0,0);");
        Label sub = new Label("Teacher / Admin Portal");
        sub.setFont(Font.font(Uihelper.FONT, 11));
        sub.setTextFill(Color.web(Uihelper.DIM));

        userField = Uihelper.styledField("Enter username");
        userField.setMaxWidth(Double.MAX_VALUE);

        passField = new PasswordField();
        passField.setPromptText("Enter password");
        passField.setMaxWidth(Double.MAX_VALUE);
        passField.setStyle(userField.getStyle());
        passField.focusedProperty().addListener((o, ov, nv) ->
                passField.setStyle(
                        "-fx-background-color:#0a0a1e;" +
                                "-fx-border-color:" + (nv ? Uihelper.CYAN : "#1a1a66") + ";" +
                                "-fx-border-width:1.5;-fx-border-radius:6;-fx-background-radius:6;" +
                                "-fx-text-fill:white;-fx-prompt-text-fill:#333366;" +
                                "-fx-padding:8 10 8 10;" +
                                "-fx-font-family:'" + Uihelper.FONT + "';-fx-font-size:12;"));

        errLabel = new Label("");
        errLabel.setFont(Font.font(Uihelper.FONT, FontWeight.BOLD, 11));
        errLabel.setTextFill(Color.web(Uihelper.RED));
        errLabel.setWrapText(true);

        // ── LOGIN button ──────────────────────────────────────────────────────
        Button loginBtn = Uihelper.actionBtn("  🔓  LOGIN  ", "#001133","#0055cc", Uihelper.CYAN);
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setFont(Font.font(Uihelper.FONT, FontWeight.BOLD, 14));
        loginBtn.setOnAction(e -> handleLogin());
        passField.setOnAction(e -> handleLogin());

        // ── OR divider ────────────────────────────────────────────────────────
        Label orLabel = new Label("─────────────  OR  ─────────────");
        orLabel.setFont(Font.font(Uihelper.FONT, 10));
        orLabel.setTextFill(Color.web("#222255"));
        orLabel.setAlignment(Pos.CENTER);

        // ── REGISTER button ───────────────────────────────────────────────────
        Button registerBtn = Uihelper.actionBtn("  ✏  REGISTER NEW USER  ", "#0d0022","#550099", Uihelper.PURPLE);
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setFont(Font.font(Uihelper.FONT, FontWeight.BOLD, 13));
        registerBtn.setOnAction(e -> showRegisterDialog());

        Label hint = new Label("Default  →  admin / admin123   |   teacher / teach123");
        hint.setFont(Font.font(Uihelper.FONT, 10));
        hint.setTextFill(Color.web("#222255"));

        final double[] off = {0, 0};
        card.setOnMousePressed(e  -> { off[0] = e.getSceneX(); off[1] = e.getSceneY(); });
        card.setOnMouseDragged(e  -> {
            loginStage.setX(e.getScreenX() - off[0]);
            loginStage.setY(e.getScreenY() - off[1]);
        });

        card.getChildren().addAll(
                topBar, icon, title, sub,
                new Separator(),
                fieldLabel("👤  Username"), userField,
                fieldLabel("🔒  Password"), passField,
                errLabel,
                loginBtn,
                orLabel,
                registerBtn,
                new Separator(),
                hint
        );
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  REGISTER DIALOG
    // ══════════════════════════════════════════════════════════════════════════
    private void showRegisterDialog() {
        Stage dialog = new Stage();
        dialog.initOwner(loginStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        TextField     regUser = Uihelper.styledField("Choose a username  (min 4 chars)");
        TextField     regName = Uihelper.styledField("Your full name");
        PasswordField regPass = new PasswordField();
        regPass.setPromptText("Choose a password  (min 6 chars)");
        regPass.setStyle(regUser.getStyle());
        PasswordField regConf = new PasswordField();
        regConf.setPromptText("Confirm password");
        regConf.setStyle(regUser.getStyle());

        // Role radio buttons
        ToggleGroup   roleGroup = new ToggleGroup();
        RadioButton   teacherRb = styledRadio("👨‍🏫  TEACHER", roleGroup);
        RadioButton   adminRb   = styledRadio("🛡  ADMIN",    roleGroup);
        teacherRb.setSelected(true);
        HBox roleBox = new HBox(20, teacherRb, adminRb);
        roleBox.setAlignment(Pos.CENTER_LEFT);

        // Admin secret key (hidden unless ADMIN selected)
        Label     adminKeyLabel = fieldLabel("🗝  Admin Secret Key");
        TextField adminKeyField = Uihelper.styledField("Enter admin secret key");
        adminKeyLabel.setVisible(false); adminKeyLabel.setManaged(false);
        adminKeyField.setVisible(false); adminKeyField.setManaged(false);

        adminRb.selectedProperty().addListener((o, ov, nv) -> {
            adminKeyLabel.setVisible(nv); adminKeyLabel.setManaged(nv);
            adminKeyField.setVisible(nv); adminKeyField.setManaged(nv);
        });

        Label statusLbl = new Label("");
        statusLbl.setFont(Font.font(Uihelper.FONT, FontWeight.BOLD, 11));
        statusLbl.setWrapText(true);

        Button regBtn  = Uihelper.actionBtn("✔  CREATE ACCOUNT", "#003322","#00cc66", Uihelper.GREEN);
        Button backBtn = Uihelper.actionBtn("←  BACK TO LOGIN",  "#001133","#003366", Uihelper.CYAN);
        regBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setMaxWidth(Double.MAX_VALUE);

        regBtn.setOnAction(e -> {
            String un   = regUser.getText().trim();
            String dn   = regName.getText().trim();
            String pw   = regPass.getText();
            String conf = regConf.getText();
            String key  = adminKeyField.getText().trim();

            if (un.isEmpty() || dn.isEmpty() || pw.isEmpty() || conf.isEmpty()) {
                setStatus(statusLbl, "⚠  Please fill all fields!", Uihelper.YELLOW); return;
            }
            if (un.length() < 4) {
                setStatus(statusLbl, "⚠  Username must be at least 4 characters!", Uihelper.YELLOW); return;
            }
            if (pw.length() < 6) {
                setStatus(statusLbl, "⚠  Password must be at least 6 characters!", Uihelper.YELLOW); return;
            }
            if (!pw.equals(conf)) {
                setStatus(statusLbl, "✘  Passwords do not match!", Uihelper.RED); return;
            }
            if (users.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(un))) {
                setStatus(statusLbl, "✘  Username already taken!", Uihelper.RED); return;
            }
            User.Role role = adminRb.isSelected() ? User.Role.ADMIN : User.Role.TEACHER;
            if (role == User.Role.ADMIN && !key.equals("admin@123")) {
                setStatus(statusLbl, "✘  Wrong admin secret key!", Uihelper.RED); return;
            }

            User newUser = new User(un, pw, role, dn);
            users.add(newUser);
            try {
                Databasemanager.saveUsers(users);
                setStatus(statusLbl, "✔  Account created successfully!", Uihelper.GREEN);
                new Timeline(new KeyFrame(Duration.millis(1000), ev -> {
                    dialog.close();
                    userField.setText(un); passField.clear();
                    errLabel.setText("✔  Registered! Now login.");
                    errLabel.setTextFill(Color.web(Uihelper.GREEN));
                })).play();
            } catch (Exception ex) {
                setStatus(statusLbl, "⚠  Could not save. Try again!", Uihelper.RED);
            }
        });

        backBtn.setOnAction(e -> dialog.close());

        // ── Card layout ───────────────────────────────────────────────────────
        Label regTitle = new Label("✏  REGISTER NEW USER");
        regTitle.setFont(Font.font(Uihelper.FONT, FontWeight.EXTRA_BOLD, 18));
        regTitle.setTextFill(Color.web(Uihelper.PURPLE));
        regTitle.setStyle("-fx-effect:dropshadow(gaussian," + Uihelper.PURPLE + ",10,0.4,0,0);");

        Label adminHint = new Label("Admin secret key:  admin@123");
        adminHint.setFont(Font.font(Uihelper.FONT, 10));
        adminHint.setTextFill(Color.web("#333355"));

        VBox content = new VBox(11,
                regTitle, new Separator(),
                fieldLabel("👤  Username"),  regUser,
                fieldLabel("📛  Full Name"), regName,
                fieldLabel("🔒  Password"),  regPass,
                fieldLabel("🔒  Confirm"),   regConf,
                fieldLabel("🔑  Role"),      roleBox,
                adminKeyLabel, adminKeyField,
                statusLbl,
                regBtn, backBtn,
                new Separator(), adminHint
        );
        content.setPadding(new Insets(28));
        content.setStyle(
                "-fx-background-color:#0d0d2e;" +
                        "-fx-border-color:" + Uihelper.PURPLE + ";" +
                        "-fx-border-width:2;-fx-border-radius:16;-fx-background-radius:16;");
        content.setEffect(new DropShadow(25, Color.web(Uihelper.PURPLE)));

        StackPane root = new StackPane(content);
        root.setStyle("-fx-background-color:" + Uihelper.BG_DARK + ";");
        root.setPadding(new Insets(20));

        dialog.setScene(new Scene(root, 440, 650));
        dialog.centerOnScreen();
        dialog.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HANDLERS
    // ══════════════════════════════════════════════════════════════════════════
    private void handleLogin() {
        String u = userField.getText().trim(), p = passField.getText();
        if (u.isEmpty() || p.isEmpty()) { shake("⚠  Please enter username and password."); return; }
        User loggedIn = Databasemanager.authenticate(users, u, p);
        if (loggedIn == null) { shake("✘  Invalid username or password!"); passField.clear(); return; }

        errLabel.setText("✔  Welcome, " + loggedIn.getDisplayName() + "!");
        errLabel.setTextFill(Color.web(Uihelper.GREEN));

        new Timeline(new KeyFrame(Duration.millis(600), ev -> {
            try {
                Studentgradetracker app = new Studentgradetracker(loggedIn, users);
                app.start(new Stage());
                loginStage.close();
            } catch (Exception ex) { ex.printStackTrace(); }
        })).play();
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font(Uihelper.FONT, FontWeight.BOLD, 12));
        l.setTextFill(Color.web(Uihelper.DIM));
        return l;
    }

    private RadioButton styledRadio(String text, ToggleGroup group) {
        RadioButton rb = new RadioButton(text);
        rb.setToggleGroup(group);
        rb.setStyle("-fx-text-fill:white;-fx-font-family:'" + Uihelper.FONT + "';-fx-font-size:12;");
        return rb;
    }

    private void setStatus(Label lbl, String msg, String color) {
        lbl.setText(msg); lbl.setTextFill(Color.web(color));
    }

    private void shake(String msg) {
        errLabel.setText(msg); errLabel.setTextFill(Color.web(Uihelper.RED));
        TranslateTransition tt = new TranslateTransition(Duration.millis(60), errLabel);
        tt.setFromX(0); tt.setByX(8); tt.setCycleCount(6); tt.setAutoReverse(true); tt.play();
    }

    public static void main(String[] args) { launch(args); }
}