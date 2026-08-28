export interface GalleryFolder {
  id: number;
  ecclesiaUid: number;
  parentId: number | null;
  depth: number;
  name: string;
  targetRoles: string[];
  sortOrder: number;
  imageCount: number;
  subFolderCount: number;
  insertTime: string;
  updateTime: string;
}

export interface GalleryFolderRequest {
  id?: number;
  parentId?: number | null;
  name: string;
  targetRoles?: string[];
  sortOrder?: number;
}

export interface GalleryImage {
  id: number;
  ecclesiaUid: number;
  folderId: number | null;
  folderName: string | null;
  fileId: number;
  originalFileDetailId: number;
  thumbnailFileDetailId: number | null;
  originalUrl: string;
  thumbnailUrl: string;
  originalFileName: string;
  fileSize: number;
  extension: string;
  targetRoles: string[];
  insertTime: string;
}

export interface GalleryImageListRequest {
  folderId?: number | null;
  includeUncategorized?: boolean;
  startDate?: string;
  endDate?: string;
  targetRole?: string;
  page?: number;
  size?: number;
}

export interface GalleryImageListResponse {
  content: GalleryImage[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface StorageStatus {
  ecclesiaUid: number;
  ecclesiaName: string;
  storageLimitBytes: number;
  storageUsedBytes: number;
  usedPercentage: number;
  formattedLimit: string;
  formattedUsed: string;
}
