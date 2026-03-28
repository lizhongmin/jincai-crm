package com.jincai.crm.report.controller;

import com.jincai.crm.common.ApiResponse;
import com.jincai.crm.report.service.ReportService;
import com.jincai.crm.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@Slf4j
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sales-funnel")
    @PreAuthorize("hasAuthority('MENU_REPORT')")
    public ApiResponse<Map<String, Object>> salesFunnel() {
        log.debug("ReportController.salesFunnel() called by user: {}", SecurityUtils.currentUserId());
        try {
            Map<String, Object> result = reportService.salesFunnel();
            log.debug("ReportController.salesFunnel() succeeded for user: {}", SecurityUtils.currentUserId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("ReportController.salesFunnel() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @GetMapping("/cashflow-aging")
    @PreAuthorize("hasAuthority('MENU_REPORT')")
    public ApiResponse<Map<String, Object>> cashflowAging() {
        log.debug("ReportController.cashflowAging() called by user: {}", SecurityUtils.currentUserId());
        try {
            Map<String, Object> result = reportService.cashflowAging();
            log.debug("ReportController.cashflowAging() succeeded for user: {}", SecurityUtils.currentUserId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("ReportController.cashflowAging() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @GetMapping("/profit")
    @PreAuthorize("hasAuthority('MENU_REPORT')")
    public ApiResponse<List<Map<String, Object>>> profit() {
        log.debug("ReportController.profit() called by user: {}", SecurityUtils.currentUserId());
        try {
            List<Map<String, Object>> result = reportService.profit();
            log.debug("ReportController.profit() succeeded for user: {}", SecurityUtils.currentUserId());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("ReportController.profit() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @GetMapping("/sales-funnel/export")
    @PreAuthorize("hasAuthority('BTN_REPORT_EXPORT')")
    public ResponseEntity<byte[]> exportSalesFunnel() throws IOException {
        log.info("ReportController.exportSalesFunnel() called by user: {}", SecurityUtils.currentUserId());
        try {
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
                log.info("ReportController.exportSalesFunnel() succeeded for user: {}", SecurityUtils.currentUserId());
                return excelResponse("sales-funnel-report.xlsx", out.toByteArray());
            }
        } catch (Exception e) {
            log.error("ReportController.exportSalesFunnel() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @GetMapping("/cashflow-aging/export")
    @SuppressWarnings("unchecked")
    @PreAuthorize("hasAuthority('BTN_REPORT_EXPORT')")
    public ResponseEntity<byte[]> exportCashflowAging() throws IOException {
        log.info("ReportController.exportCashflowAging() called by user: {}", SecurityUtils.currentUserId());
        try {
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
                log.info("ReportController.exportCashflowAging() succeeded for user: {}", SecurityUtils.currentUserId());
                return excelResponse("cashflow-aging-report.xlsx", out.toByteArray());
            }
        } catch (Exception e) {
            log.error("ReportController.exportCashflowAging() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
        }
    }

    @GetMapping("/profit/export")
    @PreAuthorize("hasAuthority('BTN_REPORT_EXPORT')")
    public ResponseEntity<byte[]> exportProfit() throws IOException {
        log.info("ReportController.exportProfit() called by user: {}", SecurityUtils.currentUserId());
        try {
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
                log.info("ReportController.exportProfit() succeeded for user: {}", SecurityUtils.currentUserId());
                return excelResponse("profit-report.xlsx", out.toByteArray());
            }
        } catch (Exception e) {
            log.error("ReportController.exportProfit() failed for user: {}", SecurityUtils.currentUserId(), e);
            throw e;
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
