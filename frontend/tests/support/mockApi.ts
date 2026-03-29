import type { Page, Request, Route } from '@playwright/test';

type PermissionItem = {
  menuPath?: string;
  code?: string;
};

type CustomerItem = {
  id: string;
  name: string;
  phone: string;
  level?: string;
  customerType?: string;
  source?: string;
  intentionLevel?: string;
  status?: string;
  ownerUserId?: string;
  ownerUserName?: string;
  [key: string]: unknown;
};

type OrderItem = {
  id: string;
  orderNo: string;
  customerId: string;
  routeId: string;
  departureId: string;
  status: string;
  contractStatus?: string;
  paymentStatus?: string;
  inventoryStatus?: string;
  settlementStatus?: string;
  totalAmount?: number;
  currency?: string;
  [key: string]: unknown;
};

type MockApiOptions = {
  menuPaths?: string[];
  buttonPermissions?: string[];
  profile?: {
    userId: string;
    username: string;
    roles: string[];
  };
  customers?: CustomerItem[];
  orders?: OrderItem[];
};

export type MockApiState = {
  orderActionLog: string[];
};

const defaultCustomers: CustomerItem[] = [
  {
    id: 'c-1',
    name: 'Auto Customer A',
    phone: '13900000001',
    level: 'B',
    customerType: 'PERSONAL',
    source: 'ONLINE',
    intentionLevel: 'MEDIUM',
    status: 'ACTIVE',
    ownerUserId: 'u-1',
    ownerUserName: 'Admin'
  }
];

const defaultOrders: OrderItem[] = [
  {
    id: 'o-1',
    orderNo: 'SO-001',
    customerId: 'c-1',
    routeId: 'r-1',
    departureId: 'd-1',
    status: 'DRAFT',
    contractStatus: 'PENDING_SIGN',
    paymentStatus: 'UNPAID',
    inventoryStatus: 'UNLOCKED',
    settlementStatus: 'UNSETTLED',
    totalAmount: 1999,
    currency: 'CNY'
  }
];

const buildPermissionItems = (menuPaths: string[], buttonPermissions: string[]): PermissionItem[] => {
  const menuItems = menuPaths.map((menuPath) => ({ menuPath, code: `MENU_${menuPath}` }));
  const buttonItems = buttonPermissions.map((code) => ({ code }));
  return [...menuItems, ...buttonItems];
};

const json = async (route: Route, status: number, body: unknown) => {
  await route.fulfill({
    status,
    contentType: 'application/json',
    body: JSON.stringify(body)
  });
};

const buildCustomerPayload = (payload: Record<string, unknown>, id: string, ownerUserId = 'u-1'): CustomerItem => ({
  id,
  name: String(payload.name || ''),
  phone: String(payload.phone || ''),
  customerType: String(payload.customerType || 'PERSONAL'),
  source: String(payload.source || 'MANUAL'),
  intentionLevel: String(payload.intentionLevel || 'MEDIUM'),
  status: String(payload.status || 'ACTIVE'),
  level: String(payload.level || 'B'),
  ownerUserId: String(payload.ownerUserId || ownerUserId),
  ownerUserName: 'Admin',
  wechat: String(payload.wechat || ''),
  email: String(payload.email || ''),
  city: String(payload.city || ''),
  tags: String(payload.tags || ''),
  remark: String(payload.remark || '')
});

const nextId = (prefix: string, currentSize: number): string => `${prefix}-${currentSize + 1}`;

