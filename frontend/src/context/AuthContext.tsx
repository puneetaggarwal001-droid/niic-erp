import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { fetchCurrentUser, login as loginRequest } from '../api/auth';
import { clearToken, getToken, storeToken } from '../api/client';

interface AuthState {
  username: string;
  role: string;
  rights: string[];
}

interface AuthContextValue {
  auth: AuthState | null;
  isAuthenticated: boolean;
  // True while a stored token is being validated/rehydrated on first load.
  // Consumers must wait for this before deciding a user is logged out, or a
  // hard refresh flashes the login page before /auth/me resolves.
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [auth, setAuth] = useState<AuthState | null>(null);
  // Only a stored token needs rehydrating; with no token we start resolved.
  const [loading, setLoading] = useState(() => getToken() !== null);

  // The JWT carries only username/role, not the per-user rights set, so after a
  // hard refresh we rehydrate full auth state from the server. An invalid or
  // expired token yields a 401 here — treat that as logged out and drop it.
  useEffect(() => {
    if (getToken() === null) return;
    let cancelled = false;
    fetchCurrentUser()
      .then((user) => {
        if (!cancelled) {
          setAuth({ username: user.username, role: user.role, rights: user.rights });
        }
      })
      .catch(() => {
        if (!cancelled) {
          clearToken();
          setAuth(null);
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      auth,
      isAuthenticated: auth !== null,
      loading,
      async login(username: string, password: string) {
        const response = await loginRequest(username, password);
        storeToken(response.token);
        setAuth({ username: response.username, role: response.role, rights: response.rights });
      },
      logout() {
        clearToken();
        setAuth(null);
      },
    }),
    [auth, loading],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
}
