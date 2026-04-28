"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { ProgressSpinner } from "primereact/progressspinner";
import { useSafeNavigation } from "@/app/(full-page)/hooks/useSafeNavigation";

export default function CandidatLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const { safeNavigate, safeReplace } = useSafeNavigation();
  const [isAuthorized, setIsAuthorized] = useState(false);

  useEffect(() => {
    const candidatInfo = localStorage.getItem("candidat_info");

    if (!candidatInfo) {
      safeReplace("/");
    } else {
      setIsAuthorized(true);
    }
  }, [safeReplace]);
  if (!isAuthorized) {
    return (
      <div className="flex justify-content-center align-items-center min-h-screen">
        <ProgressSpinner />
      </div>
    );
  }

  return <>{children}</>;
}
