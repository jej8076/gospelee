"use client"

import {useEffect, useState} from "react";
import Modal from "@/components/modal/modal";
import {fetchPushGroupCandidates} from "~/lib/api/fetch-push-group";
import {displayName, ROLE_LABELS} from "@/components/manage/pushGroupLabels";

interface PushGroupMembersModalProps {
  /** null 이면 닫힌 상태 */
  group: PushGroup | null;
  onClose: () => void;
}

/** 푸시 그룹에 속한 성도(알림 수신 대상)를 보여주는 모달 */
export default function PushGroupMembersModal({group, onClose}: PushGroupMembersModalProps) {
  const [candidates, setCandidates] = useState<PushGroupCandidate[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!group || candidates) {
      return;
    }
    setError(null);
    fetchPushGroupCandidates()
    .then(setCandidates)
    .catch((e) => setError(e?.message ?? "성도 목록을 불러오지 못했습니다."));
  }, [group, candidates]);

  const members = group && candidates
      ? candidates.filter((c) => group.memberAccountUids.includes(c.uid))
      : [];
  const unreachable = members.filter((c) => !c.pushEnabled).length;
  // 그룹에는 있지만 더는 고를 수 없는 성도(교회를 옮긴 경우 등)
  const missing = group && candidates ? group.memberAccountUids.length - members.length : 0;

  return (
      <Modal
          isOpen={group !== null}
          onClose={onClose}
          title={group ? `${group.name} 수신 대상` : undefined}
      >
        {error && <p className="text-sm text-red-600">{error}</p>}
        {!error && !candidates && <p className="text-sm text-gray-500">불러오는 중...</p>}
        {candidates && (
            <>
              <p className="text-sm text-gray-600">
                총 {group?.memberAccountUids.length ?? 0}명
                {unreachable > 0 && <span className="text-amber-600"> · 알림을 받을 수 없는 {unreachable}명 포함</span>}
              </p>
              <ul className="mt-3 max-h-80 divide-y divide-gray-100 overflow-y-auto rounded-md border border-gray-200">
                {members.length === 0 ? (
                    <li className="px-3 py-6 text-center text-sm text-gray-500">그룹에 성도가 없습니다.</li>
                ) : members.map((c) => (
                    <li key={c.uid} className="flex items-center gap-x-3 px-3 py-2">
                      <span className="text-sm text-gray-900">{displayName(c)}</span>
                      <span className="text-xs text-gray-500">{ROLE_LABELS[c.role] ?? c.role}</span>
                      {!c.pushEnabled && (
                          <span className="ml-auto text-xs text-amber-600"
                                title="앱에서 알림을 허용하지 않았거나 로그인한 적이 없어 알림을 받을 수 없습니다">
                            알림 수신 불가
                          </span>
                      )}
                    </li>
                ))}
              </ul>
              {missing > 0 && (
                  <p className="mt-2 text-xs text-gray-500">목록에 없는 {missing}명은 현재 교회 성도가 아니어서 알림이 가지 않습니다.</p>
              )}
            </>
        )}
      </Modal>
  );
}
