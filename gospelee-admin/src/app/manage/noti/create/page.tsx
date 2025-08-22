"use client"

import { useEffect, useState } from "react";
import useAuth from "~/lib/auth/check-auth";
import { getLastLoginOrElseNull } from "@/utils/user-utils";
import { fetchInsertAnnouncement } from "~/lib/api/fetch-announcement";
import { useApiClient } from "@/hooks/useApiClient";
import useDidMountEffect from "@/hooks/useDidMountEffect";
import { isEmpty } from "@/utils/validators";
import { useRouter } from "next/navigation";
import Modal from "@/components/modal/modal";
import { blueButton, grayButton } from "@/components/modal/modal-buttons";
import FormLayout from "@/components/manage/FormLayout";
import FormField from "@/components/manage/FormField";
import FileUpload from "@/components/manage/FileUpload";
import RadioGroup from "@/components/manage/RadioGroup";

export default function CreateNoti() {
  useAuth();

  const router = useRouter();
  const { callApi } = useApiClient();

  // State management
  const [userName, setUserName] = useState("");
  const [subject, setSubject] = useState("");
  const [announcementText, setAnnouncementText] = useState("");
  const [files, setFiles] = useState<File[]>([]);
  const [pushNotificationSendYn, setPushNotificationSendYn] = useState("");
  const [blobFileMapping, setBlobFileMapping] = useState<{ [key: string]: string }>({});
  
  // Modal states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isGoBackModalOpen, setIsGoBackModalOpen] = useState(false);
  
  // API response
  const [announcement, setAnnouncement] = useState<Announcement>();

  // Initialize user data
  useEffect(() => {
    const lastLoginInfo: AuthInfoType | null = getLastLoginOrElseNull();
    setUserName(lastLoginInfo?.name ?? "");
  }, []);

  // Handle API response
  useDidMountEffect(() => {
    if (!isEmpty(announcement?.id)) {
      router.push("/manage/noti");
      return;
    }
    alert("실패");
  }, [announcement]);

  // Event handlers
  const handleSubmit = () => {
    if (pushNotificationSendYn === "") {
      alert("푸시 알림 발송 여부를 선택해주세요");
      return;
    }
    setIsModalOpen(true);
  };

  const handleCancel = () => {
    setIsGoBackModalOpen(true);
  };

  const handleGoBack = () => {
    router.push("/manage/noti");
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFiles = e.target.files;
    if (selectedFiles) {
      const fileArray = Array.from(selectedFiles);
      setFiles((prevFiles) => [...prevFiles, ...fileArray]);
    }
  };

  const handleRemoveFile = (index: number) => {
    const fileToRemove = files[index];
    setFiles((prevFiles) => prevFiles.filter((_, i) => i !== index));

    // Remove related blob URL mapping
    if (fileToRemove) {
      setBlobFileMapping(prev => {
        const newMapping = { ...prev };
        Object.keys(newMapping).forEach(blobUrl => {
          if (newMapping[blobUrl] === fileToRemove.name) {
            delete newMapping[blobUrl];
          }
        });
        return newMapping;
      });
    }
  };

  const insertAnnouncement = async () => {
    const inputData: any = {
      organizationType: "ECCLESIA",
      subject: subject || "공지사항",
      text: announcementText,
      pushNotificationSendYn: pushNotificationSendYn,
    };

    if (files && files.length > 0) {
      inputData.files = files;
    }

    if (Object.keys(blobFileMapping).length > 0) {
      inputData.blobFileMapping = blobFileMapping;
    }

    await callApi(() => fetchInsertAnnouncement(inputData), setAnnouncement);
  };

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

  return (
    <>
      <FormLayout
        title="공지사항 새로만들기"
        description="공지사항을 새로 만드는 공간입니다."
        onCancel={handleCancel}
        onSubmit={handleSubmit}
        submitText="저장"
        cancelText="뒤로가기"
      >
        <div className="grid grid-cols-1 gap-x-6 gap-y-8 sm:grid-cols-6">
          <FormField label="작성자" className="sm:col-span-4">
            <div className="flex items-center rounded-md bg-white pl-3 outline-1 -outline-offset-1 outline-gray-300 focus-within:outline-2 focus-within:-outline-offset-2 focus-within:outline-indigo-600">
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

      {/* Confirmation Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="공지사항을 등록합니다."
        footer={
          <>
            {grayButton("취소", () => setIsModalOpen(false))}
            {blueButton("확인", insertAnnouncement)}
          </>
        }
      >
        <div className="text-sm text-gray-600">
          작성한 공지사항을 등록하시겠습니까?
        </div>
      </Modal>

      {/* Go Back Modal */}
      <Modal
        isOpen={isGoBackModalOpen}
        onClose={() => setIsGoBackModalOpen(false)}
        title="공지사항 작성을 그만합니다."
        footer={
          <>
            {grayButton("취소", () => setIsGoBackModalOpen(false))}
            {blueButton("확인", handleGoBack)}
          </>
        }
      >
        <div className="text-sm text-gray-600">
          작성 중인 내용이 저장되지 않습니다. 정말 나가시겠습니까?
        </div>
      </Modal>
    </>
  );
}
