package StudentGradeTracker;


import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.*;

import java.io.IOException;
import java.util.List;

/**
 *  UserManagementDialog – ADMIN only
 *  Add / delete teachers; change passwords.
 */
public class Usermanagement {

    private final Stage       owner;
    private final List<User>  users;
    private ObservableList<User> oUsers;
    private TableView<User>   table;

    public Usermanagement(Stage owner, List<User> users) {
        this.owner = owner; this.users = users;
    }

    public void show() {
        oUsers = FXCollections.observableArrayList(users);

        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        // ── Table ─────────────────────────────────────────────────────────────
        table = new TableView<>(oUsers);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(220);
        table.setStyle("-fx-background-color:#0a0a1e;-fx-font-family:'Courier New';");

        TableColumn<User, String> unCol  = tc("Username",    "username",    Uihelper.CYAN);
        TableColumn<User, String> roleCol= tc("Role",        "role",        Uihelper.YELLOW);
        TableColumn<User, String> dnCol  = tc("Display Name","displayName", "#ffffff");
        table.getColumns().addAll(unCol, roleCol, dnCol);

        // ── Form ──────────────────────────────────────────────────────────────
        TextField   unField   = Uihelper.styledField("Username");
        TextField   pwField   = Uihelper.styledField("Password");
        TextField   dnField   = Uihelper.styledField("Display Name");
        ComboBox<User.Role> roleBox = new ComboBox<>();
        roleBox.getItems().addAll(User.Role.values());
        roleBox.setValue(User.Role.TEACHER);
        roleBox.setStyle("-fx-background-color:#0a0a1e;-fx-border-color:#1a1a66;" +
                "-fx-text-fill:white;-fx-font-family:'Courier New';");

        Label statusLbl = new Label(""); statusLbl.setFont(Font.font(Uihelper.FONT, 11));

        Button addBtn = Uihelper.actionBtn("➕ ADD",   "#003322","#00cc66", Uihelper.GREEN);
        Button delBtn = Uihelper.actionBtn("🗑 DELETE","#330011","#cc0033", Uihelper.RED);
        Button clsBtn = Uihelper.actionBtn("✕ CLOSE", "#1a0022","#440066", Uihelper.PURPLE);

        addBtn.setMaxWidth(Double.MAX_VALUE);
        delBtn.setMaxWidth(Double.MAX_VALUE);
        clsBtn.setMaxWidth(Double.MAX_VALUE);

        addBtn.setOnAction(e -> {
            String un = unField.getText().trim();
            String pw = pwField.getText().trim();
            String dn = dnField.getText().trim();
            if (un.isEmpty() || pw.isEmpty() || dn.isEmpty()) {
                status(statusLbl, "⚠  Fill all fields!",Uihelper.YELLOW); return;
            }
            boolean exists = users.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(un));
            if (exists) { status(statusLbl, "⚠  Username already exists!",Uihelper.RED); return; }
            User nu = new User(un, pw, roleBox.getValue(), dn);
            users.add(nu); oUsers.add(nu);
            saveUsers(statusLbl);
            unField.clear(); pwField.clear(); dnField.clear();
            status(statusLbl, "✔  User added: " + dn, Uihelper.GREEN);
        });

        delBtn.setOnAction(e -> {
            User sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { status(statusLbl, "⚠  Select a user first!", Uihelper.YELLOW); return; }
            if (sel.getRole() == User.Role.ADMIN && countAdmins() == 1) {
                status(statusLbl, "⚠  Cannot delete the only admin!",Uihelper.RED); return;
            }
            users.remove(sel); oUsers.remove(sel);
            saveUsers(statusLbl);
            status(statusLbl, "🗑  User deleted: " + sel.getDisplayName(), Uihelper.RED);
        });

        clsBtn.setOnAction(e -> dialog.close());

        // ── Layout ────────────────────────────────────────────────────────────
        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(8);
        form.add(lbl("Username:"), 0,0); form.add(unField,  1,0);
        form.add(lbl("Password:"), 0,1); form.add(pwField,  1,1);
        form.add(lbl("Display:"),  0,2); form.add(dnField,  1,2);
        form.add(lbl("Role:"),     0,3); form.add(roleBox,  1,3);
        ColumnConstraints c0 = new ColumnConstraints(90);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c0, c1);

        VBox content = new VBox(12,
                Uihelper.sectionLabel("👥  USER MANAGEMENT  (Admin Only)"),
                table,
                new Separator(),
                Uihelper.sectionLabel("➕  ADD NEW USER"), form,
                statusLbl,
                new HBox(10, addBtn, delBtn, clsBtn)
        );
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color:#0d0d2e;-fx-border-color:#2a2aff;" +
                "-fx-border-width:2;-fx-border-radius:12;-fx-background-radius:12;");

        dialog.setScene(new Scene(content, 480, 520));
        dialog.show();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void saveUsers(Label lbl) {
        try { Databasemanager.saveUsers(users); }
        catch (IOException e) { status(lbl, "⚠  Could not persist users!",Uihelper.RED); }
    }
    private long countAdmins() {
        return users.stream().filter(User::isAdmin).count();
    }
    private void status(Label l, String msg, String color) {
        l.setText(msg); l.setTextFill(Color.web(color));
    }
    private Label lbl(String t) {
        Label l = new Label(t);
        l.setFont(Font.font(Uihelper.FONT, 12)); l.setTextFill(Color.web(Uihelper.DIM));
        return l;
    }
    private <T> TableColumn<User, T> tc(String title, String prop, String color) {
        TableColumn<User, T> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setStyle("-fx-alignment:CENTER;-fx-text-fill:" + color + ";");
        return c;
    }
}