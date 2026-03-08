import { request } from '../request';

export function fetchVehicles() {
  return request({ url: '/api/vehicles' });
}

export function createVehicle(data: any) {
  return request({ url: '/api/vehicles', method: 'post', data });
}

export function updateVehicle(id: number, data: any) {
  return request({ url: `/api/vehicles/${id}`, method: 'put', data });
}

export function deleteVehicle(id: number) {
  return request({ url: `/api/vehicles/${id}`, method: 'delete' });
}
