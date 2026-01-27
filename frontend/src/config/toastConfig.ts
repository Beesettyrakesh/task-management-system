import toast from 'react-hot-toast';

// Global toast configuration
export const toastConfig = {
  position: 'top-right' as const,
  duration: 3000,
  style: {
    borderRadius: '8px',
    fontSize: '14px',
    padding: '12px 16px',
  },
  success: {
    iconTheme: {
      primary: '#10B981',
      secondary: '#FFFFFF',
    },
  },
  error: {
    iconTheme: {
      primary: '#EF4444',
      secondary: '#FFFFFF',
    },
  },
};

// Success toast
export const showSuccessToast = (message: string) => {
  toast.success(message, toastConfig);
};

// Error toast
export const showErrorToast = (message: string) => {
  toast.error(message, toastConfig);
};

// Loading toast (returns ID to dismiss later)
export const showLoadingToast = (message: string) => {
  return toast.loading(message, toastConfig);
};

// Dismiss specific toast
export const dismissToast = (toastId: string) => {
  toast.dismiss(toastId);
};

// Info toast
export const showInfoToast = (message: string) => {
  toast(message, {
    ...toastConfig,
    icon: 'ℹ️',
  });
};

// Warning toast
export const showWarningToast = (message: string) => {
  toast(message, {
    ...toastConfig,
    icon: '⚠️',
    style: {
      ...toastConfig.style,
      border: '1px solid #F59E0B',
      backgroundColor: '#FEF3C7',
    },
  });
};

// Promise toast (for async operations)
export const showPromiseToast = <T,>(
  promise: Promise<T>,
  messages: {
    loading: string;
    success: string;
    error: string;
  }
) => {
  return toast.promise(
    promise,
    {
      loading: messages.loading,
      success: messages.success,
      error: messages.error,
    },
    toastConfig
  );
};