export const setupMockApi = async (page: Page, options: MockApiOptions = {}): Promise<MockApiState> => {
  const state = {
    profile: options.profile || { userId: 'u-1', username: 'admin', roles: ['ADMIN'] },
    menuPaths: options.menuPaths || ['/dashboard', '/customers', '/orders', '/system/role'],
    buttonPermissions: options.buttonPermissions || [
      'BTN_CUSTOMER_CREATE',
      'BTN_CUSTOMER_EDIT',
      'BTN_CUSTOMER_DELETE',
      'BTN_ORDER_CREATE',
      'BTN_ORDER_SUBMIT',
      'BTN_ORDER_APPROVE',
      'BTN_ORDER_REJECT',
      'BTN_ORDER_EDIT',
      'BTN_ORDER_DELETE'
    ],
    customers: [...(options.customers || defaultCustomers)],
    orders: [...(options.orders || defaultOrders)],
    orderActionLog: [] as string[]
  };

  await page.route(/^https?:\/\/[^/]+\/api\/.*$/, async (route: Route, request: Request) => {
    const url = new URL(request.url());
    const path = url.pathname.replace(/^\/api/, '');
    const method = request.method().toUpperCase();
    const payload = request.postDataJSON?.() || {};

    if (method === 'GET' && path === '/auth/login-state') {
      await json(route, 200, { success: true, data: { captchaRequired: false, locked: false, lockSeconds: 0 } });
      return;
    }

    if (method === 'POST' && path === '/auth/login') {
      const username = String((payload as Record<string, unknown>).username || '');
      const password = String((payload as Record<string, unknown>).password || '');
      if (username === 'admin' && password === 'Admin@123') {
        await json(route, 200, {
          success: true,
          data: {
            token: 'mock-token',
            userId: state.profile.userId,
            username: state.profile.username,
            fullName: 'Admin',
            roles: state.profile.roles
          }
        });
        return;
      }
      await json(route, 401, { success: false, message: 'Invalid username or password' });
      return;
    }

    if (method === 'GET' && path === '/auth/me') {
      await json(route, 200, {
        success: true,
        data: {
          userId: state.profile.userId,
          username: state.profile.username,
          roles: state.profile.roles
        }
      });
      return;
    }

    if (method === 'GET' && path === '/permissions/menus') {
      await json(route, 200, {
        success: true,
        data: buildPermissionItems(state.menuPaths, state.buttonPermissions)
      });
      return;
    }

    if (method === 'GET' && path === '/notifications') {
      await json(route, 200, { success: true, data: [] });
      return;
    }

    if (method === 'GET' && path === '/customers/page') {
      await json(route, 200, {
        success: true,
        data: {
          items: state.customers,
          total: state.customers.length,
          page: 1,
          size: 10
        }
      });
      return;
    }

    if (method === 'GET' && path === '/customers/owner-options') {
      await json(route, 200, {
        success: true,
        data: [{ id: 'u-1', username: 'admin', fullName: 'Admin' }]
      });
      return;
    }

    if (method === 'GET' && path === '/travelers') {
      await json(route, 200, { success: true, data: [] });
      return;
    }

    if (method === 'POST' && path === '/customers') {
      const customerId = nextId('c', state.customers.length);
      const customer = buildCustomerPayload(payload as Record<string, unknown>, customerId);
      state.customers.unshift(customer);
      await json(route, 200, { success: true, data: customer });
      return;
    }

    if (method === 'PUT' && path.startsWith('/customers/')) {
      const customerId = path.split('/').at(-1);
      const index = state.customers.findIndex((item) => item.id === customerId);
      if (index >= 0) {
        state.customers[index] = buildCustomerPayload(
          { ...state.customers[index], ...(payload as Record<string, unknown>) },
          state.customers[index].id
        );
      }
      await json(route, 200, { success: true, data: state.customers[index] || null });
      return;
    }

    if (method === 'GET' && path === '/orders/page') {
      await json(route, 200, {
        success: true,
        data: {
          items: state.orders,
          total: state.orders.length,
          page: 1,
          size: 10
        }
      });
      return;
    }

    if (method === 'GET' && path === '/orders/context-options') {
      await json(route, 200, {
        success: true,
        data: {
          customers: [{ id: 'c-1', name: 'Auto Customer A', phone: '13900000001' }],
          routes: [{ id: 'r-1', name: 'Demo Route A', category: 'DOMESTIC' }]
        }
      });
      return;
    }

    if (method === 'GET' && path.startsWith('/orders/context-options/routes/')) {
      await json(route, 200, {
        success: true,
        data: [{ id: 'd-1', routeId: 'r-1', name: 'Batch-1', startDate: '2026-04-01', endDate: '2026-04-05' }]
      });
      return;
    }

    if (method === 'GET' && path.startsWith('/orders/context-options/customers/')) {
      await json(route, 200, {
        success: true,
        data: [{ id: 't-1', name: 'Traveler One', phone: '13900000009', birthday: '1990-01-01' }]
      });
      return;
    }

    if (method === 'GET' && path.startsWith('/orders/context-options/departures/')) {
      await json(route, 200, {
        success: true,
        data: [{ id: 'p-1', priceType: 'ADULT', priceLabel: 'Adult Price', price: 1999, currency: 'CNY' }]
      });
      return;
    }

    if (method === 'POST' && path === '/orders/quote') {
      await json(route, 200, {
        success: true,
        data: {
          travelerCount: 1,
          totalAmount: 1999,
          currency: 'CNY',
          priceItems: [
            {
              id: 'qi-1',
              travelerId: 't-1',
              travelerName: 'Traveler One',
              itemName: 'Adult Price',
              unitPrice: 1999,
              quantity: 1,
              amount: 1999,
              currency: 'CNY'
            }
          ]
        }
      });
      return;
    }

    if (method === 'POST' && path === '/orders') {
      const orderId = nextId('o', state.orders.length);
      const body = payload as Record<string, unknown>;
      const newOrder: OrderItem = {
        id: orderId,
        orderNo: String(body.orderNo || `SO-${String(state.orders.length + 1).padStart(3, '0')}`),
        customerId: String(body.customerId || 'c-1'),
        routeId: String(body.routeId || 'r-1'),
        departureId: String(body.departureId || 'd-1'),
        status: 'DRAFT',
        contractStatus: 'PENDING_SIGN',
        paymentStatus: 'UNPAID',
        inventoryStatus: 'UNLOCKED',
        settlementStatus: 'UNSETTLED',
        totalAmount: Number(body.totalAmount || 1999),
        currency: String(body.currency || 'CNY')
      };
      state.orders.unshift(newOrder);
      await json(route, 200, { success: true, data: newOrder });
      return;
    }

    if (method === 'POST' && path.startsWith('/orders/') && path.endsWith('/actions')) {
      const segments = path.split('/');
      const orderId = segments[2];
      const body = payload as Record<string, unknown>;
      const action = String(body.action || '');
      const order = state.orders.find((item) => item.id === orderId);
      if (order) {
        state.orderActionLog.push(action);
        if (action === 'SUBMIT' || action === 'RESUBMIT') order.status = 'PENDING_APPROVAL';
        if (action === 'APPROVE') order.status = 'APPROVED';
        if (action === 'REJECT') order.status = 'REJECTED';
        if (action === 'MARK_IN_TRAVEL') order.status = 'IN_TRAVEL';
        if (action === 'MARK_TRAVEL_FINISHED') order.status = 'TRAVEL_FINISHED';
        if (action === 'CANCEL') order.status = 'CANCELED';
      }
      await json(route, 200, { success: true, data: order || null });
      return;
    }

    await json(route, 404, { success: false, message: `Unhandled mock API: ${method} ${path}` });
  });

  return { orderActionLog: state.orderActionLog };
};

export const mockLoginToken = async (page: Page): Promise<void> => {
  await page.addInitScript(() => {
    window.localStorage.setItem('crm_token', 'mock-token');
  });
};
