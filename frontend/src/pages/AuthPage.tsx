import { useState, type FormEvent } from "react";
import { api, ApiError } from "../api/client";
import { useAuth } from "../auth/AuthContext";
import { ErrorBox, Field } from "../components/ui";
import type { Registration } from "../types/api";
export default function AuthPage() {
  const { login } = useAuth();
  const [mode, setMode] = useState<"login" | "register" | "activate">("login");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [kind, setKind] = useState<"GUEST" | "OWNER">("GUEST");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<unknown>();
  const [notice, setNotice] = useState("");
  const [retry, setRetry] = useState<Registration>();
  const changeMode = (next: typeof mode) => {
    setMode(next);
    setError(undefined);
    setNotice("");
  };
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const value = (key: string) => String(form.get(key) || "");
    setBusy(true);
    setError(undefined);
    setNotice("");
    try {
      if (mode === "login") await login({ email, password });
      else if (mode === "activate") {
        await api.activate({ email, password, code: value("code") });
        setMode("login");
        setNotice("Account activated. You can now log in.");
        setRetry(undefined);
      } else {
        if (
          new TextEncoder().encode(password).length > 72 ||
          !/^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[@#$%^&+=!]).{8,}$/u.test(
            password,
          )
        )
          throw new Error(
            "Use at least 8 characters with uppercase, lowercase, a number and a symbol from @#$%^&+=!. Maximum 72 UTF-8 bytes.",
          );
        if (password !== value("confirm"))
          throw new Error("Passwords do not match.");
        const preferences = value("preferences")
          .split("\n")
          .map((p) => p.trim())
          .filter(Boolean);
        if (preferences.length > 50 || preferences.some((p) => p.length > 200))
          throw new Error(
            "Use at most 50 preferences, each at most 200 characters.",
          );
        const today = new Date();
        const localToday = [
          today.getFullYear(),
          String(today.getMonth() + 1).padStart(2, "0"),
          String(today.getDate()).padStart(2, "0"),
        ].join("-");
        if (kind === "GUEST" && value("dob") >= localToday)
          throw new Error("Date of birth must be before today.");
        const input: Registration = {
          accountType: kind,
          email,
          password,
          firstName: value("firstName"),
          lastName: value("lastName"),
          contactNum: value("contactNum"),
          gender: value("gender"),
          ...(kind === "GUEST"
            ? {
                nationality: value("nationality"),
                dob: value("dob"),
                preferences,
              }
            : {}),
        };
        setRetry(input);
        await api.register(input);
        setMode("activate");
        setNotice(
          "Check your email for a six-digit activation code. It expires in ten minutes.",
        );
      }
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  async function resend() {
    if (!retry) return;
    setBusy(true);
    setError(undefined);
    try {
      await api.register(retry);
      setMode("activate");
      setNotice("A fresh activation code has been sent. Check your email.");
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  return (
    <div className="auth-layout">
      <section className="intro">
        <span className="eyebrow">OSYNTRA BOOKING PLATFORM</span>
        <h1>
          A place for your
          <br />
          next chapter.
        </h1>
        <p>Find a stay, choose your dates, and make yourself at home.</p>
        <div className="intro-art" aria-hidden="true">
          ⌂
        </div>
        <p className="small">Simple stays. Thoughtfully managed.</p>
      </section>
      <section className="panel auth-panel">
        <nav className="tabs" aria-label="Account">
          <button
            disabled={busy}
            aria-current={mode === "login" ? "page" : undefined}
            onClick={() => changeMode("login")}
          >
            Log in
          </button>
          <button
            disabled={busy}
            aria-current={mode === "register" ? "page" : undefined}
            onClick={() => changeMode("register")}
          >
            Register
          </button>
          <button
            disabled={busy}
            aria-current={mode === "activate" ? "page" : undefined}
            onClick={() => changeMode("activate")}
          >
            Activate
          </button>
        </nav>
        <h2>
          {mode === "login"
            ? "Welcome back"
            : mode === "register"
              ? "Create your account"
              : "Activate your account"}
        </h2>
        <p>
          {mode === "login"
            ? "Log in to browse properties and manage your stays."
            : mode === "register"
              ? "Choose how you will use Osyntra."
              : "Enter the code sent to your email. Codes expire after ten minutes."}
        </p>
        {notice && (
          <p className="success" role="status">
            {notice}
          </p>
        )}
        <ErrorBox error={error} />
        {mode === "login" &&
          error instanceof ApiError &&
          error.status === 403 && (
            <button
              className="text-button"
              onClick={() => changeMode("activate")}
            >
              Go to account activation
            </button>
          )}
        <form onSubmit={submit}>
          <fieldset disabled={busy}>
            {mode === "register" && (
              <>
                <label>
                  Account type
                  <select
                    value={kind}
                    onChange={(e) => setKind(e.target.value as typeof kind)}
                  >
                    <option value="GUEST">Guest — book a stay</option>
                    <option value="OWNER">Owner — manage properties</option>
                  </select>
                </label>
                <div className="form-grid">
                  <Field
                    label="First name"
                    name="firstName"
                    required
                    maxLength={30}
                    autoComplete="given-name"
                  />
                  <Field
                    label="Last name"
                    name="lastName"
                    required
                    maxLength={30}
                    autoComplete="family-name"
                  />
                </div>
              </>
            )}
            <Field
              label="Email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              maxLength={80}
              autoComplete="username"
            />
            <Field
              label="Password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoComplete={
                mode === "register" ? "new-password" : "current-password"
              }
            />
            {mode === "register" && (
              <>
                <p className="hint">
                  8+ characters: uppercase, lowercase, number and @#$%^&amp;+=!.
                  Maximum 72 UTF-8 bytes.
                </p>
                <Field
                  label="Confirm password"
                  name="confirm"
                  type="password"
                  required
                  autoComplete="new-password"
                />
                <Field
                  label="Contact number"
                  name="contactNum"
                  type="tel"
                  required
                  maxLength={30}
                  autoComplete="tel"
                />
                <label>
                  Gender
                  <select name="gender">
                    <option value="NOT_SPECIFIED">Not specified</option>
                    <option value="FEMALE">Female</option>
                    <option value="MALE">Male</option>
                  </select>
                </label>
                {kind === "GUEST" && (
                  <>
                    <div className="form-grid">
                      <Field
                        label="Nationality"
                        name="nationality"
                        required
                        maxLength={80}
                      />
                      <Field
                        label="Date of birth"
                        name="dob"
                        type="date"
                        required
                      />
                    </div>
                    <label>
                      Preferences (optional, one per line)
                      <textarea name="preferences" rows={3} />
                    </label>
                  </>
                )}
              </>
            )}
            {mode === "activate" && (
              <Field
                label="Six-digit activation code"
                name="code"
                inputMode="numeric"
                pattern="[0-9]{6}"
                maxLength={6}
                required
                autoComplete="one-time-code"
              />
            )}
            <button className="primary full" type="submit">
              {busy
                ? "Please wait…"
                : mode === "login"
                  ? "Log in"
                  : mode === "register"
                    ? "Create account"
                    : "Activate account"}
            </button>
          </fieldset>
        </form>
        {retry && mode !== "login" && (
          <>
            <p className="hint">
              Delivery failed or code expired? Retry the original registration
              to send a fresh code. Existing profile details will not change.
            </p>
            <button disabled={busy} onClick={resend}>
              Retry registration / send code
            </button>
          </>
        )}
        <p className="hint">
          For your privacy, refreshing this page requires a new login.
        </p>
      </section>
    </div>
  );
}
