export type Role = "ADMIN" | "STAFF" | "CUSTOMER";
export type LineItem = {
  productId: string;
  quantity: number;
  price: number;
  tax: number;
};
export type RecordData = {
  id: string;
  name?: string;
  status?: string;
  email?: string;
  frequency?: string;
  quantity?: number;
  price?: number;
  tax?: number;
  discount?: number;
  to?: string;
  lines?: LineItem[];
  [key: string]: string | number | boolean | LineItem[] | undefined;
};
export type User = {
  id: string;
  name: string;
  email: string;
  role: Role;
  permissions: string[];
};
export type Field = {
  key: string;
  label: string;
  type?: string;
  options?: string[];
  required?: boolean;
};
export type Module = { title: string; singular: string; fields: Field[] };
