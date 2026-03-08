import { request } from '../request';

export function fetchDashboard() {
  return request({ url: '/api/dashboard' });
}

export function fetchDashboardRevenue(params: { startDate?: string; endDate?: string }) {
  return request({ url: '/api/dashboard/revenue', params });
}

export function fetchDashboardOccupancy() {
  return request({ url: '/api/dashboard/occupancy' });
}
