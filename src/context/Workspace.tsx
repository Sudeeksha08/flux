import {
  createContext,
  useContext,
  useState,
  useEffect,
  useRef,
  useCallback,
  type ReactNode,
} from "react";
import type { RecordData, Role, User } from "../types";
import { demoData } from "../utils/demo";
import { serviceFor } from "../services";
import { authService } from "../services/auth";
import { errorMessage } from "../services/api";
import { SessionScope } from "../utils/session";
import { canAccess, subscriptionActions, renewalDate } from "../utils/business";
import { validateRecord } from "../utils/validation";
type State = {
  user: User | null;
  authLoading: boolean;
  demo: boolean;
  data: Record<string, RecordData[]>;
  login: (email: string, password: string) => Promise<void>;
  enterDemo: (role: Role) => void;
  logout: () => void;
  reload: (key: string, signal?: AbortSignal) => Promise<void>;
  save: (key: string, value: RecordData) => Promise<void>;
  remove: (key: string, id: string) => Promise<void>;
  action: (key: string, id: string, verb: string) => Promise<void>;
  updateProfile: (value: { name: string; email: string }) => Promise<void>;
  toast: (text: string) => void;
};
const Context = createContext<State | null>(null);
export function useWorkspace() {
  const context = useContext(Context);
  if (!context) throw new Error("Workspace provider is required.");
  return context;
}
export function Workspace({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null),
    [demo, setDemo] = useState(false),
    [data, setData] = useState<Record<string, RecordData[]>>({}),
    [message, setMessage] = useState(""),
    [authLoading, setAuthLoading] = useState(
      () => !!sessionStorage.getItem("brightloop.token"),
    );
  const scope = useRef(new SessionScope());
  const logout = useCallback(() => {
    scope.current.invalidate();
    sessionStorage.removeItem("brightloop.token");
    setUser(null);
    setDemo(false);
    setData({});
    setMessage("");
    setAuthLoading(false);
  }, []);
  useEffect(() => {
    const controller = new AbortController();
    const version = scope.current.capture();
    const token = sessionStorage.getItem("brightloop.token");
    window.addEventListener("brightloop.unauthorized", logout);
    if (token) {
      authService
        .me(controller.signal)
        .then((result) => {
          if (!controller.signal.aborted && scope.current.isCurrent(version)) {
            setUser(result);
            setAuthLoading(false);
          }
        })
        .catch((error) => {
          if (!controller.signal.aborted && scope.current.isCurrent(version)) {
            logout();
            setMessage(errorMessage(error));
          }
        });
    } else setAuthLoading(false);
    return () => {
      controller.abort();
      window.removeEventListener("brightloop.unauthorized", logout);
    };
  }, [logout]);
  useEffect(() => {
    if (!message) return;
    const timer = setTimeout(() => setMessage(""), 5000);
    return () => clearTimeout(timer);
  }, [message]);
  const enterDemo = (role: Role) => {
    scope.current.invalidate();
    sessionStorage.removeItem("brightloop.token");
    setAuthLoading(false);
    setMessage("");
    setDemo(true);
    setData(structuredClone(demoData));
    setUser({
      id: role === "CUSTOMER" ? "c1" : "u1",
      name: role === "CUSTOMER" ? "Olivia Rhye" : "Alex Morgan",
      email: role === "CUSTOMER" ? "olivia@acme.co" : "alex@example.com",
      role,
      permissions: ["reports"],
    });
  };
  const login = async (email: string, password: string) => {
    const version = scope.current.invalidate();
    const result = await authService.login({ email, password });
    scope.current.assertCurrent(version);
    sessionStorage.setItem("brightloop.token", result.token);
    setDemo(false);
    setMessage("");
    setData({});
    setUser(result.user);
    setAuthLoading(false);
  };
  const reload = useCallback(
    async (key: string, signal?: AbortSignal) => {
      if (demo) return;
      if (!user) throw new Error("Sign in to load your workspace.");
      const version = scope.current.capture();
      const rows = await serviceFor(key).list(undefined, signal);
      if (signal?.aborted) return;
      scope.current.assertCurrent(version);
      setData((d) => ({ ...d, [key]: rows }));
    },
    [demo, user],
  );
  const assertMutation = (key: string) => {
    if (
      !user ||
      user.role === "CUSTOMER" ||
      !canAccess(user.role, key, user.permissions)
    )
      throw new Error("You do not have permission to change these records.");
  };
  const save = async (key: string, value: RecordData) => {
    assertMutation(key);
    validateRecord(key, value, data);
    const version = scope.current.capture();
    const existing = (data[key] || []).some((r) => r.id === value.id);
    const result = demo
      ? {
          ...value,
          ...(key === "payments"
            ? {
                customerId:
                  data.invoices?.find((i) => i.id === value.invoiceId)
                    ?.customerId || "",
              }
            : {}),
          id: value.id || crypto.randomUUID(),
        }
      : existing
        ? await serviceFor(key).update(value.id, value)
        : await serviceFor(key).create(value);
    scope.current.assertCurrent(version);
    setData((d) => ({
      ...d,
      [key]: existing
        ? (d[key] || []).map((r) => (r.id === value.id ? result : r))
        : [...(d[key] || []), result],
    }));
    setMessage(demo ? "Saved in this demo session." : "Saved successfully.");
  };
  const action = async (key: string, id: string, verb: string) => {
    assertMutation(key);
    if (key !== "subscriptions") throw new Error("Unsupported record action.");
    const row = data[key]?.find((r) => r.id === id);
    if (
      !row ||
      !subscriptionActions(
        row,
        data.plans?.find((p) => p.id === row.planId),
      ).includes(verb)
    )
      throw new Error(
        "This action is not allowed by the subscription status or plan.",
      );
    const version = scope.current.capture();
    if (demo) {
      const statuses: Record<string, string> = {
        confirm: "Confirmed",
        activate: "Active",
        pause: "Paused",
        renew: "Active",
        close: "Closed",
      };
      let to = row.to;
      if (verb === "renew") {
        to = renewalDate(
          String(row.to),
          data.plans?.find((p) => p.id === row.planId)?.frequency,
        );
      }
      scope.current.assertCurrent(version);
      setData((d) => ({
        ...d,
        [key]: d[key].map((item) =>
          item.id === id
            ? { ...item, status: statuses[verb], ...(to ? { to } : {}) }
            : item,
        ),
      }));
      setMessage("Updated in this demo session.");
    } else {
      await serviceFor(key).action(id, verb);
      scope.current.assertCurrent(version);
      setMessage("Subscription updated.");
      try {
        await reload(key);
      } catch {
        scope.current.assertCurrent(version);
        setMessage(
          "Action completed, but the list could not refresh. Reload before taking another action.",
        );
      }
    }
  };
  const remove = async (key: string, id: string) => {
    assertMutation(key);
    const version = scope.current.capture();
    if (demo) {
      const fk: Record<string, string> = {
        customers: "customerId",
        products: "productId",
        plans: "planId",
        invoices: "invoiceId",
      };
      if (
        fk[key] &&
        Object.values(data)
          .flat()
          .some(
            (r) =>
              r[fk[key]] === id ||
              (key === "products" &&
                r.lines?.some((line) => line.productId === id)),
          )
      )
        throw new Error(
          "This record is referenced by other records. Remove those references first.",
        );
    } else await serviceFor(key).remove(id);
    scope.current.assertCurrent(version);
    setData((d) => ({
      ...d,
      [key]: (d[key] || []).filter((r) => r.id !== id),
    }));
    setMessage(
      demo ? "Removed from this demo session." : "Deleted successfully.",
    );
  };
  const updateProfile = async (value: { name: string; email: string }) => {
    if (demo || !user)
      throw new Error(
        "Profile changes require a connected account. Demo identity is read-only.",
      );
    const version = scope.current.capture();
    await authService.updateProfile(value);
    scope.current.assertCurrent(version);
    setUser((u) => (u ? { ...u, ...value } : null));
    setMessage("Profile saved.");
  };
  return (
    <Context.Provider
      value={{
        user,
        authLoading,
        demo,
        data,
        login,
        enterDemo,
        logout,
        reload,
        save,
        remove,
        action,
        updateProfile,
        toast: setMessage,
      }}
    >
      {children}
      {message && (
        <div className="toast" role="status">
          {message}
          <button
            aria-label="Dismiss notification"
            onClick={() => setMessage("")}
          >
            ×
          </button>
        </div>
      )}
    </Context.Provider>
  );
}
