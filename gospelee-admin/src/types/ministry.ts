type MinistryRow = {
  id: number;
  name: string;
  status: 'ACTIVE' | 'SUSPENDED';
  memberCount: number;
  ownerName: string;
  storageUsedBytes: number;
  storageLimitBytes: number;
  createdAt?: string | null;
};
