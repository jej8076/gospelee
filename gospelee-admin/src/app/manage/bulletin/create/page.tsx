"use client"

import useAuth from "~/lib/auth/check-auth";
import {useRouter} from "next/navigation";
import BulletinForm from "@/components/manage/BulletinForm";
import {fetchInsertBulletin} from "~/lib/api/fetch-bulletin";

export default function CreateBulletin() {
  useAuth();

  const router = useRouter();

  const handleSubmit = async (input: BulletinInput) => {
    await fetchInsertBulletin(input);
    router.push("/manage/bulletin");
  };

  return (
      <BulletinForm
          mode="create"
          onSubmit={handleSubmit}
          onCancel={() => router.push("/manage/bulletin")}
      />
  );
}
