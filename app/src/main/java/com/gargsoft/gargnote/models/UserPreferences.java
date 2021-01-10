package com.gargsoft.gargnote.models;

public class UserPreferences {
    public enum SortDirection {
        Ascending,
        Descending
    }

    public static class CheckList {
        public enum CheckPosition {
            left,
            right
        }

        private static boolean setCompletionOnCheck = true;
        private static boolean confirmDelete = true;
        private static boolean confirmVirtualDelete = true;
        private static boolean addMoreOnEnter = true;
        private static CheckPosition checkPosition = CheckPosition.right;

        public static boolean isSetCompletionOnCheck() {
            return setCompletionOnCheck;
        }

        public static void setSetCompletionOnCheck(boolean setCompletionOnCheck) {
            CheckList.setCompletionOnCheck = setCompletionOnCheck;
        }

        public static boolean isConfirmDelete() {
            return confirmDelete;
        }

        public static void setConfirmDelete(boolean confirmDelete) {
            CheckList.confirmDelete = confirmDelete;
        }

        public static boolean isConfirmVirtualDelete() {
            return confirmVirtualDelete;
        }

        public static void setConfirmVirtualDelete(boolean confirmVirtualDelete) {
            CheckList.confirmVirtualDelete = confirmVirtualDelete;
        }

        public static boolean isAddMoreOnEnter() {
            return addMoreOnEnter;
        }

        public static void setAddMoreOnEnter(boolean addMoreOnEnter) {
            CheckList.addMoreOnEnter = addMoreOnEnter;
        }

        public static CheckPosition getCheckPosition() {
            return checkPosition;
        }

        public static void setCheckPosition(CheckPosition checkPosition) {
            CheckList.checkPosition = checkPosition;
        }
    }

    public static class General {
        public enum DisplayMode {
            Flat,
            Tree
        }

        public enum FolderPosition {
            Top,
            Bottom,
            Mixed
        }

        public enum SortMode {
            Alphabetical,
            TimeStamp,
            Grouped
        }

        private static DisplayMode displayMode = DisplayMode.Tree;
        private static FolderPosition folderPosition = FolderPosition.Top;
        private static SortMode sortMode = SortMode.Alphabetical;
        private static SortDirection sortDirection = SortDirection.Ascending;
        private static boolean showFullPathInFlatMode = true;
        private static boolean confirmDelete = true;

        public static DisplayMode getDisplayMode() {
            return displayMode;
        }

        public static void setDisplayMode(DisplayMode displayMode) {
            General.displayMode = displayMode;
        }

        public static FolderPosition getFolderPosition() {
            return folderPosition;
        }

        public static void setFolderPosition(FolderPosition folderPosition) {
            General.folderPosition = folderPosition;
        }

        public static SortMode getSortMode() {
            return sortMode;
        }

        public static void setSortMode(SortMode sortMode) {
            General.sortMode = sortMode;
        }

        public static SortDirection getSortDirection() {
            return sortDirection;
        }

        public static void setSortDirection(SortDirection sortDirection) {
            General.sortDirection = sortDirection;
        }

        public static boolean isShowFullPathInFlatMode() {
            return showFullPathInFlatMode;
        }

        public static void setShowFullPathInFlatMode(boolean showFullPathInFlatMode) {
            General.showFullPathInFlatMode = showFullPathInFlatMode;
        }

        public static boolean isConfirmDelete() {
            return confirmDelete;
        }

        public static void setConfirmDelete(boolean confirmDelete) {
            General.confirmDelete = confirmDelete;
        }
    }

    private UserPreferences() {
    }
}
