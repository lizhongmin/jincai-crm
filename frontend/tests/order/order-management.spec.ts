import { expect, test } from '@playwright/test';
import { mockLoginToken, setupMockApi } from '../support/mockApi';

test.describe('订单管理流程', () => {
  test.beforeEach(async ({ page }) => {
    await mockLoginToken(page);
  });

  test('应支持订单提交并审批通过的完整流程', async ({ page }) => {
    const state = await setupMockApi(page, {
      menuPaths: ['/orders', '/dashboard']
    });

    const row = page.locator('.ant-table-row', { hasText: 'SO-001' }).first();

    await test.step('打开订单页面并确认目标订单可见', async () => {
      await page.goto('/orders');
      await expect(page).toHaveURL(/.*orders/);
      await expect(row).toBeVisible();
    });

    await test.step('执行提交操作并确认动作日志更新', async () => {
      await row.locator('.ant-btn-primary').click();
      await page.locator('.ant-drawer-open .ant-btn-primary').click();
      await expect.poll(() => state.orderActionLog.length).toBe(1);
    });

    await test.step('执行审批操作并确认动作日志更新', async () => {
      await row.locator('.ant-btn-primary').click();
      await page.locator('.ant-drawer-open .ant-btn-primary').click();
      await expect.poll(() => state.orderActionLog.length).toBe(2);
    });

    expect(state.orderActionLog).toEqual(['SUBMIT', 'APPROVE']);
  });
});
