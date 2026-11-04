"use client"

import {useEffect, useMemo, useRef, useState} from "react";
import Modal from "@/components/modal/modal";
import {blueButton, grayButton} from "@/components/modal/modal-buttons";
import {fetchCreatePushGroup, fetchPushGroupCandidates} from "~/lib/api/fetch-push-group";
import {displayName, ROLE_LABELS} from "@/components/manage/pushGroupLabels";

const inputClass = "block w-full rounded-md bg-white px-3 py-1.5 text-base text-gray-900 border border-gray-300 placeholder:text-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600 sm:text-sm/6";

interface PushGroupCreateModalProps {
  isOpen: boolean;
  onClose: () => void;
  /** 그룹이 만들어지면 호출된다 */
  onCreated: (group: PushGroup) => void;
}

/** 알림을 보내는 화면을 떠나지 않고 푸시 그룹을 바로 만드는 모달 */
export default function PushGroupCreateModal({isOpen, onClose, onCreated}: PushGroupCreateModalProps) {
  const [name, setName] = useState("");
  const [selected, setSelected] = useState<Set<number>>(new Set());
  const [candidates, setCandidates] = useState<PushGroupCandidate[] | null>(null);
  const [search, setSearch] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const submittingRef = useRef(false);

  useEffect(() => {
    if (!isOpen || candidates) {
      return;
    }
    fetchPushGroupCandidates()
    .then(setCandidates)
    .catch((e) => setError(e?.message ?? "성도 목록을 불러오지 못했습니다."));
  }, [isOpen, candidates]);

  // 열 때마다 입력을 비운다
  useEffect(() => {
    if (isOpen) {
      setName("");
      setSelected(new Set());
      setSearch("");
      setError(null);
    }
  }, [isOpen]);

  const visible = useMemo(() => {
    const keyword = search.trim().toLowerCase();
    const all = candidates ?? [];
    return keyword ? all.filter((c) => `${c.name ?? ""} ${c.nickname ?? ""}`.toLowerCase().includes(keyword)) : all;
  }, [candidates, search]);

  const toggle = (uid: number) => {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(uid)) {
        next.delete(uid);
      } else {
        next.add(uid);
      }
      return next;
    });
  };

  const submit = async () => {
    if (submittingRef.current) {
      return;
    }
    if (!name.trim()) {
      setError("그룹 이름을 입력해 주세요.");
      return;
    }
    submittingRef.current = true;
    setIsSubmitting(true);
    setError(null);
    try {
      const group = await fetchCreatePushGroup({
        name: name.trim(),
        description: "",
        memberAccountUids: Array.from(selected),
      });
      onCreated(group);
    } catch (e: any) {
      setError(e?.message ?? "그룹을 만들지 못했습니다.");
    } finally {
      submittingRef.current = false;
      setIsSubmitting(false);
    }
  };

  return (
      <Modal
          isOpen={isOpen}
          onClose={isSubmitting ? () => {} : onClose}
          title="푸시 그룹 만들기"
          footer={
            <>
              {grayButton("취소", isSubmitting ? () => {} : onClose)}
              {blueButton(isSubmitting ? "만드는 중..." : `만들기 (${selected.size}명)`, submit)}
            </>
          }
      >
        <input
            type="text"
            value={name}
            maxLength={30}
            onChange={(e) => setName(e.target.value)}
            placeholder="그룹 이름 (예: 청년부)"
            className={inputClass}
        />
        <input
            type="search"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="이름으로 찾기"
            className={`${inputClass} mt-3`}
        />
        <ul className="mt-3 max-h-64 divide-y divide-gray-100 overflow-y-auto rounded-md border border-gray-200">
          {!candidates ? (
              <li className="px-3 py-6 text-center text-sm text-gray-500">불러오는 중...</li>
          ) : visible.length === 0 ? (
              <li className="px-3 py-6 text-center text-sm text-gray-500">
                {candidates.length === 0 ? "고를 수 있는 성도가 없습니다." : "검색 결과가 없습니다."}
              </li>
          ) : visible.map((c) => (
              <li key={c.uid}>
                <label className="flex cursor-pointer items-center gap-x-3 px-3 py-2 hover:bg-gray-50">
                  <input
                      type="checkbox"
                      checked={selected.has(c.uid)}
                      onChange={() => toggle(c.uid)}
                      className="size-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-600"
                  />
                  <span className="text-sm text-gray-900">{displayName(c)}</span>
                  <span className="text-xs text-gray-500">{ROLE_LABELS[c.role] ?? c.role}</span>
                  {!c.pushEnabled && <span className="ml-auto text-xs text-amber-600">알림 수신 불가</span>}
                </label>
              </li>
          ))}
        </ul>
        {error && <p className="mt-3 text-sm text-red-600">{error}</p>}
      </Modal>
  );
}
