"use client"

import {useEffect, useRef, useState} from "react";
import FormLayout from "@/components/manage/FormLayout";
import FormField from "@/components/manage/FormField";
import FileUpload from "@/components/manage/FileUpload";
import RadioGroup from "@/components/manage/RadioGroup";
import PushTargetSelect, {toPushPayload, validatePushTarget} from "@/components/manage/PushTargetSelect";
import Modal from "@/components/modal/modal";
import {blueButton, grayButton} from "@/components/modal/modal-buttons";
import {useModalState} from "@/hooks/useModalState";
import {bulletinFileUrl} from "~/lib/api/fetch-bulletin";

const MAX_IMAGE_COUNT = 20;

const openYnOptions = [
  {value: "Y", label: "공개", description: "교인이 앱에서 볼 수 있습니다"},
  {value: "N", label: "비공개", description: "관리자만 볼 수 있습니다"},
];

const isPdf = (name: string) => /\.pdf$/i.test(name);

/** 오늘 이후 가장 가까운 주일(일요일), 오늘이 일요일이면 오늘 */
const upcomingSunday = (): string => {
  const d = new Date();
  d.setDate(d.getDate() + ((7 - d.getDay()) % 7));
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  return `${d.getFullYear()}-${mm}-${dd}`;
};

/** 남는 파일 구성이 서버 규칙(PDF 1개 또는 이미지 최대 20장, 혼합 불가)에 맞는지 확인한다. 맞으면 null */
export const validateBulletinFiles = (names: string[]): string | null => {
  if (names.length === 0) {
    return "주보 파일을 한 개 이상 올려 주세요.";
  }
  const pdfCount = names.filter(isPdf).length;
  const imageCount = names.length - pdfCount;
  if (pdfCount > 0 && imageCount > 0) {
    return "PDF 와 이미지를 함께 올릴 수 없습니다.";
  }
  if (pdfCount > 1) {
    return "PDF 는 한 개만 올릴 수 있습니다.";
  }
  if (imageCount > MAX_IMAGE_COUNT) {
    return `이미지는 최대 ${MAX_IMAGE_COUNT}장까지 올릴 수 있습니다.`;
  }
  return null;
};

type ExistingFile = { url: string; name: string; id: number };

interface BulletinFormProps {
  mode: "create" | "edit";
  initial?: Bulletin;
  /** 저장 요청. 실패하면 예외를 던진다 */
  onSubmit: (input: BulletinInput) => Promise<void>;
  onDelete?: () => Promise<void>;
  onCancel: () => void;
}

