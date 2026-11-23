package ua.kpi.dormitory.report;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Component
public class XlsxReportExporter implements ReportExporter {
    @Override
    public String format() { return "xlsx"; }

    @Override
    public String contentType() { return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"; }

    @Override
    public byte[] export(ReportTable table) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Звіт");
            Font bold = workbook.createFont();
            bold.setBold(true);
            CellStyle header = workbook.createCellStyle();
            header.setFont(bold);

            Row head = sheet.createRow(0);
            for (int c = 0; c < table.headers().size(); c++) {
                head.createCell(c).setCellValue(table.headers().get(c));
                head.getCell(c).setCellStyle(header);
                sheet.setColumnWidth(c, 6500);   // фіксована ширина: autoSize потребує AWT-шрифтів на сервері
            }
            int r = 1;
            for (List<String> row : table.rows()) {
                Row excelRow = sheet.createRow(r++);
                for (int c = 0; c < row.size(); c++) {
                    excelRow.createCell(c).setCellValue(row.get(c));
                }
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
