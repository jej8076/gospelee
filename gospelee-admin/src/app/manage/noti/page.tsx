"use client"

import React, { useEffect, useState, type JSX } from 'react';
import useAuth from "~/lib/auth/check-auth";
import { useRouter } from "next/navigation";
import { useApiClient } from "@/hooks/useApiClient";
import { fetchAnnouncements } from "~/lib/api/fetch-announcement";
import PageHeader from "@/components/manage/PageHeader";
import DataTable from "@/components/manage/DataTable";

export default function ManageNoti(): JSX.Element {
  useAuth();

  const router = useRouter();
  const { callApi } = useApiClient();
  const [announcements, setAnnouncements] = useState<Announcement[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const handleCreate = () => {
    router.push('/manage/noti/create');
  };

  const handleEdit = (announcement: Announcement) => {
    if (announcement.id) {
      router.push(`/manage/noti/edit/${announcement.id}`);
    }
  };

  const loadAnnouncements = async () => {
    try {
      setIsLoading(true);
      await callApi(fetchAnnouncements, setAnnouncements);
    } catch (error) {
      console.error('공지사항 로드 실패:', error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadAnnouncements();
  }, []);

  const columns = [
    {
      key: 'text',
      label: '본문',
      width: 'w-2/5',
      render: (text: string) => (
        <div className="max-w-xs truncate" title={text}>
          {text}
        </div>
      )
    },
    {
      key: 'pushNotificationIds',
      label: '푸시알림',
      width: 'w-1/6',
      render: (pushNotificationIds: string | null) => (
        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
          pushNotificationIds ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
        }`}>
          {pushNotificationIds ? '발송완료' : '발송안함'}
        </span>
      )
    },
    {
      key: 'insertTime',
      label: '등록시간',
      width: 'w-1/6',
      render: (insertTime: string) => (
        <time className="text-gray-500">
          {new Date(insertTime).toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
          })}
        </time>
      )
    }
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
        title="공지사항"
        description="교회 공지사항을 관리합니다"
        buttonText="새로만들기"
        onButtonClick={handleCreate}
      />
      
      <DataTable
        columns={columns}
        data={announcements}
        onEdit={handleEdit}
      />
    </div>
  );
}
