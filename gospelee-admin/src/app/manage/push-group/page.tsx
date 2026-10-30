"use client"

import React, {useEffect, useState, type JSX} from 'react';
import useAuth from "~/lib/auth/check-auth";
import {useRouter} from "next/navigation";
import {useApiClient} from "@/hooks/useApiClient";
import {fetchPushGroups} from "~/lib/api/fetch-push-group";
import PageHeader from "@/components/manage/PageHeader";
import DataTable from "@/components/manage/DataTable";

export default function ManagePushGroup(): JSX.Element {
  useAuth();

  const router = useRouter();
  const {callApi} = useApiClient();
  const [groups, setGroups] = useState<PushGroup[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        await callApi(fetchPushGroups, setGroups);
      } finally {
        setIsLoading(false);
      }
    };
    load();
  }, []);

  const columns = [
    {key: 'name', label: '그룹 이름', width: 'w-1/4'},
    {
      key: 'description',
      label: '설명',
      width: 'w-2/5',
      render: (description: string | null) => (
          <div className="max-w-xs truncate" title={description ?? ''}>{description || '-'}</div>
      )
    },
    {
      key: 'memberCount',
      label: '멤버',
      width: 'w-1/6',
      render: (memberCount: number) => <span>{memberCount}명</span>
    },
  ];

  if (isLoading) {
    return (
        <div className="px-4 sm:px-6 lg:px-8">
          <div className="flex justify-center items-center h-64">
            <div className="text-lg text-gray-500">로딩 중...</div>
          </div>
        </div>
    );
  }

  return (
      <div className="px-4 sm:px-6 lg:px-8">
        <PageHeader
            title="푸시 그룹"
            description="성도를 직접 골라 그룹으로 묶어 두면, 공지·주보를 등록할 때 그룹에게만 알림을 보낼 수 있습니다."
            buttonText="새로만들기"
            onButtonClick={() => router.push('/manage/push-group/create')}
        />

        <DataTable
            columns={columns}
            data={groups}
            onEdit={(group: PushGroup) => router.push(`/manage/push-group/edit/${group.id}`)}
        />
      </div>
  );
}
