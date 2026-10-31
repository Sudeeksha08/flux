import { api } from "./api";
import { parseLogin, parseUser } from "./contracts";
export const authService = {
  login: async (data: { email: string; password: string }) =>
    parseLogin((await api.post<unknown>("/auth/login", data)).data),
  me: async (signal?: AbortSignal) =>
    parseUser((await api.get<unknown>("/auth/me", { signal })).data),
  register: async (data: { name: string; email: string; password: string }) => {
    await api.post("/auth/register", data);
  },
  forgot: async (data: { email: string }) => {
    await api.post("/auth/forgot-password", data);
  },
  reset: async (data: { token: string; password: string }) => {
    await api.post("/auth/reset-password", data);
  },
  updateProfile: async (data: { name: string; email: string }) => {
    await api.put("/auth/profile", data);
  },
  emailAvailable: async (email: string): Promise<boolean> => {
    const result = (
      await api.get("/auth/email-available", { params: { email } })
    ).data;
    if (typeof result?.available !== "boolean")
      throw new Error("The backend could not verify email availability.");
    return result.available;
  },
};
