import React from 'react';
import AppRoutes from './routes/AppRoutes';

/**
 * App component chính của cinema-frontend.
 * Chỉ chịu trách nhiệm kết xuất AppRoutes, không chứa logic gọi API.
 */
export default function App() {
  return <AppRoutes />;
}
