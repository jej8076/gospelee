import {expireCookie, getCookie} from "~/lib/cookie/cookie-utils";
import {AuthItems} from "~/constants/auth-items";
import {apiFetch} from "~/lib/api-client";

export const fetchAnnouncements = async (): Promise<Announcement[]> => {

  const response = await apiFetch("/api/announcement/ECCLESIA", {
    method: "POST",
    headers: {
      "X-App-Identifier": "OOG_WEB",
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
  const formData = new FormData();

  if (inputData?.file) {
    formData.append('file', inputData.file);
  }

  formData.append('body', new Blob([JSON.stringify(inputData)], {type: 'application/json'}));

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
    console.error("Error response from server:", errorData.message);
    throw {status: response.status, message: errorData.message};
  }

  return response.json();
};
