import { describe, expect, it } from 'vitest';
import { canFinanceReviewByRole, canOrderReviewByRole, hasAnyRole } from '../../utils/role';

describe('role utils', () => {
  it('checks role membership case-insensitively', () => {
    expect(hasAnyRole(['sales_manager'], ['SALES_MANAGER'])).toBe(true);
    expect(hasAnyRole(['finance'], ['ADMIN', 'SALES_MANAGER'])).toBe(false);
  });

  it('evaluates order review role policy', () => {
    expect(canOrderReviewByRole(['ADMIN'])).toBe(true);
    expect(canOrderReviewByRole(['sales_manager'])).toBe(true);
    expect(canOrderReviewByRole(['FINANCE'])).toBe(true);
    expect(canOrderReviewByRole(['SALE'])).toBe(false);
  });

  it('evaluates finance review role policy', () => {
    expect(canFinanceReviewByRole(['ADMIN'])).toBe(true);
    expect(canFinanceReviewByRole(['FINANCE'])).toBe(true);
    expect(canFinanceReviewByRole(['SALES_MANAGER'])).toBe(false);
  });
});
