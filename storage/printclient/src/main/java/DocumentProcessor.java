
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintJobAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobMediaSheetsCompleted;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.event.PrintJobAttributeListener;
import javax.print.event.PrintJobListener;
import org.xbib.applet.client.DocumentClient;

public class DocumentProcessor implements Runnable {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile boolean running;
    private long time;
    private TimeUnit unit;
    private long lastCall;
    private DocumentClient docClient;
    private String printerName;
    private PrintJobListener printJobListener;
    private PrintJobAttributeListener printJobAttributeListener;

    public DocumentProcessor(long time, TimeUnit unit, DocumentClient docClient,
            String printerName) {
        this.docClient = docClient;
        this.printerName = printerName;
        this.time = time;
        this.unit = unit;
    }

    public DocumentProcessor setPrintJobListener(PrintJobListener listener) {
        this.printJobListener = listener;
        return this;
    }
    
    public DocumentProcessor setPrintJobAttributeListener(PrintJobAttributeListener listener) {
        this.printJobAttributeListener = listener;
        return this;
    }
    
    public void start() {
        scheduler.scheduleAtFixedRate(this, time, time, unit);
    }

    public void stop() throws InterruptedException {
        this.running = false;
        scheduler.awaitTermination(60, TimeUnit.SECONDS);
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean getRunning() {
        return running;
    }

    public long getLastCallMillis() {
        return lastCall;
    }

    public long getPeriodMillis() {
        return unit.toMillis(time);
    }

    public long getNextCallMillis() {
        if (lastCall > 0) {
            return lastCall + unit.toMillis(time);
        } else {
            return System.currentTimeMillis() + unit.toMillis(time);
        }
    }

    @Override
    public void run() {
        lastCall = System.currentTimeMillis();
        if (!running) {
            scheduler.shutdown();
        } else {
            String documentName = null;
            try {
                documentName = docClient.nextDocument();
                if (documentName != null) {
                    print(documentName);
                    docClient.ok(documentName);
                }
            } catch (Exception e) {
                docClient.error(documentName, e);
            }
        }
    }

    protected void print(String documentName) throws Exception {
        ByteBuffer buffer = docClient.getDocument(documentName);
        PDFPrintable printable = new PDFPrintable(buffer);
        DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
        Doc doc = new SimpleDoc(printable, flavor, null);
        PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
        attributes.add(new JobName(documentName, Locale.GERMAN));
        attributes.add(MediaSizeName.ISO_A4);
        attributes.add(new MediaPrintableArea(0, 0, 210, 297, MediaPrintableArea.MM));
        attributes.add(OrientationRequested.PORTRAIT);
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        PrintService service = null;
        for (int i = 0; i < services.length; i++) {
            if (services[i].getName().equals(printerName)) {
                service = services[i];
            }
        }
        if (service == null) {
            throw new IOException("printer not found");
        }
        DocPrintJob job = service.createPrintJob();
        job.addPrintJobListener(printJobListener);
        PrintJobAttributeSet set = new HashPrintJobAttributeSet(job.getAttributes());
        set.add(new JobMediaSheetsCompleted(0));
        job.addPrintJobAttributeListener(printJobAttributeListener, set);
        job.print(doc, attributes);
    }
}
