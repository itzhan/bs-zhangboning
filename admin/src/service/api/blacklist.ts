import { request } from '../request';

export function fetchBlacklistPage(params: { page?: number; size?: number; keyword?: string }) {
  return request({ url: '/api/blacklist', params });
}

export function createBlacklist(data: any) {
  return request({ url: '/api/blacklist', method: 'post', data });
}

export function updateBlacklist(id: number, data: any) {
  return request({ url: `/api/blacklist/${id}`, method: 'put', data });
}

export function deleteBlacklist(id: number) {
  return request({ url: `/api/blacklist/${id}`, method: 'delete' });
}
