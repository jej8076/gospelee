"use client"

import {useEffect, useState} from "react";
import useAuth from "~/lib/auth/check-auth";
import {getLastLoginOrElseNull} from "@/utils/user-utils";
import {fetchInsertAnnouncement} from "~/lib/api/fetch-announcement";
import {useApiClient} from "@/hooks/useApiClient";
import useDidMountEffect from "@/hooks/useDidMountEffect";
import {isEmpty} from "@/utils/validators";
import {useRouter} from "next/navigation";
import Modal from "@/components/modal/modal";
import {blueButton, grayButton} from "@/components/modal/modal-buttons";
import MarkdownEditorField from '@/components/markdown/MarkdownEditorField';
import FormLayout from "@/components/manage/FormLayout";
import FormField from "@/components/manage/FormField";
import FileUpload from "@/components/manage/FileUpload";

interface ImageSize {
  width: number;
  height: number;
}

export default function CreateStory() {
  useAuth();

  const router = useRouter();
  const {callApi} = useApiClient();

  // State management
  const [userName, setUserName] = useState("");
  const [subject, setSubject] = useState("");
  const [announcementText, setAnnouncementText] = useState("");
  const [files, setFiles] = useState<File[]>([]);
  const [blobFileMapping, setBlobFileMapping] = useState<{ [key: string]: string }>({});

  // Modal states
  const [isModalOpen, setIsModalOpen] = useState(false);

  // API response
  const [announcement, setAnnouncement] = useState<Announcement>();

  const LINE = "  \n";

  // Initialize user data
  useEffect(() => {
    const lastLoginInfo: AuthInfoType | null = getLastLoginOrElseNull();
    setUserName(lastLoginInfo?.name ?? "");
  }, []);

  // Handle API response
  useDidMountEffect(() => {
    if (!isEmpty(announcement?.id)) {
      router.push("/manage/story");
      return;
    }
    alert("실패");
  }, [announcement]);

  // Event handlers
  const handleSubmit = () => {
    setIsModalOpen(true);
  };

  const handleCancel = () => {
    router.push("/manage/story");
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
        const newMapping = {...prev};
        Object.keys(newMapping).forEach(blobUrl => {
          if (newMapping[blobUrl] === fileToRemove.name) {
            delete newMapping[blobUrl];
          }
        });
        return newMapping;
      });
    }
  };

  const handleApplyImageToContent = (index: number, file: File) => {
    const blobUrl = URL.createObjectURL(file);
    const previewText = "preview-" + index;

    // Store blob URL and filename mapping
    setBlobFileMapping(prev => ({
      ...prev,
      [blobUrl]: file.name
    }));

    getImageDimensionsFromBlobUrl(blobUrl)
    .then(imageSize => {
      const markdownImage = `<img src="${blobUrl}" width="${imageSize.width}" height="${imageSize.height}" alt="${previewText}">`;
      setAnnouncementText(prevContent => prevContent + LINE + markdownImage);
    })
    .catch(error => console.error(error));
  };

  const getImageDimensionsFromBlobUrl = (blobUrl: string): Promise<ImageSize> => {
    return new Promise((resolve, reject) => {
      const img = new Image();

      img.onload = () => {
        const imageSize: ImageSize = {width: img.width, height: img.height};
        resolve(imageSize);
      };

      img.onerror = () => {
        URL.revokeObjectURL(blobUrl);
        reject(new Error("Blob URL에서 이미지를 로드하는 중 오류가 발생했습니다."));
      };

      img.src = blobUrl;
    });
  };

  const insertAnnouncement = async () => {
    const inputData: any = {
      organizationType: "BRAND_STORY",
      subject: subject || "브랜드 스토리 제목",
      text: announcementText,
      pushNotificationSendYn: "N",
    };

    if (files && files.length > 0) {
      inputData.files = files;
    }

    if (Object.keys(blobFileMapping).length > 0) {
      inputData.blobFileMapping = blobFileMapping;
    }

    await callApi(() => fetchInsertAnnouncement(inputData), setAnnouncement);
  };

  return (
      <>
        <FormLayout
            title="브랜드 스토리 새로만들기"
            description="브랜드 스토리를 새로 만드는 공간입니다."
            onCancel={handleCancel}
            onSubmit={handleSubmit}
            submitText="저장"
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
                  onFileChange={handleFileChange}
                  onRemoveFile={handleRemoveFile}
                  onApplyToContent={handleApplyImageToContent}
                  showApplyButton={true}
                  multiple={true}
                  accept="image/*"
              />
            </FormField>

            <FormField label="제목" required>
              <div
                  className="flex rounded-md bg-white shadow-sm ring-1 ring-inset ring-gray-300 focus-within:ring-2 focus-within:ring-inset focus-within:ring-indigo-600">
                <input
                    type="text"
                    value={subject}
                    onChange={(e) => setSubject(e.target.value)}
                    className="block flex-1 border-0 bg-transparent py-1.5 pl-3 pr-3 text-gray-900 placeholder:text-gray-400 focus:ring-0 sm:text-sm/6"
                    placeholder="브랜드 스토리 제목을 입력하세요"
                />
              </div>
            </FormField>

            <FormField label="본문" required>
              <MarkdownEditorField
                  value={announcementText}
                  onMarkdownChange={setAnnouncementText}
              />
            </FormField>
          </div>
        </FormLayout>

        {/* Confirmation Modal */}
        <Modal
            isOpen={isModalOpen}
            onClose={() => setIsModalOpen(false)}
            title="브랜드 스토리를 등록합니다."
            footer={
              <>
                {grayButton("취소", () => setIsModalOpen(false))}
                {blueButton("확인", insertAnnouncement)}
              </>
            }
        >
          <div className="text-sm text-gray-600">
            작성한 브랜드 스토리를 등록하시겠습니까?
          </div>
        </Modal>
      </>
  );
}
