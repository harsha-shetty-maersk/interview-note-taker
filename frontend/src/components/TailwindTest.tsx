import React from 'react';

const TailwindTest: React.FC = () => {
  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center">
      <div className="bg-white p-8 rounded-lg shadow-lg max-w-md w-full">
        <h1 className="text-3xl font-bold text-gray-900 mb-4 text-center">
          Tailwind CSS Test
        </h1>
        <p className="text-gray-600 mb-6 text-center">
          If you can see this styled component, Tailwind CSS is working correctly!
        </p>
        <div className="space-y-4">
          <button className="w-full bg-blue-500 hover:bg-blue-600 text-white font-medium py-2 px-4 rounded-md transition-colors duration-200">
            Primary Button
          </button>
          <button className="w-full bg-gray-200 hover:bg-gray-300 text-gray-800 font-medium py-2 px-4 rounded-md transition-colors duration-200">
            Secondary Button
          </button>
          <button className="w-full bg-red-500 hover:bg-red-600 text-white font-medium py-2 px-4 rounded-md transition-colors duration-200">
            Danger Button
          </button>
        </div>
        <div className="mt-6 p-4 bg-green-50 border border-green-200 rounded-md">
          <p className="text-green-800 text-sm">
            ✅ Tailwind CSS is successfully integrated!
          </p>
        </div>
      </div>
    </div>
  );
};

export default TailwindTest; 