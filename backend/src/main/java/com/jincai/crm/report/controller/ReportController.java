package com.jincai.crm.report.controller;

import com.jincai.crm.report.service.*;

import com.jincai.crm.common.ApiResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','FINANCE')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sales-funnel")
    public ApiResponse<Map<String, Object>> salesFunnel() {
        return ApiResponse.ok(reportService.salesFunnel());
    }

    @GetMapping("/cashflow-aging")
    public ApiResponse<Map<String, Object>> cashflowAging() {
        return ApiResponse.ok(reportService.cashflowAging());
    }

    @GetMapping("/profit")
    public ApiResponse<List<Map<String, Object>>> profit() {
        return ApiResponse.ok(reportService.profit());
    }

    @GetMapping("/sales-funnel/export")
    public ResponseEntity<byte[]> exportSalesFunnel() throws IOException {
        Map<String, Object> funnel = reportService.salesFunnel();
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("sales-funnel");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Metric");
            header.createCell(1).setCellValue("Value");

            writeMetricRow(sheet, 1, "Total Orders", funnel.get("totalOrders"));
            writeMetricRow(sheet, 2, "Pending Approval", funnel.get("pendingApproval"));
            writeMetricRow(sheet, 3, "Approved Orders", funnel.get("approvedOrders"));
            writeMetricRow(sheet, 4, "Completed Orders", funnel.get("completedOrders"));
            writeMetricRow(sheet, 5, "Conversion Rate", formatPercent(funnel.get("conversionRate")));

            workbook.write(out);
            return excelResponse("sales-funnel-report.xlsx", out.toByteArray());
        }
    }

    @GetMapping("/cashflow-aging/export")
    @SuppressWarnings("unchecked")
    public ResponseEntity<byte[]> exportCashflowAging() throws IOException {
        Map<String, Object> aging = reportService.cashflowAging();
        Map<String, Object> receivableAging = (Map<String, Object>) aging.getOrDefault("receivableAging", Map.of());
        Object payableOutstanding = aging.getOrDefault("payableOutstanding", BigDecimal.ZERO);

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("cashflow-aging");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Bucket");
            header.createCell(1).setCellValue("Amount");

            writeMetricRow(sheet, 1, "0-30", receivableAging.getOrDefault("0-30", BigDecimal.ZERO));
            writeMetricRow(sheet, 2, "31-60", receivableAging.getOrDefault("31-60", BigDecimal.ZERO));
            writeMetricRow(sheet, 3, "61-90", receivableAging.getOrDefault("61-90", BigDecimal.ZERO));
            writeMetricRow(sheet, 4, "90+", receivableAging.getOrDefault("90+", BigDecimal.ZERO));
            writeMetricRow(sheet, 5, "Payable Outstanding", payableOutstanding);

            workbook.write(out);
            return excelResponse("cashflow-aging-report.xlsx", out.toByteArray());
        }
    }

    @GetMapping("/profit/export")
    public ResponseEntity<byte[]> exportProfit() throws IOException {
        List<Map<String, Object>> rows = reportService.profit();
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("profit");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Route");
            header.createCell(1).setCellValue("Income");
            header.createCell(2).setCellValue("Cost");
            header.createCell(3).setCellValue("Gross Profit");

            for (int i = 0; i < rows.size(); i++) {
                Map<String, Object> rowData = rows.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(String.valueOf(rowData.get("routeName")));
                row.createCell(1).setCellValue(String.valueOf(rowData.get("income")));
                row.createCell(2).setCellValue(String.valueOf(rowData.get("cost")));
                row.createCell(3).setCellValue(String.valueOf(rowData.get("grossProfit")));
            }

            workbook.write(out);
            return excelResponse("profit-report.xlsx", out.toByteArray());
        }
    }

    private void writeMetricRow(org.apache.poi.ss.usermodel.Sheet sheet, int rowIndex, String metric, Object value) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(metric);
        row.createCell(1).setCellValue(String.valueOf(value));
    }

    private String formatPercent(Object rawValue) {
        if (rawValue instanceof BigDecimal decimal) {
            return decimal.multiply(BigDecimal.valueOf(100)).setScale(2, java.math.RoundingMode.HALF_UP) + "%";
        }
        return String.valueOf(rawValue);
    }

    private ResponseEntity<byte[]> excelResponse(String fileName, byte[] bytes) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(bytes);
    }
}
