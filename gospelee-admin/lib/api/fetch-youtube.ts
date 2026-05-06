import {apiFetch} from "~/lib/api-client";
import {authHeaders} from "~/lib/api/utils/headers";

export const fetchYoutubeVideos = async (): Promise<YoutubeVideo[]> => {
  const headers = await authHeaders();
  const response = await apiFetch("/api/youtube/videos/all", {
    method: "POST",
    headers: headers,
  });

  if (!response.ok) {
    const errorData = await response.json();
    console.error("Error response from server:", errorData.message);
    throw {status: response.status, message: errorData.message};
  }

  const result = await response.json();
  return result.data;
};

export const fetchYoutubeVideoById = async (id: string): Promise<YoutubeVideo> => {
  const headers = await authHeaders();
  const response = await apiFetch(`/api/youtube/videos/${id}`, {
    method: "POST",
    headers: headers,
  });

  if (!response.ok) {
    const errorData = await response.json();
    console.error("Error response from server:", errorData.message);
    throw {status: response.status, message: errorData.message};
  }

  const result = await response.json();
  return result.data;
};

export const fetchCreateYoutubeVideo = async (inputData: Partial<YoutubeVideo>): Promise<YoutubeVideo> => {
  const headers = await authHeaders();
  const response = await apiFetch("/api/youtube/videos/create", {
    method: "POST",
    headers: headers,
    body: JSON.stringify(inputData),
  });

  if (!response.ok) {
    const errorData = await response.json();
    console.error("Error response from server:", errorData);
    throw {status: response.status, message: errorData.message};
  }

  const result = await response.json();
  return result.data;
};

export const fetchUpdateYoutubeVideo = async (inputData: Partial<YoutubeVideo>): Promise<YoutubeVideo> => {
  const headers = await authHeaders();
  const response = await apiFetch("/api/youtube/videos", {
    method: "PUT",
    headers: headers,
    body: JSON.stringify(inputData),
  });

  if (!response.ok) {
    const errorData = await response.json();
    console.error("Error response from server:", errorData);
    throw {status: response.status, message: errorData.message};
  }

  const result = await response.json();
  return result.data;
};
