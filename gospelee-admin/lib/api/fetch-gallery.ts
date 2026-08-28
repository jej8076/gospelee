import {apiFetch} from "~/lib/api-client";
import {authHeaders, authHeadersWithoutContentsType} from "~/lib/api/utils/headers";
import {
  GalleryFolder,
  GalleryFolderRequest,
  GalleryImage,
  GalleryImageListRequest,
  GalleryImageListResponse,
  StorageStatus
} from "@/types/gallery";

export const fetchStorageStatus = async (): Promise<StorageStatus> => {
  const headers = await authHeaders();
  const response = await apiFetch("/api/gallery/storage/status", {
    method: "GET",
    headers
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw {status: response.status, message: errorData.message || "용량 조회 실패"};
  }

  return response.json();
};

export const fetchGalleryFolders = async (
  parentId?: number | null,
  roleFilter?: string
): Promise<GalleryFolder[]> => {
  const headers = await authHeaders();
  let url = "/api/gallery/folder/list";
  const params = new URLSearchParams();
  if (parentId !== undefined && parentId !== null) {
    params.append("parentId", String(parentId));
  }
  if (roleFilter) {
    params.append("roleFilter", roleFilter);
  }
  const queryString = params.toString();
  if (queryString) {
    url += `?${queryString}`;
  }

  const response = await apiFetch(url, {
    method: "GET",
    headers
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw {status: response.status, message: errorData.message || "폴더 목록 조회 실패"};
  }

  return response.json();
};

export const fetchAllGalleryFolders = async (): Promise<GalleryFolder[]> => {
  const headers = await authHeaders();
  const response = await apiFetch("/api/gallery/folder/all", {
    method: "GET",
    headers
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw {status: response.status, message: errorData.message || "전체 폴더 조회 실패"};
  }

  return response.json();
};

export const createGalleryFolder = async (
  request: GalleryFolderRequest
): Promise<GalleryFolder> => {
  const headers = await authHeaders();
  const response = await apiFetch("/api/gallery/folder", {
    method: "POST",
    headers,
    body: JSON.stringify(request)
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw {status: response.status, message: errorData.message || "폴더 생성 실패"};
  }

  return response.json();
};

export const updateGalleryFolder = async (
  request: GalleryFolderRequest
): Promise<GalleryFolder> => {
  const headers = await authHeaders();
  const response = await apiFetch("/api/gallery/folder", {
    method: "PUT",
    headers,
    body: JSON.stringify(request)
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw {status: response.status, message: errorData.message || "폴더 수정 실패"};
  }

  return response.json();
};

export const deleteGalleryFolder = async (id: number): Promise<void> => {
  const headers = await authHeaders();
  const response = await apiFetch(`/api/gallery/folder/${id}`, {
    method: "DELETE",
    headers
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw {status: response.status, message: errorData.message || "폴더 삭제 실패"};
  }
};

export const fetchGalleryImages = async (
  request: GalleryImageListRequest
): Promise<GalleryImageListResponse> => {
  const headers = await authHeaders();
  const response = await apiFetch("/api/gallery/images/list", {
    method: "POST",
    headers,
    body: JSON.stringify(request)
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw {status: response.status, message: errorData.message || "사진 목록 조회 실패"};
  }

  return response.json();
};

export const uploadGalleryImages = async (
  files: File[],
  folderId?: number | null,
  targetRoles?: string[]
): Promise<GalleryImage[]> => {
  const headers = await authHeadersWithoutContentsType();
  const formData = new FormData();

  files.forEach((file) => {
    formData.append("files", file);
  });

  let url = "/api/gallery/images/upload";
  const params = new URLSearchParams();
  if (folderId !== undefined && folderId !== null) {
    params.append("folderId", String(folderId));
  }
  if (targetRoles && targetRoles.length > 0) {
    targetRoles.forEach((role) => params.append("targetRoles", role));
  }
  const queryString = params.toString();
  if (queryString) {
    url += `?${queryString}`;
  }

  const response = await apiFetch(url, {
    method: "POST",
    headers,
    body: formData
  }, 30000); // 파일 업로드 타임아웃 30초

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw {status: response.status, message: errorData.message || "사진 업로드 실패"};
  }

  return response.json();
};

export const deleteGalleryImages = async (imageIds: number[]): Promise<void> => {
  const headers = await authHeaders();
  const response = await apiFetch("/api/gallery/images", {
    method: "DELETE",
    headers,
    body: JSON.stringify(imageIds)
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw {status: response.status, message: errorData.message || "사진 삭제 실패"};
  }
};
