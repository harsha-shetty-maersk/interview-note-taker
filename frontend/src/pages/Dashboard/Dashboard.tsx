import React, { useEffect, useState } from 'react';
import { getCandidates } from '../../services/candidateService';
import { getInterviews, getInterviewsByInterviewer } from '../../services/interviewService';
import { useAuth } from '../../context/AuthContext';

const Dashboard: React.FC = () => {
  const [totalCandidates, setTotalCandidates] = useState(0);
  const [scheduledInterviews, setScheduledInterviews] = useState(0);
  const [completedInterviews, setCompletedInterviews] = useState(0);
  const [pendingInterviews, setPendingInterviews] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { user } = useAuth();

  useEffect(() => {
    const fetchStats = async () => {
      setLoading(true);
      setError(null);
      try {
        // Fetch candidates (only for admins)
        if (user?.role === 'ADMIN') {
          const candidateData = await getCandidates();
          const candidateList = Array.isArray(candidateData.content) ? candidateData.content : Array.isArray(candidateData) ? candidateData : [];
          setTotalCandidates(candidateList.length);
        } else {
          setTotalCandidates(0); // Interviewers don't see candidate count
        }

        // Fetch interviews based on role
        let interviewData;
        if (user?.role === 'INTERVIEWER') {
          interviewData = await getInterviewsByInterviewer(user.id);
        } else {
          interviewData = await getInterviews();
        }
        const interviewList = Array.isArray(interviewData.content) ? interviewData.content : Array.isArray(interviewData) ? interviewData : [];
        setScheduledInterviews(interviewList.filter((i: any) => i.status === 'SCHEDULED').length);
        setCompletedInterviews(interviewList.filter((i: any) => i.status === 'COMPLETED').length);
        setPendingInterviews(interviewList.filter((i: any) => i.status === 'IN_PROGRESS').length);
      } catch (err) {
        setError('Failed to load dashboard stats.');
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, [user]);

  const stats = [
    ...(user?.role === 'ADMIN' ? [{ label: 'Total Candidates', value: totalCandidates }] : []),
    { label: 'Scheduled Interviews', value: scheduledInterviews },
    { label: 'Completed Interviews', value: completedInterviews },
    { label: 'Pending Interviews', value: pendingInterviews },
  ];

  return (
    <div className="pt-8">
      <h1 className="text-3xl font-bold mb-6">Dashboard</h1>
      {loading && <div className="text-gray-500">Loading...</div>}
      {error && <div className="text-red-600 mb-2">{error}</div>}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {stats.map((stat) => (
          <div key={stat.label} className="bg-white rounded-lg shadow-md p-6 flex flex-col items-start">
            <div className="text-lg font-semibold mb-2">{stat.label}</div>
            <div className="text-3xl font-bold text-blue-600">{stat.value}</div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Dashboard; 