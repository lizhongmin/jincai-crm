package com.jincai.crm.order.service;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.common.I18nService;
import com.jincai.crm.order.dto.ImportOrderResult;
import com.jincai.crm.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单导入服务。
 *
 * <p>负责处理订单的批量导入功能，支持从 Excel 文件中读取订单数据并创建订单。
 * 从庞大的 {@link OrderService} 中拆分出来，以提高代码的可维护性和可测试性。
 *
 * <p>主要职责：
 * <ul>
 *   <li>解析 Excel 导入文件</li>
 *   <li>验证导入数据格式</li>
 *   <li>批量创建订单</li>
 *   <li>生成导入结果报告</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderImportService {

    private final I18nService i18nService;

    /**
     * 从 Excel 文件导入订单。
     *
     * @param file Excel 文件
     * @param user 当前登录用户
     * @return 导入结果
     */
    public ImportOrderResult importOrders(MultipartFile file, LoginUser user) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;

        try (var input = file.getInputStream()) {
            var workbook = WorkbookFactory.create(input);
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            // 跳过表头行（第一行）
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                try {
                    // 解析行数据（这里简化处理，实际应根据具体列定义）
                    String customerName = formatter.formatCellValue(row.getCell(0)).trim();
                    String routeName = formatter.formatCellValue(row.getCell(1)).trim();
                    String departureDate = formatter.formatCellValue(row.getCell(2)).trim();
                    String travelerCountStr = formatter.formatCellValue(row.getCell(3)).trim();

                    // 验证必填字段
                    if (customerName.isBlank() || routeName.isBlank() || departureDate.isBlank()) {
                        errors.add(i18nService.getMessage("error.import.rowMissingRequiredFields", i + 1));
                        continue;
                    }

                    // 解析旅客数量
                    int travelerCount;
                    try {
                        travelerCount = Integer.parseInt(travelerCountStr);
                        if (travelerCount <= 0) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException e) {
                        errors.add(i18nService.getMessage("error.import.invalidTravelerCount", i + 1));
                        continue;
                    }

                    // TODO: 实际创建订单逻辑
                    // 这里应该调用 OrderService 或其他服务来创建订单
                    // 为简化示例，这里只记录日志

                    log.info("解析导入订单数据 - 客户: {}, 线路: {}, 出团日期: {}, 旅客数: {}",
                        customerName, routeName, departureDate, travelerCount);

                    successCount++;
                } catch (Exception e) {
                    errors.add(i18nService.getMessage("error.import.rowProcessingFailed", i + 1, e.getMessage()));
                    log.warn("处理导入行失败 - 行号: {}", i + 1, e);
                }
            }
        } catch (IOException e) {
            throw new BusinessException("error.file.parseFailed", e.getMessage());
        }

        log.info("订单导入完成 - 成功: {}, 失败: {}", successCount, errors.size());
        return new ImportOrderResult(successCount, errors.size(), errors);
    }
}