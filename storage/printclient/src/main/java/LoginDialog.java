
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import org.jdesktop.swingx.*;
import org.jdesktop.swingx.auth.LoginEvent;
import org.jdesktop.swingx.auth.LoginListener;
import org.jdesktop.swingx.auth.LoginService;
import org.jdesktop.swingx.auth.PasswordStore;
import org.jdesktop.swingx.auth.UserNameStore;
import org.jdesktop.swingx.painter.ImagePainter;

public class LoginDialog extends JDialog {
    /**
     * An optional banner at the top of the dialog
     */
    //private JXPanel banner;
    /**
     * Custom label allowing the developer to display some message to the user
     */
   // private JLabel label;
    /**
     * Shows a message such as "user name or password incorrect" or
     * "could not contact server" or something like that if something
     * goes wrong
     */
    private JLabel messageLabel;
    /**
     * If something goes wrong, this link will be displayed so the user can
     * click on it to be shown the exception, etc
     */
    private JXHyperlink detailsLink;
    /**
     * The login panel containing the username & password fields, and handling
     * the login procedures.
     */
    private JXLoginPane loginPanel;
    
    private JXPanel contentPanel;
    private JXPanel buttonPanel;
    //private JXPanel progressPanel;
    
    /**
     * Only true if the user cancels their login operation. This is reset to false
     * after the login thread is cancelled and the proper message shown
     */
    private boolean cancelled;
    
    /** Creates a new instance of LoginDialog */
    public LoginDialog() {
        this(null, null, null);
    }
    
    public LoginDialog(LoginService service, PasswordStore ps, UserNameStore us) {
        this(new JXLoginPane(service, ps, us));
    }

    public LoginDialog(JXLoginPane panel) {
        loginPanel = panel;
        initComponents();
    }
    
    private void initComponents() {
        setModal(false);
       // setTitle("Anmeldung");
        loginPanel.getLoginService().addLoginListener(new Listener());
        //progressPanel = new ProgressPane();
        
        //create the default banner
        /*banner = new JXPanel();
        ImagePainter image = new ImagePainter(
             createLoginBanner(Color.WHITE, Color.LIGHT_GRAY, Color.DARK_GRAY)
        );
        banner.setBackgroundPainter(image);
        */
        
        //create the default label
       // label = new JLabel("Anmeldung");
       // label.setFont(label.getFont().deriveFont(Font.BOLD));
        
        //create the message and hyperlink and hide them
        messageLabel = new JLabel(" ");
        messageLabel.setVisible(false);
        detailsLink = new JXHyperlink();
        detailsLink.setVisible(false);
        
        //create the buttons
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                cancelled = false;
               // loginPanel.  .startLogin();
                setVisible(false);
            }
        });
        okButton.setMnemonic('O');
        //okButton.setPreferredSize(new Dimension(80, okButton.getPreferredSize().height));

        JButton cancelButton = new JButton("Abbrechen");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                cancelled = true;
                setVisible(false);
            }
        });
        cancelButton.setMnemonic('C');
        //cancelButton.setPreferredSize(new Dimension(80, cancelButton.getPreferredSize().height));
        
        //layout the dialog
        setLayout(new BorderLayout());
        //add(banner, BorderLayout.NORTH);
        
        contentPanel = new JXPanel(new GridBagLayout());
      //  contentPanel.add(label, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(12, 12, 7, 11), 0, 0));
        contentPanel.add(loginPanel, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 36, 7, 11), 0, 0));
        contentPanel.add(messageLabel, new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 36, 0, 11), 0, 0));
        add(contentPanel, BorderLayout.CENTER);
        
        buttonPanel = new JXPanel(new GridBagLayout());
        buttonPanel.add(okButton, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(17, 12, 11, 5), 0, 0));
        buttonPanel.add(cancelButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(17, 0, 11, 11), 0, 0));
        add(buttonPanel, BorderLayout.SOUTH);
        
//        service.addLoginListener(this);
//        panel.okButton.addActionListener(this);
//        panel.cancelButton.addActionListener(this);

        getRootPane().setDefaultButton(okButton);
        pack();
        setResizable(false);
        setLocationRelativeTo(getRootPane());
        //setLocation(WindowUtils.getPointForCentering(this));
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }

