"use client"

import {useEffect, useState} from "react";
import useAuth from "~/lib/auth/check-auth";
import {useParams, useRouter} from "next/navigation";
import PushGroupForm from "@/components/manage/PushGroupForm";
import {fetchDeletePushGroup, fetchPushGroups, fetchUpdatePushGroup} from "~/lib/api/fetch-push-group";

export default function EditPushGroup() {
  useAuth();

  const params = useParams();
  const router = useRouter();
  const groupId = Number(params.id);
  const [group, setGroup] = useState<PushGroup>();

  useEffect(() => {
    // 그룹 한 건 조회 API 는 따로 없어 목록에서 찾는다 (교회당 최대 30개)
    fetchPushGroups()
    .then((groups) => {
      const found = groups.find((g) => g.id === groupId);
      if (!found) {
        alert("그룹을 찾을 수 없습니다.");
        router.push("/manage/push-group");
        return;
      }
      setGroup(found);
    })
    .catch((error) => {
      if (error?.status === 401) {
        router.push("/login");
        return;
      }
      alert(error?.message ?? "그룹을 불러오지 못했습니다.");
      router.push("/manage/push-group");
    });
  }, [groupId]);

  if (!group) {
    return (
        <div className="flex justify-center items-center h-64">
          <div className="text-lg text-gray-500">로딩 중...</div>
        </div>
    );
  }

  const handleSubmit = async (input: PushGroupInput) => {
    await fetchUpdatePushGroup(input);
    router.push("/manage/push-group");
  };

  const handleDelete = async () => {
    await fetchDeletePushGroup(group.id);
    router.push("/manage/push-group");
  };

  return (
      <PushGroupForm
          mode="edit"
          initial={group}
          onSubmit={handleSubmit}
          onDelete={handleDelete}
          onCancel={() => router.push("/manage/push-group")}
      />
  );
}
