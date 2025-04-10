"use client";

import React, {ReactNode, useEffect, useState} from "react";

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  children: ReactNode;
  footer?: ReactNode;
}

export default function Modal({isOpen, onClose, title, children, footer}: ModalProps) {
  // 모달의 가시성 상태(애니메이션 효과를 위한 상태)
  const [isAnimating, setIsAnimating] = useState(false);
  // 모달의 실제 렌더링 여부
  const [isRendered, setIsRendered] = useState(false);

  // 모달 열고 닫는 애니메이션 효과 처리
  useEffect(() => {
    if (isOpen) {
      // 모달 열기: 먼저 렌더링 후 애니메이션 시작
      setIsRendered(true);
      // 약간의 딜레이 후 애니메이션 시작 (DOM이 렌더링된 후 애니메이션 적용)
      const timer = setTimeout(() => {
        setIsAnimating(true);
      }, 30);
      return () => clearTimeout(timer);
    } else {
      // 모달 닫기: 애니메이션 효과 먼저 시작
      setIsAnimating(false);
      // 애니메이션 완료 후 실제 DOM에서 제거
      const timer = setTimeout(() => {
        setIsRendered(false);
      }, 300); // 애니메이션 지속 시간과 일치
      return () => clearTimeout(timer);
    }
  }, [isOpen]);

  // ESC 키로 모달 닫기
  useEffect(() => {
    const handleEsc = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };

    if (isOpen) {
      document.addEventListener("keydown", handleEsc);
      // 모달 열릴 때 스크롤 방지
      document.body.style.overflow = "hidden";
    }

    return () => {
      document.removeEventListener("keydown", handleEsc);
      // 모달 닫힐 때 스크롤 복원
      document.body.style.overflow = "auto";
    };
  }, [isOpen, onClose]);

  // 모달이 완전히 닫혔을 때 렌더링하지 않음
  if (!isRendered) return null;

  return (
      <div className="fixed inset-0 z-50 flex items-center justify-center">
        {/* 배경 오버레이 (서서히 나타나고 사라지는 효과) */}
        <div
            className={`absolute inset-0 bg-black transition-opacity duration-300 ease-in-out ${
                isAnimating ? "bg-opacity-50" : "bg-opacity-0"
            }`}
            onClick={onClose}
        ></div>

        {/* 모달 컨텐츠 (아래에서 위로 서서히 나타나고, 위에서 아래로 서서히 사라지는 효과) */}
        <div
            className={`relative bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4 transform transition-all duration-300 ease-in-out ${
                isAnimating
                    ? "opacity-100 translate-y-0"
                    : "opacity-0 translate-y-8"
            }`}
        >
          {title && <h2 className="text-xl font-bold mb-4">{title}</h2>}
          <div className="mb-6">{children}</div>
          {footer && <div className="flex justify-end space-x-2">{footer}</div>}
          {!footer && (
              <div className="flex justify-end">
                <button
                    onClick={onClose}
                    className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition-colors"
                >
                  닫기
                </button>
              </div>
          )}
        </div>
      </div>
  );
}