import { useState, useEffect, useRef } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import {
  LayoutDashboard,
  Users,
  Briefcase,
  Package,
  Layers,
  Repeat,
  FileText,
  Receipt,
  Tag,
  Landmark,
  CreditCard,
  ChartNoAxesCombined,
  Settings,
  Search,
  Menu,
  X,
  LogOut,
  ChevronDown,
  ArrowUpRight,
  HelpCircle,
  UserRound,
} from "lucide-react";
import { useWorkspace } from "../context/Workspace";
import { modules, nav } from "../utils/modules";
const icons: Record<string, typeof Users> = {
  dashboard: LayoutDashboard,
  customers: Users,
  staff: Briefcase,
  products: Package,
  variants: Layers,
  plans: Layers,
  quotations: FileText,
  subscriptions: Repeat,
  invoices: Receipt,
  discounts: Tag,
  taxes: Landmark,
  payments: CreditCard,
  reports: ChartNoAxesCombined,
  settings: Settings,
  profile: UserRound,
};
export default function Shell() {
  const { user, demo, logout, toast } = useWorkspace();
  const [open, setOpen] = useState(false),
    [search, setSearch] = useState("");
  const navigate = useNavigate();
  const [mobile, setMobile] = useState(
    () => window.matchMedia("(max-width: 850px)").matches,
  );
  const sidebar = useRef<HTMLElement>(null);
  useEffect(() => {
    const query = window.matchMedia("(max-width: 850px)");
    const update = () => {
      setMobile(query.matches);
      if (!query.matches) setOpen(false);
    };
    query.addEventListener("change", update);
    return () => query.removeEventListener("change", update);
  }, []);
  useEffect(() => {
    if (!open || !mobile) return;
    const previous = document.activeElement as HTMLElement | null;
    const overflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    sidebar.current?.querySelector<HTMLElement>("button,a[href]")?.focus();
    return () => {
      document.body.style.overflow = overflow;
      previous?.focus();
    };
  }, [open, mobile]);
  const links = nav[user!.role].filter(
    (x) =>
      x !== "reports" ||
      user!.role !== "STAFF" ||
      user!.permissions.includes("reports"),
  );
  return (
    <div className="workspace">
      <aside
        ref={sidebar}
        className={open ? "sidebar open" : "sidebar"}
        inert={mobile && !open}
        role={mobile && open ? "dialog" : undefined}
        aria-modal={mobile && open ? true : undefined}
        aria-label="Workspace navigation"
        onKeyDown={(event) => {
          if (!mobile || !open) return;
          if (event.key === "Escape") {
            event.preventDefault();
            setOpen(false);
          }
          if (event.key === "Tab") {
            const items = Array.from(
              sidebar.current?.querySelectorAll<HTMLElement>(
                "a[href],button:not([disabled]),input",
              ) || [],
            ).filter((item) => item.offsetParent !== null);
            const first = items[0],
              last = items[items.length - 1];
            if (event.shiftKey && document.activeElement === first) {
              event.preventDefault();
              last?.focus();
            } else if (!event.shiftKey && document.activeElement === last) {
              event.preventDefault();
              first?.focus();
            }
          }
        }}
      >
        <button
          className="sidebar-close icon-button"
          aria-label="Close navigation"
          onClick={() => setOpen(false)}
        >
          <X size={18} />
        </button>
        <NavLink to="dashboard" className="brand">
          ◉ brightloop<span>®</span>
        </NavLink>
        <button
          className="workspace-switch"
          onClick={() => navigate("settings")}
        >
          <span className="workspace-icon">B</span>
          <span>
            Brightloop workspace
            <small>{user!.role.toLowerCase()} workspace</small>
          </span>
          <ChevronDown size={14} />
        </button>
        <span className="nav-label">WORKSPACE</span>
        <nav>
          {links.map((k) => {
            const Icon = icons[k] || FileText;
            return (
              <NavLink key={k} to={k} onClick={() => setOpen(false)}>
                <Icon size={18} />
                {k === "dashboard"
                  ? "Overview"
                  : k === "reports"
                    ? "Reports & analytics"
                    : k === "profile"
                      ? "My profile"
                      : k === "settings"
                        ? "Settings"
                        : user!.role === "CUSTOMER" && k !== "plans"
                          ? `My ${k}`
                          : modules[k]?.title}
                {k === "subscriptions" && <span className="nav-dot" />}
              </NavLink>
            );
          })}
        </nav>
        <div className="sidebar-bottom">
          <div className="help-card">
            <span className="help-icon">
              <HelpCircle size={19} />
            </span>
            <strong>A little clarity goes a long way.</strong>
            <p>Find your workspace guide in settings.</p>
            <button onClick={() => navigate("settings")}>
              Explore the guide <ArrowUpRight size={14} />
            </button>
          </div>
          <button className="account" onClick={logout}>
            <span className="avatar">
              {user!.name
                .split(" ")
                .map((n) => n[0])
                .join("")}
            </span>
            <span>
              {user!.name}
              <small>Sign out of workspace</small>
            </span>
            <LogOut size={16} />
          </button>
        </div>
      </aside>
      {open && (
        <button
          className="scrim"
          aria-label="Close navigation overlay"
          tabIndex={-1}
          onClick={() => setOpen(false)}
        />
      )}
      <main inert={mobile && open}>
        <header className="topbar">
          <button
            className="icon-button mobile-menu"
            aria-label="Open navigation"
            aria-expanded={open}
            onClick={() => setOpen(true)}
          >
            <Menu />
          </button>
          <div className="breadcrumb">
            Workspace <span>/</span>{" "}
            <strong>
              {user!.role === "ADMIN"
                ? "Administration"
                : user!.role === "STAFF"
                  ? "Team operations"
                  : "Customer portal"}
            </strong>
          </div>
          <form
            className="global-search"
            onSubmit={(e) => {
              e.preventDefault();
              const match = links.find((k) =>
                (modules[k]?.title || k)
                  .toLowerCase()
                  .includes(search.toLowerCase()),
              );
              if (match) {
                navigate(match);
                setSearch("");
              } else toast("No page matches that search.");
            }}
          >
            <Search size={16} />
            <input
              aria-label="Find a page"
              placeholder="Find a page…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
            <kbd>↵</kbd>
          </form>
          <span className="top-avatar">{user!.name[0]}</span>
        </header>
        {demo && (
          <div className="demo-bar">
            <span>
              <i /> Demo workspace · Sample data only
            </span>
            <button onClick={logout}>
              Connect your account <ArrowUpRight size={13} />
            </button>
          </div>
        )}
        <div className="page">
          <Outlet />
        </div>
        <footer>
          Made for the way you grow.
          <span>Brightloop © {new Date().getFullYear()}</span>
        </footer>
      </main>
    </div>
  );
}
