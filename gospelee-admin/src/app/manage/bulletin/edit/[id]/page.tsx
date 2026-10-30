"use client"

import {useEffect, useState} from "react";
import useAuth from "~/lib/auth/check-auth";
import {useParams, useRouter} from "next/navigation";
import BulletinForm from "@/components/manage/BulletinForm";
import {fetchBulletinById, fetchDeleteBulletin, fetchUpdateBulletin} from "~/lib/api/fetch-bulletin";

export default function EditBulletin() {
  useAuth();

  const params = useParams();
  const router = useRouter();
  const bulletinId = params.id as string;
  const [bulletin, setBulletin] = useState<Bulletin>();

  useEffect(() => {
    if (!bulletinId) {
      return;
    }
    fetchBulletinById(bulletinId)
    .then(setBulletin)
    .catch((error) => {
      if (error?.status === 401) {
        router.push("/login");
        return;
      }
      alert(error?.message ?? "주보를 불러오지 못했습니다.");
      router.push("/manage/bulletin");
    });
  }, [bulletinId]);

  if (!bulletin) {
    return (
        <div className="flex justify-center items-center h-64">
          <div className="text-lg text-gray-500">로딩 중...</div>
        </div>
    );
  }

  const handleSubmit = async (input: BulletinInput) => {
    await fetchUpdateBulletin(input);
    router.push("/manage/bulletin");
  };

  const handleDelete = async () => {
    await fetchDeleteBulletin(bulletin.id);
    router.push("/manage/bulletin");
  };

  return (
      <BulletinForm
          mode="edit"
          initial={bulletin}
          onSubmit={handleSubmit}
          onDelete={handleDelete}
          onCancel={() => router.push("/manage/bulletin")}
      />
  );
}
