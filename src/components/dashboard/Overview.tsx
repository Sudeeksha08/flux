import { dashboardResources } from "../../utils/navigation";
import { canAccess } from "../../utils/business";
import { useState } from "react";
import { Link } from "react-router-dom";
import {
  ArrowUpRight,
  ArrowRight,
  Users,
  Repeat,
  CircleDollarSign,
  Clock,
  Receipt,
  Package,
  CalendarDays,
  Download,
} from "lucide-react";
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  CartesianGrid,
} from "recharts";
import { useWorkspace } from "../../context/Workspace";
import { useRecords } from "../../hooks/useRecords";
import { State, Empty, Badge } from "../Shared";
import { money, total } from "../../utils/modules";
import { downloadCsv } from "../../utils/export";
export default function Overview({ reports = false }: { reports?: boolean }) {
  const { data, user, demo } = useWorkspace();
  const customer = user!.role === "CUSTOMER";
  const keys = dashboardResources(user!.role);
  const state = useRecords(keys);
  const [range, setRange] = useState("year");
  const [query, setQuery] = useState("");
  const own = (key: string) =>
    (data[key] || []).filter(
      (r) => !customer || r.customerId === user!.id || key === "plans",
    );
  const subscriptions = own("subscriptions"),
    invoices = own("invoices"),
    payments = own("payments");
  const paid = (id: unknown) =>
    payments
      .filter((p) => p.invoiceId === id)
      .reduce((s, p) => s + Number(p.amount), 0);
  const outstanding = invoices.reduce(
    (s, i) => s + Math.max(0, total(i) - paid(i.id)),
    0,
  );
  const now = new Date(),
    start = new Date(
      now.getFullYear(),
      range === "month" ? now.getMonth() : 0,
      1,
    );
  const periodPayments = payments.filter(
    (p) => new Date(String(p.date)) >= start && new Date(String(p.date)) <= now,
  );
  const revenue = periodPayments.reduce((s, p) => s + Number(p.amount), 0);
  const chart = Array.from(
    { length: range === "month" ? now.getDate() : 12 },
    (_, i) => {
      const d =
        range === "month"
          ? new Date(now.getFullYear(), now.getMonth(), i + 1)
          : new Date(now.getFullYear(), i, 1);
      return {
        name:
          range === "month"
            ? String(i + 1)
            : d.toLocaleString("en", { month: "short" }),
        revenue: periodPayments
          .filter((p) => {
            const x = new Date(String(p.date));
            return range === "month"
              ? x.getDate() === i + 1
              : x.getMonth() === i;
          })
          .reduce((s, p) => s + Number(p.amount), 0),
        growth: subscriptions.filter((s) => {
          const x = new Date(String(s.from));
          return (
            x >= start &&
            x <= now &&
            (range === "month" ? x.getDate() === i + 1 : x.getMonth() === i)
          );
        }).length,
      };
    },
  );
  const stats = [
    {
      title: customer ? "My subscriptions" : "Active subscriptions",
      value: subscriptions.filter((s) => s.status === "Active").length,
      icon: Repeat,
      note: "Currently active",
      link: "subscriptions",
    },
    {
      title: customer ? "Payments made" : "Collected revenue",
      value: money(revenue),
      icon: CircleDollarSign,
      note: "In the selected period",
      link: "payments",
    },
    {
      title: "Outstanding balance",
      value: money(outstanding),
      icon: Clock,
      note: "After partial payments",
      link: "invoices",
    },
    ...(customer
      ? []
      : [
          {
            title: "Total customers",
            value: (data.customers || []).length,
            icon: Users,
            note: "Across your workspace",
            link: "customers",
          },
          {
            title: "Overdue invoices",
            value: invoices.filter(
              (i) =>
                String(i.due || "9999-12-31") <
                  `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}` &&
                total(i) > paid(i.id),
            ).length,
            icon: Receipt,
            note: "Awaiting settlement",
            link: "invoices",
          },
          {
            title: "Total products",
            value: (data.products || []).length,
            icon: Package,
            note: "In your catalog",
            link: "products",
          },
        ]),
  ];
  const statusColors: Record<string, string> = {
    Active: "#295e48",
    Confirmed: "#a2bfae",
    Draft: "#e5d6ae",
    Quotation: "#c5b37c",
    Paused: "#bfcb92",
    Closed: "#e8ece8",
  };
  const statusData = [
    "Active",
    "Confirmed",
    "Draft",
    "Quotation",
    "Paused",
    "Closed",
  ]
    .map((name) => ({
      name,
      value: subscriptions.filter((s) => s.status === name).length,
    }))
    .filter((s) => s.value);
  return (
    <>
      <div className="page-heading">
        <div>
          <span className="eyebrow">
            {reports ? "THE BIGGER PICTURE" : "YOUR BUSINESS, AT A GLANCE"}
          </span>
          <h1>
            {reports
              ? "Reports & analytics"
              : `A brighter overview${customer ? "" : "."}`}
            <span className="heading-dot" />
          </h1>
          <p>
            {customer
              ? `Welcome back, ${user!.name.split(" ")[0]}. Here’s your subscription snapshot.`
              : "A little clarity for your next big move. Here’s where things stand."}
          </p>
        </div>
        <div className="heading-actions">
          <label className="date-select">
            <CalendarDays size={15} />
            <select
              aria-label="Reporting period"
              value={range}
              onChange={(e) => setRange(e.target.value)}
            >
              <option value="year">This year · {now.getFullYear()}</option>
              <option value="month">This month</option>
            </select>
          </label>
          <button
            className="secondary"
            onClick={() => downloadCsv("brightloop-report", chart)}
          >
            <Download size={15} />
            Export
          </button>
        </div>
      </div>
      <State {...state} />
      {!state.loading && !state.error && (
        <>
          <div className="stats">
            {stats
              .filter((s) => canAccess(user!.role, s.link, user!.permissions))
              .map(({ title, value, icon: Icon, note, link }, i) => (
                <Link
                  to={`../${link}`}
                  relative="path"
                  className={`stat ${i === 0 ? "featured" : ""}`}
                  key={title}
                >
                  <div>
                    <span>{title}</span>
                    <Icon size={19} />
                  </div>
                  <strong>{value}</strong>
                  <small>
                    <span className="mini-dot" />
                    {note}
                    <ArrowUpRight size={14} />
                  </small>
                </Link>
              ))}
          </div>
          <div className="chart-grid">
            <section className="card revenue-card">
              <div className="card-heading">
                <div>
                  <h2>
                    {reports ? "Subscription growth" : "Revenue overview"}
                  </h2>
                  <p>
                    {reports
                      ? "New subscriptions by start date"
                      : "A clearer picture of your collected payments"}
                  </p>
                </div>
                <span className="legend">
                  <i />
                  {reports ? "Subscriptions" : "Revenue"}
                </span>
              </div>
              <div className="chart-number">
                {reports
                  ? chart.reduce((sum, point) => sum + point.growth, 0)
                  : money(revenue)}
                <small>{range === "year" ? "this year" : "this month"}</small>
              </div>
              <div className="chart">
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart
                    data={chart}
                    margin={{ top: 10, right: 15, bottom: 0, left: 0 }}
                  >
                    <defs>
                      <linearGradient id="revenue" x1="0" y1="0" x2="0" y2="1">
                        <stop
                          offset="0%"
                          stopColor="#317259"
                          stopOpacity={0.22}
                        />
                        <stop
                          offset="100%"
                          stopColor="#317259"
                          stopOpacity={0}
                        />
                      </linearGradient>
                    </defs>
                    <CartesianGrid
                      strokeDasharray="3 5"
                      vertical={false}
                      stroke="#e9eeeb"
                    />
                    <XAxis
                      dataKey="name"
                      axisLine={false}
                      tickLine={false}
                      tick={{ fill: "#8a938e", fontSize: 11 }}
                      dy={9}
                    />
                    <YAxis
                      axisLine={false}
                      tickLine={false}
                      tick={{ fill: "#8a938e", fontSize: 11 }}
                      width={45}
                    />
                    <Tooltip
                      formatter={(v) => (reports ? Number(v) : money(v))}
                    />
                    <Area
                      type="monotone"
                      dataKey={reports ? "growth" : "revenue"}
                      stroke="#317259"
                      strokeWidth={2.5}
                      fill="url(#revenue)"
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            </section>
            <section className="card distribution">
              <div className="card-heading">
                <div>
                  <h2>Subscription health</h2>
                  <p>Every stage, one view</p>
                </div>
                <span className="small-label">LIVE SNAPSHOT</span>
              </div>
              {subscriptions.length ? (
                <>
                  <div className="donut">
                    <ResponsiveContainer width="100%" height={205}>
                      <PieChart>
                        <Pie
                          data={statusData}
                          dataKey="value"
                          innerRadius={69}
                          outerRadius={85}
                          paddingAngle={5}
                          stroke="none"
                        >
                          {statusData.map((s) => (
                            <Cell key={s.name} fill={statusColors[s.name]} />
                          ))}
                        </Pie>
                        <Tooltip />
                      </PieChart>
                    </ResponsiveContainer>
                    <div className="donut-center">
                      <strong>{subscriptions.length}</strong>
                      <small>total subscriptions</small>
                    </div>
                  </div>
                  <div className="status-legend">
                    {statusData.map((s) => (
                      <div key={s.name}>
                        <span>
                          <i
                            style={{
                              background: statusColors[s.name],
                            }}
                          />
                          {s.name}
                        </span>
                        <strong>{s.value}</strong>
                      </div>
                    ))}
                  </div>
                </>
              ) : (
                <Empty title="No subscriptions yet" />
              )}
            </section>
          </div>
          {reports && (
            <div className="card report-summary">
              <h2>Payment status</h2>
              <div className="payment-chart">
                <ResponsiveContainer width="100%" height={180}>
                  <PieChart>
                    <Pie
                      data={["Paid", "Partially paid", "Unpaid"].map(
                        (name) => ({
                          name,
                          value: invoices.filter((i) =>
                            name === "Paid"
                              ? paid(i.id) >= total(i)
                              : name === "Partially paid"
                                ? paid(i.id) > 0 && paid(i.id) < total(i)
                                : paid(i.id) === 0,
                          ).length,
                        }),
                      )}
                      dataKey="value"
                      nameKey="name"
                      innerRadius={45}
                      outerRadius={70}
                      paddingAngle={4}
                    >
                      {["#295e48", "#b9cb91", "#e5d6ae"].map((c) => (
                        <Cell key={c} fill={c} />
                      ))}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              </div>
              {["Paid", "Partially paid", "Unpaid"].map((s) => (
                <div key={s}>
                  <span>{s}</span>
                  <strong>
                    {
                      invoices.filter((i) =>
                        s === "Paid"
                          ? paid(i.id) >= total(i)
                          : s === "Partially paid"
                            ? paid(i.id) > 0 && paid(i.id) < total(i)
                            : paid(i.id) === 0,
                      ).length
                    }
                  </strong>
                </div>
              ))}
            </div>
          )}
          <section className="card">
            <div className="card-heading">
              <div>
                <h2>
                  {customer ? "My subscriptions" : "Recent subscriptions"}
                </h2>
                <p>Your latest relationships, at a glance</p>
              </div>
              <Link className="text-link" to="../subscriptions" relative="path">
                View all subscriptions <ArrowRight size={15} />
              </Link>
            </div>
            <div className="table-toolbar">
              <label className="search">
                <Users size={15} />
                <input
                  placeholder="Search subscriptions…"
                  aria-label="Search recent subscriptions"
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                />
              </label>
              <span className="small-label">
                {subscriptions.length} SUBSCRIPTIONS
              </span>
            </div>
            <div className="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>{customer ? "Subscription" : "Customer"}</th>
                    <th>Plan</th>
                    <th>Status</th>
                    <th>Amount</th>
                    <th>Next renewal</th>
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {subscriptions
                    .filter(
                      (s) =>
                        JSON.stringify(s)
                          .toLowerCase()
                          .includes(query.toLowerCase()) ||
                        String(
                          data.customers?.find((c) => c.id === s.customerId)
                            ?.name || "",
                        )
                          .toLowerCase()
                          .includes(query.toLowerCase()),
                    )
                    .slice(0, 5)
                    .map((s) => {
                      const c = data.customers?.find(
                        (c) => c.id === s.customerId,
                      );
                      return (
                        <tr key={s.id}>
                          <td>
                            <div className="person">
                              <span className="avatar">
                                {String(c?.name || s.name)
                                  .slice(0, 2)
                                  .toUpperCase()}
                              </span>
                              <span>
                                <strong>
                                  {customer ? s.name : c?.name || s.name}
                                </strong>
                                <small>{c?.email || s.name}</small>
                              </span>
                            </div>
                          </td>
                          <td>
                            {data.plans?.find((p) => p.id === s.planId)?.name ||
                              "—"}
                          </td>
                          <td>
                            <Badge value={s.status} />
                          </td>
                          <td className="amount">
                            {money(total(s))}
                            <small>/ cycle</small>
                          </td>
                          <td>{s.to || "—"}</td>
                          <td>
                            <Link
                              aria-label={`View ${s.name}`}
                              to={`../subscriptions/${s.id}`}
                              relative="path"
                            >
                              <ArrowUpRight size={17} />
                            </Link>
                          </td>
                        </tr>
                      );
                    })}
                </tbody>
              </table>
            </div>
            {!subscriptions.length && <Empty />}
          </section>
          <div className="bottom-note">
            <span className="mini-dot" />
            {demo
              ? "You’re exploring sample data. Connect your backend to see your business here."
              : "Your workspace is connected to your backend."}
            <span>Small steps. Lasting growth. ↗</span>
          </div>
        </>
      )}
    </>
  );
}
