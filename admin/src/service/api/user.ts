import { request } from '../request';

export function fetchUserPage(params: {
  page?: number;
  size?: number;
  keyword?: string;
  role?: string;
}) {
  return request({ url: '/api/users', params });
}

export function fetchUserDetail(id: number) {
  return request({ url: `/api/users/${id}` });
}

export function updateUser(id: number, data: any) {
  return request({ url: `/api/users/${id}`, method: 'put', data });
}

export function updateUserStatus(id: number, data: { status: number }) {
  return request({ url: `/api/users/${id}/status`, method: 'put', data });
}

export function deleteUser(id: number) {
  return request({ url: `/api/users/${id}`, method: 'delete' });
}
