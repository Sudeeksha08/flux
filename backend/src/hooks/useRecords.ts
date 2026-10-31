import { useEffect, useState } from "react";
import { useWorkspace } from "../context/Workspace";
import { errorMessage } from "../services/api";
export function useRecords(keys: string[]) {
  const { reload, demo } = useWorkspace();
  const [state, setState] = useState({
    signature: "",
    loading: !demo,
    error: "",
  });
  const [version, setVersion] = useState(0);
  const signature = JSON.stringify([...new Set(keys)].sort());
  useEffect(() => {
    const controller = new AbortController();
    setState({ signature, loading: !demo, error: "" });
    Promise.all(
      (JSON.parse(signature) as string[]).map((key) =>
        reload(key, controller.signal),
      ),
    )
      .then(() => {
        if (!controller.signal.aborted)
          setState({ signature, loading: false, error: "" });
      })
      .catch((error) => {
        if (!controller.signal.aborted)
          setState({ signature, loading: false, error: errorMessage(error) });
      });
    return () => controller.abort();
  }, [signature, reload, demo, version]);
  return {
    loading: state.signature !== signature ? !demo : state.loading,
    error: state.signature === signature ? state.error : "",
    retry: () => setVersion((v) => v + 1),
  };
}
