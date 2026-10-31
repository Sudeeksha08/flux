import { returnPath } from "../../utils/navigation";
import {
  EMAIL_PATTERN,
  STRONG_PASSWORD,
  requiredText,
} from "../../utils/validation";
import { useState } from "react";
import { Link, Navigate, useLocation, useSearchParams } from "react-router-dom";
import { useForm } from "react-hook-form";
import { ArrowUpRight, LoaderCircle } from "lucide-react";
import { useWorkspace } from "../../context/Workspace";
import { authService } from "../../services/auth";
import { errorMessage } from "../../services/api";
import type { Role } from "../../types";
export default function Auth() {
  const { user, authLoading, login, enterDemo } = useWorkspace();
  const location = useLocation();
  const path = location.pathname;
  const [params] = useSearchParams();
  const mode = path.includes("register")
    ? "register"
    : path.includes("forgot")
      ? "forgot"
      : path.includes("reset")
        ? "reset"
        : "login";
  const [error, setError] = useState(""),
    [success, setSuccess] = useState("");
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    getValues,
  } = useForm<Record<string, string>>();
  if (authLoading)
    return (
      <div className="empty" role="status">
        Restoring your session...
      </div>
    );
  if (user)
    return <Navigate to={returnPath(user, location.state?.from)} replace />;
  const title = {
    login: "Welcome back.",
    register: "Make room for growth.",
    forgot: "Let’s get you back in.",
    reset: "A fresh start.",
  }[mode];
  return (
    <div className="auth">
      <section className="auth-story">
        <Link className="brand" to="/">
          ◉ brightloop<span>®</span>
        </Link>
        <div>
          <span className="eyebrow">LESS ADMIN. MORE POSSIBILITY.</span>
          <h1>
            Good business.
            <br />
            On repeat.
          </h1>
          <p>
            One thoughtful workspace for your customers, subscriptions, and
            everything in between.
          </p>
          <div className="orbit">
            <div>
              ↗
              <small>
                Built for your
                <br />
                next chapter.
              </small>
            </div>
          </div>
        </div>
        <small>THE SUBSCRIPTION WORKSPACE</small>
      </section>
      <section className="auth-form">
        <div className="auth-inner">
          <span className="eyebrow">YOUR WORK, CONNECTED</span>
          <h2>{title}</h2>
          <p>
            {mode === "login"
              ? "Sign in to your Brightloop workspace."
              : "Keep your subscription business moving."}
          </p>
          <form
            onSubmit={handleSubmit(async (v) => {
              setError("");
              setSuccess("");
              try {
                if (mode === "login") await login(v.email, v.password);
                if (mode === "register") {
                  if (!(await authService.emailAvailable(v.email)))
                    throw new Error("This email is already registered.");
                  await authService.register({
                    name: v.name,
                    email: v.email,
                    password: v.password,
                  });
                  setSuccess("Account created. You can now sign in.");
                }
                if (mode === "forgot") {
                  await authService.forgot({ email: v.email });
                  setSuccess(
                    "If this account exists, a reset email has been requested.",
                  );
                }
                if (mode === "reset") {
                  if (!params.get("token"))
                    throw new Error("A valid reset link is required.");
                  await authService.reset({
                    token: params.get("token")!,
                    password: v.password,
                  });
                  setSuccess("Password reset. You can now sign in.");
                }
              } catch (e) {
                setError(errorMessage(e));
              }
            })}
          >
            {mode === "register" && (
              <label>
                Full name
                <input
                  autoComplete="name"
                  {...register("name", {
                    required: "Enter your name",
                    validate: (v) => requiredText(v) || "Enter your name",
                    setValueAs: (v) => v.trim(),
                  })}
                />
                {errors.name && (
                  <small className="error">{errors.name.message}</small>
                )}
              </label>
            )}
            {mode !== "reset" && (
              <label>
                Email address
                <input
                  type="email"
                  autoComplete="email"
                  placeholder="you@company.com"
                  {...register("email", {
                    required: "Enter your email",
                    setValueAs: (v) => v.trim(),
                    pattern: {
                      value: EMAIL_PATTERN,
                      message: "Enter a valid email",
                    },
                  })}
                />
                {errors.email && (
                  <small className="error">{errors.email.message}</small>
                )}
              </label>
            )}
            {mode !== "forgot" && (
              <label>
                Password
                <input
                  type="password"
                  autoComplete={
                    mode === "login" ? "current-password" : "new-password"
                  }
                  {...register("password", {
                    required: "Enter a password",
                    ...(mode !== "login"
                      ? {
                          pattern: {
                            value: STRONG_PASSWORD,
                            message:
                              "Use 12+ characters with upper/lowercase, number and symbol.",
                          },
                        }
                      : {}),
                  })}
                />
                {errors.password && (
                  <small className="error">{errors.password.message}</small>
                )}
              </label>
            )}
            {(mode === "register" || mode === "reset") && (
              <label>
                Confirm password
                <input
                  type="password"
                  autoComplete="new-password"
                  {...register("confirm", {
                    required: "Confirm your password",
                    validate: (v) =>
                      v === getValues("password") || "Passwords must match",
                  })}
                />
                <small className="error">{errors.confirm?.message}</small>
              </label>
            )}
            {mode === "login" && (
              <Link className="forgot" to="/forgot-password">
                Forgot password?
              </Link>
            )}
            {error && (
              <div className="alert" role="alert">
                {error}
              </div>
            )}
            {success && <div className="notice">{success}</div>}
            <button className="primary" disabled={isSubmitting}>
              {isSubmitting ? (
                <LoaderCircle className="spin" />
              ) : mode === "login" ? (
                "Sign in"
              ) : mode === "register" ? (
                "Create account"
              ) : mode === "forgot" ? (
                "Send reset link"
              ) : (
                "Reset password"
              )}
              <ArrowUpRight size={17} />
            </button>
          </form>
          <p className="auth-links">
            {mode === "login" ? (
              <>
                New to Brightloop? <Link to="/register">Create an account</Link>
              </>
            ) : (
              <Link to="/login">Back to sign in</Link>
            )}
          </p>
          <div className="demo-picker">
            <span>EXPLORE THE WORKSPACE</span>
            <p>Preview with sample data. Changes stay in this session.</p>
            <div>
              {(["ADMIN", "STAFF", "CUSTOMER"] as Role[]).map((r) => (
                <button key={r} onClick={() => enterDemo(r)}>
                  {r.toLowerCase()} <ArrowUpRight size={13} />
                </button>
              ))}
            </div>
          </div>
        </div>
        <small>
          © {new Date().getFullYear()} Brightloop. A little more in sync.
        </small>
      </section>
    </div>
  );
}
