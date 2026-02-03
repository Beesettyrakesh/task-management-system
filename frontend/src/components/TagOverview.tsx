import React, { useEffect, useState } from "react";
import { showErrorToast, showSuccessToast } from "../config/toastConfig";
import API from "../services/api";
import { Tag } from "../types";
import { ConfirmationModal } from "./ConfirmationModal";

interface TagOverviewProps {
  onTagsChange?: () => void;
  onTagClick?: (tagName: string) => void;
  activeTag?: string | null;
}

const TagOverview: React.FC<TagOverviewProps> = ({
  onTagsChange,
  onTagClick,
  activeTag,
}) => {
  const [tags, setTags] = useState<Tag[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAllModal, setShowAllModal] = useState(false);

  useEffect(() => {
    fetchTags();
  }, []);

  const fetchTags = async () => {
    try {
      const response = await API.get("/tags");
      const sortedTags = response.data
        .sort(
          (a: Tag, b: Tag) =>
            new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
        )
        .slice(0, 8);
      setTags(sortedTags);
    } catch (error) {
      console.error("Failed to fetch tags:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleTagsChange = () => {
    fetchTags();
    onTagsChange?.();
  };

  return (
    <>
      <div className="bg-white rounded-lg shadow-md p-6">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-lg font-semibold text-gray-900">Tags</h3>
          <button
            onClick={() => setShowAllModal(true)}
            className="text-blue-600 hover:text-blue-800 text-sm font-medium transition-colors"
          >
            View All
          </button>
        </div>

        {loading ? (
          <div className="space-y-2">
            {[1, 2, 3].map((i) => (
              <div
                key={i}
                className="h-6 bg-gray-200 rounded animate-pulse"
              ></div>
            ))}
          </div>
        ) : tags.length > 0 ? (
          <div className="flex flex-wrap gap-2">
            {tags.map((tag) => (
              <button
                key={tag.id}
                onClick={() => onTagClick?.(tag.name)}
                className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium text-white transition-all hover:scale-105 hover:shadow-md ${
                  activeTag === tag.name
                    ? "ring-2 ring-offset-2 ring-blue-500"
                    : ""
                }`}
                style={{ backgroundColor: tag.color }}
                title={`Filter by ${tag.name}`}
              >
                {tag.name}
                {activeTag === tag.name && (
                  <svg
                    className="ml-1 w-3 h-3"
                    fill="currentColor"
                    viewBox="0 0 20 20"
                  >
                    <path
                      fillRule="evenodd"
                      d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                      clipRule="evenodd"
                    />
                  </svg>
                )}
              </button>
            ))}
          </div>
        ) : (
          <div className="text-center text-gray-500 py-4">
            <div className="text-2xl mb-2">🏷️</div>
            <p className="text-sm">No tags created yet</p>
          </div>
        )}
      </div>

      {showAllModal && (
        <TagManagementModal
          isOpen={showAllModal}
          onClose={() => setShowAllModal(false)}
          onTagsChange={handleTagsChange}
        />
      )}
    </>
  );
};

interface TagManagementModalProps {
  isOpen: boolean;
  onClose: () => void;
  onTagsChange: () => void;
}

const TagManagementModal: React.FC<TagManagementModalProps> = ({
  isOpen,
  onClose,
  onTagsChange,
}) => {
  const [tags, setTags] = useState<Tag[]>([]);
  const [loading, setLoading] = useState(true);
  const [newTagName, setNewTagName] = useState("");
  const [newTagColor, setNewTagColor] = useState("#3B82F6");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [deleteConfirmation, setDeleteConfirmation] = useState<{
    isOpen: boolean;
    tag: Tag | null;
  }>({
    isOpen: false,
    tag: null,
  });
  const [isDeleting, setIsDeleting] = useState(false);

  useEffect(() => {
    if (isOpen) {
      fetchTags();
    }
  }, [isOpen]);

  const fetchTags = async () => {
    try {
      const response = await API.get("/tags");
      setTags(
        response.data.sort((a: Tag, b: Tag) => a.name.localeCompare(b.name)),
      );
    } catch (error) {
      showErrorToast("Failed to fetch tags");
    } finally {
      setLoading(false);
    }
  };

  const handleCreateTag = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTagName.trim()) return;

    setIsSubmitting(true);
    try {
      await API.post("/tags", {
        name: newTagName.trim(),
        color: newTagColor,
      });

      setNewTagName("");
      setNewTagColor("#3B82F6");
      fetchTags();
      onTagsChange();
      showSuccessToast("Tag created successfully");
    } catch (error: any) {
      showErrorToast(error.response?.data?.message || "Failed to create tag");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteTag = async () => {
    if (!deleteConfirmation.tag) return;

    setIsDeleting(true);
    try {
      await API.delete(`/tags/${deleteConfirmation.tag.id}`);
      fetchTags();
      onTagsChange();
      showSuccessToast("Tag deleted successfully");
      setDeleteConfirmation({ isOpen: false, tag: null });
    } catch (error: any) {
      showErrorToast(error.response?.data?.message || "Failed to delete tag");
    } finally {
      setIsDeleting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-hidden flex flex-col">
        <div className="flex justify-between items-center p-6 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-900">Manage Tags</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <svg
              className="w-6 h-6"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>

        <div className="p-6 overflow-y-auto flex-1 min-h-0">
          {loading ? (
            <div className="space-y-3">
              {[1, 2, 3, 4].map((i) => (
                <div
                  key={i}
                  className="flex items-center justify-between p-3 border rounded-lg animate-pulse"
                >
                  <div className="flex items-center space-x-3">
                    <div className="w-4 h-4 bg-gray-200 rounded-full"></div>
                    <div className="h-4 bg-gray-200 rounded w-24"></div>
                  </div>
                  <div className="w-16 h-8 bg-gray-200 rounded"></div>
                </div>
              ))}
            </div>
          ) : (
            <div className="space-y-3">
              {tags.map((tag) => (
                <div
                  key={tag.id}
                  className="flex items-center justify-between p-3 border border-gray-200 rounded-lg hover:bg-gray-50"
                >
                  <div className="flex items-center space-x-3">
                    <div
                      className="w-4 h-4 rounded-full"
                      style={{ backgroundColor: tag.color }}
                    ></div>
                    <span className="font-medium text-gray-900">
                      {tag.name}
                    </span>
                  </div>
                  <button
                    onClick={() => setDeleteConfirmation({ isOpen: true, tag })}
                    className="text-red-600 hover:text-red-800 text-sm font-medium transition-colors"
                  >
                    Delete
                  </button>
                </div>
              ))}

              {tags.length === 0 && (
                <div className="text-center text-gray-500 py-8">
                  <div className="text-4xl mb-2">🏷️</div>
                  <p>No tags created yet</p>
                </div>
              )}
            </div>
          )}
        </div>

        <div className="border-t border-gray-200 p-6">
          <form onSubmit={handleCreateTag} className="space-y-4">
            <div className="flex space-x-3">
              <div className="flex-1">
                <input
                  type="text"
                  placeholder="Tag name"
                  value={newTagName}
                  onChange={(e) => setNewTagName(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  maxLength={50}
                />
              </div>
              <div className="flex items-center space-x-2">
                <input
                  type="color"
                  value={newTagColor}
                  onChange={(e) => setNewTagColor(e.target.value)}
                  className="w-10 h-10 border border-gray-300 rounded cursor-pointer"
                />
                <button
                  type="submit"
                  disabled={!newTagName.trim() || isSubmitting}
                  className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
                >
                  {isSubmitting ? "Creating..." : "Create"}
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>

      <ConfirmationModal
        isOpen={deleteConfirmation.isOpen}
        onClose={() => setDeleteConfirmation({ isOpen: false, tag: null })}
        onConfirm={handleDeleteTag}
        title="Delete Tag"
        message={`Are you sure you want to delete "${deleteConfirmation.tag?.name}"? This will remove it from all tasks.`}
        confirmText="Delete Tag"
        variant="danger"
        isLoading={isDeleting}
      />
    </div>
  );
};

export default TagOverview;
