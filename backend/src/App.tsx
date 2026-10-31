import { lazy, Suspense } from "react";
import { Navigate, Route, Routes, useParams } from "react-router-dom";
import { Workspace, useWorkspace } from "./context/Workspace";
import Auth from "./pages/auth/Auth";
import Shell from "./layouts/Shell";
const AdminDashboard = lazy(() => import("./pages/admin/Dashboard"));
const StaffDashboard = lazy(() => import("./pages/staff/Dashboard"));
const CustomerDashboard = lazy(() => import("./pages/customer/Dashboard"));
const Management = lazy(() => import("./pages/Management"));
import Settings from "./pages/customer/Settings";
import { Allowed, ProtectedRoute } from "./routes/ProtectedRoute";
import { modules } from "./utils/modules";
function Home() {
  const { user, authLoading } = useWorkspace();
  if (authLoading)
    return (
      <div className="empty" role="status">
        Restoring your session...
      </div>
    );
  return (
    <Navigate
      to={user ? `/${user.role.toLowerCase()}/dashboard` : "/login"}
      replace
    />
  );
}
function Page() {
  const { module = "dashboard", id } = useParams();
  const { user } = useWorkspace();
  const Dashboard =
    user!.role === "ADMIN"
      ? AdminDashboard
      : user!.role === "STAFF"
        ? StaffDashboard
        : CustomerDashboard;
  if (id && !modules[module])
    return (
      <div className="empty">
        <h1>Page not found</h1>
      </div>
    );
  return (
    <Allowed module={module}>
      <Suspense
        fallback={
          <div className="empty" role="status">
            Loading workspace...
          </div>
        }
      >
        {module === "dashboard" ? (
          <Dashboard />
        ) : module === "reports" ? (
          <Dashboard reports />
        ) : module === "settings" || module === "profile" ? (
          <Settings profile={module === "profile"} />
        ) : modules[module] ? (
          <Management key={module} module={module} />
        ) : (
          <div className="empty">
            <h1>Page not found</h1>
          </div>
        )}
      </Suspense>
    </Allowed>
  );
}
export default function App() {
  return (
    <Workspace>
      <Routes>
        <Route path="/" element={<Home />} />
        {["login", "register", "forgot-password", "reset-password"].map((p) => (
          <Route key={p} path={"/" + p} element={<Auth key={p} />} />
        ))}
        <Route path="/:role" element={<ProtectedRoute />}>
          <Route element={<Shell />}>
            <Route index element={<Navigate to="dashboard" replace />} />
            <Route path=":module" element={<Page />} />
            <Route path=":module/:id" element={<Page />} />
          </Route>
        </Route>
        <Route path="*" element={<Home />} />
      </Routes>
    </Workspace>
  );
}
