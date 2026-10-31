import { useState } from "react";
import { useForm, useFieldArray } from "react-hook-form";
import { Plus, Trash2 } from "lucide-react";
import { useWorkspace } from "../../context/Workspace";
import { modules, money, total } from "../../utils/modules";
import { validateRecord, requiredText } from "../../utils/validation";
import { errorMessage } from "../../services/api";
import { Modal } from "../Shared";
import type { RecordData, LineItem } from "../../types";
type FormValues = RecordData & { lines: LineItem[] };

export default function RecordEditor({
  module,
  value,
  onClose,
}: {
  module: string;
  value: RecordData;
  onClose: () => void;
}) {
  const { data, save, demo } = useWorkspace();
  const config = modules[module];
  const [error, setError] = useState("");
  const {
    register,
    control,
    setValue,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    defaultValues: {
      ...value,
      lines: value.lines || [
        {
          productId: String(value.productId || ""),
          quantity: value.quantity || 1,
          price: value.price || 0,
          tax: value.tax || 0,
        },
      ],
    },
  });
  const multi = ["quotations", "invoices", "subscriptions"].includes(module);
  const {
    fields: lineFields,
    append,
    remove: removeLine,
  } = useFieldArray({ control, name: "lines" });
  const current = watch();
  const submit = handleSubmit(async (v) => {
    setError("");
    try {
      const payload: RecordData = {
        id: value.id,
        ...Object.fromEntries(
          config.fields.map((field) => [field.key, v[field.key]]),
        ),
      };
      if (multi) payload.lines = v.lines;
      if (multi) {
        if (!v.lines?.length) throw new Error("Add at least one product line.");
        Object.assign(payload, v.lines[0]);
      } else delete payload.lines;
      if (module === "subscriptions" && value.id) payload.status = value.status;
      validateRecord(module, payload, data);
      await save(module, payload);
      onClose();
    } catch (e) {
      setError(errorMessage(e));
    }
  });
  return (
    <Modal
      title={`${value.id ? "Edit" : "New"} ${config.singular}`}
      onClose={onClose}
    >
      <form onSubmit={submit}>
        <div className="form-grid">
          {config.fields
            .filter(
              (f) =>
                (!multi ||
                  !["productId", "quantity", "price", "tax"].includes(f.key)) &&
                !(module === "subscriptions" && value.id && f.key === "status"),
            )
            .map((f) => (
              <label
                className={f.type === "textarea" ? "wide" : ""}
                key={f.key}
              >
                {f.label}
                {f.required && f.type !== "checkbox" && " *"}
                {f.type === "select" || f.type?.startsWith("relation:") ? (
                  <select
                    {...register(f.key, {
                      required: f.required ? "Select a value" : false,
                    })}
                  >
                    <option value="">Select…</option>
                    {f.options?.map((s) => (
                      <option key={s}>{s}</option>
                    ))}
                    {f.type.startsWith("relation:") &&
                      data[f.type.split(":")[1]]?.map((r) => (
                        <option key={r.id} value={r.id}>
                          {r.name}
                        </option>
                      ))}
                  </select>
                ) : f.type === "textarea" ? (
                  <textarea
                    rows={3}
                    {...register(f.key, {
                      validate: (v) =>
                        !f.required ||
                        requiredText(v) ||
                        "This field is required",
                      setValueAs: (v) => v.trim(),
                    })}
                  />
                ) : (
                  <input
                    type={f.type || "text"}
                    step={
                      f.type === "number"
                        ? f.key === "quantity" || f.key === "limit"
                          ? "1"
                          : "0.01"
                        : undefined
                    }
                    {...register(f.key, {
                      required:
                        f.type !== "checkbox" && f.required
                          ? "This field is required"
                          : false,
                      ...(f.type === "number"
                        ? {
                            setValueAs: (v: string) =>
                              v === "" ? undefined : Number(v),
                            min: {
                              value: ["quantity", "limit"].includes(f.key)
                                ? 1
                                : f.key === "amount" && module === "payments"
                                  ? 0.01
                                  : 0,
                              message: "Enter a valid positive value",
                            },
                            ...(["tax", "rate"].includes(f.key)
                              ? {
                                  max: {
                                    value: 100,
                                    message: "Maximum is 100%",
                                  },
                                }
                              : {}),
                          }
                        : {}),
                      ...(["text", "email", "tel", "date"].includes(
                        f.type || "text",
                      )
                        ? {
                            setValueAs: (v: string) => v.trim(),
                            validate: (v: unknown) =>
                              !f.required ||
                              requiredText(v) ||
                              "This field is required",
                          }
                        : {}),
                      ...(f.type === "email"
                        ? {
                            pattern: {
                              value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                              message: "Enter a valid email",
                            },
                          }
                        : {}),
                    })}
                  />
                )}
                <small className="error">
                  {String(errors[f.key]?.message || "")}
                </small>
              </label>
            ))}
        </div>
        {multi && (
          <section className="line-editor">
            <div className="line-heading">
              <h3>Product lines</h3>
              <button
                type="button"
                className="secondary"
                onClick={() =>
                  append({ productId: "", quantity: 1, price: 0, tax: 0 })
                }
              >
                <Plus size={14} />
                Add line
              </button>
            </div>
            {lineFields.map((line, index) => (
              <div className="line-row" key={line.id}>
                <label>
                  Product
                  <select
                    {...register(`lines.${index}.productId`, {
                      required: "Select a product",
                      onChange: (e) => {
                        const product = data.products?.find(
                          (p) => p.id === e.target.value,
                        );
                        if (product)
                          setValue(
                            `lines.${index}.price`,
                            Number(product.price || 0),
                          );
                      },
                    })}
                  >
                    <option value="">Select product...</option>
                    {data.products?.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.name}
                      </option>
                    ))}
                  </select>
                </label>
                <label>
                  Quantity
                  <input
                    type="number"
                    step="1"
                    {...register(`lines.${index}.quantity`, {
                      valueAsNumber: true,
                      required: true,
                      min: 1,
                      validate: (v) => Number.isInteger(v),
                    })}
                  />
                </label>
                <label>
                  Unit price
                  <input
                    type="number"
                    step="0.01"
                    {...register(`lines.${index}.price`, {
                      valueAsNumber: true,
                      required: true,
                      min: 0,
                    })}
                  />
                </label>
                <label>
                  Tax %
                  <input
                    type="number"
                    step="0.01"
                    {...register(`lines.${index}.tax`, {
                      valueAsNumber: true,
                      required: true,
                      min: 0,
                      max: 100,
                    })}
                  />
                </label>
                <button
                  className="icon-button"
                  type="button"
                  aria-label={`Remove line ${index + 1}`}
                  disabled={lineFields.length === 1}
                  onClick={() => removeLine(index)}
                >
                  <Trash2 size={16} />
                </button>
              </div>
            ))}
            {errors.lines && (
              <div className="error" role="alert">
                Each line needs a product, a whole quantity of at least 1, a
                nonnegative price and a tax rate between 0 and 100.
              </div>
            )}
          </section>
        )}
        {["quotations", "invoices", "subscriptions"].includes(module) && (
          <div className="calculation">
            Calculated total <strong>{money(total(current))}</strong>
          </div>
        )}
        {module === "variants" && (
          <div className="calculation">
            Product + variant{" "}
            <strong>
              {money(
                Number(
                  data.products?.find((p) => p.id === current.productId)
                    ?.price || 0,
                ) + Number(current.price || 0),
              )}
            </strong>
          </div>
        )}
        {module === "payments" && current.invoiceId && (
          <div className="calculation">
            Outstanding before payment{" "}
            <strong>
              {money(
                total(
                  data.invoices?.find((i) => i.id === current.invoiceId) || {},
                ) -
                  (data.payments || [])
                    .filter(
                      (p) =>
                        p.invoiceId === current.invoiceId && p.id !== value.id,
                    )
                    .reduce((s, p) => s + Number(p.amount), 0),
              )}
            </strong>
          </div>
        )}
        {error && (
          <div className="alert" role="alert">
            {error}
          </div>
        )}
        <div className="modal-actions">
          <span>
            {demo ? "Session-only demo changes" : "Saved through your backend"}
          </span>
          <button type="button" className="secondary" onClick={onClose}>
            Cancel
          </button>
          <button className="primary" disabled={isSubmitting}>
            {isSubmitting ? "Saving…" : "Save changes"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
