package net.micode.notes;
import junit.framework.TestCase;
import java.util.TimeZone;
import net.micode.notes.tool.NoteFilter;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;

public class FilterTests extends TestCase {
    public void testActualSQLiteInclusiveStartExclusiveEnd() {
        TimeZone previous=TimeZone.getDefault();TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        SQLiteDatabase db=SQLiteDatabase.create(null);
        try {
            db.execSQL("CREATE TABLE note (_id INTEGER,type INTEGER,parent_id INTEGER,bg_color_id INTEGER,modified_date INTEGER)");
            long start=NoteFilter.parseDay("2024-02-29",false),end=NoteFilter.parseDay("2024-02-29",true);
            long[] times={start-1,start,end-1,end};
            for(int i=0;i<times.length;i++)db.execSQL("INSERT INTO note VALUES(?,0,29,1,?)",new Object[]{i+1,times[i]});
            NoteFilter filter=new NoteFilter(29L,1,start,end);
            Cursor c=db.query("note",new String[]{"_id"},filter.getSelection(),filter.getSelectionArgs(),null,null,"_id");
            try {assertEquals(2,c.getCount());assertTrue(c.moveToFirst());assertEquals(2,c.getInt(0));
                assertTrue(c.moveToNext());assertEquals(3,c.getInt(0));} finally {c.close();}
            NoteFilter otherColor=new NoteFilter(29L,4,start,end);
            c=db.query("note",new String[]{"_id"},otherColor.getSelection(),otherColor.getSelectionArgs(),null,null,null);
            try {assertEquals(0,c.getCount());} finally {c.close();}
        } finally {db.close();TimeZone.setDefault(previous);}
    }
    public void testParameterBindingAndDateBoundaries() {
        TimeZone previous=TimeZone.getDefault();TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        try {
            long start=NoteFilter.parseDay("2024-02-29",false),end=NoteFilter.parseDay("2024-02-29",true);
            assertEquals(86400000L,end-start);
            NoteFilter filter=new NoteFilter(29L,1,start,end);
            assertEquals(6,filter.getSelectionArgs().length);
            assertEquals("29",filter.getSelectionArgs()[2]);assertEquals("1",filter.getSelectionArgs()[3]);
            assertFalse(filter.getSelection().contains("29"));assertTrue(filter.getSelection().contains("modified_date<?"));
            assertNull(NoteFilter.parseDay("",false));
            assertEquals("2024-02-29",NoteFilter.formatDay(end,true));
            String[] bad={"2023-02-29","2026-13-01","2026-02-30","2026-1-01","2026-01-01abc","1' OR 1=1"};
            for(String value:bad){try{NoteFilter.parseDay(value,false);fail(value);}catch(IllegalArgumentException expected){}}
            try{new NoteFilter(null,null,end,start);fail();}catch(IllegalArgumentException expected){}
            assertEquals(2,new NoteFilter(null,null,null,null).getSelectionArgs().length);
        } finally {TimeZone.setDefault(previous);}
        // A local day spanning DST is bounded by calendar dates, not a fixed 24h interval.
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
        try {assertEquals(23*60*60*1000L,NoteFilter.parseDay("2024-03-10",true)-NoteFilter.parseDay("2024-03-10",false));}
        finally {TimeZone.setDefault(previous);}
    }
}
