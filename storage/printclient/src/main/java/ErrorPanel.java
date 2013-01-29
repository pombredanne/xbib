
import java.awt.Component;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;
import org.jdesktop.swingx.error.ErrorReporter;
import org.xbib.applet.client.LogClient;

public class ErrorPanel implements ErrorReporter {

    public static void showErrorDialog(Component parent, Throwable t) {
        JXErrorPane pane = new JXErrorPane();
        ErrorInfo info = new ErrorInfo("Fehler",
                t.getMessage(), null, null,
                t, null, null);
        pane.setErrorInfo(info);
        /*if (cause != null) {
            pane.setErrorReporter(new ErrorPanel());
        }*/
        JXErrorPane.showDialog(parent, pane);
    }

    @Override
    public void reportError(ErrorInfo ei) throws NullPointerException {
        // send error mail 
    }
}
