import type { Module, Field } from "../types";
const f = (
  key: string,
  label: string,
  type = "text",
  options?: string[],
  required = true,
): Field => ({ key, label, type, options, required });
export const modules: Record<string, Module> = {
  customers: {
    title: "Customers",
    singular: "customer",
    fields: [
      f("name", "Customer name"),
      f("email", "Email", "email"),
      f("company", "Company"),
      f("phone", "Phone", "tel", undefined, false),
    ],
  },
  staff: {
    title: "Staff management",
    singular: "staff member",
    fields: [
      f("name", "Full name"),
      f("email", "Email", "email"),
      f("reports", "Reports permission", "checkbox"),
    ],
  },
  products: {
    title: "Products",
    singular: "product",
    fields: [
      f("name", "Product name"),
      f("type", "Product type", "select", ["Service", "Digital", "Physical"]),
      f("price", "Sales price", "number"),
      f("cost", "Cost price", "number"),
      f("notes", "Notes", "textarea", undefined, false),
    ],
  },
  variants: {
    title: "Product variants",
    singular: "variant",
    fields: [
      f("productId", "Product", "relation:products"),
      f("name", "Variant name"),
      f("price", "Additional price", "number"),
    ],
  },
  plans: {
    title: "Subscription plans",
    singular: "plan",
    fields: [
      f("name", "Plan name"),
      f("price", "Price", "number"),
      f("frequency", "Billing frequency", "select", [
        "Daily",
        "Weekly",
        "Monthly",
        "Yearly",
      ]),
      f("quantity", "Minimum quantity", "number"),
      f("from", "Available from", "date"),
      f("to", "Available to", "date"),
      ...[
        "Auto close",
        "Pause permission",
        "Early close permission",
        "Renewal permission",
      ].map((x) => f(x.replaceAll(" ", ""), x, "checkbox")),
    ],
  },
  quotations: {
    title: "Quotations",
    singular: "quotation",
    fields: [
      f("name", "Quotation number"),
      f("customerId", "Customer", "relation:customers"),
      f("productId", "Product", "relation:products"),
      f("quantity", "Quantity", "number"),
      f("price", "Unit price", "number"),
      f("tax", "Tax %", "number"),
      f("due", "Valid until", "date"),
    ],
  },
  templates: {
    title: "Quotation templates",
    singular: "template",
    fields: [
      f("name", "Template name"),
      f("notes", "Terms and description", "textarea"),
    ],
  },
  subscriptions: {
    title: "Subscriptions",
    singular: "subscription",
    fields: [
      f("name", "Subscription number"),
      f("customerId", "Customer", "relation:customers"),
      f("planId", "Plan", "relation:plans"),
      f("productId", "Product", "relation:products"),
      f("quantity", "Quantity", "number"),
      f("price", "Unit price", "number"),
      f("tax", "Tax %", "number"),
      f("from", "Start date", "date"),
      f("to", "Expiry date", "date"),
      f("terms", "Payment terms"),
      f("status", "Status", "select", [
        "Draft",
        "Quotation",
        "Confirmed",
        "Active",
        "Paused",
        "Closed",
      ]),
    ],
  },
  invoices: {
    title: "Invoices",
    singular: "invoice",
    fields: [
      f("name", "Invoice number"),
      f("customerId", "Customer", "relation:customers"),
      f("productId", "Product", "relation:products"),
      f("quantity", "Quantity", "number"),
      f("price", "Unit price", "number"),
      f("tax", "Tax %", "number"),
      f("discount", "Discount amount", "number"),
      f("due", "Due date", "date"),
      f("status", "Status", "select", ["Draft", "Confirmed", "Paid"]),
    ],
  },
  discounts: {
    title: "Discounts",
    singular: "discount",
    fields: [
      f("name", "Discount name"),
      f("type", "Type", "select", ["Fixed", "Percentage"]),
      f("amount", "Discount value", "number"),
      f("minimum", "Minimum purchase", "number"),
      f("quantity", "Minimum quantity", "number"),
      f("from", "Start date", "date"),
      f("to", "End date", "date"),
      f("limit", "Usage limit", "number"),
      f("productId", "Applicable product", "relation:products"),
    ],
  },
  taxes: {
    title: "Tax management",
    singular: "tax",
    fields: [f("name", "Tax name"), f("rate", "Rate %", "number")],
  },
  payments: {
    title: "Payments",
    singular: "payment",
    fields: [
      f("name", "Payment ID"),
      f("invoiceId", "Invoice", "relation:invoices"),
      f("method", "Payment method", "select", [
        "Bank transfer",
        "Card",
        "Cash",
        "Other",
      ]),
      f("amount", "Amount", "number"),
      f("date", "Payment date", "date"),
      f("notes", "Notes", "textarea", undefined, false),
    ],
  },
};
export const money = (n: unknown) =>
  new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 2,
  }).format(Number(n) || 0);
export const subtotal = (r: Record<string, unknown>) =>
  Array.isArray(r.lines) && r.lines.length
    ? r.lines.reduce((s, l) => s + Number(l.quantity) * Number(l.price), 0)
    : Number(r.quantity ?? 1) * Number(r.price || 0);
export const taxAmount = (r: Record<string, unknown>) =>
  Array.isArray(r.lines) && r.lines.length
    ? r.lines.reduce(
        (s, l) =>
          s + (Number(l.quantity) * Number(l.price) * Number(l.tax || 0)) / 100,
        0,
      )
    : (subtotal(r) * Number(r.tax || 0)) / 100;
export const total = (r: Record<string, unknown>) =>
  Math.round(
    Math.max(0, subtotal(r) + taxAmount(r) - Number(r.discount || 0)) * 100,
  ) / 100;
export const nav: Record<string, string[]> = {
  ADMIN: [
    "dashboard",
    ...Object.keys(modules).filter((x) => x !== "templates"),
    "reports",
    "settings",
  ],
  STAFF: [
    "dashboard",
    "customers",
    "quotations",
    "subscriptions",
    "invoices",
    "payments",
    "reports",
    "settings",
  ],
  CUSTOMER: [
    "dashboard",
    "profile",
    "plans",
    "subscriptions",
    "invoices",
    "payments",
    "settings",
  ],
};
