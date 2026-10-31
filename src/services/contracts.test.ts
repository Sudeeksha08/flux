import { describe, it, expect } from "vitest";
import { parseLogin, parseUser, parseRecords } from "./contracts";
const user = {
  id: "u1",
  name: "Alex",
  email: "alex@example.com",
  role: "STAFF",
};
describe("authentication response contracts", () => {
  it("requires a real token and a recognized role before login", () => {
    expect(() => parseLogin({ token: "", user })).toThrow();
    expect(() =>
      parseLogin({ token: "jwt", user: { ...user, role: "SUPERADMIN" } }),
    ).toThrow();
    expect(() => parseLogin("<html>offline fallback</html>")).toThrow();
  });
  it("defaults missing permissions to no grants", () => {
    expect(parseUser(user).permissions).toEqual([]);
    expect(() => parseUser({ ...user, permissions: "reports" })).toThrow();
  });
  it("accepts validated login data", () => {
    expect(
      parseLogin({ token: "jwt", user: { ...user, permissions: ["reports"] } })
        .user.role,
    ).toBe("STAFF");
  });
  it("rejects malformed lists instead of rendering them as records", () => {
    expect(() => parseRecords({ content: [] })).toThrow();
    expect(() => parseRecords([{ name: "missing id" }])).toThrow();
    expect(parseRecords([{ id: "p1", name: "Product" }])).toHaveLength(1);
  });
});

it("rejects malformed product lines before rendering", () => {
  expect(() => parseRecords([{ id: "i1", lines: "not an array" }])).toThrow(
    "product lines",
  );
});
