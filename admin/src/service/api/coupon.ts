import { request } from '../request';

export function fetchCouponPage(params: { page?: number; size?: number }) {
  return request({ url: '/api/coupons', params });
}

export function fetchAvailableCoupons() {
  return request({ url: '/api/coupons/available' });
}

export function createCoupon(data: any) {
  return request({ url: '/api/coupons', method: 'post', data });
}

export function updateCoupon(id: number, data: any) {
  return request({ url: `/api/coupons/${id}`, method: 'put', data });
}

export function deleteCoupon(id: number) {
  return request({ url: `/api/coupons/${id}`, method: 'delete' });
}
