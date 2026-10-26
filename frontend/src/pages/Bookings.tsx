import { useState, type FormEvent } from "react";
import { api } from "../api/client";
import { useAuth } from "../auth/AuthContext";
import {
  ErrorBox,
  Field,
  LoadState,
  money,
  useResource,
  validDates,
} from "../components/ui";
export function Bookings() {
  const { session } = useAuth();
  const credentials = session!.credentials;
  const resource = useResource(
    (s) => api.bookings(credentials, s),
    [credentials],
  );
  const properties = useResource(
    (s) => api.properties(credentials, "", s),
    [credentials],
  );
  return (
    <>
      <div className="page-heading">
        <div>
          <span className="eyebrow">GUEST DASHBOARD</span>
          <h1>Your bookings</h1>
          <p>View confirmed stays and manage your dates.</p>
        </div>
        <button onClick={resource.reload} disabled={resource.loading}>
          Refresh history
        </button>
      </div>
      <LoadState {...resource} />
      {resource.data &&
        (resource.data.length === 0 ? (
          <div className="panel empty">
            <h2>No bookings yet</h2>
            <p>Your stays will appear here once confirmed.</p>
            <a className="button primary" href="#/properties">
              Explore properties
            </a>
          </div>
        ) : (
          <div className="booking-list">
            {resource.data.map((b) => (
              <article className="panel booking-row" key={b.bookingId}>
                <div>
                  <span className="badge">{b.status.replaceAll("_", " ")}</span>
                  <h2>
                    <a href={"#/bookings/" + encodeURIComponent(b.bookingId)}>
                      {properties.data?.find(
                        (p) => p.propertyId === b.propertyId,
                      )?.propertyName || b.propertyId}
                    </a>
                  </h2>
                  <p>
                    {b.arrivalDate} → {b.departureDate}
                  </p>
                  <p className="hint">{b.bookingId}</p>
                </div>
                <div>
                  <strong>{money(b.totalCharge)}</strong>
                  <p className="hint">Payment: {b.paymentStatus}</p>
                  <a href={"#/bookings/" + encodeURIComponent(b.bookingId)}>
                    View booking →
                  </a>
                </div>
              </article>
            ))}
          </div>
        ))}
    </>
  );
}
export function BookingDetail({ id }: { id: string }) {
  const { session } = useAuth();
  const credentials = session!.credentials;
  const resource = useResource(
    (s) => api.booking(credentials, id, s),
    [credentials, id],
  );
  const properties = useResource(
    (s) => api.properties(credentials, "", s),
    [credentials],
  );
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<unknown>();
  const [notice, setNotice] = useState("");
  async function change(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const data = new FormData(e.currentTarget);
    const arrivalDate = String(data.get("arrivalDate"));
    const departureDate = String(data.get("departureDate"));
    setError(undefined);
    setNotice("");
    if (!validDates(arrivalDate, departureDate)) {
      setError("Departure must be after arrival.");
      return;
    }
    if (
      !window.confirm(
        "Change dates to " +
          arrivalDate +
          " through " +
          departureDate +
          "? Charges will be recalculated at the property’s current base rate.",
      )
    )
      return;
    setBusy(true);
    try {
      await api.changeDates(credentials, id, { arrivalDate, departureDate });
      setNotice("Dates updated. The amounts below are returned by the server.");
      resource.reload();
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  const b = resource.data;
  return (
    <>
      <a href="#/bookings">← Booking history</a>
      <h1>Booking details</h1>
      <LoadState {...resource} />
      {notice && (
        <p role="status" className="success">
          {notice}
        </p>
      )}
      {b && (
        <div className="detail-grid">
          <section className="panel card-body">
            <span className="badge">{b.status.replaceAll("_", " ")}</span>
            <h2>
              {properties.data?.find((p) => p.propertyId === b.propertyId)
                ?.propertyName || b.propertyId}
            </h2>
            <a href={"#/properties/" + encodeURIComponent(b.propertyId)}>
              View property
            </a>
            <dl>
              <dt>Booking ID</dt>
              <dd>{b.bookingId}</dd>
              <dt>Arrival</dt>
              <dd>{b.arrivalDate}</dd>
              <dt>Departure</dt>
              <dd>{b.departureDate}</dd>
              <dt>Payment status</dt>
              <dd>{b.paymentStatus}</dd>
              <dt>Created</dt>
              <dd>{b.createdAt.replace("T", " ")}</dd>
              <dt>Updated</dt>
              <dd>{b.updatedAt.replace("T", " ")}</dd>
            </dl>
            <h3>Charge breakdown</h3>
            <dl>
              {(
                ["baseCharge", "gct", "tef", "gart", "totalCharge"] as const
              ).map((k) => (
                <div
                  className={
                    k === "totalCharge" ? "total charge-line" : "charge-line"
                  }
                  key={k}
                >
                  <dt>
                    {
                      {
                        baseCharge: "Base charge",
                        gct: "GCT",
                        tef: "TEF",
                        gart: "GART",
                        totalCharge: "Total charge",
                      }[k]
                    }
                  </dt>
                  <dd>{money(b[k])}</dd>
                </div>
              ))}
            </dl>
            <p className="hint">
              Amounts are authoritative server totals. Currency is not specified
              by the API. Timestamps are shown in server-local time.
            </p>
          </section>
          <section className="panel booking-box">
            <h2>Change dates</h2>
            <p>
              Changes recalculate charges using the property’s current nightly
              rate. Dates must be available.
            </p>
            <ErrorBox error={error} />
            <form key={b.updatedAt} onSubmit={change}>
              <fieldset disabled={busy}>
                <Field
                  label="New arrival"
                  type="date"
                  name="arrivalDate"
                  defaultValue={b.arrivalDate}
                  required
                />
                <Field
                  label="New departure"
                  type="date"
                  name="departureDate"
                  defaultValue={b.departureDate}
                  required
                />
                <button className="primary">
                  {busy ? "Updating…" : "Update dates"}
                </button>
              </fieldset>
            </form>
          </section>
        </div>
      )}
    </>
  );
}
