const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;

export const apiFetch = async (endpoint: string, options?: RequestInit) => {

  const url = `${API_BASE_URL}${endpoint}`;
  return await fetch(url, options);
};