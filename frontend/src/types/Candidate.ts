export interface Candidate {
  id: number;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  phone: string;
  position?: string;
  experience?: number;
  source?: string;
  notes?: string;
  status: string;
  createdAt?: string;
  updatedAt?: string;
  interviews?: any[];
} 