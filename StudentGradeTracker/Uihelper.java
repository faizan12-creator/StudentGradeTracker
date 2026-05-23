package StudentGradeTracker;



import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 *  UIHelper – shared factory methods for consistent dark-neon styling
 */
public final class Uihelper {

    // ── Brand colours ─────────────────────────────────────────────────────────
    public static final String BG_DARK    = "#070714";
    public static final String BG_CARD    = "#0a0a1e";
    public static final String BG_PANEL   = "#0d0d2e";
    public static final String BORDER     = "#1a1a66";
    public static final String CYAN       = "#00cfff";
    public static final String GREEN      = "#00ff88";
    public static final String YELLOW     = "#ffaa00";
    public static final String RED        = "#ff4455";
    public static final String PURPLE     = "#aa44ff";
    public static final String DIM        = "#4455aa";
    public static final String FONT       = "Courier New";

    private Uihelper() {}

    // ── Section label ─────────────────────────────────────────────────────────
    public static Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font(FONT, FontWeight.BOLD, 12));
        l.setTextFill(Color.web(DIM));
        return l;
    }

    // ── Styled text field ─────────────────────────────────────────────────────
    public static TextField styledField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        String base = fieldCss("#1a1a66");
        f.setStyle(base);
        f.focusedProperty().addListener((o, ov, nv) ->
                f.setStyle(fieldCss(nv ? CYAN : "#1a1a66")));
        return f;
    }

    private static String fieldCss(String borderColor) {
        return "-fx-background-color:#0a0a1e;" +
                "-fx-border-color:" + borderColor + ";" +
                "-fx-border-width:1.5;" +
                "-fx-border-radius:6;-fx-background-radius:6;" +
                "-fx-text-fill:white;-fx-prompt-text-fill:#333366;" +
                "-fx-padding:8 10 8 10;" +
                "-fx-font-family:'" + FONT + "';-fx-font-size:12;";
    }

    // ── Action button ─────────────────────────────────────────────────────────
    public static Button actionBtn(String text, String bg, String border, String glow) {
        Button b = new Button(text);
        String base = btnCss(bg, border, "");
        String over = btnCss(border, glow,
                "-fx-effect:dropshadow(gaussian," + glow + ",12,0.5,0,0);");
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(over));
        b.setOnMouseExited(e  -> b.setStyle(base));
        return b;
    }

    private static String btnCss(String bg, String border, String extra) {
        return "-fx-background-color:" + bg + ";" +
                "-fx-border-color:" + border + ";" +
                "-fx-border-width:1.5;" +
                "-fx-border-radius:6;-fx-background-radius:6;" +
                "-fx-text-fill:white;" +
                "-fx-font-family:'" + FONT + "';" +
                "-fx-font-weight:bold;-fx-font-size:12;" +
                "-fx-padding:8 14 8 14;-fx-cursor:hand;" + extra;
    }

    // ── Grade color map ───────────────────────────────────────────────────────
    public static String gradeColor(String g) {
        return switch (g) {
            case "A+" -> "#00ff88";
            case "A"  -> "#55ff55";
            case "B+" -> "#aaff00";
            case "B"  -> "#ffff00";
            case "C"  -> "#ffaa00";
            default   -> "#ff4455";
        };
    }

    // ── Stat card ─────────────────────────────────────────────────────────────
    public static VBox statCard(String title, Label valLabel, String color) {
        valLabel.setFont(Font.font(FONT, FontWeight.EXTRA_BOLD, 20));
        valLabel.setTextFill(Color.web(color));
        valLabel.setStyle("-fx-effect:dropshadow(gaussian," + color + ",4,0.1,0,0);");
        Label t = new Label(title);
        t.setFont(Font.font(FONT, 10)); t.setTextFill(Color.web("#445566"));
        VBox card = new VBox(3, t, valLabel);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10, 14, 10, 14)); card.setPrefWidth(130);
        card.setStyle(
                "-fx-background-color:#0a0a1e;" +
                        "-fx-border-color:" + color + ";" +
                        "-fx-border-width:1.5;" +
                        "-fx-border-radius:8;-fx-background-radius:8;" +
                        "-fx-effect:dropshadow(gaussian," + color + ",4,0.1,0,0);");
        return card;
    }
}