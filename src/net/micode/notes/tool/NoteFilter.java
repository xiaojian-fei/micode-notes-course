package net.micode.notes.tool;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.NoteColumns;

/** Immutable optional conditions; every value is bound as a selection argument. */
public final class NoteFilter implements Serializable {
    private static final long serialVersionUID=1L;
    public final Long folderId, startInclusive, endExclusive;
    public final Integer colorId;

    public NoteFilter(Long folder,Integer color,Long start,Long end) {
        if(color!=null && (color<0 || color>4)) throw new IllegalArgumentException("Invalid color");
        if(start!=null && end!=null && start>=end) throw new IllegalArgumentException("Start date exceeds end date");
        folderId=folder;colorId=color;startInclusive=start;endExclusive=end;
    }

    public String getSelection() {
        String sql=NoteColumns.TYPE+"=? AND "+NoteColumns.PARENT_ID+"<>?";
        if(folderId!=null)sql+=" AND "+NoteColumns.PARENT_ID+"=?";
        if(colorId!=null)sql+=" AND "+NoteColumns.BG_COLOR_ID+"=?";
        if(startInclusive!=null)sql+=" AND "+NoteColumns.MODIFIED_DATE+">=?";
        if(endExclusive!=null)sql+=" AND "+NoteColumns.MODIFIED_DATE+"<?";
        return sql;
    }

    public String[] getSelectionArgs() {
        List<String> args=new ArrayList<String>();
        args.add(String.valueOf(Notes.TYPE_NOTE));args.add(String.valueOf(Notes.ID_TRASH_FOLER));
        if(folderId!=null)args.add(folderId.toString());
        if(colorId!=null)args.add(colorId.toString());
        if(startInclusive!=null)args.add(startInclusive.toString());
        if(endExclusive!=null)args.add(endExclusive.toString());
        return args.toArray(new String[args.size()]);
    }

    /** Dates use device timezone. End date is inclusive, represented by next midnight. */
    public static Long parseDay(String input,boolean endOfRange) {
        String text=input==null ? "" : input.trim();
        if(text.isEmpty())return null;
        if(!text.matches("\\d{4}-\\d{2}-\\d{2}"))throw new IllegalArgumentException("Use YYYY-MM-DD");
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd",Locale.US);format.setLenient(false);
        try {
            Calendar date=Calendar.getInstance();date.setTime(format.parse(text));
            if(endOfRange)date.add(Calendar.DAY_OF_MONTH,1);
            return date.getTimeInMillis();
        } catch(ParseException ex){throw new IllegalArgumentException("Invalid calendar date",ex);}
    }

    public static String formatDay(Long value,boolean endOfRange) {
        if(value==null)return "";
        return new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date(value-(endOfRange?1:0)));
    }
}
