package org._brown_tech._custom;

import javafx.application.Platform;
import org._brown_tech._model._object.ReceiptItems;

import java.awt.*;
import java.awt.print.*;
import java.util.ArrayList;

import static java.awt.print.Printable.NO_SUCH_PAGE;
import static java.awt.print.Printable.PAGE_EXISTS;

/**
 * @author Mandela
 */

public class PrintWork extends Watchdog {

    public static ArrayList<Object> bill_items = new ArrayList<>();

    public PrintWork(final String before, final String after) {
        try {
            Printable contentToPrint = (Graphics graphics, PageFormat pageFormat, int pageIndex) -> {
                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                String[] header = before.split(";");
                int spaceBtwLine = 10;
                ReceiptItems.graphics = graphics;
                ReceiptItems.someOtherSpacing = 5;
                for (String header1 : header) {
                    if (header1.contains("BROWN TECH")) {
                        g2d.setFont(new Font("HelveticaTwoBQ", Font.BOLD, 7));
                    } else if (header1.contains("Murang'a".toUpperCase()) || header1.contains("Call")) {
                        g2d.setFont(new Font("Fira Sans", Font.PLAIN, 6));
                    } else if (header1.contains("OFFICIAL BILL RECEIPT")) {
                        g2d.setFont(new Font("Fira Sans", Font.BOLD, 6));
                    } else {
                        g2d.setFont(new Font("Fira Sans", Font.PLAIN, 6));
                    }
                    graphics.drawString(header1, 5, spaceBtwLine);
                    spaceBtwLine += 10;
                }
                ReceiptItems.spaceBtwLines = spaceBtwLine;
                g2d.setFont(new Font("Fira Sans", Font.PLAIN, 5));
                ReceiptItems.printHeader();
                ReceiptItems.builtStoreItems(bill_items).forEach(ReceiptItems::printInvoice);
                spaceBtwLine = ReceiptItems.spaceBtwLines;
                String[] footer = after.split(";");
                for (String footer1 : footer) {
                    if (footer1.startsWith(" Total")) {
                        g2d.setFont(new Font("Fira Sans", Font.PLAIN, 7));
                    } else if (footer1.contains(" NOTE:") || footer1.startsWith("   * C")) {
                        g2d.setFont(new Font("Fira Sans", Font.PLAIN, 4));
                    } else {
                        g2d.setFont(new Font("Fira Sans", Font.PLAIN, 6));
                    }
                    graphics.drawString(footer1, 5, spaceBtwLine);
                    spaceBtwLine += 10;
                }
                if (pageIndex > 0) {
                    return NO_SUCH_PAGE;
                }
                return PAGE_EXISTS;
            };
            PageFormat pageFormat = new PageFormat();
            pageFormat.setOrientation(PageFormat.PORTRAIT);
            Paper pPaper = pageFormat.getPaper();
            pPaper.setImageableArea(0, 0, pPaper.getWidth(), pPaper.getHeight() - 2);
            pageFormat.setPaper(pPaper);
            PrinterJob printerJob = PrinterJob.getPrinterJob();
            printerJob.setPrintable(contentToPrint, pageFormat);
            try {
                printerJob.print();
                Platform.runLater(() -> information_message("CUSTOMER RECEIPT IS DONE").show());
                bill_items.clear();
                ReceiptItems.someOtherSpacing = 0;
                ReceiptItems.spaceBtwLines = 0;
            } catch (PrinterException ex) {
                ex.printStackTrace();
                Platform.runLater(() -> programmer_error(ex).show());
                new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + ex, 1)).start();
                new Thread(write_stack_trace(ex)).start();
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            Platform.runLater(() -> programmer_error(ex).show());
            new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + ex, 1)).start();
            new Thread(write_stack_trace(ex)).start();
        } catch (Exception ex) {
            ex.printStackTrace();
            programmer_error(ex).show();
            new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + ex, 1)).start();
            new Thread(write_stack_trace(ex)).start();
        }
    }
}
