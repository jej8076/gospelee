"use client"

import React, {useEffect, useState, type JSX} from 'react';
import useAuth from "~/lib/auth/check-auth";
import {useRouter} from "next/navigation";
import {useApiClient} from "@/hooks/useApiClient";
import {fetchYoutubeVideos} from "~/lib/api/fetch-youtube";
import PageHeader from "@/components/manage/PageHeader";
import DataTable from "@/components/manage/DataTable";

export default function ManageVideo(): JSX.Element {
  useAuth();

  const router = useRouter();
  const {callApi} = useApiClient();
  const [videos, setVideos] = useState<YoutubeVideo[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const handleCreate = () => {
    router.push('/manage/video/create');
  };

  const handleEdit = (video: YoutubeVideo) => {
    if (video.id) {
      router.push(`/manage/video/edit/${video.id}`);
    }
  };

  const loadVideos = async () => {
    try {
      setIsLoading(true);
      await callApi(fetchYoutubeVideos, setVideos);
    } catch (error) {
      console.error('영상 목록 로드 실패:', error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadVideos();
  }, []);

  const columns = [
    {
      key: 'title',
      label: '제목',
      width: 'w-1/4',
      render: (title: string) => (
          <div className="max-w-xs truncate" title={title}>
            {title}
          </div>
      )
    },
    {
      key: 'videoId',
      label: 'Video ID',
      width: 'w-1/6',
      render: (videoId: string) => (
          <code className="text-xs bg-gray-100 px-1.5 py-0.5 rounded">{videoId}</code>
      )
    },
    {
      key: 'channelTitle',
      label: '채널',
      width: 'w-1/6',
    },
    {
      key: 'isActive',
      label: '상태',
      width: 'w-1/12',
      render: (isActive: boolean) => (
          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
              isActive ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
          }`}>
            {isActive ? '공개' : '비공개'}
          </span>
      )
    },
    {
      key: 'sortOrder',
      label: '정렬',
      width: 'w-1/12',
    },
    {
      key: 'insertTime',
      label: '등록시간',
      width: 'w-1/6',
      render: (insertTime: string) => (
          <time className="text-gray-500">
            {insertTime ? new Date(insertTime).toLocaleDateString('ko-KR', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit',
              hour: '2-digit',
              minute: '2-digit'
            }) : '-'}
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
            title="영상 관리"
            description="YouTube 영상을 관리합니다"
            buttonText="새로만들기"
            onButtonClick={handleCreate}
        />

        <DataTable
            columns={columns}
            data={videos}
            onEdit={handleEdit}
        />
      </div>
  );
}
