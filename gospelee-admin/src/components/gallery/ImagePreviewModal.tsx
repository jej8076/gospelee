"use client";

import React, { Fragment } from "react";
import { Dialog, Transition } from "@headlessui/react";
import { XMarkIcon } from "@heroicons/react/24/outline";
import { GalleryImage } from "@/types/gallery";

interface ImagePreviewModalProps {
  isOpen: boolean;
  onClose: () => void;
  image: GalleryImage | null;
}

export default function ImagePreviewModal({
  isOpen,
  onClose,
  image,
}: ImagePreviewModalProps) {
  if (!image) return null;

  const apiBaseUrl = process.env.NEXT_PUBLIC_API_URL || "";
  const fullImageUrl = `${apiBaseUrl}${image.originalUrl}`;

  return (
    <Transition.Root show={isOpen} as={Fragment}>
      <Dialog as="div" className="relative z-50" onClose={onClose}>
        <Transition.Child
          as={Fragment}
          enter="ease-out duration-300"
          enterFrom="opacity-0"
          enterTo="opacity-100"
          leave="ease-in duration-200"
          leaveFrom="opacity-100"
          leaveTo="opacity-0"
        >
          <div className="fixed inset-0 bg-black/80 transition-opacity" />
        </Transition.Child>

        <div className="fixed inset-0 z-10 overflow-y-auto">
          <div className="flex min-h-full items-center justify-center p-4 text-center">
            <Transition.Child
              as={Fragment}
              enter="ease-out duration-300"
              enterFrom="opacity-0 scale-95"
              enterTo="opacity-100 scale-100"
              leave="ease-in duration-200"
              leaveFrom="opacity-100 scale-100"
              leaveTo="opacity-0 scale-95"
            >
              <Dialog.Panel className="relative transform overflow-hidden rounded-2xl bg-white text-left shadow-2xl transition-all sm:max-w-3xl sm:w-full">
                <div className="flex items-center justify-between px-4 py-3 border-b border-gray-200 bg-gray-50">
                  <span className="text-sm font-semibold text-gray-800 truncate max-w-md">
                    {image.originalFileName}
                  </span>
                  <button
                    type="button"
                    onClick={onClose}
                    className="p-1 rounded-lg text-gray-400 hover:bg-gray-200 hover:text-gray-600"
                  >
                    <XMarkIcon className="h-5 w-5" />
                  </button>
                </div>

                <div className="p-4 flex items-center justify-center bg-gray-900 min-h-[360px] max-h-[70vh]">
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img
                    src={fullImageUrl}
                    alt={image.originalFileName}
                    className="max-h-[65vh] max-w-full object-contain rounded-md"
                  />
                </div>

                <div className="px-5 py-3 bg-white border-t border-gray-200 flex flex-wrap items-center justify-between text-xs text-gray-500 gap-2">
                  <div>
                    소속 폴더:{" "}
                    <span className="font-semibold text-gray-700">
                      {image.folderName || "최상위 (루트)"}
                    </span>
                  </div>
                  <div>
                    용량:{" "}
                    <span className="font-semibold text-gray-700">
                      {(image.fileSize / (1024 * 1024)).toFixed(2)} MB
                    </span>
                  </div>
                  <div>
                    등록일:{" "}
                    <span className="font-semibold text-gray-700">
                      {new Date(image.insertTime).toLocaleString("ko-KR")}
                    </span>
                  </div>
                </div>
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>
    </Transition.Root>
  );
}
