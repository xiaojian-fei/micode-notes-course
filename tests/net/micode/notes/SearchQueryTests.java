package net.micode.notes;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import net.micode.notes.tool.SearchQuery;

public class SearchQueryTests extends AndroidTestCase {
    public void testLiteralBoundSqlAndAsciiCase() {
        SQLiteDatabase db = SQLiteDatabase.create(null);
        try {
            db.execSQL("CREATE TABLE sample (_id INTEGER, body TEXT)");
            String[] bodies = {"ALPHA alpha", "cost 100%", "a_b", "C:\\work", "%_\\", "æ", "Æ", "中文"};
            for (int i = 0; i < bodies.length; i++) db.execSQL("INSERT INTO sample VALUES (?,?)", new Object[]{i+1,bodies[i]});
            String[] queries = {"alpha", "ALPHA", "%", "_", "\\", "%_\\", "æ", "Æ", "中文", "' OR 1=1 --"};
            // This Android build's SQLite also matches æ/Æ; preserve its native query behavior.
            String[] expected = {"1,", "1,", "2,5,", "3,5,", "4,5,", "5,", "6,7,", "6,7,", "8,", ""};
            for (int i = 0; i < queries.length; i++) {
                Cursor c = db.rawQuery("SELECT _id FROM sample WHERE body LIKE ? ESCAPE '\\' ORDER BY _id",new String[]{SearchQuery.toLikeArgument(queries[i])});
                StringBuilder ids = new StringBuilder();
                try {while(c.moveToNext())ids.append(c.getInt(0)).append(',');} finally {c.close();}
                assertEquals(queries[i],expected[i],ids.toString());
            }
        } finally {db.close();}
    }
    public void testAsciiFoldPreservesUnicodeAndPositions() {
        assertEquals("alpha æÆ 中文 😀",SearchQuery.foldAscii("ALPHA æÆ 中文 😀"));
        assertEquals("ALPHA æÆ 中文 😀".length(),SearchQuery.foldAscii("ALPHA æÆ 中文 😀").length());
        assertEquals("",SearchQuery.foldAscii(""));
    }
}
