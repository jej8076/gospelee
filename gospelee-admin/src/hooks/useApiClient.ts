"use client";

import {useRouter} from "next/navigation";

type ApiFunction<T> = (...args: any[]) => Promise<T>;

export const useApiClient = () => {
  const router = useRouter();

  const callApi = async <T>(
      apiFunction: ApiFunction<T>,
      onSuccess: (data: T) => void,
      ...args: any[]
  ): Promise<void> => {
    try {
      const data = await apiFunction(...args);
      onSuccess(data);
    } catch (error: any) {
      if (error.status === 401) {
        debugger;
        router.push("/login");
      } else if (error.status === 403) {
        debugger;
        router.push("/apply/ecclesia");
      } else {
        console.error("Unhandled error:", error.message || error);
      }
    }
  };

  return {callApi};
};
