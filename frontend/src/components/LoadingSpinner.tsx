import React from 'react';
import { Loader2 } from 'lucide-react';

interface LoadingSpinnerProps {
  size?: 'sm' | 'md' | 'lg';
  className?: string;
  text?: string;
}

export const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({ 
  size = 'md', 
  className = '',
  text
}) => {
  const sizeClasses = {
    sm: 'w-4 h-4',
    md: 'w-6 h-6',
    lg: 'w-8 h-8'
  };

  return (
    <div className="flex items-center justify-center gap-2">
      <Loader2 className={`animate-spin ${sizeClasses[size]} ${className}`} />
      {text && <span className="text-sm text-gray-600">{text}</span>}
    </div>
  );
};

// Full page loading spinner
export const LoadingPage: React.FC<{ text?: string }> = ({ text = 'Loading...' }) => {
  return (
    <div className="flex items-center justify-center min-h-screen">
      <div className="text-center">
        <LoadingSpinner size="lg" className="text-blue-600 mx-auto mb-4" />
        <p className="text-gray-600">{text}</p>
      </div>
    </div>
  );
};

// Button loading spinner (inline with text)
export const ButtonSpinner: React.FC = () => {
  return <Loader2 className="animate-spin w-4 h-4" />;
};