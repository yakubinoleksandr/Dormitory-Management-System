package ua.kpi.dormitory.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

/** PDF із вбудованим шрифтом DejaVu Sans (кирилиця); шрифт лежить у resources/fonts. */
@Component
public class PdfReportExporter implements ReportExporter {
    private final byte[] fontBytes;

    public PdfReportExporter() {
        try (InputStream in = new ClassPathResource("fonts/DejaVuSans.ttf").getInputStream()) {
            this.fontBytes = in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String format() { return "pdf"; }

    @Override
    public String contentType() { return "application/pdf"; }

    @Override
    public byte[] export(ReportTable table) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BaseFont base = BaseFont.createFont("DejaVuSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, null);
            Font title = new Font(base, 14, Font.BOLD);
            Font head = new Font(base, 10, Font.BOLD);
            Font cell = new Font(base, 10);

            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();
            Paragraph heading = new Paragraph(table.title(), title);
            heading.setSpacingAfter(12);
            document.add(heading);

            PdfPTable pdf = new PdfPTable(table.headers().size());
            pdf.setWidthPercentage(100);
            pdf.setHeaderRows(1);
            for (String h : table.headers()) {
                PdfPCell c = new PdfPCell(new Phrase(h, head));
                c.setHorizontalAlignment(Element.ALIGN_LEFT);
                c.setPadding(4);
                pdf.addCell(c);
            }
            for (List<String> row : table.rows()) {
                for (String value : row) {
                    PdfPCell c = new PdfPCell(new Phrase(value == null ? "" : value, cell));
                    c.setPadding(4);
                    pdf.addCell(c);
                }
            }
            document.add(pdf);
            document.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("Не вдалося сформувати PDF", e);
        }
    }
}
