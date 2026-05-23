package StudentGradeTracker;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.BarChart;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 *  StudentGradeTracker – main application window
 *  Receives logged-in User and full user list from LoginPage.
 */
public class Studentgradetracker extends Application {

    // ── State ─────────────────────────────────────────────────────────────────
    private final User        currentUser;
    private final List<User>  allUsers;
    private final ObservableList<Student> studentList = FXCollections.observableArrayList();
    private FilteredList<Student> filteredList;
    private SortedList<Student>   sortedList;

    private Stage             stage;
    private TableView<Student> table;
    private TextField         nameField, rollField, sectionField, marksField, attField, searchField;
    private Label             totalVal, avgVal, highVal, lowVal, passVal, failVal, msgLabel;
    private Button            addBtn;
    private Student           editingStudent = null;
    private PieChart          pieChart;
    private BarChart<String, Number> barChart;
    private VBox              leaderboardBox;

    // ── Dialogs ───────────────────────────────────────────────────────────────
    private Subjectsdialog    subjectsDialog;
    private Reportcard  reportCardDialog;
    private Usermanagement userMgmtDialog;

    // ── Constructors ──────────────────────────────────────────────────────────
    public Studentgradetracker() {
        this.currentUser = new User("admin","",User.Role.ADMIN,"Admin");
        this.allUsers    = List.of(currentUser);
    }
    public Studentgradetracker(User user, List<User> users) {
        this.currentUser = user;
        this.allUsers    = users;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  START
    // ══════════════════════════════════════════════════════════════════════════
    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;

        // load data
        try { studentList.addAll(Databasemanager.loadStudents()); }
        catch (Exception ignored) {}

        filteredList = new FilteredList<>(studentList, p -> true);
        sortedList   = new SortedList<>(filteredList);

        // init dialogs
        subjectsDialog   = new Subjectsdialog(stage);
        reportCardDialog = new Reportcard(stage);
        userMgmtDialog   = new Usermanagement(stage, allUsers);

        subjectsDialog.setOnSave(() -> { table.refresh(); updateStats(); Platform.runLater(this::updateCharts); });
        reportCardDialog.setOnMsg(() -> showMsg(reportCardDialog.getLastMsg(), reportCardDialog.getLastMsgColor()));

        // build UI
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + Uihelper.BG_DARK + ";");
        root.setTop(buildHeader());
        root.setLeft(buildSidebar());
        root.setCenter(buildCenterTabs());

        updateStats();
        Platform.runLater(this::updateCharts);

        Scene scene = new Scene(root, 1220, 760);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setMaximized(true);
        primaryStage.setTitle("Student Grade Tracker");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HEADER
    // ══════════════════════════════════════════════════════════════════════════
    private HBox buildHeader() {
        HBox header = new HBox(12);
        header.setPadding(new Insets(14, 28, 14, 28));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
                "-fx-background-color:linear-gradient(to right,#0d0d2e 0%,#111144 50%,#0d0d2e 100%);" +
                        "-fx-border-color:#2a2aff;-fx-border-width:0 0 2 0;");

        Label icon  = new Label("🎓"); icon.setFont(Font.font(26));

        VBox titles = new VBox(2);
        Label title = new Label("STUDENT GRADE TRACKER");
        title.setFont(Font.font(Uihelper.FONT, FontWeight.EXTRA_BOLD, 21));
        title.setTextFill(Color.web(Uihelper.CYAN));
        title.setStyle("-fx-effect:dropshadow(gaussian," + Uihelper.CYAN + ",8,0.3,0,0);");
        Label sub = new Label("CodeAlpha Internship  ·  Java Built-in Database  ·  Professional Edition");
        sub.setFont(Font.font(Uihelper.FONT, 10)); sub.setTextFill(Color.web(Uihelper.DIM));
        titles.getChildren().addAll(title, sub);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        msgLabel = new Label("Welcome, " + currentUser.getDisplayName() + "  [" + currentUser.getRole() + "]");
        msgLabel.setFont(Font.font(Uihelper.FONT, FontWeight.BOLD, 11));
        msgLabel.setTextFill(Color.web(Uihelper.GREEN));

