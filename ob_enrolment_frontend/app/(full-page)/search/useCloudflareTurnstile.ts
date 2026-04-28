// hooks/useCloudflareTurnstile.ts
import { useState, useCallback } from 'react';

interface UseCloudflareTurnstileReturn {
  turnstileToken: string;
  isTurnstileVerified: boolean;
  turnstileError: string;
  isTurnstileLoading: boolean;
  widgetKey: number; // clé dynamique pour forcer le reload
  handleTurnstileVerify: (token: string) => void;
  handleTurnstileError: (error?: any) => void;
  handleTurnstileExpire: () => void;
  handleTurnstileLoad: () => void;
  resetTurnstile: () => void;
}

export const useCloudflareTurnstile = (): UseCloudflareTurnstileReturn => {
  const [turnstileToken, setTurnstileToken] = useState<string>('');
  const [isTurnstileVerified, setIsTurnstileVerified] = useState<boolean>(false);
  const [turnstileError, setTurnstileError] = useState<string>('');
  const [isTurnstileLoading, setIsTurnstileLoading] = useState<boolean>(true);
  const [widgetKey, setWidgetKey] = useState<number>(0); // clé dynamique

  const handleTurnstileVerify = useCallback((token: string) => {
    // console.log('✅ Cloudflare Turnstile vérifié, token:', token);
    setTurnstileToken(token);
    setIsTurnstileVerified(true);
    setTurnstileError('');
    setIsTurnstileLoading(false);
  }, []);

  const handleTurnstileError = useCallback((error?: any) => {
    console.error('❌ Erreur Cloudflare Turnstile:', error);
    setTurnstileToken('');
    setIsTurnstileVerified(false);
    setTurnstileError('Échec de la vérification de sécurité. Veuillez réessayer.');
    setIsTurnstileLoading(false);
  }, []);

  const handleTurnstileExpire = useCallback(() => {
    // console.log('⚠️ Token Cloudflare Turnstile expiré');
    setTurnstileToken('');
    setIsTurnstileVerified(false);
    setTurnstileError('La vérification de sécurité a expiré. Veuillez actualiser.');
  }, []);

  const handleTurnstileLoad = useCallback(() => {
    // console.log('🔄 Cloudflare Turnstile chargé');
    setIsTurnstileLoading(false);
  }, []);

  const resetTurnstile = useCallback(() => {
    setTurnstileToken('');
    setIsTurnstileVerified(false);
    setTurnstileError('');
    setIsTurnstileLoading(true);
    setWidgetKey(prev => prev + 1); // force le reload du widget
  }, []);

  return {
    turnstileToken,
    isTurnstileVerified,
    turnstileError,
    isTurnstileLoading,
    widgetKey,
    handleTurnstileVerify,
    handleTurnstileError,
    handleTurnstileExpire,
    handleTurnstileLoad,
    resetTurnstile,
  };
};
