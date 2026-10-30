"use client"

import React, {useEffect, useState, type JSX} from 'react';
import useAuth from "~/lib/auth/check-auth";
import {useRouter} from "next/navigation";
import {useApiClient} from "@/hooks/useApiClient";
import {fetchBulletins} from "~/lib/api/fetch-bulletin";
import PageHeader from "@/components/manage/PageHeader";
import DataTable from "@/components/manage/DataTable";

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토'];

const formatWorshipDate = (ymd: string): string => {
  const [y, m, d] = ymd.split('-').map(Number);
  const weekday = WEEKDAYS[new Date(y, m - 1, d).getDay()];
  return `${y}.${String(m).padStart(2, '0')}.${String(d).padStart(2, '0')} (${weekday})`;
};

const describeFiles = (bulletin: Bulletin): string => {
  if (bulletin.fileKind === 'PDF') {
    return 'PDF';
  }
  if (bulletin.fileKind === 'IMAGE') {
    return `이미지 ${bulletin.fileList.length}장`;
  }
  return '-';
};

export default function ManageBulletin(): JSX.Element {
  useAuth();

  const router = useRouter();
  const {callApi} = useApiClient();
  const [bulletins, setBulletins] = useState<Bulletin[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        // 한 해 분량(52주)까지 보여준다
        await callApi(() => fetchBulletins(52), setBulletins);
      } finally {
        setIsLoading(false);
      }
    };
    load();
  }, []);

  const columns = [
    {
      key: 'worshipDate',
      label: '예배 날짜',
      width: 'w-1/5',
      render: (worshipDate: string) => <span>{formatWorshipDate(worshipDate)}</span>
    },
    {
      key: 'title',
      label: '제목',
      width: 'w-2/5',
      render: (title: string | null) => (
          <div className="max-w-xs truncate" title={title ?? ''}>{title || '-'}</div>
      )
    },
    {
      key: 'fileKind',
      label: '파일',
      width: 'w-1/6',
      render: (_: unknown, item: Bulletin) => <span>{describeFiles(item)}</span>
    },
    {
      key: 'openYn',
      label: '공개',
      width: 'w-1/12',
      render: (openYn: string) => (
          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
              openYn === 'Y' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
          }`}>
          {openYn === 'Y' ? '공개' : '비공개'}
        </span>
      )
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
            title="주보"
            description="교회 주보를 관리합니다. 예배 날짜(주일)마다 한 건씩 올립니다."
            buttonText="새로만들기"
            onButtonClick={() => router.push('/manage/bulletin/create')}
        />

        <DataTable
            columns={columns}
            data={bulletins}
            onEdit={(bulletin: Bulletin) => router.push(`/manage/bulletin/edit/${bulletin.id}`)}
        />
      </div>
  );
}
