package net.micode.notes;
import junit.framework.TestCase;
import net.micode.notes.tool.FolderNamePolicy;

public class FolderNameTests extends TestCase {
    public void testUnicodeWhitespacePolicy() {
        String[] invalid={null,""," ","   ","\u3000","\t","\n\r","\u00a0"," \t\u3000 "};
        for(String value:invalid) assertFalse(FolderNamePolicy.isValid(value));
        assertEquals("课程",FolderNamePolicy.normalize("  课程\u3000"));
        assertEquals("Work",FolderNamePolicy.normalize("\tWork "));
        assertEquals("My Work",FolderNamePolicy.normalize("  My Work  "));
        assertTrue(FolderNamePolicy.isValid("😀"));
    }
}
