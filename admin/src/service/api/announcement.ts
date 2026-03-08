import { request } from '../request';

export function fetchAnnouncementPage(params: {
  page?: number;
  size?: number;
  type?: number;
  status?: number;
}) {
  return request({ url: '/api/announcements', params });
}

export function fetchAnnouncementDetail(id: number) {
  return request({ url: `/api/announcements/${id}` });
}

export function createAnnouncement(data: any) {
  return request({ url: '/api/announcements', method: 'post', data });
}

export function updateAnnouncement(id: number, data: any) {
  return request({ url: `/api/announcements/${id}`, method: 'put', data });
}

export function publishAnnouncement(id: number) {
  return request({ url: `/api/announcements/${id}/publish`, method: 'put' });
}

export function deleteAnnouncement(id: number) {
  return request({ url: `/api/announcements/${id}`, method: 'delete' });
}
