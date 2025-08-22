"use client"

import {useEffect, useState} from "react";
import useAuth from "~/lib/auth/check-auth";
import {getLastLoginOrElseNull} from "@/utils/user-utils";
import {fetchUpdateAnnouncement, fetchAnnouncementById} from "~/lib/api/fetch-announcement";
import {useApiClient} from "@/hooks/useApiClient";
import useDidMountEffect from "@/hooks/useDidMountEffect";
import {isEmpty} from "@/utils/validators";
import {useRouter, useParams} from "next/navigation";
import Modal from "@/components/modal/modal";
import {blueButton, grayButton} from "@/components/modal/modal-buttons";
import FormLayout from "@/components/manage/FormLayout";
import FormField from "@/components/manage/FormField";
import FileUpload from "@/components/manage/FileUpload";
import RadioGroup from "@/components/manage/RadioGroup";
import {useModalState} from "@/hooks/useModalState";

export default function EditNoti() {
  useAuth();

  const params = useParams();
  const router = useRouter();
  const {callApi} = useApiClient();
  const announcementId = params.id as string;
  const confirmModal = useModalState();

  // State management
  const [userName, setUserName] = useState("");
  const [announcementText, setAnnouncementText] = useState("");
  const [files, setFiles] = useState<File[]>([]);
  const [existingFiles, setExistingFiles] = useState<FileResource[]>([]);
  const [pushNotificationSendYn, setPushNotificationSendYn] = useState("");
  const [blobFileMapping, setBlobFileMapping] = useState<{ [key: string]: string }>({});
  const [deleteFileDetailIdList, setDeleteFileDetailIdList] = useState<number[]>([]);

  const [isLoading, setIsLoading] = useState(true);

  // API responses
  const [announcement, setAnnouncement] = useState<Announcement>();
  const [originalAnnouncement, setOriginalAnnouncement] = useState<Announcement>();

  const TYPE = "ECCLESIA";

  // Initialize user data
  useEffect(() => {
    const lastLoginInfo: AuthInfoType | null = getLastLoginOrElseNull();
    setUserName(lastLoginInfo?.name ?? "");
  }, []);

  // Load announcement data
  useEffect(() => {
    if (announcementId) {
      loadAnnouncementData();
    }
  }, [announcementId]);

  // Update form when original data is loaded
  useEffect(() => {
    if (originalAnnouncement) {
      setAnnouncementText(originalAnnouncement.text || "");
      setPushNotificationSendYn(originalAnnouncement.pushNotificationSendYn || "N");

      // 기존 파일 리소스 설정
      if (originalAnnouncement.fileResources) {
        setExistingFiles(originalAnnouncement.fileResources);
      }
    }
  }, [originalAnnouncement]);

  // Handle API response
  useDidMountEffect(() => {
    if (!isEmpty(announcement?.id)) {
      // 메모리 정리
      cleanupBlobUrls();
      router.push("/manage/noti");
      return;
    }
    if (announcement === null) {
      alert("수정 실패");
    }
  }, [announcement]);

  // Load announcement data
  const loadAnnouncementData = async () => {
    try {
      setIsLoading(true);
      await callApi(
          () => fetchAnnouncementById(TYPE, announcementId),
          (data: Announcement) => {
            setOriginalAnnouncement(data);
          }
      );
    } catch (error) {
      console.error("공지사항 로드 실패:", error);
      alert("공지사항을 불러오는데 실패했습니다.");
      router.push("/manage/noti");
    } finally {
      setIsLoading(false);
    }
  };

  // Event handlers
  const handleSubmit = () => {
    confirmModal.openModal({
      content: '수정하신 공지사항을 저장합니다',
      onConfirm: () => {
        updateAnnouncement();
      }
    });

  };

  const handleCancel = () => {
    cleanupBlobUrls();
    router.push("/manage/noti");
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFiles = e.target.files;
    if (selectedFiles) {
      const fileArray = Array.from(selectedFiles);
      setFiles((prevFiles) => [...prevFiles, ...fileArray]);
    }
  };

  const handleRemoveFile = (index: number, isExisting: boolean = false) => {
    if (isExisting) {

      confirmModal.openModal({
        content: <div>기존 파일을 삭제합니다</div>,
        onConfirm: () => {

          // 기존 파일 제거
          const fileToRemove = existingFiles[index];
          if (fileToRemove) {
            // 메모리 정리 (API에서 받은 데이터로 생성한 blob URL)
            URL.revokeObjectURL(fileToRemove.url);

            // 삭제할 파일 ID 목록에 추가 (서버에 삭제 요청용)
            setDeleteFileDetailIdList(prev => [...prev, fileToRemove.id]);

            console.log(`기존 파일 제거 - fileDetailId: ${fileToRemove.id}, 파일명: ${fileToRemove.name}`);
          }
          setExistingFiles(prevFiles => prevFiles.filter((_, i) => i !== index));

        }
      });

    } else {

      // 새로 추가한 파일 제거
      const fileToRemove = files[index];
      setFiles(prevFiles => prevFiles.filter((_, i) => i !== index));

      if (!fileToRemove) {
        return;
      }

      // 관련 blob URL 매핑 제거
      setBlobFileMapping(prev => {
        const newMapping = {...prev};
        Object.keys(newMapping).forEach(blobUrl => {
          if (newMapping[blobUrl] === fileToRemove.name) {
            delete newMapping[blobUrl];
          }
        });
        return newMapping;
      });

      console.log(`새 파일 제거 - 파일명: ${fileToRemove.name}`);
    }
  };

  const updateAnnouncement = async () => {
    const inputData: any = {
      id: announcementId,
      organizationType: TYPE,
      subject: originalAnnouncement?.subject || "",
      text: announcementText,
      pushNotificationSendYn: pushNotificationSendYn,
    };

    if (files && files.length > 0) {
      inputData.files = files;
    }

    if (Object.keys(blobFileMapping).length > 0) {
      inputData.blobFileMapping = blobFileMapping;
    }

    // 삭제할 파일 ID 목록 추가
    if (deleteFileDetailIdList.length > 0) {
      inputData.deleteFileDetailIdList = deleteFileDetailIdList;
      console.log(`삭제할 파일 ID 목록:`, deleteFileDetailIdList);
    }

    console.log('수정 요청 데이터:', inputData);

    await callApi(() => fetchUpdateAnnouncement(inputData), setAnnouncement);
  };

  // Cleanup blob URLs
  const cleanupBlobUrls = () => {
    existingFiles.forEach(file => {
      URL.revokeObjectURL(file.url);
    });
  };

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      cleanupBlobUrls();
    };
  }, []);

  const pushNotificationOptions = [
    {
      value: "Y",
      label: "등록 교인 전체발송",
      description: "모든 등록된 교인에게 푸시 알림을 발송합니다"
    },
    {
      value: "N",
      label: "발송안함",
      description: "푸시 알림을 발송하지 않습니다"
    }
  ];

  if (isLoading) {
    return (
        <div className="flex justify-center items-center h-64">
          <div className="text-lg text-gray-500">로딩 중...</div>
        </div>
    );
  }

  return (
      <>
        <FormLayout
            title="공지사항 수정하기"
            description="공지사항을 수정하는 공간입니다."
            onCancel={handleCancel}
            onSubmit={handleSubmit}
            submitText="수정"
            cancelText="취소"
        >
          <div className="grid grid-cols-1 gap-x-6 gap-y-8 sm:grid-cols-6">
            <FormField label="작성자" className="sm:col-span-4">
              <div
                  className="flex items-center rounded-md bg-white pl-3 outline-1 -outline-offset-1 outline-gray-300 focus-within:outline-2 focus-within:-outline-offset-2 focus-within:outline-indigo-600">
                <div className="shrink-0 text-base text-gray-500 select-none sm:text-sm/6">
                  {userName}
                </div>
                <input
                    type="text"
                    className="block min-w-0 grow py-1.5 pr-3 pl-1 text-base text-gray-900 placeholder:text-gray-400 focus:outline-none sm:text-sm/6"
                    readOnly
                />
              </div>
            </FormField>

            <FormField label="첨부파일">
              <FileUpload
                  files={files}
                  existingFiles={existingFiles}
                  onFileChange={handleFileChange}
                  onRemoveFile={handleRemoveFile}
                  multiple={true}
                  accept="image/*"
              />
            </FormField>

            <FormField label="본문" required>
            <textarea
                value={announcementText}
                onChange={(e) => setAnnouncementText(e.target.value)}
                rows={6}
                className="block w-full rounded-md bg-white px-3 py-1.5 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600 sm:text-sm/6 border border-gray-300"
                placeholder="공지사항 내용을 입력해주세요..."
            />
            </FormField>

            <div className="col-span-full border-t border-gray-900/10 pt-8">
              <RadioGroup
                  title="푸시 알림"
                  description="앱 푸시 알림을 발송할 지 선택해 주세요"
                  options={pushNotificationOptions}
                  value={pushNotificationSendYn}
                  onChange={setPushNotificationSendYn}
                  name="push-notifications"
              />
            </div>
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
