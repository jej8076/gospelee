import React from 'react';

interface FormLayoutProps {
  title: string;
  description: string;
  children: React.ReactNode;
  onCancel: () => void;
  onSubmit: () => void;
  submitText?: string;
  cancelText?: string;
}

export default function FormLayout({ 
  title, 
  description, 
  children, 
  onCancel, 
  onSubmit, 
  submitText = "저장",
  cancelText = "취소"
}: FormLayoutProps) {
  return (
    <div className="max-w-4xl mx-auto">
      <div className="space-y-12 px-8">
        <div className="border-b border-gray-900/10 pb-12">
          <h2 className="text-base/7 font-semibold text-gray-900">{title}</h2>
          <p className="mt-1 text-sm/6 text-gray-600">{description}</p>
          
          <div className="mt-10">
            {children}
          </div>
        </div>
      </div>

      <div className="mt-6 mr-6 flex items-center justify-end gap-x-6">
        <button
          type="button"
          className="text-sm/6 font-semibold text-gray-900 cursor-pointer hover:text-gray-700"
          onClick={onCancel}
        >
          {cancelText}
        </button>
        <button
          type="button"
          className="rounded-md bg-indigo-600 px-3 py-2 text-sm font-semibold text-white shadow-xs hover:bg-indigo-500 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 cursor-pointer"
          onClick={onSubmit}
        >
          {submitText}
        </button>
      </div>
    </div>
  );
}
