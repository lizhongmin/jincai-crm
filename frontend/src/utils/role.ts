export const hasAnyRole = (roles: string[] | undefined, expectedRoles: string[]) => {
  const currentRoles = (roles || []).map((role) => String(role).toUpperCase());
  return expectedRoles.some((role) => currentRoles.includes(String(role).toUpperCase()));
};

export const canOrderReviewByRole = (roles: string[] | undefined) =>
  hasAnyRole(roles, ['ADMIN', 'SALES_MANAGER', 'FINANCE']);

export const canFinanceReviewByRole = (roles: string[] | undefined) =>
  hasAnyRole(roles, ['ADMIN', 'FINANCE']);
