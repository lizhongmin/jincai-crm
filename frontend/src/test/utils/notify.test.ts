import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('ant-design-vue', () => ({
  message: {
    error: vi.fn(),
    success: vi.fn()
  }
}));

import { message } from 'ant-design-vue';
import { notifyError, notifySuccess } from '../../utils/notify';

describe('notify utils', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('uses fallback message when error payload is empty', () => {
    notifyError(undefined, 'fallback-error');
    expect(message.error).toHaveBeenCalledWith('fallback-error');
  });

  it('formats field validation details from response payload', () => {
    notifyError({
      response: {
        data: {
          message: 'Validation failed',
          data: {
            name: 'name is required',
            phone: 'phone is required'
          }
        }
      }
    });

    expect(message.error).toHaveBeenCalledWith(
      'Validation failed (name: name is required, phone: phone is required)'
    );
  });

  it('forwards success message', () => {
    notifySuccess('saved');
    expect(message.success).toHaveBeenCalledWith('saved');
  });
});
