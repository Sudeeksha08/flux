import { Navigate, Outlet, useParams, useLocation } from "react-router-dom";
import { useWorkspace } from "../context/Workspace";
import { canAccess } from "../utils/business";
export function ProtectedRoute() {
  const { user, authLoading } = useWorkspace();
  const location = useLocation();
  const { role } = useParams();
  if (authLoading)
    return (
      <div className="empty" role="status">
        Restoring your session...
      </div>
    );
  if (!user)
    return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  if (role !== user.role.toLowerCase())
    return <Navigate to={`/${user.role.toLowerCase()}/dashboard`} replace />;
  return <Outlet />;
}
export function Allowed({
  module,
  children,
}: {
  module: string;
  children: React.ReactNode;
}) {
  const { user } = useWorkspace();
  if (!user || !canAccess(user.role, module, user.permissions))
    return (
      <div className="empty">
        <h2>Access restricted</h2>
        <p>Your role does not have access to this workspace.</p>
      </div>
    );
  return <>{children}</>;
}
