import RecordEditor from "../components/management/RecordEditor";
import { invoicesService } from "../services/invoices";
import { subscriptionActions } from "../utils/business";
import { useState, useEffect } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import {
  Plus,
  Search,
  Download,
  ArrowUpRight,
  Pencil,
  Trash2,
  ArrowLeft,
  Printer,
} from "lucide-react";
import { useWorkspace } from "../context/Workspace";
import { useRecords } from "../hooks/useRecords";
import { modules, money, total, subtotal, taxAmount } from "../utils/modules";
import { errorMessage } from "../services/api";
import { Modal, State, Empty, Badge } from "../components/Shared";
import { downloadCsv } from "../utils/export";
import type { RecordData } from "../types";
export default function Management({ module }: { module: string }) {
  const config = modules[module];
  const multiDocument = ["quotations", "invoices", "subscriptions"].includes(
    module,
  );
  const listFields = config.fields
    .filter(
      (f) =>
        f.type !== "textarea" &&
        f.type !== "checkbox" &&
        (!multiDocument ||
          !["productId", "quantity", "price", "tax"].includes(f.key)),
    )
    .slice(0, 5);
  const { data, user, demo, remove, action } = useWorkspace();
  const { id } = useParams();
  const navigate = useNavigate();
  const [edit, setEdit] = useState<RecordData | null>(null),
    [deleting, setDeleting] = useState<RecordData | null>(null),
    [query, setQuery] = useState(""),
    [filter, setFilter] = useState("All"),
    [page, setPage] = useState(1),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false),
    [confirmation, setConfirmation] = useState("");
  const customer = user!.role === "CUSTOMER";
  const relations = Array.from(
    new Set(
      config.fields
        .filter((f) => f.type?.startsWith("relation:"))
        .map((f) => f.type!.split(":")[1]),
    ),
  );
  const keys = [
    module,
    ...relations,
    ...(module === "invoices" ? ["payments"] : []),
  ];
  const state = useRecords(keys);
  const rows = (data[module] || []).filter(
    (r) =>
      !customer ||
      module === "plans" ||
      r.customerId === user!.id ||
      (module === "customers" && r.id === user!.id),
  );
  const visible = rows.filter(
    (r) =>
      Object.values(r).some((v) =>
        String(v).toLowerCase().includes(query.toLowerCase()),
      ) &&
      (filter === "All" || r.status === filter),
  );
  useEffect(() => {
    setPage((current) =>
      Math.min(current, Math.max(1, Math.ceil(visible.length / 10))),
    );
  }, [visible.length]);
  const row = rows.find((r) => r.id === id);
  const resolve = (key: string, value: unknown) =>
    key.endsWith("Id")
      ? data[
          key === "invoiceId"
            ? "invoices"
            : key === "planId"
              ? "plans"
              : key === "productId"
                ? "products"
                : "customers"
        ]?.find((r) => r.id === value)?.name || value
      : value;
  const execute = async (fn: () => Promise<void>) => {
    setBusy(true);
    setError("");
    try {
      await fn();
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  };
  const pdf = async () => {
    if (demo) {
      window.print();
      return;
    }
    await execute(async () => {
      const blob = await invoicesService.pdf(id!);
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `${row?.name || "invoice"}.pdf`;
      a.click();
      setTimeout(() => URL.revokeObjectURL(url), 1000);
    });
  };
  return (
    <>
      <div className="page-heading">
        <div>
          <span className="eyebrow">
            {customer ? "YOUR PERSONAL WORKSPACE" : "KEEP EVERYTHING IN SYNC"}
          </span>
          <h1>
            {customer && module !== "plans" ? "My " : ""}
            {config.title}
            <span className="heading-dot" />
          </h1>
          <p>
            {module === "plans"
              ? "Flexible plans for every stage of growth."
              : `A clear view of your ${config.title.toLowerCase()}. All in one place.`}
          </p>
        </div>
        <div className="heading-actions">
          {module === "quotations" && (
            <Link
              className="secondary"
              to={`/${user!.role.toLowerCase()}/templates`}
            >
              Templates
            </Link>
          )}
          <button
            className="secondary"
            onClick={() => downloadCsv(module, visible)}
          >
            <Download size={15} />
            Export
          </button>
          {!customer && (
            <button className="primary" onClick={() => setEdit({ id: "" })}>
              <Plus size={16} />
              {module === "payments"
                ? "Record payment"
                : `Add ${config.singular}`}
            </button>
          )}
        </div>
      </div>
      <State {...state} />
      {error && (
        <div className="alert" role="alert">
          {error}
        </div>
      )}
      {!state.loading &&
        !state.error &&
        (id ? (
          row ? (
            <section className="card detail">
              <div className="card-heading">
                <button
                  className="text-link"
                  onClick={() =>
                    navigate(`/${user!.role.toLowerCase()}/${module}`)
                  }
                >
                  <ArrowLeft size={15} />
                  Back to {config.title.toLowerCase()}
                </button>
                <div className="heading-actions">
                  {module === "invoices" && (
                    <>
                      <button
                        className="secondary"
                        onClick={() => window.print()}
                      >
                        <Printer size={15} />
                        Preview / print
                      </button>
                      <button className="primary" disabled={busy} onClick={pdf}>
                        <Download size={15} />
                        {demo ? "Save as PDF (print)" : "Download PDF"}
                      </button>
                    </>
                  )}
                  {!customer && (
                    <button className="secondary" onClick={() => setEdit(row)}>
                      <Pencil size={15} />
                      Edit
                    </button>
                  )}
                </div>
              </div>
              <div className="invoice-heading">
                <span className="brand">◉ brightloop</span>
                <div>
                  <h2>{String(row.name)}</h2>
                  {row.status && <Badge value={row.status} />}
                </div>
              </div>
              <dl>
                {config.fields
                  .filter(
                    (f) =>
                      !multiDocument ||
                      !["productId", "quantity", "price", "tax"].includes(
                        f.key,
                      ),
                  )
                  .map((f) => (
                    <div key={f.key}>
                      <dt>{f.label}</dt>
                      <dd>
                        {f.type === "checkbox"
                          ? row[f.key]
                            ? "Yes"
                            : "No"
                          : String(resolve(f.key, row[f.key]) ?? "—")}
                      </dd>
                    </div>
                  ))}
              </dl>
              {["invoices", "quotations", "subscriptions"].includes(module) && (
                <>
                  <div className="table-scroll">
                    <table>
                      <thead>
                        <tr>
                          <th>Product</th>
                          <th>Quantity</th>
                          <th>Unit price</th>
                          <th>Tax</th>
                          <th>Line total</th>
                        </tr>
                      </thead>
                      <tbody>
                        {(
                          row.lines || [
                            {
                              productId: String(row.productId || ""),
                              quantity: Number(row.quantity || 1),
                              price: Number(row.price || 0),
                              tax: Number(row.tax || 0),
                            },
                          ]
                        ).map((line, index) => (
                          <tr key={index}>
                            <td>
                              {String(
                                data.products?.find(
                                  (p) => p.id === line.productId,
                                )?.name ||
                                  line.productId ||
                                  "Product",
                              )}
                            </td>
                            <td>{line.quantity}</td>
                            <td>{money(line.price)}</td>
                            <td>{line.tax}%</td>
                            <td>{money(total(line))}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                  <div className="totals">
                    <span>
                      Subtotal <b>{money(subtotal(row))}</b>
                    </span>
                    <span>
                      Tax <b>{money(taxAmount(row))}</b>
                    </span>
                    <span>
                      Discount <b>{money(row.discount)}</b>
                    </span>
                    <strong>
                      Total <b>{money(total(row))}</b>
                    </strong>
                    {module === "invoices" && (
                      <>
                        <span>
                          Paid{" "}
                          <b>
                            {money(
                              (data.payments || [])
                                .filter((p) => p.invoiceId === row.id)
                                .reduce((s, p) => s + Number(p.amount), 0),
                            )}
                          </b>
                        </span>
                        <strong>
                          Outstanding{" "}
                          <b>
                            {money(
                              Math.max(
                                0,
                                total(row) -
                                  (data.payments || [])
                                    .filter((p) => p.invoiceId === row.id)
                                    .reduce((s, p) => s + Number(p.amount), 0),
                              ),
                            )}
                          </b>
                        </strong>
                      </>
                    )}
                  </div>
                </>
              )}
              {module === "products" && (
                <div className="notice">
                  Calculated sales price: {money(row.price)} + variant
                  additional price. Base margin:{" "}
                  {money(Number(row.price) - Number(row.cost))}.
                  <Link to={`/${user!.role.toLowerCase()}/variants`}>
                    {" "}
                    Manage variants ↗
                  </Link>
                </div>
              )}
              {module === "subscriptions" && !customer && (
                <div className="action-bar">
                  {subscriptionActions(
                    row,
                    data.plans?.find((p) => p.id === row.planId),
                  )?.map((verb) => (
                    <button
                      disabled={busy}
                      className="secondary"
                      key={verb}
                      onClick={() => setConfirmation(verb)}
                    >
                      {verb}
                    </button>
                  ))}
                </div>
              )}
            </section>
          ) : (
            <Empty
              title="Record not found"
              text="This record does not exist or is not accessible to your account."
            />
          )
        ) : module === "plans" && customer ? (
          <div className="plan-grid">
            {rows.map((r) => (
              <section className="card plan-card" key={r.id}>
                <span className="eyebrow">ROOM TO GROW</span>
                <h2>{r.name}</h2>
                <strong>
                  {money(r.price)}
                  <small> / {r.frequency}</small>
                </strong>
                <p>Minimum quantity: {r.quantity || 1}</p>
                <p>
                  {r.Pausepermission
                    ? "Pause when you need to."
                    : "A consistent subscription for your business."}
                </p>
                <Link className="secondary" to={r.id}>
                  View plan details <ArrowUpRight size={16} />
                </Link>
              </section>
            ))}
          </div>
        ) : (
          <section className="card">
            <div className="table-toolbar">
              <label className="search">
                <Search size={16} />
                <input
                  aria-label={`Search ${module}`}
                  value={query}
                  onChange={(e) => {
                    setQuery(e.target.value);
                    setPage(1);
                  }}
                  placeholder={`Search ${config.title.toLowerCase()}…`}
                />
              </label>
              {config.fields.some((f) => f.key === "status") && (
                <select
                  aria-label="Filter by status"
                  value={filter}
                  onChange={(e) => {
                    setFilter(e.target.value);
                    setPage(1);
                  }}
                >
                  <option>All</option>
                  {config.fields
                    .find((f) => f.key === "status")
                    ?.options?.map((s) => (
                      <option key={s}>{s}</option>
                    ))}
                </select>
              )}
            </div>
            <div className="table-scroll">
              <table>
                <thead>
                  <tr>
                    {listFields.map((f) => (
                      <th key={f.key}>{f.label}</th>
                    ))}
                    {config.fields.some((f) => f.key === "status") &&
                      !listFields.some((f) => f.key === "status") && (
                        <th>Status</th>
                      )}
                    {multiDocument && <th>Total</th>}
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {visible.slice((page - 1) * 10, page * 10).map((r) => (
                    <tr key={r.id}>
                      {listFields.map((f, i) => (
                        <td key={f.key}>
                          {f.key === "status" ? (
                            <Badge value={r[f.key]} />
                          ) : i === 0 ? (
                            <Link className="record-link" to={r.id}>
                              {String(r[f.key] || "—")}
                            </Link>
                          ) : ["price", "amount", "cost"].includes(f.key) ? (
                            money(r[f.key])
                          ) : (
                            String(resolve(f.key, r[f.key]) ?? "—")
                          )}
                        </td>
                      ))}
                      {config.fields.some((f) => f.key === "status") &&
                        !listFields.some((f) => f.key === "status") && (
                          <td>
                            <Badge value={r.status} />
                          </td>
                        )}
                      {multiDocument && (
                        <td className="amount">{money(total(r))}</td>
                      )}
                      <td>
                        <div className="row-actions">
                          <Link aria-label={`View ${r.name}`} to={r.id}>
                            <ArrowUpRight size={16} />
                          </Link>
                          {!customer && (
                            <>
                              <button
                                aria-label={`Edit ${r.name}`}
                                onClick={() => setEdit(r)}
                              >
                                <Pencil size={15} />
                              </button>
                              <button
                                aria-label={`Delete ${r.name}`}
                                onClick={() => setDeleting(r)}
                              >
                                <Trash2 size={15} />
                              </button>
                            </>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {!visible.length && (
              <Empty
                title={
                  query
                    ? "No matches found"
                    : `No ${config.title.toLowerCase()} yet`
                }
                text={
                  query
                    ? "Try another search or filter."
                    : "Create your first record to get started."
                }
              />
            )}
            <div className="pagination">
              <span>
                {visible.length} records · Page {page} of{" "}
                {Math.max(1, Math.ceil(visible.length / 10))}
              </span>
              <div>
                <button
                  disabled={page === 1}
                  onClick={() => setPage((p) => p - 1)}
                >
                  Previous
                </button>
                <button
                  disabled={page * 10 >= visible.length}
                  onClick={() => setPage((p) => p + 1)}
                >
                  Next
                </button>
              </div>
            </div>
          </section>
        ))}
      {edit && (
        <RecordEditor
          module={module}
          value={edit}
          onClose={() => setEdit(null)}
        />
      )}
      {deleting && (
        <Modal
          title={`Delete ${config.singular}?`}
          onClose={() => setDeleting(null)}
        >
          <p>
            This will remove {deleting.name}. Referenced records may prevent
            deletion.
          </p>
          <div className="alert">
            {error || "Review this action before continuing."}
          </div>
          <div className="modal-actions">
            <button className="secondary" onClick={() => setDeleting(null)}>
              Cancel
            </button>
            <button
              className="danger"
              disabled={busy}
              onClick={() =>
                execute(async () => {
                  await remove(module, deleting.id);
                  setDeleting(null);
                  if (id) navigate(`/${user!.role.toLowerCase()}/${module}`);
                })
              }
            >
              Delete record
            </button>
          </div>
        </Modal>
      )}
      {confirmation && row && (
        <Modal
          title={`${confirmation} subscription?`}
          onClose={() => setConfirmation("")}
        >
          <p>
            Update {row.name}?{" "}
            {confirmation === "renew"
              ? "The demo extends expiry by one plan billing cycle; API mode uses the backend renewal policy."
              : ""}
          </p>
          {error && <div className="alert">{error}</div>}
          <div className="modal-actions">
            <button className="secondary" onClick={() => setConfirmation("")}>
              Cancel
            </button>
            <button
              className="primary"
              disabled={busy}
              onClick={() =>
                execute(async () => {
                  await action(module, row.id, confirmation);
                  setConfirmation("");
                })
              }
            >
              Confirm {confirmation}
            </button>
          </div>
        </Modal>
      )}
    </>
  );
}
