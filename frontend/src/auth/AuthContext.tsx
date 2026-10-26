import { createContext, useContext, useState, type ReactNode } from "react";
import { api, detectRole } from "../api/client";
import type { Account, Credentials, Role } from "../types/api";
interface Session {
  credentials: Credentials;
  account: Account;
  role: Role;
}
const Context = createContext<{
  session: Session | null;
  login: (c: Credentials) => Promise<void>;
  logout: () => void;
}>(null!);
export function AuthProvider({ children }: { children: ReactNode }) {
  // Session and credentials exist only in React memory. No persistence or global private cache.
  const [session, setSession] = useState<Session | null>(null);
  async function login(credentials: Credentials) {
    const account = await api.login(credentials);
    const role = await detectRole(credentials);
    setSession({ account, credentials, role });
  }
  return (
    <Context.Provider
      value={{ session, login, logout: () => setSession(null) }}
    >
      {children}
    </Context.Provider>
  );
}
export const useAuth = () => useContext(Context);
