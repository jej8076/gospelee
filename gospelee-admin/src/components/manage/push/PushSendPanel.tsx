"use client"

import {useEffect, useRef, useState} from "react";
import FormField from "@/components/manage/FormField";
import {XMarkIcon} from "@heroicons/react/24/outline";
import PushTargetSelect from "@/components/manage/PushTargetSelect";
import Modal from "@/components/modal/modal";
import {blueButton, grayButton} from "@/components/modal/modal-buttons";
import {useModalState} from "@/hooks/useModalState";
import {fetchPushGroups} from "~/lib/api/fetch-push-group";
import {fetchSendPushDirect} from "~/lib/api/fetch-push-direct";

const TITLE_MAX = 50;
const MESSAGE_MAX = 200;

const inputClass = "block w-full rounded-md bg-white px-3 py-1.5 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600 sm:text-sm/6 border border-gray-300";

interface PushSendPanelProps {
  open: boolean;
  onClose: () => void;
  /** 알림을 보낸 뒤 호출한다. 호출한 쪽에서 발송 현황을 새로고침한다 */
  onSent: () => void;
}

/** 공지·주보 등록 없이 알림만 보내는 오른쪽 패널. 닫아도 작성 중인 내용은 남는다 */
export default function PushSendPanel({open, onClose, onSent}: PushSendPanelProps) {
  const confirmModal = useModalState();
  const resultModal = useModalState();
  const [title, setTitle] = useState("");
  const [message, setMessage] = useState("");
  // 실수로 전체에게 보내지 않도록 대상을 직접 고르게 한다
  const [target, setTarget] = useState<PushTarget>({mode: "", groupIds: []});
  const [groups, setGroups] = useState<PushGroup[]>([]);
  const [isSending, setIsSending] = useState(false);
  const sendingRef = useRef(false);

  useEffect(() => {
    fetchPushGroups().then(setGroups).catch(() => setGroups([]));
  }, []);

  const targetLabel = () => {
    if (target.mode === "ALL") {
      return "교회 성도 전체";
    }
    const names = groups.filter((g) => target.groupIds.includes(g.id)).map((g) => g.name);
    return `선택한 그룹(${names.join(", ")})`;
  };

  const send = async () => {
    if (sendingRef.current) {
      return;
    }
    sendingRef.current = true;
    setIsSending(true);
    try {
      const result = await fetchSendPushDirect({
        title: title.trim(),
        message: message.trim(),
        groupIds: target.mode === "GROUPS" ? target.groupIds : [],
      });
      setTitle("");
      setMessage("");
      setTarget({mode: "", groupIds: []});
      onSent();
      onClose();
      resultModal.openModal({
        title: "푸시 알림을 보냈습니다.",
        content: (
            <div className="space-y-1 text-sm text-gray-600">
              <p>받을 성도 {result.targetCount}명 중 <b>{result.sentCount}명</b>에게 전달했습니다.</p>
              {result.noTokenCount > 0 && (
                  <p>{result.noTokenCount}명은 앱에서 알림을 허용하지 않았거나 로그인한 적이 없어 보내지 못했습니다.</p>
              )}
              {result.failedCount > 0 && (
                  <p>{result.failedCount}명은 전달에 실패했습니다. (만료된 알림 정보 등)</p>
              )}
            </div>
        ),
      });
    } catch (error: any) {
      // 응답만 늦은 것이면 서버는 계속 보내고 있을 수 있어 다시 보내기 전에 확인하도록 알린다
      if (error?.name === "TimeoutError") {
        alert("응답이 늦어지고 있습니다. 알림은 이미 보내졌을 수 있으니, 다시 보내기 전에 성도에게 도착했는지 먼저 확인해 주세요.");
      } else {
        alert(error?.message ?? "푸시 알림을 보내지 못했습니다.");
      }
    } finally {
      sendingRef.current = false;
      setIsSending(false);
    }
  };

  const handleSubmit = () => {
    if (!title.trim()) {
      alert("알림 제목을 입력해 주세요.");
      return;
    }
    if (!message.trim()) {
      alert("알림 내용을 입력해 주세요.");
      return;
    }
    if (target.mode === "") {
      alert("알림을 받을 대상을 선택해 주세요.");
      return;
    }
    // 그룹을 고르지 않으면 서버가 전체 발송으로 처리하므로, 의도치 않은 전체 발송을 막는다
    if (target.mode === "GROUPS" && target.groupIds.length === 0) {
      alert("알림을 보낼 그룹을 한 개 이상 선택해 주세요.");
      return;
    }
    confirmModal.openModal({
      title: "푸시 알림을 보냅니다.",
      content: (
          <div className="space-y-2 text-sm text-gray-600">
            <p><b>{targetLabel()}</b>에게 아래 알림을 보냅니다. 보낸 알림은 취소할 수 없습니다.</p>
            <div className="rounded-md bg-gray-50 p-3">
              <p className="font-semibold text-gray-900">{title.trim()}</p>
              <p className="mt-1 whitespace-pre-wrap">{message.trim()}</p>
            </div>
          </div>
      ),
      onConfirm: send,
    });
  };

  const close = () => {
    // 보내는 중에 닫으면 결과를 볼 수 없으므로 막는다
    if (!sendingRef.current) {
      onClose();
    }
  };

  useEffect(() => {
    if (!open) {
      return;
    }
    const onKey = (e: KeyboardEvent) => {
      // 확인 창이 떠 있을 때의 ESC 는 확인 창이 처리한다
      if (e.key === "Escape" && !confirmModal.isOpen) {
        close();
      }
    };
    document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, [open, confirmModal.isOpen]);

  return (
      <>
        <div
            className={`fixed inset-0 z-40 bg-black/30 transition-opacity ${open ? "opacity-100" : "pointer-events-none opacity-0"}`}
            onClick={close}
            aria-hidden="true"
        />
        <aside
            role="dialog"
            aria-modal="true"
            aria-label="새 알림 보내기"
            aria-hidden={!open}
            className={`fixed inset-y-0 right-0 z-40 flex w-full max-w-md flex-col bg-white shadow-xl transition-transform duration-300 ${open ? "translate-x-0" : "translate-x-full invisible"}`}
        >
          <div className="flex items-start justify-between border-b border-gray-200 px-6 py-4">
            <div>
              <h2 className="text-base/7 font-semibold text-gray-900">새 알림 보내기</h2>
              <p className="mt-1 text-sm/6 text-gray-600">공지나 주보를 등록하지 않고, 교회 성도에게 알림만 바로 보냅니다.</p>
            </div>
            <button type="button" onClick={close} aria-label="닫기"
                    className="cursor-pointer rounded-md p-1 text-gray-400 hover:text-gray-600">
              <XMarkIcon className="size-6"/>
            </button>
          </div>

          <div className="flex-1 overflow-y-auto px-6 py-6">
            <div className="grid grid-cols-1 gap-y-8">
              <FormField label="알림 제목" required>
                <input
                    type="text"
                    value={title}
                    maxLength={TITLE_MAX}
                    onChange={(e) => setTitle(e.target.value)}
                    placeholder="예) 이번 주 합심기도회 안내"
                    className={inputClass}
                />
                <p className="mt-1 text-right text-xs text-gray-500">{title.length}/{TITLE_MAX}</p>
              </FormField>

              <FormField label="알림 내용" required>
                <textarea
                    value={message}
                    maxLength={MESSAGE_MAX}
                    rows={4}
                    onChange={(e) => setMessage(e.target.value)}
                    placeholder="성도에게 전달할 내용을 적어 주세요"
                    className={inputClass}
                />
                <p className="mt-1 text-right text-xs text-gray-500">{message.length}/{MESSAGE_MAX}</p>
              </FormField>

              <div className="border-t border-gray-900/10 pt-8">
                <PushTargetSelect
                    value={target}
                    onChange={setTarget}
                    name="push-send-target"
                    title="받는 대상"
                    description="알림을 누구에게 보낼지 선택해 주세요"
                    allowNone={false}
                />
              </div>
            </div>
          </div>

          <div className="flex items-center justify-end gap-x-4 border-t border-gray-200 px-6 py-4">
            <button type="button" onClick={close}
                    className="cursor-pointer text-sm/6 font-semibold text-gray-900 hover:text-gray-700">
              닫기
            </button>
            <button
                type="button"
                onClick={isSending ? undefined : handleSubmit}
                disabled={isSending}
                className="cursor-pointer rounded-md bg-indigo-600 px-3 py-2 text-sm font-semibold text-white shadow-xs hover:bg-indigo-500 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isSending ? "보내는 중..." : "알림 보내기"}
            </button>
          </div>
        </aside>

        <Modal
            isOpen={confirmModal.isOpen}
            onClose={confirmModal.closeModal}
            title={confirmModal.config?.title}
            footer={
              <>
                {grayButton("취소", confirmModal.handleCancel)}
                {blueButton("보내기", confirmModal.handleConfirm)}
              </>
            }
        >
          {confirmModal.config?.content}
        </Modal>

        <Modal
            isOpen={resultModal.isOpen}
            onClose={resultModal.closeModal}
            title={resultModal.config?.title}
            footer={blueButton("확인", resultModal.closeModal)}
        >
          {resultModal.config?.content}
        </Modal>
      </>
  );
}
