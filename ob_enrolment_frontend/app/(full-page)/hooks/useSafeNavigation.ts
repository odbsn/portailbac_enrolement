"use client";

import { useRouter } from "next/navigation";
import { useCallback, useEffect, useState } from "react";

export const useSafeNavigation = () => {
  const router = useRouter();
  const [isClient, setIsClient] = useState(false);
  const [isMobileSimulator, setIsMobileSimulator] = useState(false);

  useEffect(() => {
    setIsClient(true);

    // Détecter si on est dans un simulateur mobile
    const isSimulator =
      /Mobi|Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(
        navigator.userAgent,
      ) ||
      (window.navigator.userAgent.includes("Chrome") &&
        window.innerWidth <= 768);

    setIsMobileSimulator(isSimulator);
  }, []);

  const safeNavigate = useCallback(
    (path: string) => {
      if (!isClient) return;

      // Utiliser window.location pour les simulateurs mobiles
      if (isMobileSimulator) {
        window.location.href = path;
      } else {
        router.push(path);
      }
    },
    [isClient, isMobileSimulator, router],
  );

  const safeReplace = useCallback(
    (path: string) => {
      if (!isClient) return;

      if (isMobileSimulator) {
        window.location.replace(path);
      } else {
        router.replace(path);
      }
    },
    [isClient, isMobileSimulator, router],
  );

  return { safeNavigate, safeReplace, isMobileSimulator };
};
