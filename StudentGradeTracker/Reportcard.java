package StudentGradeTracker;



import javafx.geometry.*;
        import javafx.scene.Scene;
import javafx.scene.control.*;
        import javafx.scene.layout.*;
        import javafx.scene.paint.Color;
import javafx.scene.text.*;
        import javafx.stage.*;

        import java.io.IOException;

/**
 *  ReportCardDialog – formatted report card popup + save as TXT
 */
public class Reportcard {

    private final Stage   owner;
    private Runnable      onMsg;
    private String        lastMsg  = "";
    private String        lastColor = Uihelper.GREEN;

    public Reportcard(Stage owner)  { this.owner = owner; }
    public void setOnMsg(Runnable r)      { this.onMsg = r; }
    public String getLastMsg()            { return lastMsg; }
    public String getLastMsgColor()       { return lastColor; }

    public void show(Student sel) {
        if (sel == null) return;

        String report = Exportmanager.buildReportText(sel);

        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        // ── Report text area ──────────────────────────────────────────────────
        TextArea area = new TextArea(report);
        area.setEditable(false);
        area.setStyle(
                "-fx-font-family:'Courier New';-fx-font-size:13;" +
                        "-fx-background-color:#0a0a1e;" +
                        "-fx-text-fill:" + Uihelper.CYAN + ";" +
                        "-fx-control-inner-background:#0a0a1e;");
        area.setPrefSize(490, 310);

        // ── Buttons ───────────────────────────────────────────────────────────
        Button saveBtn  = Uihelper.actionBtn("💾  SAVE TXT",  "#001122","#003366", Uihelper.CYAN);
        Button closeBtn = Uihelper.actionBtn("✕  CLOSE",      "#330011","#cc0033", Uihelper.RED);
        Label  statusLbl = new Label("");
        statusLbl.setFont(Font.font(Uihelper.FONT, FontWeight.BOLD, 11));

        saveBtn.setOnAction(e -> {
            try {
                String fn = Exportmanager.saveReportTXT(sel);
                lastMsg   = "📄 Saved: " + fn; lastColor = Uihelper.GREEN;
                statusLbl.setText(lastMsg); statusLbl.setTextFill(Color.web(Uihelper.GREEN));
                if (onMsg != null) onMsg.run();
            } catch (IOException ex) {
                lastMsg   = "⚠  Save failed!"; lastColor = Uihelper.RED;
                statusLbl.setText(lastMsg); statusLbl.setTextFill(Color.web(Uihelper.RED));
            }
        });
        closeBtn.setOnAction(e -> dialog.close());

        HBox btns = new HBox(10, saveBtn, closeBtn);
        btns.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(12,
                Uihelper.sectionLabel("🖨  REPORT CARD  —  " + sel.getName()),
                area, statusLbl, btns);
        content.setPadding(new Insets(18));
        content.setStyle(
                "-fx-background-color:#0d0d2e;" +
                        "-fx-border-color:#2a2aff;-fx-border-width:2;" +
                        "-fx-border-radius:12;-fx-background-radius:12;");

        dialog.setScene(new Scene(content, 510, 430));
        dialog.show();
    }
}