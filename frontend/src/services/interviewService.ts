import axios from 'axios';
import authService from './authService';

export interface Interview {
  id: number;
  candidateId: number;
  candidateName: string;
  position: string;
  date: string;
  status: string;
  type: string;
  interviewerId?: number;
  interviewerName: string;
  duration: number;
}

export interface InterviewFormValues {
  candidateId: string;
  position: string;
  date: string;
  time: string;
  type: string;
  interviewerId: string;
  status: string;
}

export const getInterviews = async (): Promise<Interview[] | any> => {
  const response = await axios.get('/api/interviews', { headers: authService.getAuthHeaders() });
  return response.data;
};

export const getInterviewsByInterviewer = async (interviewerId: number): Promise<Interview[] | any> => {
  const response = await axios.get(`/api/interviews/interviewer/${interviewerId}`, { headers: authService.getAuthHeaders() });
  return response.data;
};

export const createInterview = async (data: any): Promise<Interview> => {
  const response = await axios.post('/api/interviews', data, { headers: authService.getAuthHeaders() });
  return response.data;
};

export const updateInterview = async (id: number, data: any): Promise<Interview> => {
  const response = await axios.put(`/api/interviews/${id}`, data, { headers: authService.getAuthHeaders() });
  return response.data;
};

export const deleteInterview = async (id: number): Promise<void> => {
  await axios.delete(`/api/interviews/${id}`, { headers: authService.getAuthHeaders() });
}; 