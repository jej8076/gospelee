"use client"

import useAuth from "~/lib/auth/check-auth";
import {useRouter} from "next/navigation";
import PushGroupForm from "@/components/manage/PushGroupForm";
import {fetchCreatePushGroup} from "~/lib/api/fetch-push-group";

export default function CreatePushGroup() {
  useAuth();

  const router = useRouter();

  const handleSubmit = async (input: PushGroupInput) => {
    await fetchCreatePushGroup(input);
    router.push("/manage/push-group");
  };

  return (
      <PushGroupForm
          mode="create"
          onSubmit={handleSubmit}
          onCancel={() => router.push("/manage/push-group")}
      />
  );
}
