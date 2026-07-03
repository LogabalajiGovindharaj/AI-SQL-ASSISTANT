import React, { useState } from "react";
import { AuthProvider, useAuth } from "./context/AuthContext";
import Landing from "./pages/Landing";
import Login from "./pages/Login";
import QueryConsole from "./pages/QueryConsole";

function Shell() {
  const { auth } = useAuth();
  const [showLanding, setShowLanding] = useState(true);

  if (auth) return <QueryConsole />;
  if (showLanding) return <Landing onGetStarted={() => setShowLanding(false)} />;
  return <Login />;
}

export default function App() {
  return (
    <AuthProvider>
      <Shell />
    </AuthProvider>
  );
}
