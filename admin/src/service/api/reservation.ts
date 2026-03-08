import { request } from '../request';

export function fetchReservationPage(params: {
  page?: number;
  size?: number;
  status?: number;
  lotId?: number;
}) {
  return request({ url: '/api/reservations/admin', params });
}

export function fetchReservationDetail(id: number) {
  return request({ url: `/api/reservations/${id}` });
}

export function cancelReservation(id: number) {
  return request({ url: `/api/reservations/${id}/cancel`, method: 'put' });
}
