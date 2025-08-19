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
        router.push("/login");
      } else {
        console.log("Unhandled error:", error.message || error);
      }
    }
  };

  return {callApi};
};
