import {expireCookie, getCookie} from "~/lib/cookie/cookie-utils";
import {AuthItems} from "~/constants/auth-items";
import {apiFetch} from "~/lib/api-client";
import {isBlank} from "@/utils/common-utils";
import {authHeaders} from "~/lib/api/utils/headers";

export const fetchAnnouncements = async (type?: string): Promise<Announcement[]> => {

  type = isBlank(type) ? 'ECCLESIA' : type;

  const headers = await authHeaders();
  const response = await apiFetch(`/api/announcement/${type}`, {
    method: "POST",
    headers: headers
  });

  if (!response.ok) {
    await expireCookie(AuthItems.Authorization);
    const errorData = await response.json();
    console.error("Error response from server:", errorData.message);
    throw {status: response.status, message: errorData.message};
  }

  return response.json();
};

export const fetchAnnouncementById = async (type: string, id: string): Promise<Announcement> => {

  const headers = await authHeaders();
  const response = await apiFetch(`/api/announcement/${type}/${id}`, {
    method: "POST",
    headers: headers
  });

  if (!response.ok) {
    await expireCookie(AuthItems.Authorization);
    const errorData = await response.json();
    console.error("Error response from server:", errorData.message);
    throw {status: response.status, message: errorData.message};
  }

  const announcement: Announcement = await response.json();

  // fileDataList가 있는 경우 Blob URL로 변환
  if (announcement.fileDataList && announcement.fileDetailList) {
    const fileResources: FileResource[] = [];

    for (let i = 0; i < announcement.fileDataList.length && i < announcement.fileDetailList.length; i++) {
      try {
        const base64Data = announcement.fileDataList[i];
        if (base64Data) {
          // Base64 데이터를 Blob으로 변환
          const byteCharacters = atob(base64Data);
          const byteNumbers = new Array(byteCharacters.length);
          for (let j = 0; j < byteCharacters.length; j++) {
            byteNumbers[j] = byteCharacters.charCodeAt(j);
          }
          const byteArray = new Uint8Array(byteNumbers);
          const blob = new Blob([byteArray], {type: announcement.fileDetailList[i].fileType});
          const url = URL.createObjectURL(blob);

          fileResources.push({
            url: url,
            name: announcement.fileDetailList[i].fileOriginalName,
            id: announcement.fileDetailList[i].id
          });
        }
      } catch (error) {
        console.warn(`파일 리소스 처리 실패 - index: ${i}`, error);
      }
    }

    // 변환된 파일 리소스를 announcement에 추가
    announcement.fileResources = fileResources;
  }

  return announcement;
};

export const fetchInsertAnnouncement = async (inputData: {
  [key: string]: any
}): Promise<Announcement> => {
  console.log('전송할 데이터:', inputData);

  const formData = new FormData();

  // 여러 파일 처리
  if (inputData?.files && Array.isArray(inputData.files)) {
    console.log('파일 개수:', inputData.files.length);
    inputData.files.forEach((file: File, index: number) => {
      console.log(`파일 ${index}:`, file.name, file.size);
      formData.append('files', file);
    });
    delete inputData.files;
  }

  console.log('JSON으로 전송할 데이터:', inputData);
  formData.append('body', new Blob([JSON.stringify(inputData)], {type: 'application/json'}));

  // FormData 내용 확인
  console.log('FormData 내용:');
  formData.forEach((value, key) => {
    console.log(key, value);
  });

  const headers = await authHeaders();
  const response = await apiFetch("/api/announcement", {
    method: "POST",
    headers: headers,
    body: formData,
  });

  if (!response.ok) {
    await expireCookie(AuthItems.Authorization);
    const errorData = await response.json();
    console.error("Error response from server:", errorData);
    throw {status: response.status, message: errorData.message};
  }

  return response.json();
};

export const fetchUpdateAnnouncement = async (inputData: {
  [key: string]: any
}): Promise<Announcement> => {
  console.log('수정할 데이터:', inputData);

  const formData = new FormData();

  // 여러 파일 처리
  if (inputData?.files && Array.isArray(inputData.files)) {
    console.log('파일 개수:', inputData.files.length);
    inputData.files.forEach((file: File, index: number) => {
      console.log(`파일 ${index}:`, file.name, file.size);
      formData.append('files', file);
    });
    delete inputData.files;
  }

  console.log('JSON으로 전송할 데이터:', inputData);
  formData.append('body', new Blob([JSON.stringify(inputData)], {type: 'application/json'}));

  // FormData 내용 확인
  console.log('FormData 내용:');
  formData.forEach((value, key) => {
    console.log(key, value);
  });

  const headers = await authHeaders();
  const response = await apiFetch("/api/announcement", {
    method: "PUT",
    headers: headers,
    body: formData,
  });

  if (!response.ok) {
    await expireCookie(AuthItems.Authorization);
    const errorData = await response.json();
    console.error("Error response from server:", errorData);
    throw {status: response.status, message: errorData.message};
  }

  return response.json();
};
