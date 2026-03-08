import { request } from '../request';

export function fetchAppealPage(params: { page?: number; size?: number; status?: number }) {
  return request({ url: '/api/appeals/admin', params });
}

export function replyAppeal(id: number, data: { status: number; reply: string }) {
  return request({ url: `/api/appeals/${id}/reply`, method: 'put', data });
}
