import { Tag } from "@/types";
import { isLightColor } from "@/utils/tagUtils";
import React from "react";
import TagBadge from "./TagBadge";

interface TagSelectorProps {
  selectedTags: Tag[];
  availableTags: Tag[];
  onTagsChange: (tags: Tag[]) => void;
  placeholder?: string;
  disabled?: boolean;
}

const TagSelector: React.FC<TagSelectorProps> = ({
  selectedTags,
  availableTags,
  onTagsChange,
}) => {
  const isSelected = (tag: Tag): boolean => {
    return selectedTags.some((selectedTag) => selectedTag.id === tag.id);
  };
  const handleToggle = (tag: Tag) => {
    if (isSelected(tag)) {
      const newSelected = selectedTags.filter((t) => t.id !== tag.id);
      onTagsChange(newSelected);
    } else {
      onTagsChange([...selectedTags, tag]);
    }
  };

  return (
    <div className="tag-selector">
      <h4 className="text-sm font-medium mb-2">Select tags:</h4>
      {selectedTags.length > 0 && (
        <div className="mb-3">
          <span className="text-xs text-gray-500">Selected:</span>
          {selectedTags.map((tag) => (
            <TagBadge
              key={tag.id}
              tag={tag}
              size="sm"
              removable
              onRemove={() => handleToggle(tag)}
            />
          ))}
        </div>
      )}
      <div className="flex flex-wrap gap-2">
        {availableTags.map((tag) => (
          <button
            key={tag.id}
            type="button"
            onClick={() => handleToggle(tag)}
            className={`
                px-3 py-1 rounded-full text-sm font-medium transition-all duration-200 ${
                  isSelected(tag)
                    ? "ring-2 ring-blue-500 ring-offset-1 opacity-100"
                    : "hover:opacity-80 opacity-70"
                }
            `}
            style={{
              backgroundColor: tag.color,
              color: isLightColor(tag.color) ? "#000" : "#fff",
            }}
          >
            {tag.name}
            {isSelected(tag) && "✓"}
          </button>
        ))}
      </div>

      {availableTags.length === 0 && (
        <p className="text-gray-500 text-sm">
          No tags available. Create some tags first!
        </p>
      )}
    </div>
  );
};

export default TagSelector;
