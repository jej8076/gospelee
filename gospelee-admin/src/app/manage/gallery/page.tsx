"use client";

import React, { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import useAuth from "~/lib/auth/check-auth";
import {
  fetchGalleryFolders,
  fetchAllGalleryFolders,
  deleteGalleryFolder,
  fetchGalleryImages,
  deleteGalleryImages,
} from "~/lib/api/fetch-gallery";
import { GalleryFolder, GalleryImage } from "@/types/gallery";
import FolderModal from "@/components/gallery/FolderModal";
import ImagePreviewModal from "@/components/gallery/ImagePreviewModal";
import {
  FolderIcon,
  FolderPlusIcon,
  ArrowUpTrayIcon,
  TrashIcon,
  PencilSquareIcon,
  ChevronRightIcon,
  MagnifyingGlassIcon,
  PhotoIcon,
  ArrowPathIcon,
} from "@heroicons/react/24/outline";

const ROLE_LABEL_MAP: Record<string, string> = {
  LAYMAN: "성도",
  TEACHER: "교사",
  COORDINATOR: "간사",
  MINISTER: "전도사",
  LICENSED_MINISTER: "강도사",
  PASTOR: "목사",
  SENIOR_PASTOR: "담임목사",
  ADMIN: "관리자",
};

export default function GalleryPage() {
  useAuth();
  const router = useRouter();

  // 현재 탐색 중인 폴더 경로 스택 (Breadcrumb)
  const [folderPath, setFolderPath] = useState<GalleryFolder[]>([]);
  const currentFolder =
    folderPath.length > 0 ? folderPath[folderPath.length - 1] : null;

  // 데이터 상태
  const [subFolders, setSubFolders] = useState<GalleryFolder[]>([]);
  const [allFolders, setAllFolders] = useState<GalleryFolder[]>([]);
  const [images, setImages] = useState<GalleryImage[]>([]);
  const [totalElements, setTotalElements] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  // 필터 & 페이징 상태
  const [startDate, setStartDate] = useState<string>("");
  const [endDate, setEndDate] = useState<string>("");
  const [targetRole, setTargetRole] = useState<string>("ALL");
  const [pageSize, setPageSize] = useState<number>(20);
  const [currentPage, setCurrentPage] = useState<number>(0);

  // 선택 상태 & 모달 상태
  const [selectedImageIds, setSelectedImageIds] = useState<number[]>([]);
  const [isFolderModalOpen, setIsFolderModalOpen] = useState<boolean>(false);
  const [folderToEdit, setFolderToEdit] = useState<GalleryFolder | null>(null);
  const [previewImage, setPreviewImage] = useState<GalleryImage | null>(null);

  const apiBaseUrl = process.env.NEXT_PUBLIC_API_URL || "";

  const getImageUrl = (url?: string | null) => {
    if (!url) return "/images/logo/podo_logo.svg";
    if (url.startsWith("http://") || url.startsWith("https://")) return url;
    return `${apiBaseUrl}${url.startsWith("/") ? "" : "/"}${url}`;
  };

  const loadData = async () => {
    try {
      setIsLoading(true);
      const parentId = currentFolder ? currentFolder.id : null;

      // 1. 현재 폴더의 하위 폴더 조회
      const folders = await fetchGalleryFolders(parentId);
      setSubFolders(folders);

      // 2. 전체 폴더 목록 (폴더 생성 모달용)
      const all = await fetchAllGalleryFolders();
      setAllFolders(all);

      // 3. 현재 위치의 이미지 조회
      const imageRes = await fetchGalleryImages({
        folderId: parentId,
        includeUncategorized: parentId === null,
        startDate: startDate || undefined,
        endDate: endDate || undefined,
        targetRole: targetRole !== "ALL" ? targetRole : undefined,
        page: currentPage,
        size: pageSize,
      });

      setImages(imageRes.content);
      setTotalElements(imageRes.totalElements);
      setTotalPages(imageRes.totalPages);
      setSelectedImageIds([]);
    } catch (error) {
      console.error("갤러리 데이터 로드 실패:", error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [currentFolder, currentPage, pageSize]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setCurrentPage(0);
    loadData();
  };

  const handleResetFilter = () => {
    setStartDate("");
    setEndDate("");
    setTargetRole("ALL");
    setCurrentPage(0);
  };

  // 폴더 이동 (Enter)
  const handleEnterFolder = (folder: GalleryFolder) => {
    setFolderPath((prev) => [...prev, folder]);
    setCurrentPage(0);
  };

  // Breadcrumb 클릭 시 해당 위치로 이동
  const handleNavigateBreadcrumb = (index: number) => {
    if (index === -1) {
      setFolderPath([]);
    } else {
      setFolderPath((prev) => prev.slice(0, index + 1));
    }
    setCurrentPage(0);
  };

  // 폴더 삭제
  const handleDeleteFolder = async (folder: GalleryFolder) => {
    if (
      !confirm(
        `'${folder.name}' 폴더를 삭제하시겠습니까?\n하위 폴더와 사진들도 함께 삭제됩니다.`
      )
    ) {
      return;
    }

    try {
      await deleteGalleryFolder(folder.id);
      loadData();
    } catch (err: any) {
      alert(err.message || "폴더 삭제에 실패했습니다.");
    }
  };

  // 사진 삭제 (단건)
  const handleDeleteSingleImage = async (imageId: number) => {
    if (!confirm("해당 사진을 삭제하시겠습니까?")) return;
    try {
      await deleteGalleryImages([imageId]);
      loadData();
    } catch (err: any) {
      alert(err.message || "사진 삭제에 실패했습니다.");
    }
  };

  // 사진 일괄 삭제
  const handleBatchDeleteImages = async () => {
    if (selectedImageIds.length === 0) return;
    if (
      !confirm(`선택한 ${selectedImageIds.length}개의 사진을 삭제하시겠습니까?`)
    ) {
      return;
    }

    try {
      await deleteGalleryImages(selectedImageIds);
      loadData();
    } catch (err: any) {
      alert(err.message || "사진 삭제에 실패했습니다.");
    }
  };

  // 체크박스 선택 제어
  const handleSelectAllImages = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.checked) {
      setSelectedImageIds(images.map((img) => img.id));
    } else {
      setSelectedImageIds([]);
    }
  };

  const handleToggleSelectImage = (id: number) => {
    setSelectedImageIds((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  const formatFileSize = (bytes: number) => {
    if (bytes >= 1024 * 1024) {
      return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
    }
    return `${Math.round(bytes / 1024)} KB`;
  };

  return (
    <div className="px-4 sm:px-6 lg:px-8 space-y-6">
      {/* 1. Header & Actions */}
      <div className="sm:flex sm:items-center sm:justify-between pb-4 border-b border-gray-200">
        <div>
          <h1 className="text-xl font-bold text-gray-900 flex items-center gap-2">
            <PhotoIcon className="h-6 w-6 text-indigo-600" />
            교회 갤러리 관리
          </h1>
          <p className="mt-1 text-xs text-gray-500">
            교회 사진을 폴더별로 관리하고 권한에 따라 교인들에게 공유합니다.
          </p>
        </div>
        <div className="mt-4 sm:mt-0 flex items-center gap-2.5">
          {(!currentFolder || currentFolder.depth < 3) && (
            <button
              type="button"
              onClick={() => {
                setFolderToEdit(null);
                setIsFolderModalOpen(true);
              }}
              className="inline-flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 shadow-xs cursor-pointer"
            >
              <FolderPlusIcon className="h-4 w-4 text-gray-500" />
              새 폴더 만들기
            </button>
          )}

          <button
            type="button"
            onClick={() => {
              const query = currentFolder ? `?folderId=${currentFolder.id}` : "";
              router.push(`/manage/gallery/upload${query}`);
            }}
            className="inline-flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 shadow-xs cursor-pointer"
          >
            <ArrowUpTrayIcon className="h-4 w-4" />
            사진 업로드
          </button>
        </div>
      </div>

      {/* 2. Breadcrumb Navigation */}
      <nav className="flex items-center gap-2 text-xs font-medium text-gray-500 bg-gray-50 px-4 py-2.5 rounded-lg border border-gray-200">
        <button
          onClick={() => handleNavigateBreadcrumb(-1)}
          className={`hover:text-indigo-600 cursor-pointer ${
            folderPath.length === 0 ? "font-bold text-indigo-600" : ""
          }`}
        >
          루트 (최상위)
        </button>

        {folderPath.map((folder, idx) => {
          const isLast = idx === folderPath.length - 1;
          return (
            <React.Fragment key={folder.id}>
              <ChevronRightIcon className="h-3.5 w-3.5 text-gray-400" />
              <button
                onClick={() => handleNavigateBreadcrumb(idx)}
                className={`hover:text-indigo-600 cursor-pointer ${
                  isLast ? "font-bold text-indigo-600" : ""
                }`}
              >
                {folder.name}
              </button>
            </React.Fragment>
          );
        })}
      </nav>

      {/* 3. Subfolders Section */}
      {subFolders.length > 0 && (
        <div>
          <h2 className="text-xs font-semibold text-gray-500 mb-3 flex items-center gap-1.5">
            <FolderIcon className="h-4 w-4 text-amber-500" />
            하위 폴더 ({subFolders.length})
          </h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3.5">
            {subFolders.map((folder) => (
              <div
                key={folder.id}
                className="group relative flex items-center justify-between p-3.5 rounded-xl border border-gray-200 bg-white hover:border-indigo-300 hover:shadow-xs transition-all"
              >
                <div
                  className="flex items-center gap-3 cursor-pointer flex-1 min-w-0"
                  onClick={() => handleEnterFolder(folder)}
                >
                  <div className="p-2.5 rounded-lg bg-amber-50 text-amber-600 group-hover:bg-amber-100">
                    <FolderIcon className="h-6 w-6" />
                  </div>
                  <div className="min-w-0 flex-1">
                    <h3 className="text-xs font-bold text-gray-900 truncate group-hover:text-indigo-600">
                      {folder.name}
                    </h3>
                    <p className="text-[11px] text-gray-500 mt-0.5">
                      사진 {folder.imageCount || 0}장 · 하위폴더{" "}
                      {folder.subFolderCount || 0}개
                    </p>
                  </div>
                </div>

                <div className="flex items-center gap-1 pl-2">
                  <button
                    type="button"
                    onClick={() => {
                      setFolderToEdit(folder);
                      setIsFolderModalOpen(true);
                    }}
                    title="폴더 수정"
                    className="p-1.5 text-gray-400 hover:text-indigo-600 hover:bg-gray-50 rounded-md"
                  >
                    <PencilSquareIcon className="h-4 w-4" />
                  </button>
                  <button
                    type="button"
                    onClick={() => handleDeleteFolder(folder)}
                    title="폴더 삭제"
                    className="p-1.5 text-gray-400 hover:text-rose-600 hover:bg-gray-50 rounded-md"
                  >
                    <TrashIcon className="h-4 w-4" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* 4. Filter & Search Bar */}
      <form
        onSubmit={handleSearch}
        className="rounded-xl border border-gray-200 bg-white p-4 shadow-2xs space-y-3 sm:space-y-0 sm:flex sm:items-center sm:justify-between sm:gap-4"
      >
        <div className="flex flex-wrap items-center gap-3">
          {/* 등록일자 필터 */}
          <div className="flex items-center gap-1.5 text-xs text-gray-700">
            <span className="font-semibold">등록일:</span>
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="rounded-lg border border-gray-300 px-2.5 py-1.5 text-xs focus:border-indigo-500 focus:outline-hidden"
            />
            <span>~</span>
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="rounded-lg border border-gray-300 px-2.5 py-1.5 text-xs focus:border-indigo-500 focus:outline-hidden"
            />
          </div>

          {/* 권한 필터 */}
          <div className="flex items-center gap-1.5 text-xs text-gray-700">
            <span className="font-semibold">권한:</span>
            <select
              value={targetRole}
              onChange={(e) => setTargetRole(e.target.value)}
              className="rounded-lg border border-gray-300 px-2.5 py-1.5 text-xs focus:border-indigo-500 focus:outline-hidden"
            >
              <option value="ALL">전체 권한</option>
              <option value="LAYMAN">성도(평신도)</option>
              <option value="TEACHER">교사</option>
              <option value="COORDINATOR">간사</option>
              <option value="MINISTER">전도사</option>
              <option value="PASTOR">목사</option>
              <option value="ADMIN">관리자</option>
            </select>
          </div>

          {/* 검색 & 초기화 버튼 */}
          <div className="flex gap-2">
            <button
              type="submit"
              className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-semibold text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 cursor-pointer"
            >
              <MagnifyingGlassIcon className="h-3.5 w-3.5" />
              검색
            </button>
            <button
              type="button"
              onClick={handleResetFilter}
              className="inline-flex items-center gap-1 px-2.5 py-1.5 text-xs font-semibold text-gray-600 bg-gray-100 rounded-lg hover:bg-gray-200 cursor-pointer"
            >
              <ArrowPathIcon className="h-3.5 w-3.5" />
              초기화
            </button>
          </div>
        </div>

        {/* 페이지당 보기 개수 선택 */}
        <div className="flex items-center gap-2 text-xs text-gray-600">
          <span>보기:</span>
          <select
            value={pageSize}
            onChange={(e) => {
              setPageSize(Number(e.target.value));
              setCurrentPage(0);
            }}
            className="rounded-lg border border-gray-300 px-2.5 py-1 text-xs focus:border-indigo-500 focus:outline-hidden"
          >
            <option value={10}>10개씩</option>
            <option value={20}>20개씩</option>
            <option value={50}>50개씩</option>
          </select>
        </div>
      </form>

      {/* 5. Photos Table */}
      <div className="rounded-xl border border-gray-200 bg-white overflow-hidden shadow-2xs">
        {/* Table Control Header */}
        <div className="px-5 py-3.5 bg-gray-50/75 border-b border-gray-200 flex items-center justify-between">
          <span className="text-xs font-semibold text-gray-700">
            사진 목록 (총 {totalElements}장)
          </span>

          {selectedImageIds.length > 0 && (
            <button
              type="button"
              onClick={handleBatchDeleteImages}
              className="inline-flex items-center gap-1.5 px-3 py-1 text-xs font-semibold text-rose-700 bg-rose-50 border border-rose-200 rounded-lg hover:bg-rose-100 cursor-pointer"
            >
              <TrashIcon className="h-3.5 w-3.5" />
              선택 {selectedImageIds.length}개 일괄 삭제
            </button>
          )}
        </div>

        {isLoading ? (
          <div className="p-12 text-center text-xs text-gray-500">
            사진을 불러오는 중입니다...
          </div>
        ) : images.length === 0 ? (
          <div className="p-12 text-center text-xs text-gray-500">
            등록된 사진이 없습니다.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200 text-xs">
              <thead className="bg-gray-50/50">
                <tr>
                  <th className="w-12 px-4 py-3 text-left">
                    <input
                      type="checkbox"
                      checked={
                        images.length > 0 &&
                        selectedImageIds.length === images.length
                      }
                      onChange={handleSelectAllImages}
                      className="h-3.5 w-3.5 rounded-sm border-gray-300 text-indigo-600 focus:ring-indigo-500"
                    />
                  </th>
                  <th className="w-20 px-3 py-3 text-left font-semibold text-gray-700">
                    미리보기
                  </th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-700">
                    파일명
                  </th>
                  <th className="w-24 px-3 py-3 text-left font-semibold text-gray-700">
                    확장자
                  </th>
                  <th className="w-28 px-3 py-3 text-left font-semibold text-gray-700">
                    용량
                  </th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-700">
                    열람 권한
                  </th>
                  <th className="w-40 px-4 py-3 text-left font-semibold text-gray-700">
                    등록일시
                  </th>
                  <th className="w-16 px-4 py-3 text-center font-semibold text-gray-700">
                    삭제
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 bg-white">
                {images.map((image) => {
                  const isChecked = selectedImageIds.includes(image.id);
                  const thumbSrc = getImageUrl(image.thumbnailUrl);

                  return (
                    <tr
                      key={image.id}
                      className={`hover:bg-gray-50/80 transition-colors ${
                        isChecked ? "bg-indigo-50/30" : ""
                      }`}
                    >
                      <td className="px-4 py-2.5">
                        <input
                          type="checkbox"
                          checked={isChecked}
                          onChange={() => handleToggleSelectImage(image.id)}
                          className="h-3.5 w-3.5 rounded-sm border-gray-300 text-indigo-600 focus:ring-indigo-500"
                        />
                      </td>

                      {/* 섬네일 미리보기 */}
                      <td className="px-3 py-2.5">
                        <div
                          onClick={() => setPreviewImage(image)}
                          className="h-12 w-12 rounded-lg border border-gray-200 overflow-hidden bg-gray-100 cursor-pointer hover:opacity-80 transition-opacity"
                        >
                          {/* eslint-disable-next-line @next/next/no-img-element */}
                          <img
                            src={thumbSrc}
                            alt={image.originalFileName}
                            className="h-full w-full object-cover"
                            onError={(e) => {
                              (e.target as any).src =
                                "/images/logo/podo_logo.svg";
                            }}
                          />
                        </div>
                      </td>

                      <td className="px-4 py-2.5 font-medium text-gray-900 truncate max-w-xs">
                        {image.originalFileName}
                      </td>

                      <td className="px-3 py-2.5 text-gray-500 uppercase font-mono">
                        {image.extension}
                      </td>

                      <td className="px-3 py-2.5 text-gray-600 font-mono">
                        {formatFileSize(image.fileSize)}
                      </td>

                      {/* 열람 권한 뱃지 */}
                      <td className="px-4 py-2.5">
                        {image.targetRoles && image.targetRoles.length > 0 ? (
                          <div className="flex flex-wrap gap-1">
                            {image.targetRoles.map((role) => (
                              <span
                                key={role}
                                className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-medium bg-indigo-50 text-indigo-700 border border-indigo-100"
                              >
                                {ROLE_LABEL_MAP[role] || role}
                              </span>
                            ))}
                          </div>
                        ) : (
                          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-medium bg-green-50 text-green-700 border border-green-100">
                            전체 교인
                          </span>
                        )}
                      </td>

                      <td className="px-4 py-2.5 text-gray-500 font-mono text-[11px]">
                        {new Date(image.insertTime).toLocaleDateString("ko-KR", {
                          year: "numeric",
                          month: "2-digit",
                          day: "2-digit",
                          hour: "2-digit",
                          minute: "2-digit",
                        })}
                      </td>

                      <td className="px-4 py-2.5 text-center">
                        <button
                          type="button"
                          onClick={() => handleDeleteSingleImage(image.id)}
                          className="p-1 text-gray-400 hover:text-rose-600 hover:bg-rose-50 rounded-md"
                          title="삭제"
                        >
                          <TrashIcon className="h-4 w-4" />
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}

        {/* 6. Pagination Controls */}
        {totalPages > 1 && (
          <div className="px-5 py-3 border-t border-gray-200 flex items-center justify-between text-xs text-gray-600 bg-gray-50/50">
            <div>
              {currentPage + 1} / {totalPages} 페이지
            </div>
            <div className="flex gap-1">
              <button
                type="button"
                disabled={currentPage === 0}
                onClick={() => setCurrentPage((prev) => Math.max(0, prev - 1))}
                className="px-2.5 py-1 rounded-md border border-gray-300 bg-white hover:bg-gray-50 disabled:opacity-40 cursor-pointer"
              >
                이전
              </button>
              <button
                type="button"
                disabled={currentPage >= totalPages - 1}
                onClick={() =>
                  setCurrentPage((prev) => Math.min(totalPages - 1, prev + 1))
                }
                className="px-2.5 py-1 rounded-md border border-gray-300 bg-white hover:bg-gray-50 disabled:opacity-40 cursor-pointer"
              >
                다음
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Modals */}
      <FolderModal
        isOpen={isFolderModalOpen}
        onClose={() => {
          setIsFolderModalOpen(false);
          setFolderToEdit(null);
        }}
        onSuccess={loadData}
        folderToEdit={folderToEdit}
        parentFolder={currentFolder}
        allFolders={allFolders}
      />

      <ImagePreviewModal
        isOpen={previewImage !== null}
        onClose={() => setPreviewImage(null)}
        image={previewImage}
      />
    </div>
  );
}
