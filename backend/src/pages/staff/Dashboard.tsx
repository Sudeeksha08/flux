import Overview from "../../components/dashboard/Overview";
export default function Dashboard({ reports = false }: { reports?: boolean }) {
  return <Overview reports={reports} />;
}
