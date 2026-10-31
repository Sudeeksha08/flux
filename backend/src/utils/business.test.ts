import { describe, it, expect } from "vitest";
import { canAccess, outstanding, subscriptionActions } from "./business";
import { total } from "./modules";
describe("role permissions", () => {
  it("blocks admin modules for staff and customers", () => {
    expect(canAccess("STAFF", "staff")).toBe(false);
    expect(canAccess("CUSTOMER", "customers")).toBe(false);
    expect(canAccess("CUSTOMER", "reports")).toBe(false);
    expect(canAccess("ADMIN", "products")).toBe(true);
  });
  it("requires an explicit staff report permission", () => {
    expect(canAccess("STAFF", "reports")).toBe(false);
    expect(canAccess("STAFF", "reports", ["reports"])).toBe(true);
  });
});
describe("billing", () => {
  it("applies quantity, taxes and fixed discounts", () => {
    expect(
      total({ quantity: 2, price: 100, tax: 10, discount: 20 }),
    ).toBeCloseTo(200);
  });
  it("subtracts partial payments for only the matching invoice", () => {
    expect(
      outstanding({ id: "i1", quantity: 1, price: 129 }, [
        { id: "p1", invoiceId: "i1", amount: 29 },
        { id: "p2", invoiceId: "i2", amount: 500 },
      ]),
    ).toBe(100);
  });
  it("excludes the edited payment and avoids negative balances", () => {
    expect(
      outstanding(
        { id: "i1", price: 100 },
        [{ id: "p1", invoiceId: "i1", amount: 25 }],
        "p1",
      ),
    ).toBe(100);
    expect(
      outstanding({ id: "i1", price: 10 }, [
        { id: "p1", invoiceId: "i1", amount: 20 },
      ]),
    ).toBe(0);
  });
});
describe("subscription lifecycle", () => {
  it("offers only valid transitions", () => {
    expect(subscriptionActions({ id: "s", status: "Draft" })).toEqual([
      "confirm",
    ]);
    expect(subscriptionActions({ id: "s", status: "Closed" })).toEqual([]);
  });
  it("respects the plan action permissions", () => {
    expect(
      subscriptionActions(
        { id: "s", status: "Active" },
        {
          id: "p",
          Pausepermission: true,
          Renewalpermission: false,
          Earlyclosepermission: false,
        },
      ),
    ).toEqual(["pause"]);
  });
});

it("calculates multi-line invoices with per-line tax and a document discount", () => {
  expect(
    total({
      lines: [
        { productId: "p1", quantity: 2, price: 100, tax: 10 },
        { productId: "p2", quantity: 1, price: 50, tax: 0 },
      ],
      discount: 20,
    }),
  ).toBe(250);
});
