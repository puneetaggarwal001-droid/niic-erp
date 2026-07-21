import type { ReactNode } from 'react';
import { Link, Navigate, Route, Routes } from 'react-router-dom';
import { RequireAuth } from './components/RequireAuth';
import { useAuth } from './context/AuthContext';
import AttendancePage from './pages/AttendancePage';
import GatePassPage from './pages/GatePassPage';
import EmployeesPage from './pages/EmployeesPage';
import UsersPage from './pages/UsersPage';
import EprPage from './pages/EprPage';
import PayrollPage from './pages/PayrollPage';
import LoginPage from './pages/LoginPage';
import StorePage from './pages/store/StorePage';
import MastersPage from './pages/production/MastersPage';
import JobsPage from './pages/production/JobsPage';
import RoutingPage from './pages/production/RoutingPage';
import ProductionEntryPage from './pages/production/ProductionEntryPage';
import OperationClosuresPage from './pages/production/OperationClosuresPage';
import QcPage from './pages/production/QcPage';
import TransferChallansPage from './pages/production/TransferChallansPage';

function AppLayout({ children }: { children: ReactNode }) {
  const { auth, logout } = useAuth();
  const isAdmin = auth?.role === 'ADMIN';
  return (
    <div style={{ fontFamily: 'sans-serif' }}>
      <header
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          padding: '12px 24px',
          background: '#1B2E72',
          color: '#fff',
        }}
      >
        <nav style={{ display: 'flex', gap: 16, flexWrap: 'wrap' }}>
          <Link to="/employees" style={{ color: '#fff' }}>
            Employees
          </Link>
          <Link to="/attendance" style={{ color: '#fff' }}>
            Attendance
          </Link>
          <Link to="/gate-passes" style={{ color: '#fff' }}>
            Gate Pass
          </Link>
          <Link to="/production/jobs" style={{ color: '#fff' }}>
            Jobs
          </Link>
          <Link to="/production/routing" style={{ color: '#fff' }}>
            Routing
          </Link>
          <Link to="/production/entries" style={{ color: '#fff' }}>
            Production Entries
          </Link>
          <Link to="/production/qc" style={{ color: '#fff' }}>
            QC
          </Link>
          <Link to="/production/closures" style={{ color: '#fff' }}>
            Closures
          </Link>
          <Link to="/production/transfers" style={{ color: '#fff' }}>
            Transfers
          </Link>
          <Link to="/store" style={{ color: '#fff' }}>
            Store
          </Link>
          <Link to="/production/epr" style={{ color: '#fff' }}>
            EPR
          </Link>
          <Link to="/production/masters" style={{ color: '#fff' }}>
            Masters
          </Link>
          {isAdmin && (
            <Link to="/payroll" style={{ color: '#fff' }}>
              Payroll
            </Link>
          )}
          {isAdmin && (
            <Link to="/users" style={{ color: '#fff' }}>
              Users
            </Link>
          )}
        </nav>
        <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
          {auth && <span>{auth.username}</span>}
          <button onClick={logout}>Log out</button>
        </div>
      </header>
      <main style={{ padding: 24 }}>{children}</main>
    </div>
  );
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/employees"
        element={
          <RequireAuth>
            <AppLayout>
              <EmployeesPage />
            </AppLayout>
          </RequireAuth>
        }
      />
      <Route
        path="/attendance"
        element={
          <RequireAuth>
            <AppLayout>
              <AttendancePage />
            </AppLayout>
          </RequireAuth>
        }
      />
      <Route
        path="/gate-passes"
        element={
          <RequireAuth>
            <AppLayout>
              <GatePassPage />
            </AppLayout>
          </RequireAuth>
        }
      />
      <Route
        path="/production/masters"
        element={
          <RequireAuth>
            <AppLayout>
              <MastersPage />
            </AppLayout>
          </RequireAuth>
        }
      />
      <Route
        path="/production/jobs"
        element={
          <RequireAuth>
            <AppLayout>
              <JobsPage />
            </AppLayout>
          </RequireAuth>
        }
      />
      <Route
        path="/production/routing"
        element={
          <RequireAuth>
            <AppLayout>
              <RoutingPage />
            </AppLayout>
          </RequireAuth>
        }
      />
      <Route
        path="/production/entries"
        element={
          <RequireAuth>
            <AppLayout>
              <ProductionEntryPage />
            </AppLayout>
          </RequireAuth>
        }
      />
      <Route
        path="/production/closures"
        element={
          <RequireAuth>
            <AppLayout>
              <OperationClosuresPage />
            </AppLayout>
          </RequireAuth>
        }
      />
      <Route
        path="/production/qc"
        element={
          <RequireAuth>
            <AppLayout>
              <QcPage />
            </AppLayout>
          </RequireAuth>
        }
      />
      <Route
        path="/production/transfers"
        element={
          <RequireAuth>
            <AppLayout>
              <TransferChallansPage />
            </AppLayout>
          </RequireAuth>
        }
      />
      <Route
        path="/production/epr"
        element={
          <RequireAuth>
            <AppLayout>
              <EprPage />
            </AppLayout>
          </RequireAuth>
        }
      />
      <Route
        path="/store"
        element={
          <RequireAuth>
            <AppLayout>
              <StorePage />
            </AppLayout>
          </RequireAuth>
        }
      />
      <Route
        path="/payroll"
        element={
          <RequireAuth>
            <AppLayout>
              <PayrollPage />
            </AppLayout>
          </RequireAuth>
        }
      />
      <Route
        path="/users"
        element={
          <RequireAuth>
            <AppLayout>
              <UsersPage />
            </AppLayout>
          </RequireAuth>
        }
      />
      <Route path="*" element={<Navigate to="/employees" replace />} />
    </Routes>
  );
}
