import { EMAIL_PATTERN, requiredText } from "../../utils/validation";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { useWorkspace } from "../../context/Workspace";
import { errorMessage } from "../../services/api";
export default function Settings({ profile = false }: { profile?: boolean }) {
  const { user, demo, updateProfile, logout } = useWorkspace();
  const [error, setError] = useState("");
  const {
    register,
    handleSubmit,
    formState: { isSubmitting, errors },
  } = useForm({ defaultValues: { name: user!.name, email: user!.email } });
  return (
    <>
      <div className="page-heading">
        <div>
          <span className="eyebrow">A WORKSPACE THAT FEELS LIKE YOU</span>
          <h1>{profile ? "My profile" : "Settings"}</h1>
          <p>Your account and workspace essentials.</p>
        </div>
      </div>
      <section className="card settings">
        <h2>Personal details</h2>
        <form
          onSubmit={handleSubmit(async (v) => {
            setError("");
            try {
              await updateProfile(v);
            } catch (e) {
              setError(errorMessage(e));
            }
          })}
        >
          <label>
            Full name
            <input
              {...register("name", {
                required: "Enter your name",
                setValueAs: (v) => v.trim(),
                validate: (v) => requiredText(v) || "Enter your name",
              })}
            />
            <small className="error">{errors.name?.message}</small>
          </label>
          <label>
            Email
            <input
              type="email"
              {...register("email", {
                required: "Enter your email",
                setValueAs: (v) => v.trim(),
                pattern: {
                  value: EMAIL_PATTERN,
                  message: "Enter a valid email",
                },
              })}
            />
            <small className="error">{errors.email?.message}</small>
          </label>
          {error && <div className="alert">{error}</div>}
          <button className="primary" disabled={isSubmitting}>
            Save profile
          </button>
        </form>
        <hr />
        <h2>Workspace guide</h2>
        <p>
          Use the sidebar to manage records. Open a record for details, edit it
          using validated forms, or export your filtered view as CSV.
        </p>
        <p>
          Subscription actions follow the lifecycle: Draft → Confirmed → Active
          → Paused or Closed. Renewals extend eligible subscriptions. Plans and
          backend policies govern action availability.
        </p>
        <p>
          Payments may be partial. Invoice details calculate the remaining
          balance from recorded payments.
        </p>
        <h2>Connection</h2>
        <p>
          {demo
            ? "Demo mode uses sample records in memory. Sign out and log in to connect to your backend."
            : `API endpoint: ${import.meta.env.VITE_API_BASE_URL || "/api"}`}
        </p>
        <button className="secondary" onClick={logout}>
          Sign out
        </button>
      </section>
    </>
  );
}
