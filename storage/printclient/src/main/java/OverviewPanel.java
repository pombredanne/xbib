
import java.util.Locale;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTaskPane;
import org.xbib.applet.util.DateUtil;
import org.xbib.sessionstorage.SessionAttributes;

public class OverviewPanel extends JXTaskPane {

    SessionAttributes attributes;
    JXLabel userLabel;
    JXLabel numberOfNextDocs;
    JXLabel numberOfErrorDocs;
    JXLabel lastActivity;
    JXLabel nextActivity;
    JXButton tryAgainButton;
    JXButton startButton;
    JXButton stopButton;
    JXStatusBar statusBar;

    OverviewPanel(SessionAttributes attributes) {
        this.attributes = attributes;
        build();
    }

    private void build() {
        setTitle("Überblick");
        setCollapsed(false);
        userLabel = new JXLabel();
        numberOfNextDocs = new JXLabel();
        numberOfErrorDocs = new JXLabel();
        lastActivity = new JXLabel();
        nextActivity = new JXLabel();
        startButton = new JXButton("Start");
        stopButton = new JXButton("Stop");
        tryAgainButton = new JXButton("Erneut versuchen");
        add(userLabel);
        add(numberOfNextDocs);
        add(lastActivity);
        add(nextActivity);
        add(startButton);
        add(stopButton);
        add(numberOfErrorDocs);
        add(tryAgainButton);
        refresh(attributes);
    }

    public void refresh(SessionAttributes attributes) {
        userLabel.setText("ISIL: " + attributes.getAsString("user"));
        numberOfNextDocs.setText("Es liegen " + attributes.getAsLong("numberOfNextDocs") + " Dokumente bereit.");
        Long l = attributes.getAsLong("numberOfErrorDocs");
        if (l > 0) {
            numberOfErrorDocs.setText("Es liegen " + l + " fehlerhafte Dokumente vor.");
            tryAgainButton.setVisible(true);
        } else {
            numberOfErrorDocs.setVisible(false);
            tryAgainButton.setVisible(false);
        }
        l = attributes.getAsLong("lastActivity");
        if (l > 0) {
            lastActivity.setText("Letzte Aktivität: " + DateUtil.formatDate(l, Locale.GERMAN));
            lastActivity.setVisible(true);
        } else {
            lastActivity.setVisible(false);
        }
        l = attributes.getAsLong("nextActivity");
        if (l > 0) {
            nextActivity.setText("Nächste Aktivität: " + DateUtil.formatDate(l, Locale.GERMAN));
            nextActivity.setVisible(true);
        } else {
            nextActivity.setVisible(false);
        }
    }
}