        // ── Admin-only user management button ─────────────────────────────────
        Button userBtn  = Uihelper.actionBtn("👥 USERS", "#1a0033","#550099", Uihelper.PURPLE);
        userBtn.setVisible(currentUser.isAdmin());
        userBtn.setOnAction(e -> userMgmtDialog.show());

        Button logoutBtn = Uihelper.actionBtn("⏻ LOGOUT","#110022","#440066", Uihelper.PURPLE);
        logoutBtn.setOnAction(e -> {
            stage.close();
            try {
                Loginpage lp = new Loginpage();
                lp.start(new Stage());
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        Button closeBtn = Uihelper.actionBtn("✕","#1a0000","#440000", Uihelper.RED);
        closeBtn.setOnAction(e -> stage.close());

        header.getChildren().addAll(icon, titles, spacer, msgLabel, userBtn, logoutBtn, closeBtn);
        return header;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ══════════════════════════════════════════════════════════════════════════
    private ScrollPane buildSidebar() {
        VBox sidebar = new VBox(11);
        sidebar.setPadding(new Insets(18, 16, 18, 16));
        sidebar.setPrefWidth(316);
        sidebar.setStyle(
                "-fx-background-color:linear-gradient(to bottom,#0d0d2e,#090920);" +
                        "-fx-border-color:#1a1a66;-fx-border-width:0 2 0 0;");

        // ── Stats cards ───────────────────────────────────────────────────────
        totalVal = new Label("0"); avgVal  = new Label("—");
        highVal  = new Label("—"); lowVal  = new Label("—");
        passVal  = new Label("0"); failVal = new Label("0");

        HBox r1 = hbox(Uihelper.statCard("TOTAL",   totalVal, Uihelper.CYAN),
                Uihelper.statCard("AVERAGE", avgVal,   Uihelper.YELLOW));
        HBox r2 = hbox(Uihelper.statCard("HIGHEST", highVal, Uihelper.GREEN),
                Uihelper.statCard("LOWEST",  lowVal,   Uihelper.RED));
        HBox r3 = hbox(Uihelper.statCard("✔ PASS",  passVal, Uihelper.GREEN),
                Uihelper.statCard("✘ FAIL",  failVal,  Uihelper.RED));

        // ── Form ──────────────────────────────────────────────────────────────
        nameField    = Uihelper.styledField("Student Name");
        rollField    = Uihelper.styledField("Roll No (e.g. R-001)");
        sectionField = Uihelper.styledField("Section (e.g. A)");
        marksField   = Uihelper.styledField("Overall Marks  (0–100)");
        attField     = Uihelper.styledField("Attendance %  (0–100)");

        addBtn               = Uihelper.actionBtn("ADD",           "#003322","#00cc66", Uihelper.GREEN);
        Button editBtn       = Uihelper.actionBtn("EDIT",          "#002233","#005588", Uihelper.CYAN);
        Button cancelBtn     = Uihelper.actionBtn("CANCEL",        "#221100","#554400", Uihelper.YELLOW);
        Button delBtn        = Uihelper.actionBtn("DELETE",        "#330011","#cc0033", Uihelper.RED);

        Button subjBtn       = Uihelper.actionBtn("📋 SUBJECTS",   "#001122","#003366", Uihelper.CYAN);
        Button reportBtn     = Uihelper.actionBtn("🖨  REPORT CARD","#110022","#440066",Uihelper.PURPLE);
        Button exportBtn     = Uihelper.actionBtn("📤 EXPORT CSV", "#001122","#005522", Uihelper.GREEN);

        Button saveBtn       = Uihelper.actionBtn("💾 SAVE DB",    "#001133","#0055cc",Uihelper.CYAN);
        Button loadBtn       = Uihelper.actionBtn("📂 LOAD DB",    "#221100","#aa5500", Uihelper.YELLOW);
        Button clrBtn        = Uihelper.actionBtn("🗑 CLEAR ALL",  "#1a1a00","#777700", "#dddd00");

        for (Button b : new Button[]{addBtn,editBtn,cancelBtn,delBtn,
                subjBtn,reportBtn,exportBtn,saveBtn,loadBtn,clrBtn})
            b.setMaxWidth(Double.MAX_VALUE);

        addBtn.setOnAction(e   -> addStudent());
        editBtn.setOnAction(e  -> startEdit());
        cancelBtn.setOnAction(e-> cancelEdit());
        delBtn.setOnAction(e   -> deleteStudent());
        subjBtn.setOnAction(e  -> subjectsDialog.show(table.getSelectionModel().getSelectedItem()));
        reportBtn.setOnAction(e-> reportCardDialog.show(table.getSelectionModel().getSelectedItem()));
        exportBtn.setOnAction(e-> exportCSV());
        saveBtn.setOnAction(e  -> saveDB());
        loadBtn.setOnAction(e  -> loadDB());
        clrBtn.setOnAction(e   -> clearAll());

        sidebar.getChildren().addAll(
                Uihelper.sectionLabel("📊  STATISTICS"), r1, r2, r3,
                new Separator(),
                Uihelper.sectionLabel("➕  ADD / EDIT STUDENT"),
                nameField, rollField, sectionField, marksField, attField,
                addBtn, editBtn, cancelBtn, delBtn,
                new Separator(),
                Uihelper.sectionLabel("⚙  ACTIONS"),
                subjBtn, reportBtn, exportBtn,
                new Separator(),
                Uihelper.sectionLabel("💾  DATABASE"), saveBtn, loadBtn, clrBtn,
                new Separator(),
                Uihelper.sectionLabel("📋  GRADE SCALE"), gradeScaleBox()
        );

        ScrollPane sp = new ScrollPane(sidebar);
        sp.setFitToWidth(true); sp.setPrefWidth(316);
        sp.setStyle("-fx-background-color:transparent;-fx-background:#070714;");
        return sp;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CENTER – TabPane
    // ══════════════════════════════════════════════════════════════════════════
    private TabPane buildCenterTabs() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-background-color:transparent;-fx-tab-min-height:36;");

        Tab t1 = new Tab("  📋  STUDENTS RECORD  ");
        t1.setContent(buildTablePanel());
        Tab t2 = new Tab("  📊  CHARTS & ANALYTICS  ");
        t2.setContent(buildChartsPanel());

        String ts = "-fx-font-family:'Courier New';-fx-font-size:12;-fx-font-weight:bold;";
        t1.setStyle(ts); t2.setStyle(ts);
        tabs.getTabs().addAll(t1, t2);
        return tabs;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TABLE PANEL
    // ══════════════════════════════════════════════════════════════════════════
    private VBox buildTablePanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color:transparent;");

        // ── Search + sort bar ─────────────────────────────────────────────────
        searchField = Uihelper.styledField("🔍  Search by name or roll no...");
        searchField.textProperty().addListener((o, ov, nv) -> filteredList.setPredicate(s ->
                nv == null || nv.isEmpty() ||
                        s.getName().toLowerCase().contains(nv.toLowerCase()) ||
                        s.getRollNo().toLowerCase().contains(nv.toLowerCase())));

        Button sortNameBtn  = Uihelper.actionBtn("⬆ Name",  "#001133","#0055cc", Uihelper.CYAN);
        Button sortMarkBtn  = Uihelper.actionBtn("⬆ Marks", "#003322","#00cc66", Uihelper.GREEN);
        Button sortAttBtn   = Uihelper.actionBtn("⬆ Attend","#221100","#aa5500", Uihelper.YELLOW);
        Button resetSortBtn = Uihelper.actionBtn("⟳ Reset", "#1a1a00","#555500","#dddd00");

        sortNameBtn.setOnAction(e  -> sortedList.setComparator(Comparator.comparing(Student::getName)));
        sortMarkBtn.setOnAction(e  -> sortedList.setComparator(Comparator.comparingDouble(Student::getMarks).reversed()));
        sortAttBtn.setOnAction(e   -> sortedList.setComparator(Comparator.comparingDouble(Student::getAttendance).reversed()));
        resetSortBtn.setOnAction(e -> sortedList.setComparator(null));

        HBox sortBar = new HBox(8,Uihelper.sectionLabel("Sort:"), sortNameBtn, sortMarkBtn, sortAttBtn, resetSortBtn);
        sortBar.setAlignment(Pos.CENTER_LEFT);
        HBox topBar  = new HBox(10, searchField, sortBar);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        // ── Table ─────────────────────────────────────────────────────────────
        table = new TableView<>(sortedList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setStyle(
                "-fx-background-color:#0a0a1e;-fx-control-inner-background:#0a0a1e;" +
                        "-fx-control-inner-background-alt:#0d0d28;-fx-border-color:#1a1a66;" +
                        "-fx-border-radius:10;-fx-background-radius:10;" +
                        "-fx-table-cell-border-color:#111144;" +
                        "-fx-font-family:'Courier New';-fx-font-size:13px;");
        table.setFixedCellSize(42);

        // columns
        TableColumn<Student,String> numCol  = plainCol("#",       null,       "#555588", 45);
        TableColumn<Student,String> rollCol = plainCol("ROLL",    "rollNo",  Uihelper.DIM,  80);
        TableColumn<Student,String> nameCol = plainCol("👤 NAME", "name", Uihelper.CYAN,  0);
        TableColumn<Student,String> secCol  = plainCol("SEC",     "section",  "#888888", 55);
        TableColumn<Student,Double> mrkCol  = dblCol("📝 MARKS",  "marks",    "#aaaaff",  0);
        TableColumn<Student,String> grdCol  = plainCol("🏆 GRADE","grade",    "#ffffff",  75);
        TableColumn<Student,String> stCol   = plainCol("STATUS",  "status",   "#ffffff",  95);
        TableColumn<Student,Double> attCol  = dblCol("ATTEND %",  "attendance","#ffaa00",  0);

        // row number
        numCol.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String i, boolean e) {
                super.updateItem(i, e); setText(e ? null : String.valueOf(getIndex()+1));
                setStyle("-fx-text-fill:#555588;-fx-alignment:CENTER;");
            }
        });
        // grade color
        grdCol.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String g, boolean e) {
                super.updateItem(g, e);
                if (e||g==null){setText(null);setStyle("");return;}
                String c = Uihelper.gradeColor(g);
                setText(g); setStyle("-fx-text-fill:"+c+";-fx-font-weight:bold;-fx-alignment:CENTER;" +
                        "-fx-effect:dropshadow(gaussian,"+c+",6,0.5,0,0);");
            }
        });
        // status color
        stCol.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean e) {
                super.updateItem(s, e);
                if (e||s==null){setText(null);setStyle("");return;}
                String c = s.contains("Pass") ? Uihelper.GREEN : Uihelper.RED;
                setText(s); setStyle("-fx-text-fill:"+c+";-fx-font-weight:bold;-fx-alignment:CENTER;");
            }
        });
        // attendance color
        attCol.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean e) {
                super.updateItem(v, e);
                if (e||v==null){setText(null);setStyle("");return;}
                String c = v >= 75 ? Uihelper.GREEN :Uihelper.RED;
                setText(String.format("%.1f%%", v));
                setStyle("-fx-text-fill:"+c+";-fx-alignment:CENTER;");
            }
        });

        table.getColumns().addAll(numCol,rollCol,nameCol,secCol,mrkCol,grdCol,stCol,attCol);

        Label ph = new Label("No students yet.\nAdd a student from the left panel.");
        ph.setTextFill(Color.web("#333366")); ph.setFont(Font.font(Uihelper.FONT, 14));
        ph.setAlignment(Pos.CENTER); table.setPlaceholder(ph);

        panel.getChildren().addAll(topBar, table);
        return panel;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CHARTS PANEL
    // ══════════════════════════════════════════════════════════════════════════
    private ScrollPane buildChartsPanel() {
        pieChart = new PieChart();
        pieChart.setTitle("Grade Distribution");
        pieChart.setPrefSize(420, 320);
        pieChart.setStyle("-fx-background-color:#0a0a1e;");

        CategoryAxis xAxis = new CategoryAxis(); xAxis.setLabel("Grade");
        NumberAxis   yAxis = new NumberAxis();   yAxis.setLabel("Students");
        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Students per Grade");
        barChart.setLegendVisible(false);
        barChart.setPrefSize(420, 320);
        barChart.setStyle("-fx-background-color:#0a0a1e;");

        HBox chartsRow = new HBox(20, pieChart, barChart);
        chartsRow.setAlignment(Pos.CENTER); chartsRow.setPadding(new Insets(10));

        leaderboardBox = new VBox(10);
        leaderboardBox.setPadding(new Insets(16));
        leaderboardBox.setStyle(
                "-fx-background-color:#0a0a1e;-fx-border-color:#1a1a66;" +
                        "-fx-border-radius:10;-fx-background-radius:10;");

        VBox panel = new VBox(16);
        panel.setPadding(new Insets(22));
        panel.setStyle("-fx-background-color:" + Uihelper.BG_DARK + ";");
        panel.getChildren().addAll(
                Uihelper.sectionLabel("📊  GRADE DISTRIBUTION"),
                chartsRow,
                Uihelper.sectionLabel("🏅  TOP 3 LEADERBOARD"),
                leaderboardBox
        );

        ScrollPane sp = new ScrollPane(panel);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color:" + Uihelper.BG_DARK + ";-fx-border-color:transparent;");
        return sp;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  STUDENT CRUD
    // ══════════════════════════════════════════════════════════════════════════
    private void addStudent() {
        String name = nameField.getText().trim();
        String roll = rollField.getText().trim();
        String sec  = sectionField.getText().trim();
        String mStr = marksField.getText().trim();
        String aStr = attField.getText().trim();

        if (name.isEmpty() || roll.isEmpty() || mStr.isEmpty()) {
            showMsg("⚠  Name, Roll No, and Marks are required!",Uihelper.YELLOW); return;
        }
        try {
            double marks = Double.parseDouble(mStr);
            double att   = aStr.isEmpty() ? 100.0 : Double.parseDouble(aStr);
            if (marks < 0 || marks > 100 || att < 0 || att > 100) {
                showMsg("⚠  Marks/Attendance must be 0–100!", Uihelper.YELLOW); return;
            }
            if (editingStudent != null) {
                // UPDATE existing
                int idx = studentList.indexOf(editingStudent);
                if (idx >= 0) {
                    Student updated = new Student(name, roll, sec.isEmpty()?"—":sec, marks);
                    updated.setAttendance(att);
                    updated.setSubjects(editingStudent.getSubjects());
                    studentList.set(idx, updated);
                    showMsg("✔  " + name + " updated!",Uihelper.CYAN);
                }
                editingStudent = null; addBtn.setText("ADD");
            } else {
                Student s = new Student(name, roll, sec.isEmpty()?"—":sec, marks);
                s.setAttendance(att);
                studentList.add(s);
                showMsg("✔  " + name + " added!",Uihelper.GREEN);
            }
            clearForm(); updateStats(); Platform.runLater(this::updateCharts);
        } catch (NumberFormatException ex) { showMsg("⚠  Invalid numeric value!", Uihelper.RED); }
    }

    private void startEdit() {
        Student sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { showMsg("⚠  Select a student first!", Uihelper.YELLOW); return; }
        editingStudent = sel;
        nameField.setText(sel.getName());
        rollField.setText(sel.getRollNo());
        sectionField.setText(sel.getSection());
        marksField.setText(String.format("%.1f", sel.getMarks()));
        attField.setText(String.format("%.1f", sel.getAttendance()));
        addBtn.setText("UPDATE"); nameField.requestFocus();
        showMsg("✏  Editing: " + sel.getName(),Uihelper.YELLOW);
    }

    private void cancelEdit() {
        editingStudent = null; addBtn.setText("ADD"); clearForm();
        showMsg("✖  Edit cancelled.",Uihelper.YELLOW);
    }

    private void deleteStudent() {
        Student sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { showMsg("⚠  Select a student first!", Uihelper.YELLOW); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete " + sel.getName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                studentList.remove(sel);
                updateStats(); Platform.runLater(this::updateCharts);
                showMsg("🗑  " + sel.getName() + " deleted.", Uihelper.RED);
            }
        });
    }

    private void clearForm() {
        nameField.clear(); rollField.clear(); sectionField.clear();
        marksField.clear(); attField.clear();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  DATABASE / EXPORT
    // ══════════════════════════════════════════════════════════════════════════
    private void saveDB() {
        try { Databasemanager.saveStudents(studentList);
            showMsg("💾  Saved " + studentList.size() + " records!", Uihelper.CYAN);
        } catch (IOException e) { showMsg("⚠  Save failed: " + e.getMessage(),Uihelper.RED); }
    }

    private void loadDB() {
        try { List<Student> loaded = Databasemanager.loadStudents();
            studentList.setAll(loaded);
            updateStats(); Platform.runLater(this::updateCharts);
            showMsg("✔  Loaded (" + loaded.size() + " records)", Uihelper.GREEN);
        } catch (Exception e) { showMsg("⚠  Load failed!", Uihelper.YELLOW); }
    }

    private void clearAll() {
        if (studentList.isEmpty()) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Clear ALL " + studentList.size() + " students?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                studentList.clear(); editingStudent = null; addBtn.setText("ADD"); clearForm();
                updateStats(); Platform.runLater(this::updateCharts);
                showMsg("All records cleared.", "#dddd00");
            }
        });
    }

    private void exportCSV() {
        try { String fn = Exportmanager.exportCSV(studentList);
            showMsg("📤  Exported " + studentList.size() + " records → " + fn, Uihelper.CYAN);
        } catch (IOException e) { showMsg("⚠  Export failed!", Uihelper.RED); }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  STATS
    // ══════════════════════════════════════════════════════════════════════════
    private void updateStats() {
        StatsCalculator.Stats st = StatsCalculator.compute(studentList);
        totalVal.setText(String.valueOf(st.total()));
        avgVal.setText(st.total()>0 ? String.format("%.1f",st.avg()) : "—");
        highVal.setText(st.total()>0 ? String.format("%.1f",st.highest()) : "—");
        lowVal.setText(st.total()>0  ? String.format("%.1f",st.lowest())  : "—");
        passVal.setText(String.valueOf(st.pass()));
        failVal.setText(String.valueOf(st.fail()));
        if (leaderboardBox != null) updateLeaderboard(st.top3());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  LEADERBOARD
    // ══════════════════════════════════════════════════════════════════════════
    private void updateLeaderboard(List<Student> top) {
        leaderboardBox.getChildren().clear();
        if (top.isEmpty()) {
            Label e = new Label("No students yet."); e.setTextFill(Color.web("#333366"));
            e.setFont(Font.font(Uihelper.FONT, 13)); leaderboardBox.getChildren().add(e); return;
        }
        String[] medals = {"🥇","🥈","🥉"};
        String[] colors = {"#ffd700","#c0c0c0","#cd7f32"};
        for (int i = 0; i < top.size(); i++) {
            Student s = top.get(i); final String col = colors[i];
            HBox row = new HBox(14); row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10,14,10,14));
            row.setStyle("-fx-background-color:#0d0d28;-fx-border-color:"+col+
                    ";-fx-border-width:1.5;-fx-border-radius:8;-fx-background-radius:8;");
            Label medal = new Label(medals[i]); medal.setFont(Font.font(22));
            VBox info = new VBox(3);
            Label nl = new Label(s.getName()+"  ["+s.getRollNo()+"]");
            nl.setFont(Font.font(Uihelper.FONT, FontWeight.BOLD, 14));
            nl.setTextFill(Color.web(col));
            Label ml = new Label(String.format("%.1f  ·  %s  ·  %s  ·  Sec: %s",
                    s.getMarks(), s.getGrade(), s.getStatus(), s.getSection()));
            ml.setFont(Font.font(Uihelper.FONT, 11)); ml.setTextFill(Color.web("#445566"));
            info.getChildren().addAll(nl, ml);
            row.getChildren().addAll(medal, info);
            leaderboardBox.getChildren().add(row);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CHARTS
    // ══════════════════════════════════════════════════════════════════════════
    private void updateCharts() {
        if (pieChart == null || barChart == null) return;
        Map<String,Integer> counts = StatsCalculator.gradeDistribution(studentList);
        String[] gs = {"A+","A","B+","B","C","F"};
        String[] gc = {Uihelper.GREEN,"#55ff55","#aaff00","#ffff00",Uihelper.YELLOW,Uihelper.RED};

        // pie
        ObservableList<PieChart.Data> pd = FXCollections.observableArrayList();
        for (int i=0;i<gs.length;i++){int c=counts.get(gs[i]);if(c>0) pd.add(new PieChart.Data(gs[i]+" ("+c+")",c));}
        pieChart.setData(pd);
        Platform.runLater(()->{int i=0;for(PieChart.Data d:pieChart.getData()){if(d.getNode()!=null)d.getNode().setStyle("-fx-pie-color:"+gc[i%gc.length]+";");i++;}});

        // bar
        XYChart.Series<String,Number> series = new XYChart.Series<>();
        for (String g : gs) series.getData().add(new XYChart.Data<>(g, counts.get(g)));
        barChart.getData().clear(); barChart.getData().add(series);
        Platform.runLater(()->{int i=0;for(XYChart.Data<String,Number> d:series.getData()){if(d.getNode()!=null)d.getNode().setStyle("-fx-bar-fill:"+gc[i]+";");i++;}});
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════════════
    private void showMsg(String msg, String color) {
        if (msgLabel != null) { msgLabel.setText(msg); msgLabel.setTextFill(Color.web(color)); }
    }

    private HBox hbox(VBox a, VBox b) {
        HBox h = new HBox(10, a, b); h.setAlignment(Pos.CENTER); return h;
    }

    private VBox gradeScaleBox() {
        VBox box = new VBox(4);
        String[][] g = {{"90–100","A+",Uihelper.GREEN},{"80–89","A","#55ff55"},
                {"70–79","B+","#aaff00"},{"60–69","B","#ffff00"},
                {"50–59","C",Uihelper.YELLOW},{"0–49","F",Uihelper.RED}};
        for (String[] r : g) {
            HBox row = new HBox(8);
            Label range = new Label(r[0]); range.setPrefWidth(60);
            range.setTextFill(Color.web("#445566")); range.setFont(Font.font(Uihelper.FONT, 11));
            Label arr = new Label("→"); arr.setTextFill(Color.web("#222255"));
            Label grade = new Label(r[1]); grade.setTextFill(Color.web(r[2]));
            grade.setFont(Font.font(Uihelper.FONT, FontWeight.BOLD, 12));
            grade.setStyle("-fx-effect:dropshadow(gaussian,"+r[2]+",6,0.5,0,0);");
            row.getChildren().addAll(range, arr, grade); box.getChildren().add(row);
        }
        return box;
    }

    private <T> TableColumn<Student,T> plainCol(String title, String prop, String color, double w) {
        TableColumn<Student,T> c = new TableColumn<>(title);
        if (prop != null) c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setStyle("-fx-alignment:CENTER;-fx-text-fill:"+color+";");
        if (w > 0) { c.setMinWidth(w); c.setMaxWidth(w); }
        return c;
    }

    private TableColumn<Student,Double> dblCol(String title, String prop, String color, double w) {
        TableColumn<Student,Double> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean e) {
                super.updateItem(v, e);
                setText(e||v==null ? null : String.format("%.1f", v));
                setStyle("-fx-text-fill:"+color+";-fx-alignment:CENTER;");
            }
        });
        if (w > 0) { c.setMinWidth(w); c.setMaxWidth(w); }
        return c;
    }

    public static void main(String[] args) { launch(args); }
}