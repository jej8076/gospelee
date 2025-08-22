import {useCallback, useState} from "react";

export const useModalState = () => {
  const [modalState, setModalState] = useState({
    isOpen: false,
    config: null as {
      title?: string;
      content: React.ReactNode;
      data?: any;
      onConfirm?: () => void;
      onCancel?: () => void;
    } | null
  });

  const openModal = useCallback((config: NonNullable<typeof modalState.config>) => {
    setModalState({
      isOpen: true,
      config
    });
  }, []);

  const closeModal = useCallback(() => {
    setModalState(prev => ({...prev, isOpen: false}));
  }, []);

  const handleConfirm = useCallback(() => {
    if (modalState.config?.onConfirm) {
      modalState.config.onConfirm();
    }
    closeModal();
  }, [modalState.config, closeModal]);

  const handleCancel = useCallback(() => {
    if (modalState.config?.onCancel) {
      modalState.config.onCancel();
    }
    closeModal();
  }, [modalState.config, closeModal]);

  return {
    isOpen: modalState.isOpen,
    config: modalState.config,
    openModal,
    closeModal,
    handleConfirm,
    handleCancel
  };
};
