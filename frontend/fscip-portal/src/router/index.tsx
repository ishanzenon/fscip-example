import { createBrowserRouter, Navigate } from 'react-router-dom';
import { Layout } from '../components/Layout';
import { AuthGuard } from '../guards/AuthGuard';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
      {
        index: true,
        element: <Navigate to="/dashboard" replace />,
      },
      {
        path: 'login',
        lazy: () => import('../pages/auth/LoginPage'),
      },
      {
        path: 'register',
        lazy: () => import('../pages/auth/RegisterPage'),
      },
      {
        path: 'dashboard',
        element: <AuthGuard />,
        children: [
          {
            index: true,
            lazy: () => import('../pages/DashboardPage'),
          },
        ],
      },
      {
        path: 'accounts',
        element: <AuthGuard />,
        children: [
          {
            index: true,
            lazy: () => import('../pages/accounts/AccountsPage'),
          },
          {
            path: ':accountId',
            lazy: () => import('../pages/accounts/AccountDetailPage'),
          },
        ],
      },
      {
        path: 'transactions',
        element: <AuthGuard />,
        children: [
          {
            index: true,
            lazy: () => import('../pages/transactions/TransactionsPage'),
          },
        ],
      },
      {
        path: 'applications',
        element: <AuthGuard />,
        children: [
          {
            index: true,
            lazy: () => import('../pages/applications/ApplicationsPage'),
          },
          {
            path: 'new',
            lazy: () => import('../pages/applications/NewApplicationPage'),
          },
        ],
      },
      {
        path: 'support',
        element: <AuthGuard />,
        children: [
          {
            index: true,
            lazy: () => import('../pages/support/SupportPage'),
          },
        ],
      },
      {
        path: '*',
        lazy: () => import('../pages/NotFoundPage'),
      },
    ],
  },
]);