import { request } from '../request';

export function fetchPaymentPage(params: { page?: number; size?: number }) {
  return request({ url: '/api/payments/admin', params });
}

export function fetchPaymentByOrder(orderId: number) {
  return request({ url: `/api/payments/order/${orderId}` });
}
