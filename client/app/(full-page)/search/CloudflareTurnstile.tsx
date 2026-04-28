import React from 'react';
import Turnstile from 'react-turnstile';

interface CloudflareTurnstileProps {
  onVerify: (token: string) => void;
  onError?: (error?: any) => void;
  onExpire?: () => void;
  onLoad?: () => void;
  theme?: 'light' | 'dark' | 'auto';
  size?: 'normal' | 'compact';
  className?: string;
  widgetKey: number; // clé dynamique pour reset
}

export const CloudflareTurnstile: React.FC<CloudflareTurnstileProps> = ({
  onVerify,
  onError,
  onExpire,
  onLoad,
  theme = 'light',
  size = 'normal',
  className = '',
  widgetKey,
}) => {
   const CLOUDFLARE_SITEKEY = process.env.NEXT_PUBLIC_CLOUDFLARE_SITEKEY || '0x4AAAAAAAVotreSiteKeyIci';
  return (
    <div className={`flex justify-center ${className}`}>
      <Turnstile
        key={widgetKey} // force reload à chaque reset
        sitekey={CLOUDFLARE_SITEKEY}
        onVerify={onVerify}
        onError={onError}
        onExpire={onExpire}
        onLoad={onLoad}
        theme={theme}
        size={size}
        retry="auto"
        retryInterval={3000}
        refreshExpired="auto"
      />
    </div>
  );
};
