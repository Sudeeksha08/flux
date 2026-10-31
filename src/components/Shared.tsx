import { useEffect, useRef, useId } from "react";
import { X, Inbox, LoaderCircle } from "lucide-react";
export function Modal({
  title,
  children,
  onClose,
}: {
  title: string;
  children: React.ReactNode;
  onClose: () => void;
}) {
  const ref = useRef<HTMLDialogElement>(null);
  const titleId = useId();
  useEffect(() => {
    const d = ref.current!;
    d.showModal();
    return () => d.close();
  }, []);
  return (
    <dialog
      ref={ref}
      aria-labelledby={titleId}
      onCancel={onClose}
      onClick={(e) => {
        if (e.target === ref.current) onClose();
      }}
    >
      <div className="modal-head">
        <h2 id={titleId}>{title}</h2>
        <button
          className="icon-button"
          aria-label="Close dialog"
          onClick={onClose}
        >
          <X size={20} />
        </button>
      </div>
      {children}
    </dialog>
  );
}
export function State({
  loading,
  error,
  retry,
}: {
  loading: boolean;
  error: string;
  retry: () => void;
}) {
  return loading ? (
    <div className="empty" role="status">
      <LoaderCircle className="spin" />
      <p>Loading your workspace…</p>
    </div>
  ) : error ? (
    <div className="alert" role="alert">
      {error} <button onClick={retry}>Try again</button>
    </div>
  ) : null;
}
export function Empty({
  title = "Nothing here yet",
  text = "Your records will appear here as your business grows.",
}: {
  title?: string;
  text?: string;
}) {
  return (
    <div className="empty">
      <Inbox size={30} />
      <h3>{title}</h3>
      <p>{text}</p>
    </div>
  );
}
export function Badge({ value }: { value: unknown }) {
  return (
    <span className={`badge ${String(value).toLowerCase()}`}>
      <i />
      {String(value)}
    </span>
  );
}
