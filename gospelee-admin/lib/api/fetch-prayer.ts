import {apiFetch} from "~/lib/api-client";
import {authHeaders} from "~/lib/api/utils/headers";
import {parseApiResponse} from "~/lib/api/utils/parse-response";

const get = async <T>(path: string): Promise<T> => {
  const response = await apiFetch(`/api/prayer/admin/${path}`, {
    method: "GET",
    headers: await authHeaders(),
  });
  return parseApiResponse<T>(response);
};

const put = async <T>(path: string, body?: unknown): Promise<T> => {
  const response = await apiFetch(`/api/prayer/admin/${path}`, {
    method: "PUT",
    headers: await authHeaders(),
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  return parseApiResponse<T>(response);
};

export const fetchPrayerSetting = () => get<PrayerAdminSetting>("setting");

export const fetchSavePrayerSetting = (setting: PrayerSetting) =>
    put<PrayerAdminSetting>("setting", setting);

export const fetchSavePrayerTreeEvent = (event: PrayerTreeEvent) =>
    put<PrayerAdminSetting>("tree-event", event);

export const fetchPrayerReports = () => get<PrayerAdminCard[]>("reports");

export const fetchPrayerPending = () => get<PrayerAdminCard[]>("pending");

export const fetchPrayerStats = () => get<PrayerAdminStats>("stats");

export type PrayerCardAction = "hide" | "restore" | "approve" | "reject";

export const fetchPrayerCardAction = (id: number, action: PrayerCardAction) =>
    put<{ id: number }>(`cards/${id}/${action}`);
