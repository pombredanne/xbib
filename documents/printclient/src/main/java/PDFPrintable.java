
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.nio.ByteBuffer;

public class PDFPrintable implements Printable {

    private PDFFile pdf;

    public PDFPrintable(ByteBuffer buffer) throws Exception {
        this.pdf = new PDFFile(buffer);
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        int pg = pageIndex + 1;
        if (pdf == null) {
            throw new PrinterException("No PDF data specified");
        }
        if (pg < 1 || pg > pdf.getNumPages()) {
            return NO_SUCH_PAGE;
        }

        PDFPage pdfPage = pdf.getPage(pg);

        Graphics2D g2 = (Graphics2D) graphics;

        // Translate to accomodate the requested top and left margins.
        g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        // Figure out how big the drawing is, and how big the page
        // (excluding margins) is
        double pageWidth = pageFormat.getImageableWidth(); // Page width
        double pageHeight = pageFormat.getImageableHeight(); // Page height

        // If the component is too wide or tall for the page, scale it down
        if (pdfPage.getWidth() > pageWidth) {
            double factor = pageWidth / pdfPage.getWidth(); // How much to scale
            g2.scale(factor, factor); // Adjust coordinate system
            pageWidth /= factor; // Adjust page size up
            pageHeight /= factor;
        }
        if (pdfPage.getHeight() > pageHeight) { // Do the same thing for height
            double factor = pageHeight / pdfPage.getHeight();
            g2.scale(factor, factor);
            pageWidth /= factor;
            pageHeight /= factor;
        }

        // Now we know the component will fit on the page. Center it by
        // translating as necessary.
        g2.translate((pageWidth - pdfPage.getWidth()) / 2, (pageHeight - pdfPage.getHeight()) / 2);

        PDFRenderer pgs = new PDFRenderer(pdfPage, g2, pdfPage.getPageBox().getBounds(), pdfPage.getBBox(), null);
        try {
            pdfPage.waitForFinish();
            pgs.run();
        } catch (InterruptedException ie) {
        }
        return PAGE_EXISTS;
    }
}