import { expect, test } from '@playwright/test';
import { mockLoginToken, setupMockApi } from '../support/mockApi';

test.describe('Customer Management', () => {
  test.beforeEach(async ({ page }) => {
    await mockLoginToken(page);
    await setupMockApi(page, {
      menuPaths: ['/customers', '/dashboard']
    });
  });

  test('should create and edit customer in customer page', async ({ page }) => {
    await page.goto('/customers');
    await expect(page).toHaveURL(/.*customers/);
    await expect(page.getByText('Auto Customer A')).toBeVisible();

    await page.locator('.list-toolbar .toolbar-row .ant-btn-primary').first().click();

    const drawer = page.locator('.ant-drawer-open');
    await expect(drawer).toBeVisible();
    const textInputs = drawer.locator('input.ant-input');
    await textInputs.nth(0).fill('E2E Customer');
    await textInputs.nth(1).fill('13900000002');
    await drawer.locator('.ant-drawer-header .ant-btn-primary').click();

    const createdRow = page.locator('.customer-table .ant-table-row', { hasText: 'E2E Customer' }).first();
    await expect(createdRow).toBeVisible();

    await createdRow.locator('.ant-btn-link').first().click();
    const editDrawer = page.locator('.ant-drawer-open');
    await editDrawer.locator('input.ant-input').nth(0).fill('E2E Customer Updated');
    await editDrawer.locator('.ant-drawer-header .ant-btn-primary').click();

    await expect(page.locator('.customer-table .ant-table-row', { hasText: 'E2E Customer Updated' }).first()).toBeVisible();
  });
});
