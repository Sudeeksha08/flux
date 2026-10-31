import { api, resource } from "./api";
export const invoicesService = {
  ...resource("/invoices"),
  pdf: async (id: string) =>
    (
      await api.get<Blob>(`/invoices/${encodeURIComponent(id)}/pdf`, {
        responseType: "blob",
      })
    ).data,
};
