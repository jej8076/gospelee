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

type FileResource = {
  url: string;
  name: string;
  id: number;
};

type Announcement = {
  id?: bigint; // 선택적 필드, 백엔드에서 생성될 수 있음
  organizationType: string;
  subject: string;
  text: string;
  fileUid?: bigint | null; // 파일이 없을 수도 있음
  fileDetailList?: FileDetail[]; // 파일 상세 정보 리스트
  fileDataList?: string[]; // Base64 인코딩된 파일 데이터 리스트 (API에서 제공)
  fileResources?: FileResource[]; // 변환된 파일 리소스 리스트 (클라이언트에서 생성)
  pushNotificationSendYn: 'Y' | 'N'; // 문자열로 Y/N 제한
  pushNotificationIds: string;
  openYn: string;
  insertTime: string;
  updateTime: string;
};
