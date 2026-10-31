import { customersService } from "./customers";
import { productsService } from "./products";
import { plansService } from "./plans";
import { quotationsService } from "./quotations";
import { subscriptionsService } from "./subscriptions";
import { invoicesService } from "./invoices";
import { paymentsService } from "./payments";
import { discountsService } from "./discounts";
import { staffService } from "./staff";
import { variantsService } from "./variants";
import { taxesService } from "./taxes";
import { templatesService } from "./templates";
const services = {
  customers: customersService,
  products: productsService,
  plans: plansService,
  quotations: quotationsService,
  subscriptions: subscriptionsService,
  invoices: invoicesService,
  payments: paymentsService,
  discounts: discountsService,
  staff: staffService,
  variants: variantsService,
  taxes: taxesService,
  templates: templatesService,
};
export type ResourceKey = keyof typeof services;
export function serviceFor(key: string) {
  if (!Object.hasOwn(services, key))
    throw new Error("Unknown resource: " + key);
  return services[key as ResourceKey];
}
