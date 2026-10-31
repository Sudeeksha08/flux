import { describe, it, expect } from "vitest";
import { SessionScope } from "./session";
import { returnPath, workspacePath, dashboardResources } from "./navigation";
import { nav } from "./modules";
import { canAccess, renewalDate } from "./business";
import { validateRecord } from "./validation";
import { demoData } from "./demo";
import type { Role, User, RecordData } from "../types";
describe("session isolation", () => {
  it("invalidates pending work on identity changes", () => {
    const scope = new SessionScope();
    const pending = scope.capture();
    scope.invalidate();
    expect(scope.isCurrent(pending)).toBe(false);
    expect(() => scope.assertCurrent(pending)).toThrow("session changed");
  });
  it("accepts work only from the current session", () => {
    const scope = new SessionScope();
    const current = scope.invalidate();
    expect(() => scope.assertCurrent(current)).not.toThrow();
    scope.invalidate();
    expect(() => scope.assertCurrent(current)).toThrow();
  });
});
describe("role navigation", () => {
  it.each(["ADMIN", "STAFF", "CUSTOMER"] as Role[])(
    "resolves every visible %s navigation item",
    (role) => {
      for (const module of nav[role]) {
        expect(canAccess(role, module, ["reports"])).toBe(true);
        expect(workspacePath(role, module)).toBe(
          `/${role.toLowerCase()}/${module}`,
        );
      }
    },
  );
  it("does not request admin products for staff dashboards", () => {
    expect(dashboardResources("STAFF")).not.toContain("products");
    expect(dashboardResources("CUSTOMER")).not.toContain("customers");
  });
  it("restores only a permitted same-role deep link", () => {
    const user: User = {
      id: "c1",
      name: "Olivia",
      email: "o@example.com",
      role: "CUSTOMER",
      permissions: [],
    };
    expect(returnPath(user, "/customer/invoices/i1")).toBe(
      "/customer/invoices/i1",
    );
    for (const unsafe of [
      "https://evil.example",
      "//evil.example",
      "/admin/products",
      "/customer/customers",
      "/customer/dashboard/bogus",
    ])
      expect(returnPath(user, unsafe)).toBe("/customer/dashboard");
  });
  it("encodes a record id as a single URL segment", () => {
    expect(workspacePath("ADMIN", "invoices", "a/b?c")).toBe(
      "/admin/invoices/a%2Fb%3Fc",
    );
  });
});
describe("management form validation", () => {
  const valid: RecordData = { id: "", name: "Test", notes: "Terms" };
  it("rejects whitespace-only required textareas", () => {
    expect(() =>
      validateRecord("templates", { ...valid, notes: "   " }, {}),
    ).toThrow("required");
  });
  it("rejects fractional integer fields", () => {
    expect(() =>
      validateRecord(
        "plans",
        {
          id: "",
          name: "Plan",
          price: 10,
          frequency: "Monthly",
          quantity: 1.5,
          from: "2026-01-01",
          to: "2027-01-01",
        },
        {},
      ),
    ).toThrow("whole number");
  });
  it("rejects invalid and reversed dates", () => {
    const plan = {
      id: "",
      name: "Plan",
      price: 10,
      frequency: "Monthly",
      quantity: 1,
      from: "2026-02-30",
      to: "2027-01-01",
    };
    expect(() => validateRecord("plans", plan, {})).toThrow("valid date");
    expect(() =>
      validateRecord("plans", { ...plan, from: "2027-02-01" }, {}),
    ).toThrow("End date");
  });
  it("requires a selected existing invoice and blocks overpayments", () => {
    const payment = {
      id: "",
      name: "PAY",
      invoiceId: "i3",
      method: "Card",
      amount: 200,
      date: "2026-10-31",
    };
    expect(() => validateRecord("payments", payment, demoData)).toThrow(
      "outstanding",
    );
    expect(() =>
      validateRecord(
        "payments",
        { ...payment, amount: 1, invoiceId: "missing" },
        demoData,
      ),
    ).toThrow("available invoice");
    expect(() =>
      validateRecord("payments", { ...payment, amount: 199 }, demoData),
    ).not.toThrow();
  });
  it("accepts a valid multi-line invoice and rejects invalid tax", () => {
    const invoice = {
      id: "",
      name: "INV",
      customerId: "c1",
      discount: 0,
      due: "2026-11-01",
      status: "Draft",
      lines: [{ productId: "p1", quantity: 1, price: 129, tax: 0 }],
    };
    expect(() => validateRecord("invoices", invoice, demoData)).not.toThrow();
    expect(() =>
      validateRecord(
        "invoices",
        { ...invoice, lines: [{ ...invoice.lines[0], tax: 101 }] },
        demoData,
      ),
    ).toThrow("tax");
  });
});
describe("renewal dates", () => {
  it("uses a billing cycle rather than always adding a year", () => {
    expect(renewalDate("2026-10-31", "Monthly")).toBe("2026-11-30");
    expect(renewalDate("2026-10-31", "Weekly")).toBe("2026-11-07");
    expect(renewalDate("2026-10-31", "Daily")).toBe("2026-11-01");
  });
  it("clamps month ends and leap-day yearly renewals", () => {
    expect(renewalDate("2028-01-31", "Monthly")).toBe("2028-02-29");
    expect(renewalDate("2028-02-29", "Yearly")).toBe("2029-02-28");
  });
});