export default function BulletinForm({mode, initial, onSubmit, onDelete, onCancel}: BulletinFormProps) {
  const confirmModal = useModalState();
  const [worshipDate, setWorshipDate] = useState(initial?.worshipDate ?? upcomingSunday());
  const [title, setTitle] = useState(initial?.title ?? "");
  const [openYn, setOpenYn] = useState<"Y" | "N">(initial?.openYn ?? "Y");
  const [files, setFiles] = useState<File[]>([]);
  // 등록할 때는 실수로 전체 발송하지 않도록 직접 고르게 하고, 수정할 때는 기본으로 보내지 않는다
  const [pushTarget, setPushTarget] = useState<PushTarget>({mode: mode === "create" ? "" : "NONE", groupIds: []});
  const [existingFiles, setExistingFiles] = useState<ExistingFile[]>([]);
  const [deleteIds, setDeleteIds] = useState<number[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const submittingRef = useRef(false);

  useEffect(() => {
    if (!initial) {
      return;
    }
    setExistingFiles(
        initial.fileList.map((f) => ({
          url: initial.accessToken ? bulletinFileUrl(initial.accessToken, f.fileDetailId) : "",
          name: f.fileOriginalName,
          id: f.fileDetailId,
        }))
    );
  }, [initial]);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selected = e.target.files;
    if (selected) {
      setFiles((prev) => [...prev, ...Array.from(selected)]);
    }
    // 같은 파일을 지웠다가 다시 고를 수 있게 입력값을 비운다
    e.target.value = "";
  };

  const handleRemoveFile = (index: number, isExisting: boolean = false) => {
    if (isExisting) {
      const target = existingFiles[index];
      if (!target) {
        return;
      }
      setDeleteIds((prev) => [...prev, target.id]);
      setExistingFiles((prev) => prev.filter((_, i) => i !== index));
      return;
    }
    setFiles((prev) => prev.filter((_, i) => i !== index));
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
    if (!worshipDate) {
      alert("예배 날짜를 선택해 주세요.");
      return;
    }
    const fileError = validateBulletinFiles([
      ...existingFiles.map((f) => f.name),
      ...files.map((f) => f.name),
    ]);
    if (fileError) {
      alert(fileError);
      return;
    }
    const pushError = validatePushTarget(pushTarget);
    if (pushError) {
      alert(pushError);
      return;
    }
    confirmModal.openModal({
      title: mode === "create" ? "주보를 등록합니다." : "주보를 수정합니다.",
      content: <div className="text-sm text-gray-600">
        {mode === "create" ? "작성한 주보를 등록하시겠습니까?" : "수정한 내용을 저장하시겠습니까?"}
      </div>,
      onConfirm: () => runSafely(() => onSubmit({
        id: initial?.id,
        worshipDate,
        title,
        openYn,
        files,
        deleteFileDetailIdList: deleteIds,
        ...toPushPayload(pushTarget),
      })),
    });
  };

  const handleDelete = () => {
    if (!onDelete) {
      return;
    }
    confirmModal.openModal({
      title: "주보를 삭제합니다.",
      content: <div className="text-sm text-gray-600">삭제한 주보는 되돌릴 수 없습니다. 삭제하시겠습니까?</div>,
      onConfirm: () => runSafely(onDelete),
    });
  };

  return (
      <>
        <FormLayout
            title={mode === "create" ? "주보 새로만들기" : "주보 수정하기"}
            description="PDF 한 개 또는 이미지(jpg, png, webp) 최대 20장을 올릴 수 있습니다."
            onCancel={onCancel}
            onSubmit={isSubmitting ? () => {} : handleSubmit}
            submitText={isSubmitting ? "처리 중..." : mode === "create" ? "저장" : "수정"}
            cancelText="뒤로가기"
        >
          <div className="grid grid-cols-1 gap-x-6 gap-y-8 sm:grid-cols-6">
            <FormField label="예배 날짜" required className="sm:col-span-3">
              <input
                  type="date"
                  value={worshipDate}
                  onChange={(e) => setWorshipDate(e.target.value)}
                  className="block w-full rounded-md bg-white px-3 py-1.5 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600 sm:text-sm/6 border border-gray-300"
              />
            </FormField>

            <FormField label="제목" className="sm:col-span-6">
              <input
                  type="text"
                  value={title}
                  maxLength={100}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="예) 11월 첫째 주 주보"
                  className="block w-full rounded-md bg-white px-3 py-1.5 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600 sm:text-sm/6 border border-gray-300"
              />
            </FormField>

            <FormField label="주보 파일" required>
              <FileUpload
                  files={files}
                  existingFiles={existingFiles}
                  onFileChange={handleFileChange}
                  onRemoveFile={handleRemoveFile}
                  multiple={true}
                  accept="application/pdf,image/jpeg,image/png,image/webp"
              />
            </FormField>

            <div className="col-span-full border-t border-gray-900/10 pt-8">
              <RadioGroup
                  title="공개 여부"
                  description="공개하면 교인이 앱에서 볼 수 있습니다"
                  options={openYnOptions}
                  value={openYn}
                  onChange={(v: string) => setOpenYn(v === "N" ? "N" : "Y")}
                  name="bulletin-open-yn"
              />
            </div>

            <div className="col-span-full border-t border-gray-900/10 pt-8">
              <PushTargetSelect value={pushTarget} onChange={setPushTarget} name="bulletin-push"/>
              {mode === "edit" && (
                  <p className="mt-3 text-sm text-gray-500">수정할 때 발송을 선택하면 알림이 다시 나갑니다.</p>
              )}
            </div>

            {mode === "edit" && onDelete && (
                <div className="col-span-full border-t border-gray-900/10 pt-8">
                  <button
                      type="button"
                      onClick={handleDelete}
                      disabled={isSubmitting}
                      className="rounded-md border border-red-300 bg-white px-3 py-2 text-sm font-semibold text-red-600 shadow-xs hover:bg-red-50 cursor-pointer disabled:opacity-50"
                  >
                    이 주보 삭제
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
