import { request } from '../request';

export function fetchOrderPage(params: {
  page?: number;
  size?: number;
  status?: number;
  lotId?: number;
  keyword?: string;
}) {
  return request({ url: '/api/orders/admin', params });
}

export function fetchOrderDetail(id: number) {
  return request({ url: `/api/orders/${id}` });
}

export function createOrderEntry(data: { lotId: number; plateNumber: string }) {
  return request({ url: '/api/orders/entry', method: 'post', data });
}

export function exitOrder(id: number) {
  return request({ url: `/api/orders/${id}/exit`, method: 'post' });
}
