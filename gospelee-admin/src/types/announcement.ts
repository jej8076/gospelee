type FileDetail = {
  id: number;
  fileId: number;
  filePath: string;
  fileOriginalName: string;
  fileSize: number;
  fileType: string;
  extension: string;
  delYn: string;
};

type Announcement = {
  id?: bigint; // 선택적 필드, 백엔드에서 생성될 수 있음
  organizationType: string;
  subject: string;
  text: string;
  fileUid?: bigint | null; // 파일이 없을 수도 있음
  fileDetailList?: FileDetail[]; // 파일 상세 정보 리스트
  pushNotificationSendYn: 'Y' | 'N'; // 문자열로 Y/N 제한
  pushNotificationIds: string;
  insertTime: string;
  updateTime: string;
};
