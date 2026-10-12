package net.micode.notes;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.test.InstrumentationTestCase;
import net.micode.notes.data.NotesDatabaseHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/** Upgrade a real pre-maintenance database copy without replacing the live database. */
public class MigrationTests extends InstrumentationTestCase {
    private List<String> values(SQLiteDatabase db, String table, int columns) {
        Cursor c=db.rawQuery("SELECT * FROM " + table + " ORDER BY _id",null);
        List<String> result=new ArrayList<String>();
        try { while(c.moveToNext()) for(int i=0;i<columns;i++)
            result.add(c.getType(i)+":"+c.getString(i));
        } finally {c.close();}
        return result;
    }

    public void testRealVersion4CopyPreservesEveryField() throws Exception {
        Context target=getInstrumentation().getTargetContext();
        final File directory=new File(target.getFilesDir(),"migration-test-"+System.nanoTime());
        assertTrue(directory.mkdirs());
        File database=new File(directory,"note.db");
        FileInputStream in=new FileInputStream("/sdcard/Download/course-migration-v4.db");
        FileOutputStream out=new FileOutputStream(database);
        try {byte[] buffer=new byte[8192];int count;
            while((count=in.read(buffer))!=-1)out.write(buffer,0,count);
        } finally {in.close();out.close();}
        SQLiteDatabase before=SQLiteDatabase.openDatabase(database.getPath(),null,SQLiteDatabase.OPEN_READONLY);
        assertEquals(4,before.getVersion());
        List<String> notes,data;
        // Obtain actual original column counts, rather than assume the schema.
        Cursor noteColumns=before.rawQuery("SELECT * FROM note LIMIT 0",null);
        int noteCount=noteColumns.getColumnCount();noteColumns.close();
        Cursor dataColumns=before.rawQuery("SELECT * FROM data LIMIT 0",null);
        int dataCount=dataColumns.getColumnCount();dataColumns.close();
        notes=values(before,"note",noteCount);data=values(before,"data",dataCount);
        before.close();
        Context isolated=new ContextWrapper(target) {
            @Override public File getDatabasePath(String name){return new File(directory,name);}
            @Override public SQLiteDatabase openOrCreateDatabase(String name,int mode,SQLiteDatabase.CursorFactory factory){
                return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name),factory);}
            @Override public SQLiteDatabase openOrCreateDatabase(String name,int mode,SQLiteDatabase.CursorFactory factory,DatabaseErrorHandler handler){
                return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name).getPath(),factory,handler);}
        };
        Constructor<NotesDatabaseHelper> constructor=NotesDatabaseHelper.class.getDeclaredConstructor(Context.class);
        constructor.setAccessible(true);
        SQLiteOpenHelper helper=constructor.newInstance(isolated);
        try {
            SQLiteDatabase after=helper.getWritableDatabase();
            assertEquals(5,after.getVersion());
            assertEquals(notes,values(after,"note",noteCount));
            assertEquals(data,values(after,"data",dataCount));
            Cursor pins=after.rawQuery("SELECT count(*) FROM note WHERE pinned<>0",null);
            try {assertTrue(pins.moveToFirst());assertEquals(0,pins.getInt(0));} finally {pins.close();}
        } finally {helper.close();}
    }
}
