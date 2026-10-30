"use client"

import {useEffect, useMemo, useRef, useState} from "react";
import FormLayout from "@/components/manage/FormLayout";
import FormField from "@/components/manage/FormField";
import Modal from "@/components/modal/modal";
import {blueButton, grayButton} from "@/components/modal/modal-buttons";
import {useModalState} from "@/hooks/useModalState";
import {fetchPushGroupCandidates} from "~/lib/api/fetch-push-group";

const ROLE_LABELS: Record<string, string> = {
  LAYMAN: '성도',
  TEACHER: '교사',
  COORDINATOR: '간사',
  MINISTER: '전도사',
  LICENSED_MINISTER: '강도사',
  PASTOR: '목사',
  SENIOR_PASTOR: '담임목사',
  ADMIN: '관리자',
};

const inputClass = "block w-full rounded-md bg-white px-3 py-1.5 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600 sm:text-sm/6 border border-gray-300";

const displayName = (c: PushGroupCandidate) => c.name?.trim() || c.nickname?.trim() || `성도 ${c.uid}`;

interface PushGroupFormProps {
  mode: "create" | "edit";
  initial?: PushGroup;
  /** 저장 요청. 실패하면 예외를 던진다 */
  onSubmit: (input: PushGroupInput) => Promise<void>;
  onDelete?: () => Promise<void>;
  onCancel: () => void;
}

export default function PushGroupForm({mode, initial, onSubmit, onDelete, onCancel}: PushGroupFormProps) {
  const confirmModal = useModalState();
  const [name, setName] = useState(initial?.name ?? "");
  const [description, setDescription] = useState(initial?.description ?? "");
  const [selected, setSelected] = useState<Set<number>>(new Set(initial?.memberAccountUids ?? []));
  const [candidates, setCandidates] = useState<PushGroupCandidate[]>([]);
  const [search, setSearch] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const submittingRef = useRef(false);

  useEffect(() => {
    fetchPushGroupCandidates()
    .then(setCandidates)
    .catch((error) => alert(error?.message ?? "성도 목록을 불러오지 못했습니다."));
  }, []);

  const visible = useMemo(() => {
    const keyword = search.trim().toLowerCase();
    if (!keyword) {
      return candidates;
    }
    return candidates.filter((c) =>
        `${c.name ?? ""} ${c.nickname ?? ""}`.toLowerCase().includes(keyword));
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

  // 검색으로 걸러진 목록 기준 전체 선택/해제
  const allVisibleSelected = visible.length > 0 && visible.every((c) => selected.has(c.uid));
  const toggleVisible = () => {
    setSelected((prev) => {
      const next = new Set(prev);
      visible.forEach((c) => allVisibleSelected ? next.delete(c.uid) : next.add(c.uid));
      return next;
    });
  };

  const runSafely = async (action: () => Promise<void>) => {
    if (submittingRef.current) {
      return;
    }
    submittingRef.current = true;
    setIsSubmitting(true);
    try {
      await action();
    } catch (error: any) {
      alert(error?.message ?? "요청에 실패했습니다.");
    } finally {
      submittingRef.current = false;
      setIsSubmitting(false);
    }
  };

  const handleSubmit = () => {
    if (!name.trim()) {
      alert("그룹 이름을 입력해 주세요.");
      return;
    }
    confirmModal.openModal({
      title: mode === "create" ? "푸시 그룹을 만듭니다." : "푸시 그룹을 수정합니다.",
      content: <div className="text-sm text-gray-600">
        {`"${name.trim()}" 그룹 (${selected.size}명)을 저장하시겠습니까?`}
      </div>,
      onConfirm: () => runSafely(() => onSubmit({
        id: initial?.id,
        name: name.trim(),
        description: description.trim(),
        memberAccountUids: Array.from(selected),
      })),
    });
  };

  const handleDelete = () => {
    if (!onDelete) {
      return;
    }
    confirmModal.openModal({
      title: "푸시 그룹을 삭제합니다.",
      content: <div className="text-sm text-gray-600">그룹만 삭제되고 성도는 그대로입니다. 삭제하시겠습니까?</div>,
      onConfirm: () => runSafely(onDelete),
    });
  };

  return (
      <>
        <FormLayout
            title={mode === "create" ? "푸시 그룹 새로만들기" : "푸시 그룹 수정하기"}
            description="성도를 직접 골라 그룹으로 묶어 두면, 공지·주보를 등록할 때 이 그룹에게만 알림을 보낼 수 있습니다."
            onCancel={onCancel}
            onSubmit={isSubmitting ? () => {} : handleSubmit}
            submitText={isSubmitting ? "처리 중..." : "저장"}
            cancelText="뒤로가기"
        >
          <div className="grid grid-cols-1 gap-x-6 gap-y-8 sm:grid-cols-6">
            <FormField label="그룹 이름" required className="sm:col-span-4">
              <input
                  type="text"
                  value={name}
                  maxLength={30}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="예) 청년부, 새가족, 찬양팀"
                  className={inputClass}
              />
            </FormField>

            <FormField label="설명">
              <input
                  type="text"
                  value={description}
                  maxLength={100}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="관리자가 알아볼 수 있는 간단한 설명 (선택)"
                  className={inputClass}
              />
            </FormField>

            <FormField label={`멤버 (${selected.size}명 선택)`}>
              <div className="rounded-lg border border-gray-200 bg-gray-50 p-3">
                <div className="flex items-center gap-x-3">
                  <input
                      type="search"
                      value={search}
                      onChange={(e) => setSearch(e.target.value)}
                      placeholder="이름으로 찾기"
                      className={inputClass}
                  />
                  <button
                      type="button"
                      onClick={toggleVisible}
                      disabled={visible.length === 0}
                      className="shrink-0 rounded-md bg-white px-3 py-1.5 text-sm font-semibold text-gray-900 border border-gray-300 hover:bg-gray-50 cursor-pointer disabled:opacity-50"
                  >
                    {allVisibleSelected ? "보이는 항목 해제" : "보이는 항목 모두 선택"}
                  </button>
                </div>
                <ul className="mt-3 max-h-80 divide-y divide-gray-100 overflow-y-auto rounded-md bg-white">
                  {visible.length === 0 ? (
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
                          {!c.pushEnabled && (
                              <span className="ml-auto text-xs text-amber-600" title="앱에서 알림을 허용하지 않았거나 로그인한 적이 없어 알림을 받을 수 없습니다">
                                알림 수신 불가
                              </span>
                          )}
                        </label>
                      </li>
                  ))}
                </ul>
              </div>
            </FormField>

            {mode === "edit" && onDelete && (
                <div className="col-span-full border-t border-gray-900/10 pt-8">
                  <button
                      type="button"
                      onClick={handleDelete}
                      disabled={isSubmitting}
                      className="rounded-md border border-red-300 bg-white px-3 py-2 text-sm font-semibold text-red-600 shadow-xs hover:bg-red-50 cursor-pointer disabled:opacity-50"
                  >
                    이 그룹 삭제
                  </button>
                </div>
            )}
          </div>
        </FormLayout>

        <Modal
            isOpen={confirmModal.isOpen}
            onClose={confirmModal.closeModal}
            title={confirmModal.config?.title}
            footer={
              <>
                {grayButton("취소", confirmModal.handleCancel)}
                {blueButton("확인", confirmModal.handleConfirm)}
              </>
            }
        >
          {confirmModal.config?.content}
        </Modal>
      </>
  );
}
