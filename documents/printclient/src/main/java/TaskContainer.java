
import java.awt.Color;
import java.awt.Font;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.FontUIResource;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.plaf.basic.BasicStatusBarUI;
import org.jdesktop.swingx.plaf.metal.MetalStatusBarUI;
import org.xbib.sessionstorage.SessionAttributes;

public class TaskContainer {

    private JXTaskPaneContainer taskpanecontainer;
    private OverviewPanel overviewpanel;
    private ConfigurationPanel configpanel;
    private JXStatusBar statusBar;
    private JXLabel statusLabel;
    private SessionAttributes attributes;

    public TaskContainer(SessionAttributes attributes) {
        this.attributes = attributes;
        build();
    }
    
    private void build() {
        UIManager.put("TaskPaneContainer.background", Colors.White.color());
        UIManager.put("TaskPane.font", new FontUIResource(new Font("Helvetica", Font.BOLD, 16)));
        UIManager.put("TaskPane.titleBackgroundGradientStart", Colors.White.color());
        UIManager.put("TaskPane.titleBackgroundGradientEnd", Colors.LightBlue.color());
        UIManager.put("TaskPaneContainer.useGradient", Boolean.FALSE);

        this.statusLabel = new JXLabel("Leerlauf");
        this.overviewpanel = new OverviewPanel(attributes);
        this.configpanel = new ConfigurationPanel(attributes);
        
        taskpanecontainer = new JXTaskPaneContainer();
        taskpanecontainer.add(overviewpanel);
        taskpanecontainer.add(configpanel);
        taskpanecontainer.add(createStatusBar(statusLabel));
    }
    
    public void refresh(SessionAttributes attributes) {
        overviewpanel.refresh(attributes);
        configpanel.refresh(attributes);
    }

    public JXTaskPaneContainer getJXTaskPaneContainer() {
        return taskpanecontainer;
    }
    
    public JXLabel getStatusLabel() {
        return statusLabel;
    }
    
    private JXStatusBar createStatusBar(JXLabel label) {
        final JXStatusBar bar = new JXStatusBar();
        bar.setUI(new MetalStatusBarUI());
        bar.putClientProperty(BasicStatusBarUI.AUTO_ADD_SEPARATOR, false);
        bar.setBackground(Colors.LightGray.color());
        final MatteBorder outerBorder = new MatteBorder(1, 0, 0, 0, Colors.Gray.color());
        final EmptyBorder innerBorder = new EmptyBorder(2, 2, 2, 2);
        bar.setBorder(new CompoundBorder(outerBorder, innerBorder));
        final JXStatusBar.Constraint constraint = new JXStatusBar.Constraint(JXStatusBar.Constraint.ResizeBehavior.FILL);
        bar.add(label, constraint);
        return bar;
            
    }

    enum Colors {

        Pink(255, 175, 175),
        Green(159, 205, 20),
        Orange(213, 113, 13),
        Yellow(Color.yellow),
        Red(189, 67, 67),
        LightBlue(208, 223, 245),
        Blue(Color.blue),
        DarkBlue(67, 67, 190),
        Black(0, 0, 0),
        White(255, 255, 255),
        Gray(Color.gray.getRed(), Color.gray.getGreen(), Color.gray.getBlue()),
        LightGray(200, 200, 200);

        Colors(Color c) {
            color = c;
        }

        Colors(int r, int g, int b) {
            color = new Color(r, g, b);
        }

        Colors(int r, int g, int b, int alpha) {
            color = new Color(r, g, b, alpha);
        }

        Colors(float r, float g, float b, float alpha) {
            color = new Color(r, g, b, alpha);
        }
        private Color color;

        public Color alpha(float t) {
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (t * 255f));
        }

        public static Color alpha(Color c, float t) {
            return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (t * 255f));
        }

        public Color color() {
            return color;
        }

        public Color color(float f) {
            return alpha(f);
        }
    }
}
