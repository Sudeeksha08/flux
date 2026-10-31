import axios from "axios";
import { parseRecord, parseRecords } from "./contracts";
declare module "axios" {
  export interface AxiosRequestConfig {
    brightloopToken?: string | null;
  }
}
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "/api",
  timeout: 15000,
});
api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem("brightloop.token");
  config.brightloopToken = token;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const sentToken = error.config?.brightloopToken;
    if (
      error.response?.status === 401 &&
      sentToken &&
      sessionStorage.getItem("brightloop.token") === sentToken
    ) {
      sessionStorage.removeItem("brightloop.token");
      window.dispatchEvent(new Event("brightloop.unauthorized"));
    }
    return Promise.reject(error);
  },
);
export function errorMessage(error: unknown): string {
  if (!axios.isAxiosError(error))
    return error instanceof Error ? error.message : "Something went wrong.";
  const message = error.response?.data?.message;
  if (typeof message === "string" && message.trim()) return message;
  if (!error.response)
    return "Unable to reach the backend. The result could not be verified; refresh before retrying.";
  const status = error.response.status;
  if (status === 401)
    return "Your sign-in details are invalid or your session has expired.";
  if (status === 403)
    return "You do not have permission to perform this action.";
  if (status === 404) return "The requested record or endpoint was not found.";
  if (status === 409)
    return "This change conflicts with an existing record. Refresh and try again.";
  if (status === 422 || status === 400)
    return "The backend rejected these details. Check your entries.";
  return "The backend could not complete this request. Refresh before retrying.";
}
export const resource = (path: string) => ({
  list: async (params?: object, signal?: AbortSignal) =>
    parseRecords((await api.get<unknown>(path, { params, signal })).data),
  create: async (data: object) => {
    const { id, ...payload } = data as { id?: string; [key: string]: unknown };
    return parseRecord((await api.post<unknown>(path, payload)).data);
  },
  update: async (id: string, data: object) =>
    parseRecord(
      (await api.put<unknown>(`${path}/${encodeURIComponent(id)}`, data)).data,
    ),
  remove: async (id: string) => {
    await api.delete(`${path}/${encodeURIComponent(id)}`);
  },
  action: async (id: string, action: string) => {
    await api.post(
      `${path}/${encodeURIComponent(id)}/${encodeURIComponent(action)}`,
    );
  },
});
