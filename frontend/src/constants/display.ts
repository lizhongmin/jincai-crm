export const DEPARTURE_STATUS_LABEL_MAP: Record<string, string> = {
  OPEN: '报名中',
  CLOSED: '已关闭',
  FULL: '已满团'
};

export const DEPARTURE_STATUS_OPTIONS = Object.entries(DEPARTURE_STATUS_LABEL_MAP).map(([value, label]) => ({
  value,
  label
}));

export const PRICE_TYPE_LABEL_MAP: Record<string, string> = {
  ADULT: '成人价',
  CHILD: '儿童价',
  INFANT: '婴儿价',
  SINGLE_ROOM: '单房差',
  EXTRA: '附加项'
};

export const PRICE_TYPE_OPTIONS = Object.entries(PRICE_TYPE_LABEL_MAP).map(([value, label]) => ({
  value,
  label
}));

export const ORDER_TYPE_LABEL_MAP: Record<string, string> = {
  GROUP: '跟团游',
  CUSTOM: '定制团'
};

export const ORDER_STATUS_LABEL_MAP: Record<string, string> = {
  DRAFT: '草稿',
  PENDING_APPROVAL: '待审批',
  APPROVED: '已审批',
  REJECTED: '已驳回',
  IN_TRAVEL: '履约中',
  TRAVEL_FINISHED: '已回团',
  SETTLING: '结算中',
  COMPLETED: '已完结',
  CANCELED: '已取消'
};

export const CONTRACT_STATUS_LABEL_MAP: Record<string, string> = {
  NOT_REQUIRED: '无需签约',
  PENDING_SIGN: '待签约',
  SIGNED: '已签约'
};

export const PAYMENT_STATUS_LABEL_MAP: Record<string, string> = {
  UNPAID: '未收款',
  PARTIAL: '部分收款',
  PAID: '已收清',
  REFUNDING: '退款中',
  REFUNDED: '已退款'
};

export const INVENTORY_STATUS_LABEL_MAP: Record<string, string> = {
  UNLOCKED: '未锁位',
  LOCKED: '已锁位',
  RELEASED: '已释放'
};

export const SETTLEMENT_STATUS_LABEL_MAP: Record<string, string> = {
  UNSETTLED: '未结算',
  PARTIAL: '部分结算',
  SETTLED: '已结算'
};

export const CURRENCY_LABEL_MAP: Record<string, string> = {
  CNY: '人民币 (CNY)'
};

export const LOCK_POLICY_LABEL_MAP: Record<string, string> = {
  ON_APPROVAL: '审批通过锁位',
  ON_DEPOSIT: '定金到账锁位',
  MANUAL: '手工锁位'
};

export const PAYMENT_POLICY_LABEL_MAP: Record<string, string> = {
  FULL: '一次性全款',
  DEPOSIT_BALANCE: '定金+尾款'
};

export const DEPOSIT_TYPE_LABEL_MAP: Record<string, string> = {
  PERCENT: '比例(%)',
  FIXED: '固定金额'
};

export const enumLabel = (mapping: Record<string, string>, value?: string | null, fallback = '-') => {
  if (!value) {
    return fallback;
  }
  return mapping[value] || value;
};

