package usta.parking.infrastructure.adapter.in.web.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Piezas comunes de los tres reportes PDF (estilo del sistema de diseño: azul marino y dorado)
final class PdfSupport {

    static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    static final Color NAVY = new Color(0x1F, 0x2A, 0x44);
    static final Color GOLD = new Color(0xC9, 0xA2, 0x27);
    static final Color ZEBRA = new Color(0xF5, 0xF7, 0xFB);
    static final Color BORDER = new Color(0xE1, 0xE5, 0xEE);
    static final Color TEXT = new Color(0x1B, 0x22, 0x33);
    static final Color MUTED = new Color(0x6B, 0x73, 0x85);

    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA, 20, Font.BOLD, NAVY);
    private static final Font SUBTITLE = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, MUTED);
    private static final Font HEADER = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.BOLD, Color.WHITE);
    private static final Font BODY = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, TEXT);
    private static final Font STAT_LABEL = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, MUTED);
    private static final Font STAT_VALUE = FontFactory.getFont(FontFactory.HELVETICA, 15, Font.BOLD, NAVY);
    private static final Font FOOTER = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, MUTED);

    private PdfSupport() {
    }

    // Lo que cada reporte escribe dentro del documento
    @FunctionalInterface
    interface Content {
        void write(Document document) throws DocumentException;
    }

    // Crea el documento, deja que el reporte escriba su contenido y devuelve los bytes del PDF
    static byte[] build(Rectangle pageSize, Content content) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(pageSize, 36, 36, 40, 40);
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            content.write(document);
            addFooter(document);
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
        return out.toByteArray();
    }

    static String now() {
        return LocalDateTime.now().format(DATE_TIME);
    }

    // Título del reporte con su subtítulo (fecha de generación, filtros, etc.)
    static void addTitle(Document document, String title, String subtitle) throws DocumentException {
        Paragraph paragraph = new Paragraph(title, TITLE);
        paragraph.setSpacingAfter(2);
        document.add(paragraph);

        Paragraph sub = new Paragraph(subtitle, SUBTITLE);
        sub.setSpacingAfter(12);
        document.add(sub);
    }

    // Franja de datos resumen: etiquetas arriba y valores abajo
    static PdfPTable stats(String[] labels, String[] values) {
        PdfPTable table = new PdfPTable(labels.length);
        table.setWidthPercentage(100);
        table.setSpacingAfter(12);
        for (int i = 0; i < labels.length; i++) {
            PdfPCell cell = new PdfPCell();
            cell.setBackgroundColor(ZEBRA);
            cell.setBorderColor(BORDER);
            cell.setPadding(8);
            cell.addElement(new Paragraph(labels[i], STAT_LABEL));
            cell.addElement(new Paragraph(values[i], STAT_VALUE));
            table.addCell(cell);
        }
        return table;
    }

    // Tabla con encabezado azul que se repite en cada página
    static PdfPTable table(float[] widths, String... headers) {
        PdfPTable table = new PdfPTable(widths);
        table.setWidthPercentage(100);
        table.setHeaderRows(1);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER));
            cell.setBackgroundColor(NAVY);
            cell.setBorderColor(NAVY);
            cell.setPadding(6);
            table.addCell(cell);
        }
        return table;
    }

    // Fila de datos; las filas impares llevan fondo gris claro
    static void row(PdfPTable table, int index, String... values) {
        for (String value : values) {
            PdfPCell cell = new PdfPCell(new Phrase(value == null ? "-" : value, BODY));
            cell.setBorderColor(BORDER);
            cell.setPadding(5);
            if (index % 2 == 1) {
                cell.setBackgroundColor(ZEBRA);
            }
            table.addCell(cell);
        }
    }

    // Mensaje cuando no hay datos para mostrar
    static void addEmptyNote(Document document, String message) throws DocumentException {
        Paragraph note = new Paragraph(message, SUBTITLE);
        note.setSpacingBefore(8);
        document.add(note);
    }

    private static void addFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph(
                "Universidad Santo Tomás - Seccional Tunja · Desarrollo Orientado a Servicios 2026-2", FOOTER);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(16);
        document.add(footer);
    }
}
