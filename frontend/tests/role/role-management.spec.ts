import { expect, test } from '@playwright/test';
import { mockLoginToken, setupMockApi } from '../support/mockApi';

test.describe('Permission Guard', () => {
  test('should redirect to first allowed page when current route has no permission', async ({ page }) => {
    await mockLoginToken(page);
    await setupMockApi(page, {
      menuPaths: ['/customers']
    });

    await page.goto('/orders');

    await expect(page).toHaveURL(/.*customers/);
  });
});
