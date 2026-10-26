import { useState, type FormEvent } from "react";
import { api, ApiError } from "../api/client";
import { useAuth } from "../auth/AuthContext";
import {
  Field,
  ErrorBox,
  LoadState,
  money,
  useResource,
  validDates,
} from "../components/ui";
export function Properties({ owner = false }: { owner?: boolean }) {
  const { session } = useAuth();
  const credentials = session!.credentials;
  const [query, setQuery] = useState("");
  const [error, setError] = useState<unknown>();
  const resource = useResource(
    (s) => api.properties(credentials, owner ? "/mine" : query, s),
    [credentials, owner, query],
  );
  function search(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const data = new FormData(e.currentTarget);
    const arrivalDate = String(data.get("arrivalDate"));
    const departureDate = String(data.get("departureDate"));
    if (!validDates(arrivalDate, departureDate)) {
      setError("Departure must be after arrival.");
      return;
    }
    setError(undefined);
    setQuery(
      "/available?" + new URLSearchParams({ arrivalDate, departureDate }),
    );
  }
  return (
    <>
      <div className="page-heading">
        <div>
          <span className="eyebrow">
            {owner ? "OWNER DASHBOARD" : "FIND YOUR NEXT STAY"}
          </span>
          <h1>{owner ? "Your properties" : "Somewhere to feel at home"}</h1>
          <p>
            {owner
              ? "Create and manage the places you share."
              : "Explore properties or check availability for your dates."}
          </p>
        </div>
        {owner && (
          <a className="button primary" href="#/properties/new">
            Add property
          </a>
        )}
      </div>
      {!owner && (
        <form className="panel search" onSubmit={search}>
          <Field label="Arrival" name="arrivalDate" type="date" required />
          <Field label="Departure" name="departureDate" type="date" required />
          <button className="primary" disabled={resource.loading}>
            Check availability
          </button>
          <button
            type="button"
            onClick={() => {
              setQuery("");
              setError(undefined);
            }}
          >
            Show all
          </button>
        </form>
      )}
      <ErrorBox error={error} />
      <LoadState {...resource} />
      {query && (
        <p className="hint">
          Availability is informational until your booking is confirmed. Arrival
          on an existing confirmed booking’s departure date is unavailable.
        </p>
      )}
      {resource.data && (
        <>
          <p className="small">
            {resource.data.length} {query ? "available " : ""}properties
          </p>
          {resource.data.length === 0 ? (
            <div className="panel empty">
              <h2>{owner ? "No properties yet" : "No properties found"}</h2>
              <p>
                {owner
                  ? "Add your first property to get started."
                  : "Try different dates or check back later."}
              </p>
            </div>
          ) : (
            <div className="cards">
              {resource.data.map((p) => (
                <article className="panel property-card" key={p.propertyId}>
                  <div
                    className="property-art"
                    aria-label="Property placeholder"
                  >
                    <span aria-hidden="true">⌂</span>
                  </div>
                  <div className="card-body">
                    <p className="eyebrow">
                      {p.address.city}, {p.address.country}
                    </p>
                    <h2>
                      <a
                        href={
                          "#/properties/" + encodeURIComponent(p.propertyId)
                        }
                      >
                        {p.propertyName}
                      </a>
                    </h2>
                    <p className="clamp">
                      {p.description || "Property details available inside."}
                    </p>
                    <div className="card-bottom">
                      <span>
                        <strong>{money(p.baseRate)}</strong> / night
                      </span>
                      <a
                        href={
                          "#/properties/" +
                          encodeURIComponent(p.propertyId) +
                          (owner ? "/edit" : "")
                        }
                      >
                        {owner ? "Manage property" : "View property"} →
                      </a>
                    </div>
                  </div>
                </article>
              ))}
            </div>
          )}
        </>
      )}
    </>
  );
}
export function PropertyDetail({ id }: { id: string }) {
  const { session } = useAuth();
  const credentials = session!.credentials;
  const resource = useResource(
    (s) => api.property(credentials, id, s),
    [credentials, id],
  );
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<unknown>();
  const [uncertain, setUncertain] = useState(false);
  async function book(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const data = new FormData(e.currentTarget);
    const arrivalDate = String(data.get("arrivalDate"));
    const departureDate = String(data.get("departureDate"));
    setError(undefined);
    if (!validDates(arrivalDate, departureDate)) {
      setError("Departure must be after arrival.");
      return;
    }
    if (
      !window.confirm(
        "Confirm booking from " +
          arrivalDate +
          " to " +
          departureDate +
          "? This immediately creates a confirmed booking. Charges are calculated by the server; there is no price quote.",
      )
    )
      return;
    setBusy(true);
    try {
      const booking = await api.createBooking(credentials, {
        propertyId: id,
        arrivalDate,
        departureDate,
      });
      location.hash = "/bookings/" + encodeURIComponent(booking.bookingId);
    } catch (e) {
      setError(e);
      // Never retry a possibly committed write. History is the recovery path.
      if (!(e instanceof ApiError) || e.status === 0 || e.status >= 500)
        setUncertain(true);
    } finally {
      setBusy(false);
    }
  }
  const p = resource.data;
  return (
    <>
      <a href="#/properties">← Browse properties</a>
      <LoadState {...resource} />
      {p && (
        <>
          <div className="page-heading">
            <div>
              <span className="eyebrow">
                {p.address.city}, {p.address.country}
              </span>
              <h1>{p.propertyName}</h1>
              <p>
                {p.address.street}
                {p.address.suite ? ", " + p.address.suite : ""},{" "}
                {p.address.city}, {p.address.state} {p.address.postalCode},{" "}
                {p.address.country}
              </p>
            </div>
          </div>
          <div className="detail-grid">
            <section className="panel">
              <div
                className="property-art large"
                aria-label="Property placeholder"
              >
                <span aria-hidden="true">⌂</span>
              </div>
              <div className="card-body">
                <h2>About this property</h2>
                <p className="preserve">
                  {p.description || "No description provided."}
                </p>
                <dl>
                  <dt>House size</dt>
                  <dd>{p.houseSize} sq ft</dd>
                  <dt>Land size</dt>
                  <dd>{p.landSize} sq ft</dd>
                  <dt>Coordinates</dt>
                  <dd>
                    {p.latitude}, {p.longitude}
                  </dd>
                  <dt>Property ID</dt>
                  <dd>{p.propertyId}</dd>
                </dl>
              </div>
            </section>
            <section className="panel booking-box">
              <h2>
                {money(p.baseRate)} <small>/ night</small>
              </h2>
              <p>
                Amounts are shown as provided by the property. No currency code
                is supplied.
              </p>
              {session!.role === "guest" ? (
                <>
                  <h3>Book your stay</h3>
                  <p className="hint">
                    Final charges are calculated on confirmation. Availability
                    can change before you submit.
                  </p>
                  <ErrorBox error={error} />
                  {uncertain ? (
                    <div className="error">
                      <p>
                        Your booking may have succeeded. Check your latest
                        history before attempting another booking.
                      </p>
                      <a className="button" href="#/bookings">
                        Check booking history
                      </a>
                    </div>
                  ) : (
                    <form onSubmit={book}>
                      <fieldset disabled={busy}>
                        <Field
                          label="Arrival"
                          name="arrivalDate"
                          type="date"
                          required
                        />
                        <Field
                          label="Departure"
                          name="departureDate"
                          type="date"
                          required
                        />
                        <button className="primary full">
                          {busy ? "Confirming…" : "Confirm booking"}
                        </button>
                      </fieldset>
                    </form>
                  )}
                </>
              ) : (
                <p>Booking is available to guest accounts.</p>
              )}
            </section>
          </div>
        </>
      )}
    </>
  );
}
export function PropertyEditor({ id }: { id?: string }) {
  const { session } = useAuth();
  const credentials = session!.credentials;
  // Management data comes from /mine; property responses contain no ownership field.
  const resource = useResource(
    (s) => api.properties(credentials, "/mine", s),
    [credentials, id],
  );
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<unknown>();
  const property = resource.data?.find((p) => p.propertyId === id);
  async function save(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(undefined);
    const data = new FormData(e.currentTarget);
    const v = (k: string) => String(data.get(k) || "");
    const rate = v("baseRate");
    if (
      !/^\d+(\.\d{1,2})?$/.test(rate) ||
      rate.replace(".", "").replace(/^0+/, "").length > 12
    ) {
      setError(
        "Base rate must have at most 2 decimal places and 12 significant digits.",
      );
      return;
    }
    const address = {
      street: v("street"),
      suite: v("suite"),
      city: v("city"),
      state: v("state"),
      postalCode: v("postalCode"),
      country: v("country"),
    };
    setBusy(true);
    try {
      await api.saveProperty(
        credentials,
        {
          propertyName: v("propertyName"),
          description: v("description"),
          address,
          latitude: Number(v("latitude")),
          longitude: Number(v("longitude")),
          houseSize: Number(v("houseSize")),
          landSize: Number(v("landSize")),
          baseRate: Number(rate),
        },
        id,
      );
      location.hash = "/owner";
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  async function remove() {
    if (
      !id ||
      !window.confirm(
        "Permanently delete " +
          property?.propertyName +
          "? This cannot be undone.",
      )
    )
      return;
    setBusy(true);
    setError(undefined);
    try {
      await api.deleteProperty(credentials, id);
      location.hash = "/owner";
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  return (
    <>
      <a href="#/owner">← Your properties</a>
      <h1>{id ? "Edit property" : "Add a property"}</h1>
      <LoadState {...resource} />
      {resource.data &&
        (id && !property ? (
          <div className="error">
            Property not found in your owner dashboard. It may be missing or
            belong to another owner.
          </div>
        ) : (
          <section className="panel editor">
            <ErrorBox error={error} />
            <form onSubmit={save}>
              <fieldset disabled={busy}>
                <Field
                  label="Property name"
                  name="propertyName"
                  defaultValue={property?.propertyName}
                  required
                  maxLength={100}
                />
                <label>
                  Description
                  <textarea
                    name="description"
                    defaultValue={property?.description || ""}
                    maxLength={2000}
                    rows={4}
                  />
                </label>
                <h2>Address</h2>
                <div className="form-grid">
                  {(
                    [
                      "street",
                      "suite",
                      "city",
                      "state",
                      "postalCode",
                      "country",
                    ] as const
                  ).map((k) => (
                    <Field
                      key={k}
                      label={
                        k === "postalCode"
                          ? "Postal code (optional)"
                          : k === "suite"
                            ? "Suite (optional)"
                            : k[0].toUpperCase() + k.slice(1)
                      }
                      name={k}
                      defaultValue={property?.address[k] || ""}
                      required={!["suite", "postalCode"].includes(k)}
                      maxLength={k === "postalCode" ? 20 : 100}
                    />
                  ))}
                </div>
                <h2>Property information</h2>
                <div className="form-grid">
                  {(
                    [
                      "latitude",
                      "longitude",
                      "houseSize",
                      "landSize",
                      "baseRate",
                    ] as const
                  ).map((k) => (
                    <Field
                      key={k}
                      label={
                        {
                          latitude: "Latitude",
                          longitude: "Longitude",
                          houseSize: "House size (sq ft)",
                          landSize: "Land size (sq ft)",
                          baseRate: "Nightly base rate",
                        }[k]
                      }
                      name={k}
                      type="number"
                      required
                      step={k === "baseRate" ? "0.01" : "any"}
                      min={
                        k === "latitude" ? -90 : k === "longitude" ? -180 : 0
                      }
                      max={
                        k === "latitude"
                          ? 90
                          : k === "longitude"
                            ? 180
                            : undefined
                      }
                      defaultValue={property?.[k]}
                    />
                  ))}
                </div>
                <div className="actions">
                  <button className="primary">
                    {busy ? "Saving…" : "Save property"}
                  </button>
                  <a className="button" href="#/owner">
                    Back to dashboard
                  </a>
                </div>
              </fieldset>
            </form>
            {id && (
              <div className="danger-zone">
                <h3>Delete property</h3>
                <p>
                  Properties referenced by bookings or reviews cannot be
                  deleted.
                </p>
                <button className="danger" disabled={busy} onClick={remove}>
                  Delete property
                </button>
              </div>
            )}
          </section>
        ))}
    </>
  );
}
