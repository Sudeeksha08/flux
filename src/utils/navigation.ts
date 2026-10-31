import type { Role, User } from "../types";
import { canAccess } from "./business";
export const workspacePath = (role: Role, module = "dashboard", id?: string) =>
  `/${role.toLowerCase()}/${module}${id ? "/" + encodeURIComponent(id) : ""}`;
export function returnPath(user: User, from: unknown) {
  if (typeof from !== "string") return workspacePath(user.role);
  const match = from.match(
    /^\/(admin|staff|customer)\/([a-z]+)(?:\/([^/]+))?$/,
  );
  if (
    !match ||
    match[1] !== user.role.toLowerCase() ||
    !canAccess(user.role, match[2], user.permissions) ||
    (match[3] &&
      ["dashboard", "reports", "settings", "profile"].includes(match[2]))
  )
    return workspacePath(user.role);
  return from;
}
export function dashboardResources(role: Role) {
  const common = ["subscriptions", "invoices", "payments", "plans"];
  return role === "CUSTOMER"
    ? common
    : role === "STAFF"
      ? ["customers", ...common]
      : ["customers", "products", ...common];
}
