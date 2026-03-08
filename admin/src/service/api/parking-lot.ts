import { request } from '../request';

export function fetchParkingLotPage(params: { page?: number; size?: number; keyword?: string; status?: number }) {
  return request({ url: '/api/parking-lots', params });
}

export function fetchAllParkingLots() {
  return request({ url: '/api/parking-lots/all' });
}

export function fetchParkingLotDetail(id: number) {
  return request({ url: `/api/parking-lots/${id}` });
}

export function createParkingLot(data: any) {
  return request({ url: '/api/parking-lots', method: 'post', data });
}

export function updateParkingLot(id: number, data: any) {
  return request({ url: `/api/parking-lots/${id}`, method: 'put', data });
}

export function deleteParkingLot(id: number) {
  return request({ url: `/api/parking-lots/${id}`, method: 'delete' });
}
