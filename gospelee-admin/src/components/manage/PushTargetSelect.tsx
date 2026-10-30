"use client"

import {useEffect, useState} from "react";
import Link from "next/link";
import {fetchPushGroups} from "~/lib/api/fetch-push-group";

interface PushTargetSelectProps {
  value: PushTarget;
  onChange: (value: PushTarget) => void;
  /** 라디오 name 이 겹치지 않게 화면마다 다르게 준다 */
  name: string;
  title?: string;
}

/** 서버에 보낼 푸시 필드로 바꾼다. 그룹을 고르지 않았으면 서버는 교회 성도 전체로 본다 */
export const toPushPayload = (target: PushTarget): { pushNotificationSendYn: 'Y' | 'N'; pushGroupIds: number[] } => {
  if (target.mode === 'ALL') {
    return {pushNotificationSendYn: 'Y', pushGroupIds: []};
  }
  if (target.mode === 'GROUPS') {
    return {pushNotificationSendYn: 'Y', pushGroupIds: target.groupIds};
  }
  return {pushNotificationSendYn: 'N', pushGroupIds: []};
};

/** 문제가 있으면 안내 문구를, 없으면 null 을 돌려준다 */
export const validatePushTarget = (target: PushTarget): string | null => {
  if (target.mode === '') {
    return "푸시 알림 발송 여부를 선택해주세요";
  }
  // 그룹을 고르지 않으면 서버가 전체 발송으로 처리하므로, 의도치 않은 전체 발송을 막는다
  if (target.mode === 'GROUPS' && target.groupIds.length === 0) {
    return "알림을 보낼 그룹을 한 개 이상 선택해주세요";
  }
  return null;
};

export default function PushTargetSelect({value, onChange, name, title = "푸시 알림"}: PushTargetSelectProps) {
  const [groups, setGroups] = useState<PushGroup[]>([]);
  const [loaded, setLoaded] = useState(false);

  useEffect(() => {
    fetchPushGroups()
    .then(setGroups)
    .catch(() => setGroups([]))
    .finally(() => setLoaded(true));
  }, []);

  const selectMode = (mode: PushTarget['mode']) => {
    onChange({mode, groupIds: mode === 'GROUPS' ? value.groupIds : []});
  };

  const toggleGroup = (id: number) => {
    const next = value.groupIds.includes(id)
        ? value.groupIds.filter((g) => g !== id)
        : [...value.groupIds, id];
    onChange({mode: 'GROUPS', groupIds: next});
  };

  const options: { value: PushTarget['mode']; label: string; description: string }[] = [
    {value: 'ALL', label: '등록 교인 전체발송', description: '교회의 모든 교인에게 푸시 알림을 발송합니다'},
    {value: 'GROUPS', label: '그룹 선택 발송', description: '선택한 푸시 그룹에게만 발송합니다 (여러 그룹 선택 가능)'},
    {value: 'NONE', label: '발송안함', description: '푸시 알림을 발송하지 않습니다'},
  ];

  return (
      <fieldset>
        <legend className="text-sm/6 font-semibold text-gray-900">{title}</legend>
        <p className="mt-1 text-sm/6 text-gray-600">앱 푸시 알림을 누구에게 발송할지 선택해 주세요</p>
        <div className="mt-6 space-y-6">
          {options.map((option) => (
              <div key={option.value}>
                <div className="flex items-center gap-x-3">
                  <input
                      id={`${name}-${option.value}`}
                      name={name}
                      type="radio"
                      checked={value.mode === option.value}
                      onChange={() => selectMode(option.value)}
                      className="size-4 border-gray-300 text-indigo-600 focus:ring-indigo-600"
                  />
                  <label htmlFor={`${name}-${option.value}`} className="block text-sm/6 font-medium text-gray-900">
                    {option.label}
                    <span className="ml-2 font-normal text-gray-500">{option.description}</span>
                  </label>
                </div>

                {option.value === 'GROUPS' && value.mode === 'GROUPS' && (
                    <div className="mt-3 ml-7 rounded-md border border-gray-200 bg-gray-50 p-3">
                      {loaded && groups.length === 0 ? (
                          <p className="text-sm text-gray-600">
                            만들어 둔 푸시 그룹이 없습니다.{" "}
                            <Link href="/manage/push-group/create" className="font-semibold text-indigo-600 hover:text-indigo-500">
                              푸시 그룹 만들기
                            </Link>
                          </p>
                      ) : (
                          <ul className="space-y-2">
                            {groups.map((group) => (
                                <li key={group.id} className="flex items-center gap-x-3">
                                  <input
                                      id={`${name}-group-${group.id}`}
                                      type="checkbox"
                                      checked={value.groupIds.includes(group.id)}
                                      onChange={() => toggleGroup(group.id)}
                                      className="size-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-600"
                                  />
                                  <label htmlFor={`${name}-group-${group.id}`} className="text-sm text-gray-900">
                                    {group.name}
                                    <span className="ml-2 text-gray-500">{group.memberCount}명</span>
                                  </label>
                                </li>
                            ))}
                          </ul>
                      )}
                    </div>
                )}
              </div>
          ))}
        </div>
      </fieldset>
  );
}
