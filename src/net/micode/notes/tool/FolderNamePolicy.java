package net.micode.notes.tool;

/** A shared Unicode whitespace policy for folder creation and renaming. */
public final class FolderNamePolicy {
    private FolderNamePolicy() { }

    public static String normalize(String name) {
        if (name == null) return "";
        int start=0, end=name.length();
        while (start<end && isBlank(name.codePointAt(start)))
            start+=Character.charCount(name.codePointAt(start));
        while (end>start && isBlank(name.codePointBefore(end)))
            end-=Character.charCount(name.codePointBefore(end));
        return name.substring(start,end);
    }

    public static boolean isValid(String name) { return !normalize(name).isEmpty(); }

    private static boolean isBlank(int codePoint) {
        return Character.isWhitespace(codePoint) || Character.isSpaceChar(codePoint);
    }
}
