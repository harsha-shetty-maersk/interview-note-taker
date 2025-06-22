import axios from 'axios';
import { Candidate } from '../types/Candidate';
import { CandidateFormValues } from '../types/CandidateFormValues';
import authService from './authService';

export const getCandidates = async (): Promise<Candidate[] | any> => {
  const response = await axios.get<Candidate[]>('/api/candidates', { headers: authService.getAuthHeaders() });
  console.log('API /candidates response:', response.data);
  return response.data;
};

export const getCandidateById = async (id: number): Promise<Candidate> => {
  const response = await axios.get<Candidate>(`/api/candidates/${id}`, { headers: authService.getAuthHeaders() });
  return response.data;
};

export const createCandidate = async (data: CandidateFormValues): Promise<Candidate> => {
  const response = await axios.post<Candidate>('/api/candidates', data, { headers: authService.getAuthHeaders() });
  return response.data;
};

export const updateCandidate = async (id: number, data: CandidateFormValues): Promise<Candidate> => {
  const response = await axios.put<Candidate>(`/api/candidates/${id}`, data, { headers: authService.getAuthHeaders() });
  return response.data;
};

export const deleteCandidate = async (id: number): Promise<void> => {
  await axios.delete(`/api/candidates/${id}`, { headers: authService.getAuthHeaders() });
};