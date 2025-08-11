import {expireCookie, getCookie} from "~/lib/cookie/cookie-utils";
import {AuthItems} from "~/constants/auth-items";
import {apiFetch} from "~/lib/api-client";

export const fetchAnnouncements = async (): Promise<Announcement[]> => {

  const response = await apiFetch("/api/announcement/ECCLESIA", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: AuthItems.Bearer + (await getCookie(AuthItems.Authorization)),
    }
  });

  if (!response.ok) {
    await expireCookie(AuthItems.Authorization);
    const errorData = await response.json();
    console.error("Error response from server:", errorData.message);
    throw {status: response.status, message: errorData.message};
  }

  return response.json();
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

  const response = await apiFetch("/api/announcement", {
    method: "POST",
    headers: {
      // "Content-Type": "application/json",
      Authorization: AuthItems.Bearer + (await getCookie(AuthItems.Authorization)),
    },
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
