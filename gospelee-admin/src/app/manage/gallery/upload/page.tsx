"use client";

import React, { useEffect, useState, useRef, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import useAuth from "~/lib/auth/check-auth";
import {
  fetchAllGalleryFolders,
  uploadGalleryImages,
} from "~/lib/api/fetch-gallery";
import { GalleryFolder } from "@/types/gallery";
import {
  ArrowLeftIcon,
  ArrowUpTrayIcon,
  PhotoIcon,
  XMarkIcon,
  ExclamationTriangleIcon,
} from "@heroicons/react/24/outline";

const ALLOWED_EXTENSIONS = [
  "jpg",
  "jpeg",
  "jfif",
  "png",
  "heic",
  "webp",
  "gif",
  "avif",
  "bmp",
];
const MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB
const MAX_COUNT = 20;

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

export default function GalleryUploadPage() {
  return (
    <Suspense fallback={<div className="p-8 text-center text-xs text-gray-500">로딩 중...</div>}>
      <GalleryUploadForm />
    </Suspense>
  );
}

function GalleryUploadForm() {
  useAuth();
  const router = useRouter();
  const searchParams = useSearchParams();
  const initialFolderId = searchParams.get("folderId")
    ? Number(searchParams.get("folderId"))
    : null;

  const [allFolders, setAllFolders] = useState<GalleryFolder[]>([]);
  const [selectedFolderId, setSelectedFolderId] = useState<number | null>(
    initialFolderId
  );
  const [selectedRoles, setSelectedRoles] = useState<string[]>([]);
  const [files, setFiles] = useState<File[]>([]);
  const [previews, setPreviews] = useState<{ file: File; url: string }[]>([]);
  const [isUploading, setIsUploading] = useState<boolean>(false);
  const [errorMsg, setErrorMsg] = useState<string>("");

  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    fetchAllGalleryFolders()
      .then(setAllFolders)
      .catch((err) => console.error("폴더 목록 로드 실패", err));
  }, []);

  const handleFiles = (incomingFiles: FileList | File[]) => {
    setErrorMsg("");
    const fileArray = Array.from(incomingFiles);

    if (files.length + fileArray.length > MAX_COUNT) {
      setErrorMsg(`한 번에 최대 ${MAX_COUNT}장까지 업로드할 수 있습니다.`);
      return;
    }

    const validNewFiles: File[] = [];

    for (const f of fileArray) {
      const ext = f.name.split(".").pop()?.toLowerCase() || "";
      if (!ALLOWED_EXTENSIONS.includes(ext)) {
        setErrorMsg(
          `지원하지 않는 파일 형식입니다: ${f.name} (지원: ${ALLOWED_EXTENSIONS.join(
            ", "
          )})`
        );
        return;
      }
      if (f.size > MAX_FILE_SIZE) {
        setErrorMsg(`파일 크기가 20MB를 초과했습니다: ${f.name}`);
        return;
      }
      validNewFiles.push(f);
    }

    const updatedFiles = [...files, ...validNewFiles];
    setFiles(updatedFiles);

    // 프리뷰 생성
    const newPreviews = validNewFiles.map((file) => ({
      file,
      url: URL.createObjectURL(file),
    }));
    setPreviews((prev) => [...prev, ...newPreviews]);
  };

  const handleRemoveFile = (index: number) => {
    const previewToRemove = previews[index];
    if (previewToRemove) {
      URL.revokeObjectURL(previewToRemove.url);
    }
    setFiles((prev) => prev.filter((_, i) => i !== index));
    setPreviews((prev) => prev.filter((_, i) => i !== index));
  };

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
    if (files.length === 0) {
      setErrorMsg("업로드할 사진을 선택해주세요.");
      return;
    }

    try {
      setIsUploading(true);
      setErrorMsg("");

      await uploadGalleryImages(
        files,
        selectedFolderId,
        selectedFolderId === null && selectedRoles.length > 0
          ? selectedRoles
          : undefined
      );

      // 업로드 성공 후 갤러리 관리 페이지로 이동
      router.push("/manage/gallery");
    } catch (err: any) {
      setErrorMsg(err.message || "사진 업로드에 실패했습니다.");
    } finally {
      setIsUploading(false);
    }
  };

  const selectedFolder = allFolders.find((f) => f.id === selectedFolderId);

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between pb-4 border-b border-gray-200">
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={() => router.push("/manage/gallery")}
            className="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-lg cursor-pointer"
          >
            <ArrowLeftIcon className="h-5 w-5" />
          </button>
          <div>
            <h1 className="text-xl font-bold text-gray-900">사진 업로드</h1>
            <p className="text-xs text-gray-500 mt-0.5">
              교회 갤러리에 사진을 업로드합니다 (최대 20장, 장당 20MB 이하).
            </p>
          </div>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {errorMsg && (
          <div className="flex items-center gap-2 p-3.5 bg-rose-50 border border-rose-200 text-rose-700 text-xs rounded-xl">
            <ExclamationTriangleIcon className="h-5 w-5 shrink-0" />
            <span>{errorMsg}</span>
          </div>
        )}

        {/* 1. 업로드 위치 (폴더 선택) */}
        <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-2xs space-y-3">
          <label className="block text-xs font-bold text-gray-800">
            업로드 위치 (폴더 선택)
          </label>
          <select
            value={selectedFolderId === null ? "" : selectedFolderId}
            onChange={(e) =>
              setSelectedFolderId(
                e.target.value ? Number(e.target.value) : null
              )
            }
            className="w-full rounded-lg border border-gray-300 px-3.5 py-2 text-xs focus:border-indigo-500 focus:outline-hidden"
          >
            <option value="">최상위 (루트) 갤러리</option>
            {allFolders.map((f) => (
              <option key={f.id} value={f.id}>
                {"— ".repeat(f.depth - 1)} {f.name} ({f.depth}단계)
              </option>
            ))}
          </select>
        </div>

        {/* 2. 열람 권한 설정 (루트 업로드 시에만 직접 설정, 폴더 선택 시 자동 상속) */}
        <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-2xs space-y-3">
          <div className="flex items-center justify-between">
            <label className="text-xs font-bold text-gray-800">
              열람 권한 설정
            </label>
            {selectedFolder ? (
              <span className="text-[11px] font-semibold text-indigo-600 bg-indigo-50 px-2 py-0.5 rounded-md">
                폴더 권한 자동 상속
              </span>
            ) : (
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
            )}
          </div>

          {selectedFolder ? (
            <p className="text-xs text-gray-500 bg-gray-50 p-3 rounded-lg border border-gray-200">
              선택한 폴더(
              <span className="font-semibold text-gray-800">
                {selectedFolder.name}
              </span>
              )의 권한 설정이 업로드되는 모든 사진에 고정 적용됩니다.
            </p>
          ) : (
            <>
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                {ROLE_OPTIONS.map((opt) => {
                  const checked = selectedRoles.includes(opt.value);
                  return (
                    <label
                      key={opt.value}
                      className={`flex items-center gap-2 p-2.5 rounded-lg border text-xs cursor-pointer transition-colors ${
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
              <p className="text-[11px] text-gray-500">
                * 아무것도 선택하지 않으면 모든 등록 교인이 열람할 수 있습니다.
              </p>
            </>
          )}
        </div>

        {/* 3. 파일 드래그 & 드롭 영역 */}
        <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-2xs space-y-4">
          <div className="flex items-center justify-between">
            <label className="text-xs font-bold text-gray-800">
              사진 선택 ({files.length} / {MAX_COUNT}장)
            </label>
            {files.length > 0 && (
              <button
                type="button"
                onClick={() => {
                  previews.forEach((p) => URL.revokeObjectURL(p.url));
                  setFiles([]);
                  setPreviews([]);
                }}
                className="text-[11px] text-rose-600 hover:underline cursor-pointer"
              >
                전체 비우기
              </button>
            )}
          </div>

          <label
            onDragOver={(e) => e.preventDefault()}
            onDrop={(e) => {
              e.preventDefault();
              if (e.dataTransfer.files) {
                handleFiles(e.dataTransfer.files);
              }
            }}
            className="block border-2 border-dashed border-gray-300 hover:border-indigo-400 hover:bg-indigo-50/20 rounded-xl p-8 text-center cursor-pointer transition-colors"
          >
            <PhotoIcon className="mx-auto h-12 w-12 text-gray-400" />
            <div className="mt-2 text-xs font-semibold text-gray-800">
              클릭하여 사진을 선택하거나 마우스로 끌어다 놓으세요
            </div>
            <p className="mt-1 text-[11px] text-gray-500">
              지원 형식: JPG, JPEG, PNG, WEBP, HEIC, GIF, AVIF, BMP (장당 최대 20MB)
            </p>
            <input
              ref={fileInputRef}
              type="file"
              multiple
              accept={ALLOWED_EXTENSIONS.map((e) => `.${e}`).join(",")}
              onChange={(e) => {
                if (e.target.files) {
                  handleFiles(e.target.files);
                  e.target.value = "";
                }
              }}
              className="sr-only"
            />
          </label>

          {/* 프리뷰 그리드 */}
          {previews.length > 0 && (
            <div className="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-5 gap-3 pt-2">
              {previews.map((item, idx) => (
                <div
                  key={idx}
                  className="group relative rounded-lg border border-gray-200 overflow-hidden bg-gray-100 aspect-square shadow-2xs"
                >
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img
                    src={item.url}
                    alt={item.file.name}
                    className="h-full w-full object-cover"
                  />
                  <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex flex-col justify-between p-1.5">
                    <button
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleRemoveFile(idx);
                      }}
                      className="self-end p-1 bg-rose-600 text-white rounded-md hover:bg-rose-700 cursor-pointer"
                    >
                      <XMarkIcon className="h-3.5 w-3.5" />
                    </button>
                    <div className="text-[10px] text-white truncate px-1">
                      {item.file.name}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Submit Buttons */}
        <div className="flex justify-end gap-3 pt-2">
          <button
            type="button"
            onClick={() => router.push("/manage/gallery")}
            disabled={isUploading}
            className="px-5 py-2.5 text-xs font-semibold text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 cursor-pointer"
          >
            취소
          </button>
          <button
            type="submit"
            disabled={isUploading || files.length === 0}
            className="inline-flex items-center gap-2 px-5 py-2.5 text-xs font-semibold text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 disabled:opacity-50 cursor-pointer shadow-xs"
          >
            <ArrowUpTrayIcon className="h-4 w-4" />
            {isUploading
              ? `업로드 중 (${files.length}장)...`
              : `${files.length}장 업로드 시작`}
          </button>
        </div>
      </form>
    </div>
  );
}
