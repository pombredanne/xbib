
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.print.attribute.Attribute;
import javax.print.attribute.standard.JobName;
import javax.print.event.PrintJobAttributeEvent;
import javax.print.event.PrintJobAttributeListener;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;
import javax.swing.JApplet;
import javax.swing.JPanel;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXLoginPane;
import org.jdesktop.swingx.JXLoginPane.Status;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.auth.LoginService;
import org.xbib.applet.client.DocumentClient;
import org.xbib.applet.client.LogClient;
import org.xbib.applet.client.SessionClient;
import org.xbib.sessionstorage.SessionAttributes;

public class PrintPDFApplet extends JApplet {

    private SessionClient sessionClient;
    private DocumentClient docClient;
    private LogClient logger;
    private SessionAttributes attributes;
    private DocumentProcessor processor;

    @Override
    public void init() {
        try {
            sessionClient = new SessionClient(getDocumentBase(), "session", getParameter("jsessionid"));
            final JPanel contentPane = (JPanel) this.getContentPane();
            setBackground(Color.WHITE);
            contentPane.setLayout(new BorderLayout(4, 4));
            attributes = sessionClient.get();
            logger = new LogClient(getDocumentBase(), "log", getParameter("jsessionid"));
            logger.info("Hello server");
            final TaskContainer tasks = new TaskContainer(attributes);
            contentPane.add(tasks.getJXTaskPaneContainer(), BorderLayout.CENTER);
            docClient = new DocumentClient(getDocumentBase(), "proxy", getParameter("jsessionid"));
            if (attributes.containsKey("user")) {
                tasks.refresh(attributes);
                docClient.setAuthorization(attributes.getAsString("user"), attributes.getAsString("user"));
            } else {
                LoginService loginService = new LoginService() {
                    @Override
                    public boolean authenticate(String name, char[] password, String server) throws IOException {
                        String pw = new String(password);
                        int code = sessionClient.head();
                        logger.info("head returns with code=" + code);
                        if (code == 200) {
                            attributes.put("user", name);
                            attributes.put("password", pw);
                            docClient.setAuthorization(name, pw);
                        } else {
                            attributes.remove("user");
                            attributes.remove("password");
                        }
                        return code == 200;
                    }
                };
                JXLoginPane loginPanel = new JXLoginPane(loginService);
                loginPanel.setBannerText("Anmeldung");

                JXLoginPane.Status status = JXLoginPane.showLoginDialog(contentPane, loginPanel);
                if (status.equals(Status.SUCCEEDED)) {
                    tasks.refresh(attributes);
                    sessionClient.post(attributes);
                    processor = new DocumentProcessor(15, TimeUnit.MINUTES, docClient,
                            attributes.getAsString("printer"))
                            .setPrintJobListener(new PDFPrintJobListener(tasks.getStatusLabel()))
                            .setPrintJobAttributeListener(new PDFPrintJobAttributeListener(tasks.getStatusLabel())
                    );
                    processor.setRunning(true);
                    processor.start();
                } else {
                    logger.warn("invalid user");
                    processor.setRunning(false);
                }
            }
        } catch (Exception e) {
            ErrorPanel.showErrorDialog(this.getContentPane(), e); // --> user
            e.printStackTrace(); // --> console
            logger.error(e); // --> servlet
        }
    }

    private class PDFPrintJobListener implements PrintJobListener {

        JXLabel statusLabel;

        PDFPrintJobListener(JXLabel statusLabel) {
            this.statusLabel = statusLabel;
        }

        @Override
        public void printDataTransferCompleted(PrintJobEvent pje) {
            Attribute a = pje.getPrintJob().getAttributes().get(JobName.class);
            if (a != null) {
                statusLabel.setText(a.toString() + " fertig gedruckt");
            }
        }

        @Override
        public void printJobCompleted(PrintJobEvent pje) {
            //model.addElement("printJobCompleted");
        }

        @Override
        public void printJobFailed(PrintJobEvent pje) {
            //model.addElement("printJobFailed");
        }

        @Override
        public void printJobCanceled(PrintJobEvent pje) {
            //model.addElement("printJobCanceled");
        }

        @Override
        public void printJobNoMoreEvents(PrintJobEvent pje) {
            //model.addElement("printJobNoMoreEvents");
        }

        @Override
        public void printJobRequiresAttention(PrintJobEvent pje) {
            //model.addElement("printJobRequiresAttention");
        }
    }

    private class PDFPrintJobAttributeListener implements PrintJobAttributeListener {

        JXLabel statusLabel;

        PDFPrintJobAttributeListener(JXLabel statusBar) {
            this.statusLabel = statusLabel;
        }

        @Override
        public void attributeUpdate(PrintJobAttributeEvent pjae) {
        }
    }
}