package StudentGradeTracker;



import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *  SubjectsDialog – modal popup to enter / edit per-subject marks for a student
 */
public class Subjectsdialog {

    private static final String[] SUBJECTS =
            {"Math", "English", "Science", "Urdu", "Computer", "Physics", "Chemistry"};

    private final Stage owner;
    private Runnable    onSave;        // callback: refresh table + charts in parent

    public Subjectsdialog(Stage owner) { this.owner = owner; }
    public void setOnSave(Runnable r)  { this.onSave = r; }

    public void show(Student sel) {
        if (sel == null) return;

        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        Map<String, TextField> fields = new LinkedHashMap<>();

        VBox content = new VBox(10);
        content.setPadding(new Insets(22));
        content.setStyle(
                "-fx-background-color:#0d0d2e;" +
                        "-fx-border-color:#2a2aff;-fx-border-width:2;" +
                        "-fx-border-radius:12;-fx-background-radius:12;");

        // ── Header ────────────────────────────────────────────────────────────
        Label heading = new Label("📋  Subject Marks  —  " + sel.getName());
        heading.setFont(Font.font(Uihelper.FONT, FontWeight.BOLD, 13));
        heading.setTextFill(Color.web(Uihelper.CYAN));
        content.getChildren().add(heading);
        content.getChildren().add(new Separator());

        // ── Rows ──────────────────────────────────────────────────────────────
        for (String subj : SUBJECTS) {
            HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
            Label lbl = new Label(subj + ":");
            lbl.setFont(Font.font(Uihelper.FONT, 12));
            lbl.setTextFill(Color.WHITE); lbl.setPrefWidth(95);
            TextField tf = Uihelper.styledField("0 – 100"); tf.setPrefWidth(140);
            if (sel.getSubjects().containsKey(subj))
                tf.setText(String.format("%.1f", sel.getSubjects().get(subj)));
            fields.put(subj, tf);
            row.getChildren().addAll(lbl, tf);
            content.getChildren().add(row);
        }

        // ── Status label ──────────────────────────────────────────────────────
        Label statusLbl = new Label("");
        statusLbl.setFont(Font.font(Uihelper.FONT, FontWeight.BOLD, 11));

        // ── Buttons ───────────────────────────────────────────────────────────
        Button saveBtn  = Uihelper.actionBtn("✔  SAVE",  "#003322","#00cc66", Uihelper.GREEN);
        Button clearBtn = Uihelper.actionBtn("⟳ CLEAR",  "#221100","#554400", Uihelper.YELLOW);
        Button closeBtn = Uihelper.actionBtn("✕  CLOSE", "#330011","#cc0033", Uihelper.RED);
        HBox btns = new HBox(10, saveBtn, clearBtn, closeBtn);
        btns.setAlignment(Pos.CENTER_RIGHT);

        saveBtn.setOnAction(e -> {
            LinkedHashMap<String, Double> map = new LinkedHashMap<>();
            boolean anyInvalid = false;
            for (Map.Entry<String, TextField> en : fields.entrySet()) {
                String txt = en.getValue().getText().trim();
                if (txt.isEmpty()) continue;
                try {
                    double v = Double.parseDouble(txt);
                    if (v < 0 || v > 100) { anyInvalid = true; break; }
                    map.put(en.getKey(), v);
                } catch (NumberFormatException ex) { anyInvalid = true; break; }
            }
            if (anyInvalid) {
                statusLbl.setText("⚠  Invalid value – marks must be 0–100!");
                statusLbl.setTextFill(Color.web(Uihelper.RED));
            } else {
                sel.setSubjects(map);
                if (onSave != null) onSave.run();
                statusLbl.setText("✔  Subjects saved!");
                statusLbl.setTextFill(Color.web(Uihelper.GREEN));
                new javafx.animation.Timeline(new javafx.animation.KeyFrame(
                        javafx.util.Duration.millis(800), ev -> dialog.close())).play();
            }
        });

        clearBtn.setOnAction(e -> {
            fields.values().forEach(TextField::clear);
            sel.getSubjects().clear();
            if (onSave != null) onSave.run();
            statusLbl.setText("Subjects cleared.");
            statusLbl.setTextFill(Color.web(Uihelper.YELLOW));
        });

        closeBtn.setOnAction(e -> dialog.close());

        content.getChildren().addAll(new Separator(), statusLbl, btns);
        dialog.setScene(new Scene(content, 340, 520));
        dialog.show();
    }
}