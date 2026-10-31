import type { RecordData, User } from "../types";
const object = (value: unknown): value is Record<string, unknown> =>
  typeof value === "object" && value !== null && !Array.isArray(value);
export function parseUser(value: unknown): User {
  if (
    !object(value) ||
    typeof value.id !== "string" ||
    !value.id.trim() ||
    typeof value.name !== "string" ||
    !value.name.trim() ||
    typeof value.email !== "string" ||
    !["ADMIN", "STAFF", "CUSTOMER"].includes(String(value.role)) ||
    (value.permissions !== undefined &&
      (!Array.isArray(value.permissions) ||
        !value.permissions.every((p) => typeof p === "string")))
  )
    throw new Error(
      "The backend returned an invalid user. Sign-in was not completed.",
    );
  return {
    id: value.id,
    name: value.name,
    email: value.email,
    role: value.role as User["role"],
    permissions: (value.permissions || []) as string[],
  };
}
export function parseLogin(value: unknown): { token: string; user: User } {
  if (!object(value) || typeof value.token !== "string" || !value.token.trim())
    throw new Error("The backend returned an invalid sign-in response.");
  return { token: value.token, user: parseUser(value.user) };
}
export function parseRecord(value: unknown): RecordData {
  if (!object(value) || typeof value.id !== "string" || !value.id.trim())
    throw new Error(
      "The backend returned an invalid record. Refresh the page before continuing.",
    );
  const normalized = { ...value };
  for (const [key, field] of Object.entries(normalized)) {
    if (field === null) {
      normalized[key] = undefined;
      continue;
    }
    if (key === "lines") {
      if (
        field !== undefined &&
        (!Array.isArray(field) ||
          !field.every(
            (line) =>
              object(line) &&
              typeof line.productId === "string" &&
              typeof line.quantity === "number" &&
              Number.isFinite(line.quantity) &&
              typeof line.price === "number" &&
              Number.isFinite(line.price) &&
              typeof line.tax === "number" &&
              Number.isFinite(line.tax),
          ))
      )
        throw new Error("The backend returned invalid product lines.");
    } else if (
      field !== undefined &&
      !["string", "number", "boolean"].includes(typeof field)
    )
      throw new Error(
        "The backend returned an unsupported nested field: " + key,
      );
  }
  for (const key of ["name", "status", "email", "frequency", "to"])
    if (normalized[key] !== undefined && typeof normalized[key] !== "string")
      throw new Error("The backend returned an invalid " + key + " field.");
  return normalized as RecordData;
}
export function parseRecords(value: unknown): RecordData[] {
  if (!Array.isArray(value))
    throw new Error(
      "The backend returned an invalid record list. Expected a JSON array.",
    );
  return value.map(parseRecord);
}
