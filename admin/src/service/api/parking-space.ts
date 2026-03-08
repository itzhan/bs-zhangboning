import { request } from '../request';

export function fetchParkingSpacePage(params: {
  page?: number;
  size?: number;
  lotId?: number;
  status?: number;
  type?: number;
}) {
  return request({ url: '/api/parking-spaces', params });
}

export function fetchParkingSpaceDetail(id: number) {
  return request({ url: `/api/parking-spaces/${id}` });
}

export function fetchAvailableParkingSpaces(lotId: number) {
  return request({ url: `/api/parking-spaces/available/${lotId}` });
}

export function createParkingSpace(data: any) {
  return request({ url: '/api/parking-spaces', method: 'post', data });
}

export function createParkingSpacesBatch(data: {
  lotId: number;
  prefix: string;
  count: number;
  type: number;
  floor?: number;
  area?: number;
}) {
  return request({ url: '/api/parking-spaces/batch', method: 'post', data });
}

export function updateParkingSpace(id: number, data: any) {
  return request({ url: `/api/parking-spaces/${id}`, method: 'put', data });
}

export function deleteParkingSpace(id: number) {
  return request({ url: `/api/parking-spaces/${id}`, method: 'delete' });
}
