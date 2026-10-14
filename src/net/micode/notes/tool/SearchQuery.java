package net.micode.notes.tool;

/** Literal queries shared by suggestions, submitted search and highlighting. */
public final class SearchQuery {
    private SearchQuery() { }
    public static String toLikeArgument(String query) {
        String text = query == null ? "" : query;
        return "%" + text.replace("\\", "\\\\").replace("%", "\\%")
                .replace("_", "\\_") + "%";
    }
    /** Fold only ASCII letters, preserving UTF-16 positions and Unicode case. */
    public static String foldAscii(String text) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] >= 'A' && chars[i] <= 'Z') chars[i] = (char)(chars[i] + ('a' - 'A'));
        }
        return new String(chars);
    }
}
