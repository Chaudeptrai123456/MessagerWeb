
import { UserProvider } from "@/context/UserContext";
export default function Layout({children}: {children: React.ReactNode}) {
  return (
      <div>
        <UserProvider>
          {children}
        </UserProvider>
      </div>
  );
}
