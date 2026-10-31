import { afterEach, beforeEach, describe, it, expect, vi } from "vitest";
import { AxiosError, type InternalAxiosRequestConfig } from "axios";
import { api, resource, errorMessage } from "./api";
const original = api.defaults.adapter;
beforeEach(() => {
  const store = new Map<string, string>();
  vi.stubGlobal("sessionStorage", {
    getItem: (key: string) => store.get(key) || null,
    setItem: (key: string, value: string) => store.set(key, value),
    removeItem: (key: string) => store.delete(key),
  });
  vi.stubGlobal("window", new EventTarget());
});
afterEach(() => {
  api.defaults.adapter = original;
  vi.unstubAllGlobals();
});
const unauthorized = (config: InternalAxiosRequestConfig) =>
  new AxiosError("Unauthorized", "ERR_BAD_REQUEST", config, undefined, {
    data: {},
    status: 401,
    statusText: "Unauthorized",
    headers: {},
    config,
  });
describe("authenticated API requests", () => {
  it("attaches the bearer token and parses record lists", async () => {
    sessionStorage.setItem("brightloop.token", "session-one");
    api.defaults.adapter = async (config) => {
      expect(config.headers.Authorization).toBe("Bearer session-one");
      return {
        data: [{ id: "p1" }],
        status: 200,
        statusText: "OK",
        headers: {},
        config,
      };
    };
    expect(await resource("/products").list()).toEqual([{ id: "p1" }]);
  });
  it("expires only the current session on 401", async () => {
    sessionStorage.setItem("brightloop.token", "current");
    const notify = vi.fn();
    window.addEventListener("brightloop.unauthorized", notify);
    api.defaults.adapter = async (config) => {
      throw unauthorized(config);
    };
    await expect(api.get("/customers")).rejects.toThrow();
    expect(sessionStorage.getItem("brightloop.token")).toBeNull();
    expect(notify).toHaveBeenCalledOnce();
  });
  it("ignores a late 401 from a previous token", async () => {
    sessionStorage.setItem("brightloop.token", "old");
    const notify = vi.fn();
    window.addEventListener("brightloop.unauthorized", notify);
    api.defaults.adapter = async (config) => {
      sessionStorage.setItem("brightloop.token", "new");
      throw unauthorized(config);
    };
    await expect(api.get("/customers")).rejects.toThrow();
    expect(sessionStorage.getItem("brightloop.token")).toBe("new");
    expect(notify).not.toHaveBeenCalled();
  });
  it("does not sign out a demo session after an old request fails", async () => {
    sessionStorage.setItem("brightloop.token", "old");
    const notify = vi.fn();
    window.addEventListener("brightloop.unauthorized", notify);
    api.defaults.adapter = async (config) => {
      sessionStorage.removeItem("brightloop.token");
      throw unauthorized(config);
    };
    await expect(api.get("/customers")).rejects.toThrow();
    expect(notify).not.toHaveBeenCalled();
  });
  it("rejects malformed successful responses rather than fabricating records", async () => {
    api.defaults.adapter = async (config) => ({
      data: "<html/>",
      status: 200,
      statusText: "OK",
      headers: {},
      config,
    });
    await expect(resource("/products").list()).rejects.toThrow(
      "invalid record list",
    );
    await expect(
      resource("/products").create({ id: "", name: "New" }),
    ).rejects.toThrow("invalid record");
  });
  it("omits client ids on create and encodes update ids", async () => {
    api.defaults.adapter = async (config) => {
      if (config.method === "post")
        expect(JSON.parse(config.data)).toEqual({ name: "New" });
      else expect(config.url).toBe("/products/a%2Fb");
      return {
        data: { id: "p1", name: "New" },
        status: 200,
        statusText: "OK",
        headers: {},
        config,
      };
    };
    await resource("/products").create({ id: "", name: "New" });
    await resource("/products").update("a/b", { name: "New" });
  });
  it("describes uncertain network results without claiming a rollback", () => {
    expect(errorMessage(new AxiosError("Network"))).toContain(
      "could not be verified",
    );
  });
});
