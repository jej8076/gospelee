"use client";

import React, { useState, useEffect } from "react";
import { Fragment } from "react";
import { Dialog, Transition } from "@headlessui/react";
import { XMarkIcon, FolderPlusIcon } from "@heroicons/react/24/outline";
import { GalleryFolder, GalleryFolderRequest } from "@/types/gallery";
import { createGalleryFolder, updateGalleryFolder } from "~/lib/api/fetch-gallery";

const ROLE_OPTIONS = [
  { value: "LAYMAN", label: "성도(평신도)" },
  { value: "TEACHER", label: "교사" },
  { value: "COORDINATOR", label: "간사" },
  { value: "MINISTER", label: "전도사" },
  { value: "LICENSED_MINISTER", label: "강도사" },
  { value: "PASTOR", label: "목사" },
  { value: "SENIOR_PASTOR", label: "담임목사" },
  { value: "ADMIN", label: "관리자" },
];

interface FolderModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  folderToEdit?: GalleryFolder | null;
  parentFolder?: GalleryFolder | null;
  allFolders?: GalleryFolder[];
}

export default function FolderModal({
  isOpen,
  onClose,
  onSuccess,
  folderToEdit,
  parentFolder,
  allFolders = [],
}: FolderModalProps) {
  const [name, setName] = useState("");
  const [parentId, setParentId] = useState<number | null>(null);
  const [selectedRoles, setSelectedRoles] = useState<string[]>([]);
  const [sortOrder, setSortOrder] = useState<number>(0);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");

  useEffect(() => {
    if (folderToEdit) {
      setName(folderToEdit.name);
      setParentId(folderToEdit.parentId);
      setSelectedRoles(folderToEdit.targetRoles || []);
      setSortOrder(folderToEdit.sortOrder || 0);
    } else {
      setName("");
      setParentId(parentFolder ? parentFolder.id : null);
      setSelectedRoles(parentFolder ? parentFolder.targetRoles || [] : []);
      setSortOrder(0);
    }
    setErrorMsg("");
  }, [folderToEdit, parentFolder, isOpen]);

  const handleRoleToggle = (role: string) => {
    setSelectedRoles((prev) =>
      prev.includes(role) ? prev.filter((r) => r !== role) : [...prev, role]
    );
  };

  const handleSelectAllRoles = (selectAll: boolean) => {
    if (selectAll) {
      setSelectedRoles(ROLE_OPTIONS.map((r) => r.value));
    } else {
      setSelectedRoles([]);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setErrorMsg("폴더명을 입력해주세요.");
      return;
    }

    try {
      setIsSubmitting(true);
      setErrorMsg("");

      const request: GalleryFolderRequest = {
        id: folderToEdit?.id,
        parentId: parentId,
        name: name.trim(),
        targetRoles: selectedRoles.length > 0 ? selectedRoles : undefined,
        sortOrder,
      };

      if (folderToEdit) {
        await updateGalleryFolder(request);
      } else {
        await createGalleryFolder(request);
      }

      onSuccess();
      onClose();
    } catch (err: any) {
      setErrorMsg(err.message || "폴더 저장에 실패했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  };

  // 3단계 초과 방지를 위해 depth가 1 또는 2인 폴더만 부모로 선택 가능
  const validParentFolders = allFolders.filter(
    (f) => f.depth < 3 && (!folderToEdit || f.id !== folderToEdit.id)
  );

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
          <div className="fixed inset-0 bg-gray-500/75 transition-opacity" />
        </Transition.Child>

        <div className="fixed inset-0 z-10 overflow-y-auto">
          <div className="flex min-h-full items-center justify-center p-4 text-center sm:p-0">
            <Transition.Child
              as={Fragment}
              enter="ease-out duration-300"
              enterFrom="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
              enterTo="opacity-100 translate-y-0 sm:scale-100"
              leave="ease-in duration-200"
              leaveFrom="opacity-100 translate-y-0 sm:scale-100"
              leaveTo="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
            >
              <Dialog.Panel className="relative transform overflow-hidden rounded-2xl bg-white text-left shadow-2xl transition-all sm:my-8 sm:w-full sm:max-w-lg">
                <form onSubmit={handleSubmit}>
                  {/* Modal Header */}
                  <div className="bg-gray-50/80 px-6 py-4 border-b border-gray-200 flex items-center justify-between">
                    <div className="flex items-center gap-2.5">
                      <div className="p-2 bg-indigo-50 text-indigo-600 rounded-lg">
                        <FolderPlusIcon className="h-5 w-5" />
                      </div>
                      <Dialog.Title className="text-base font-semibold text-gray-900">
                        {folderToEdit ? "폴더 수정" : "새 폴더 만들기"}
                      </Dialog.Title>
                    </div>
                    <button
                      type="button"
                      onClick={onClose}
                      className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100 hover:text-gray-500"
                    >
                      <XMarkIcon className="h-5 w-5" />
                    </button>
                  </div>

                  {/* Modal Body */}
                  <div className="px-6 py-5 space-y-4">
                    {errorMsg && (
                      <div className="p-3 bg-rose-50 text-rose-700 text-xs rounded-lg border border-rose-200">
                        {errorMsg}
                      </div>
                    )}

                    {/* 폴더명 */}
                    <div>
                      <label className="block text-xs font-semibold text-gray-700 mb-1">
                        폴더명 <span className="text-rose-500">*</span>
                      </label>
                      <input
                        type="text"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        placeholder="예: 2026 여름 수련회"
                        className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-hidden focus:ring-1 focus:ring-indigo-500"
                        maxLength={50}
                        required
                      />
                    </div>

                    {/* 상위 폴더 선택 */}
                    {!folderToEdit && (
                      <div>
                        <label className="block text-xs font-semibold text-gray-700 mb-1">
                          위치 (상위 폴더)
                        </label>
                        <select
                          value={parentId === null ? "" : parentId}
                          onChange={(e) =>
                            setParentId(
                              e.target.value ? Number(e.target.value) : null
                            )
                          }
                          className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-hidden focus:ring-1 focus:ring-indigo-500"
                        >
                          <option value="">최상위 (Root - 1단계)</option>
                          {validParentFolders.map((f) => (
                            <option key={f.id} value={f.id}>
                              {"— ".repeat(f.depth - 1)} {f.name} ({f.depth}단계)
                            </option>
                          ))}
                        </select>
                        <p className="mt-1 text-[11px] text-gray-500">
                          * 폴더는 최대 3단계 깊이까지 생성할 수 있습니다.
                        </p>
                      </div>
                    )}

                    {/* 열람 권한 (RoleType 복수 선택) */}
                    <div>
                      <div className="flex items-center justify-between mb-2">
                        <label className="text-xs font-semibold text-gray-700">
                          열람 가능 교인 권한
                        </label>
                        <div className="flex gap-2">
                          <button
                            type="button"
                            onClick={() => handleSelectAllRoles(true)}
                            className="text-[11px] text-indigo-600 hover:underline"
                          >
                            전체 선택
                          </button>
                          <span className="text-gray-300">|</span>
                          <button
                            type="button"
                            onClick={() => handleSelectAllRoles(false)}
                            className="text-[11px] text-gray-500 hover:underline"
                          >
                            초기화
                          </button>
                        </div>
                      </div>

                      <div className="grid grid-cols-2 gap-2 rounded-lg border border-gray-200 bg-gray-50/50 p-3 max-h-44 overflow-y-auto">
                        {ROLE_OPTIONS.map((opt) => {
                          const checked = selectedRoles.includes(opt.value);
                          return (
                            <label
                              key={opt.value}
                              className={`flex items-center gap-2 p-2 rounded-md border text-xs cursor-pointer transition-colors ${
                                checked
                                  ? "bg-indigo-50/80 border-indigo-200 text-indigo-900 font-medium"
                                  : "bg-white border-gray-200 text-gray-700 hover:bg-gray-50"
                              }`}
                            >
                              <input
                                type="checkbox"
                                checked={checked}
                                onChange={() => handleRoleToggle(opt.value)}
                                className="h-3.5 w-3.5 rounded-sm border-gray-300 text-indigo-600 focus:ring-indigo-500"
                              />
                              <span>{opt.label}</span>
                            </label>
                          );
                        })}
                      </div>
                      <p className="mt-1.5 text-[11px] text-gray-500">
                        * 선택하지 않으면 모든 등록 교인이 열람할 수 있습니다.
                      </p>
                    </div>
                  </div>

                  {/* Modal Footer */}
                  <div className="bg-gray-50 px-6 py-3.5 flex justify-end gap-2 border-t border-gray-200 rounded-b-2xl">
                    <button
                      type="button"
                      onClick={onClose}
                      disabled={isSubmitting}
                      className="px-4 py-2 text-xs font-semibold text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50"
                    >
                      취소
                    </button>
                    <button
                      type="submit"
                      disabled={isSubmitting}
                      className="px-4 py-2 text-xs font-semibold text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 disabled:opacity-50"
                    >
                      {isSubmitting
                        ? "저장 중..."
                        : folderToEdit
                        ? "수정 완료"
                        : "폴더 생성"}
                    </button>
                  </div>
                </form>
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>
    </Transition.Root>
  );
}