//    public void loginFailed(LoginEvent source) {
//        finishedLogin(false);
//        loginPanel.loginProgress.setText(UIManager.getString(CLASS_NAME + ".loginFailed"));
//    }
//
//    public void loginSucceeded(LoginEvent source) {
//        finishedLogin(true);
//        dialog.dispose();
//    }
//
//    public void loginStarted(LoginEvent source) {
//
//    }
//
//
//    void finishedLogin(boolean result) {
//        loginPanel.cancelLogin.setEnabled(false);
//        loginPanel.progressIndicator.setIndeterminate(false);
//        loginPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//    }
//
//    void cancelAuthentication() {
//        service.cancelAuthentication();
//        loginPanel.cancelLogin.setEnabled(false);
//        loginPanel.progressIndicator.setIndeterminate(false);
//        loginPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//    }
//
//    public void loginCanceled(LoginEvent source) {
//        cancelled = true;
//    }
//
//    public void actionPerformed(ActionEvent ae) {
//        Object source = ae.getSource();
//        if (source == loginPanel.okButton) {
//            startLogin();
//        } else if (source == loginPanel.cancelLogin) {
//            cancelAuthentication();
//        } else if (source == loginPanel.cancelButton) {
//            dialog.dispose();
//        }
//    }
    
    private BufferedImage createLoginBanner(Color foreground, Color lightBackground, Color darkBackground) {
        int w = 400;
        int h = 60;
        
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        //draw a big square
        g2.setColor(darkBackground);
        g2.fillRect(0, 0, w, h);
        
        //create the curve shape
        GeneralPath curveShape = new GeneralPath(GeneralPath.WIND_NON_ZERO);
        curveShape.moveTo(0, h * .6f);
        curveShape.curveTo(w * .167f, h * 1.2f, w * .667f, h * -.5f, w, h * .75f);
        curveShape.lineTo(w, h);
        curveShape.lineTo(0, h);
        curveShape.lineTo(0, h * .8f);
        curveShape.closePath();
        
        //draw into the buffer a gradient (bottom to top), and the text "Login"
        GradientPaint gp = new GradientPaint(0, h, 
                darkBackground,
                0, 0, 
                lightBackground
        );
        g2.setPaint(gp);
        g2.fill(curveShape);

        Font font = new Font("Arial Bold", Font.PLAIN, 36);
        g2.setFont(font);
        g2.setColor(foreground
                );
        g2.drawString("Login", w * .05f, h * .75f);
        return img;
    }

    /**
     * Used as a glass pane when doing the login procedure
     */
    /*private final class ProgressPane extends JXPanel {
        public ProgressPane() {
            setLayout(new BorderLayout(24, 24));
            JXPanel contentPanel = new JXPanel(new GridBagLayout());
            add(contentPanel, BorderLayout.CENTER);
            
            JLabel label = new JLabel("Bitte warten, Anmeldung läuft....");
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            
            JProgressBar pb = new JProgressBar();
            pb.setIndeterminate(true);
            
            JButton stopButton = new JButton("Anmeldung abbrechen");
            stopButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    //loginPanel.cancelLogin();
                }
            });
            
            contentPanel.add(label, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(12, 12, 11, 11), 0, 0));
            contentPanel.add(pb, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 24, 11, 7), 0, 0));
            contentPanel.add(stopButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 11, 11), 0, 0));
        }
    }*/
    
    private final class Listener implements LoginListener {
        @Override
        public void loginSucceeded(LoginEvent source) {
            //setVisible(false);
        }

        @Override
        public void loginStarted(LoginEvent source) {
            //switch to login animation
            buttonPanel.setVisible(false);
            remove(contentPanel);
           // add(progressPanel, BorderLayout.CENTER);
        }

        @Override
        public void loginFailed(LoginEvent source) {
            //switch to input fields, show error
            buttonPanel.setVisible(true);
           // remove(progressPanel);
            add(contentPanel, BorderLayout.CENTER);
        }

        @Override
        public void loginCanceled(LoginEvent source) {
            //switch to input fields, show message
            buttonPanel.setVisible(true);
          //  remove(progressPanel);
            add(contentPanel, BorderLayout.CENTER);
        }
    }
}
