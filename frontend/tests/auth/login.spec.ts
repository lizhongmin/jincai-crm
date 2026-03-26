import { test, expect } from '@playwright/test';

test.describe('Authentication', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to login page before each test
    await page.goto('/');
    // The app should redirect to login if not authenticated
  });

  test('should display login form', async ({ page }) => {
    // Check that login form elements are present
    await expect(page.getByText('金财')).toBeVisible({ timeout: 10000 });
    await expect(page.getByPlaceholder('用户名')).toBeVisible({ timeout: 10000 });
    await expect(page.getByPlaceholder('密码')).toBeVisible();
    await expect(page.getByRole('button', { name: '登录' })).toBeVisible();
  });

  test('should login with valid credentials', async ({ page }) => {
    // Fill in login form
    await page.getByPlaceholder('用户名').fill('admin');
    await page.getByPlaceholder('密码').fill('Admin@123');

    // Click login button
    await page.getByRole('button', { name: '登录' }).click();

    // Should redirect to dashboard
    await expect(page).toHaveURL(/.*dashboard/);

    // Should display user info in header
    await expect(page.getByText('admin')).toBeVisible({ timeout: 10000 });
  });

  test('should show error with invalid credentials', async ({ page }) => {
    // Fill in login form with invalid credentials
    await page.getByPlaceholder('用户名').fill('invalid');
    await page.getByPlaceholder('密码').fill('invalid');

    // Click login button
    await page.getByRole('button', { name: '登录' }).click();

    // Should show error message
    await expect(page.getByText('用户名或密码错误')).toBeVisible({ timeout: 10000 });

    // Should remain on login page
    await expect(page).toHaveURL(/.*login/);
  });
});
