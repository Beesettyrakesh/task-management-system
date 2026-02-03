import API from "@/services/api";
import { Tag, TagFormData } from "@/types";
import React, { useEffect, useState } from "react";
import toast from "react-hot-toast";
import TagBadge from "./TagBadge";

interface TagManagerProps {
  onTagsChange?: () => void;
}

const TagManager: React.FC<TagManagerProps> = ({ onTagsChange }) => {
  const COLOR_OPTIONS = [
    "#EF4444",
    "#F97316",
    "#EAB308",
    "#22C55E",
    "#3B82F6",
    "#8B5CF6",
    "#EC4899",
    "#6B7280",
  ];

  const [tags, setTags] = useState<Tag[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingTag, setEditingTag] = useState<Tag | null>(null);
  const [formData, setFormData] = useState<TagFormData>({
    name: "",
    color: COLOR_OPTIONS[0],
  });

  const fetchTags = async () => {
    try {
      setIsLoading(true);
      const response = await API.get("/tags");
      setTags(response.data);
    } catch (error) {
      console.error("Failed to fetch tags:", error);
      toast.error("Failed to fetch tags, Try again later");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchTags();
  }, []);

  const handleCreateTag = async () => {
    if (!formData.name.trim()) return;

    try {
      setIsLoading(true);
      await API.post("/tags", formData);
      await fetchTags();
      setFormData({ name: "", color: COLOR_OPTIONS[0] });
      setShowCreateForm(false);
      onTagsChange?.();
    } catch (error) {
      console.error("Failed to create tag:", error);
      toast.error(`Failed to create tag:${error}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handleUpdateTag = async () => {
    if (!editingTag || !formData.name.trim()) return;

    try {
      setIsLoading(true);
      await API.put(`/tags/${editingTag.id}`, formData);
      await fetchTags();
      setEditingTag(null);
      setFormData({ name: "", color: COLOR_OPTIONS[0] });
      onTagsChange?.();
    } catch (error) {
      console.error(`Failed to update tag with id:${editingTag.id}`, error);
      toast.error(`Failed to update tag with id:${editingTag.id}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteTag = async (id: number, name: string) => {
    if (!window.confirm("Are you sure you want to delete this tag?")) return;

    try {
      setIsLoading(true);
      await API.delete(`/tags/${id}`);
      await fetchTags();
      onTagsChange?.();
    } catch (error) {
      console.error(`Failed to delete '${name}' tag`, error);
      toast.error(`Failed to delete '${name}' tag`);
    } finally {
      setIsLoading(false);
    }
  };

  const startEditing = (tag: Tag) => {
    setEditingTag(tag);
    setFormData({ name: tag.name, color: tag.color });
    setShowCreateForm(true);
  };

  const cancelForm = () => {
    setShowCreateForm(false);
    setEditingTag(null);
    setFormData({ name: "", color: COLOR_OPTIONS[0] });
  };

  const ColorPicker = () => (
    <div className="mb-4">
      <label className="block text-sm font-medium mb-2">Color:</label>
      <div className="flex gap-2 flex-wrap">
        {COLOR_OPTIONS.map((color) => (
          <button
            key={color}
            onClick={() => setFormData({ ...formData, color })}
            className={`w-8 h-8 rounded-full border-2 transition-all ${
              formData.color === color
                ? "border-gray-800 scale-110"
                : "border-gray-300 hover:border-gray-500"
            }`}
            style={{ backgroundColor: color }}
            title={`Select ${color}`}
          />
        ))}
      </div>
    </div>
  );

  return (
    <div className="tag-manager p-4">
      <div className="flex justify-between items-center mb-4">
        <button
          onClick={() => setShowCreateForm(!showCreateForm)}
          className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
          disabled={isLoading}
        >
          {showCreateForm ? "Cancel" : "+ Create New Tag"}
        </button>
      </div>

      {showCreateForm && (
        <div className="mb-6 p-4 border border-gray-200 rounded bg-gray-50">
          <h3 className="font-medium mb-3">
            {editingTag ? "Edit Tag" : "Create New Tag"}
          </h3>

          <div className="mb-4">
            <label className="block text-sm font-medium mb-2">Name:</label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) =>
                setFormData({ ...formData, name: e.target.value })
              }
              className="w-full p-2 border border-gray-300 rounded"
              placeholder="Enter tag name..."
              disabled={isLoading}
            />
          </div>

          <ColorPicker />

          <div className="flex gap-2">
            <button
              onClick={editingTag ? handleUpdateTag : handleCreateTag}
              className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600"
              disabled={isLoading || !formData.name.trim()}
            >
              {isLoading
                ? "Saving..."
                : editingTag
                  ? "Update Tag"
                  : "Create Tag"}
            </button>
            <button
              onClick={cancelForm}
              className="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600"
              disabled={isLoading}
            >
              Cancel
            </button>
          </div>
        </div>
      )}

      <div>
        <h3 className="font-medium mb-3">Your Tags ({tags.length}):</h3>
        {isLoading && <p>Loading...</p>}
        {!isLoading && tags.length === 0 && (
          <p className="text-gray-500">No tags yet. Create your first tag!</p>
        )}
        {!isLoading && tags.length > 0 && (
          <div className="grid gap-2">
            {tags.map((tag) => (
              <div
                key={tag.id}
                className="flex items-center justify-between p-2 border rounded"
              >
                <TagBadge tag={tag} />
                <div className="flex gap-2">
                  <button
                    onClick={() => startEditing(tag)}
                    className="text-blue-500 hover:text-blue-700 px-2 py-1"
                    disabled={isLoading}
                  >
                    Edit
                  </button>
                  <button
                    onClick={() => handleDeleteTag(tag.id, tag.name)}
                    className="text-red-500 hover:text-red-700 px-2 py-1"
                    disabled={isLoading}
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default TagManager;
