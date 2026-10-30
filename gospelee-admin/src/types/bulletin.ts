type BulletinFile = {
  fileDetailId: number;
  fileOriginalName: string;
  fileSize: number;
  extension: string;
};

type Bulletin = {
  id: number;
  ecclesiaUid: number;
  worshipDate: string; // yyyy-MM-dd
  title: string | null;
  openYn: 'Y' | 'N';
  fileKind: 'PDF' | 'IMAGE' | null;
  accessToken: string | null;
  fileList: BulletinFile[];
  insertTime: string;
  updateTime: string;
};

type BulletinInput = {
  id?: number;
  worshipDate: string;
  title: string;
  openYn: 'Y' | 'N';
  files: File[];
  deleteFileDetailIdList?: number[];
};
