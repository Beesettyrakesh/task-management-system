import { Tag } from "@/types";
import { isLightColor } from "@/utils/tagUtils";
import React from "react";

interface TagBadgeProps {
  tag: Tag;
  removable?: boolean;
  onRemove?: (tagId: number) => void;
  size?: "sm" | "md" | "lg";
  className?: string;
}

const TagBadge: React.FC<TagBadgeProps> = ({
  tag,
  removable,
  onRemove,
  size,
}) => {
  const sizeClasses = {
    sm: "px-2 py-1 text-xs",
    md: "px-3 py-1 text-sm",
    lg: "px-4 py-2 text-base",
  };

  const textColor = isLightColor(tag.color) ? "text-black" : "text-white";
  const sizeClass = sizeClasses[size || "md"];

  const handleRemove = () => {
    onRemove?.(tag.id);
  };

  return (
    <span
      className={`inline-flex items-center rounded-full font-medium ${sizeClass} ${textColor}`}
      style={{ backgroundColor: tag.color }}
    >
      {tag.name}
      {removable && (
        <button
          type="button"
          onClick={handleRemove}
          className="ml-1 hover:bg-black/20 rounded-full p-1 text-xs"
        >
          x
        </button>
      )}
    </span>
  );
};

export default TagBadge;
