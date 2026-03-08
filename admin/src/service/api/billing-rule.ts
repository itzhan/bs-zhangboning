import { request } from '../request';

export function fetchBillingRulePage(params: { page?: number; size?: number }) {
  return request({ url: '/api/billing-rules', params });
}

export function fetchBillingRuleDetail(id: number) {
  return request({ url: `/api/billing-rules/${id}` });
}

export function fetchBillingRulesByLot(lotId: number) {
  return request({ url: `/api/billing-rules/lot/${lotId}` });
}

export function createBillingRule(data: any) {
  return request({ url: '/api/billing-rules', method: 'post', data });
}

export function updateBillingRule(id: number, data: any) {
  return request({ url: `/api/billing-rules/${id}`, method: 'put', data });
}

export function deleteBillingRule(id: number) {
  return request({ url: `/api/billing-rules/${id}`, method: 'delete' });
}
