
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.xbib.sessionstorage.SessionAttributes;

public class ConfigurationPanel extends JXTaskPane {

    String docPath;
    SessionAttributes attributes;
    final JXComboBox printerBox = new JXComboBox();

    public ConfigurationPanel(SessionAttributes attributes) {
        this.attributes = attributes;
        build();
    }

    private void build() {
        setCollapsed(false);
        setTitle("Konfiguration");
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        List<String> printerNames = new ArrayList();
        for (int i = 0; i < services.length; i++) {
            printerNames.add(services[i].getName());
        }
        ListComboBoxModel model = new ListComboBoxModel(printerNames);

        JXLabel label = new JXLabel("Fernleihe/Scanner/Auftragsblätter");
        final JXLabel statusLabel = new JXLabel("Keine Auswahl");
        final JXStatusBar statusBar = new JXStatusBar();
        statusBar.add(statusLabel);
        
        printerBox.setModel(model);
        printerBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
               statusLabel.setText("Auswahl: " + printerBox.getSelectedItem().toString());
            }
        });
        setLayout(new MigLayout("wrap"));
        add(label);
        add(printerBox);
        add(statusBar);
    }

    public String getSelection() {
        return printerBox.getSelectedItem().toString();
    }
    
    public void refresh(SessionAttributes attributes) {
        
    }
}
