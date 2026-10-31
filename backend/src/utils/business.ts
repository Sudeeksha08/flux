import type { RecordData, Role } from "../types";
import { nav, total } from "./modules";
export function canAccess(
  role: Role,
  module: string,
  permissions: string[] = [],
) {
  return (
    (nav[role].includes(module) ||
      (module === "templates" && role !== "CUSTOMER")) &&
    !(
      module === "reports" &&
      role === "STAFF" &&
      !permissions.includes("reports")
    )
  );
}
export function outstanding(
  invoice: RecordData,
  payments: RecordData[],
  excludeId = "",
) {
  return Math.max(
    0,
    Math.round(
      (total(invoice) -
        payments
          .filter((p) => p.invoiceId === invoice.id && p.id !== excludeId)
          .reduce((s, p) => s + Number(p.amount), 0)) *
        100,
    ) / 100,
  );
}
export function subscriptionActions(row: RecordData, plan?: RecordData) {
  const stages: Record<string, string[]> = {
    Draft: ["confirm"],
    Quotation: ["confirm"],
    Confirmed: ["activate"],
    Active: ["pause", "renew", "close"],
    Paused: ["activate", "close"],
    Closed: [],
  };
  return (stages[String(row.status)] || []).filter(
    (v) =>
      !plan ||
      !(
        (v === "pause" && !plan.Pausepermission) ||
        (v === "renew" && !plan.Renewalpermission) ||
        (v === "close" && !plan.Earlyclosepermission)
      ),
  );
}

export function renewalDate(expiry: string, frequency: unknown) {
  const date = new Date(expiry);
  if (Number.isNaN(date.getTime()))
    throw new Error("Set an expiry date before renewal.");
  const day = date.getUTCDate();
  if (frequency === "Daily") date.setUTCDate(day + 1);
  else if (frequency === "Weekly") date.setUTCDate(day + 7);
  else {
    date.setUTCDate(1);
    if (frequency === "Monthly") date.setUTCMonth(date.getUTCMonth() + 1);
    else date.setUTCFullYear(date.getUTCFullYear() + 1);
    const lastDay = new Date(
      Date.UTC(date.getUTCFullYear(), date.getUTCMonth() + 1, 0),
    ).getUTCDate();
    date.setUTCDate(Math.min(day, lastDay));
  }
  return date.toISOString().slice(0, 10);
}
