import { createContext, useContext, useMemo, useState, type ReactNode } from 'react';
import { login as loginRequest } from '../api/auth';
import { clearToken, getToken, storeToken } from '../api/client';

interface AuthState {
  username: string;
  role: string;
  rights: string[];
}

interface AuthContextValue {
  auth: AuthState | null;
  isAuthenticated: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  // We don't persist username/role/rights across a hard refresh yet (phase 2:
  // decode them from the JWT or add a GET /api/auth/me). For now, a stored
  // token without cached auth state is treated as logged-out on reload.
  const [auth, setAuth] = useState<AuthState | null>(null);

  const value = useMemo<AuthContextValue>(
    () => ({
      auth,
      isAuthenticated: auth !== null || getToken() !== null,
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
    [auth],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
}
