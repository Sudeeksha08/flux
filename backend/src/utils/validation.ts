import type { RecordData } from "../types";
import { modules, subtotal, taxAmount } from "./modules";
import { outstanding } from "./business";
export const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
export const STRONG_PASSWORD =
  /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{12,}$/;
export const requiredText = (value: unknown) =>
  typeof value === "string" && value.trim().length > 0;
const validDate = (value: string) =>
  /^\d{4}-\d{2}-\d{2}$/.test(value) &&
  !Number.isNaN(Date.parse(value)) &&
  new Date(value).toISOString().slice(0, 10) === value;
export function validateRecord(
  module: string,
  value: RecordData,
  data: Record<string, RecordData[]>,
) {
  const config = modules[module];
  if (!config) throw new Error("Unknown management module.");
  const multi = ["quotations", "invoices", "subscriptions"].includes(module);
  for (const field of config.fields) {
    if (multi && ["productId", "quantity", "price", "tax"].includes(field.key))
      continue;
    const current = value[field.key];
    if (field.type === "checkbox") continue;
    if (
      field.required &&
      (current === undefined ||
        current === null ||
        String(current).trim() === "")
    )
      throw new Error(field.label + " is required.");
    if (current === undefined || current === "") continue;
    if (field.type === "number") {
      const n = Number(current);
      if (!Number.isFinite(n) || n < 0)
        throw new Error(field.label + " must be a nonnegative number.");
      if (
        ["quantity", "limit"].includes(field.key) &&
        (!Number.isInteger(n) || n < 1)
      )
        throw new Error(field.label + " must be a whole number of at least 1.");
      if (["tax", "rate"].includes(field.key) && n > 100)
        throw new Error(field.label + " cannot exceed 100%.");
    }
    if (field.type === "email" && !EMAIL_PATTERN.test(String(current)))
      throw new Error("Enter a valid email.");
    if (field.type === "date" && !validDate(String(current)))
      throw new Error(field.label + " must be a valid date.");
    if (field.type === "select" && !field.options?.includes(String(current)))
      throw new Error("Select a valid " + field.label.toLowerCase() + ".");
    if (
      field.type?.startsWith("relation:") &&
      !data[field.type.split(":")[1]]?.some((row) => row.id === current)
    )
      throw new Error("Select an available " + field.label.toLowerCase() + ".");
  }
  if (value.from && value.to && String(value.to) < String(value.from))
    throw new Error("End date must be on or after the start date.");
  if (
    module === "discounts" &&
    value.type === "Percentage" &&
    Number(value.amount) > 100
  )
    throw new Error("Percentage discounts cannot exceed 100%.");
  if (multi) {
    const lines = value.lines;
    if (!Array.isArray(lines) || !lines.length)
      throw new Error("Add at least one product line.");
    for (const line of lines) {
      if (!data.products?.some((product) => product.id === line.productId))
        throw new Error("Select an available product on each line.");
      if (
        !Number.isInteger(line.quantity) ||
        line.quantity < 1 ||
        !Number.isFinite(line.price) ||
        line.price < 0 ||
        !Number.isFinite(line.tax) ||
        line.tax < 0 ||
        line.tax > 100
      )
        throw new Error(
          "Each line needs a whole positive quantity, nonnegative price and tax between 0 and 100%.",
        );
    }
    if (Number(value.discount || 0) > subtotal(value) + taxAmount(value))
      throw new Error("Discount cannot exceed the document amount.");
    if (module === "subscriptions") {
      const plan = data.plans?.find((plan) => plan.id === value.planId);
      if (
        plan &&
        lines.reduce((sum, line) => sum + line.quantity, 0) <
          Number(plan.quantity || 1)
      )
        throw new Error("Quantity is below the selected plan minimum.");
    }
  }
  if (module === "payments") {
    const invoice = data.invoices?.find(
      (invoice) => invoice.id === value.invoiceId,
    );
    if (!invoice) throw new Error("Select an available invoice.");
    const amount = Number(value.amount);
    if (!Number.isFinite(amount) || amount <= 0)
      throw new Error("Payment amount must be greater than zero.");
    if (
      Math.round(amount * 100) >
      Math.round(outstanding(invoice, data.payments || [], value.id) * 100)
    )
      throw new Error("Payment exceeds the outstanding balance.");
  }
  // Canonical totals and concurrent balance checks must also be enforced by the backend.
}
