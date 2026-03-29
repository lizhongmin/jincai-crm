import { expect, test } from '@playwright/test';
import { setupMockApi } from '../support/mockApi';

test.describe('Authentication', () => {
  test.beforeEach(async ({ page }) => {
    await setupMockApi(page);
    await page.goto('/login');
  });

  test('should display login form', async ({ page }) => {
    await expect(page.locator('#form_item_username')).toBeVisible({ timeout: 10000 });
    await expect(page.locator('#form_item_password')).toBeVisible();
    await expect(page.locator('button.submit-btn')).toBeVisible();
  });

  test('should login with valid credentials', async ({ page }) => {
    await page.locator('#form_item_username').fill('admin');
    await page.locator('#form_item_password').fill('Admin@123');
    await page.locator('button.submit-btn').click();

    await expect(page).toHaveURL(/.*dashboard/);
    await expect(page.getByText('admin')).toBeVisible({ timeout: 10000 });
  });

  test('should stay on login page with invalid credentials', async ({ page }) => {
    await page.locator('#form_item_username').fill('invalid');
    await page.locator('#form_item_password').fill('invalid');
    await page.locator('button.submit-btn').click();

    await expect(page).toHaveURL(/.*login/);
  });
});
