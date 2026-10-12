package net.micode.notes.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import net.micode.notes.R;
import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.NoteColumns;
import net.micode.notes.tool.NoteFilter;
import net.micode.notes.tool.FolderNamePolicy;

/** Collects criteria without writing application data. */
public class NoteFilterDialog extends AlertDialog {
    public interface OnApplyListener { void onApply(NoteFilter filter); }
    private final Spinner mFolder,mColor;
    private final EditText mStart,mEnd;
    private final List<Long> mFolderIds=new ArrayList<Long>();
    private final OnApplyListener mListener;

    public NoteFilterDialog(Context context,NoteFilter current,OnApplyListener listener) {
        super(context);mListener=listener;setTitle(R.string.filter_notes);
        LinearLayout fields=new LinearLayout(context);fields.setOrientation(LinearLayout.VERTICAL);
        int padding=(int)(16*context.getResources().getDisplayMetrics().density);fields.setPadding(padding,padding,padding,padding);
        List<String> folders=new ArrayList<String>();folders.add(context.getString(R.string.filter_all_folders));mFolderIds.add(null);
        folders.add(context.getString(R.string.filter_root_folder));mFolderIds.add((long)Notes.ID_ROOT_FOLDER);
        Cursor c=context.getContentResolver().query(Notes.CONTENT_NOTE_URI,
                new String[]{NoteColumns.ID,NoteColumns.SNIPPET},NoteColumns.TYPE+"=? AND "+NoteColumns.PARENT_ID+"<>?",
                new String[]{String.valueOf(Notes.TYPE_FOLDER),String.valueOf(Notes.ID_TRASH_FOLER)},NoteColumns.SNIPPET+" ASC");
        if(c!=null)try {while(c.moveToNext()){
            long id=c.getLong(0);String name=c.getString(1);
            folders.add(FolderNamePolicy.isValid(name)?name:"[unnamed #"+id+"]");mFolderIds.add(id);
        }}finally {c.close();}
        mFolder=spinner(context,fields,R.string.filter_folder,R.id.filter_folder,folders.toArray(new String[folders.size()]));
        mColor=spinner(context,fields,R.string.filter_color,R.id.filter_color,context.getResources().getStringArray(R.array.filter_colors));
        mStart=dateField(context,fields,R.string.filter_start_date,R.id.filter_start);
        mEnd=dateField(context,fields,R.string.filter_end_date,R.id.filter_end);
        if(current!=null){
            mFolder.setSelection(Math.max(0,mFolderIds.indexOf(current.folderId)));
            mColor.setSelection(current.colorId==null?0:current.colorId+1);
            mStart.setText(NoteFilter.formatDay(current.startInclusive,false));
            mEnd.setText(NoteFilter.formatDay(current.endExclusive,true));
        }
        ScrollView scroll=new ScrollView(context);scroll.addView(fields);setView(scroll);
        setButton(BUTTON_POSITIVE,context.getString(R.string.filter_apply),(DialogInterface.OnClickListener)null);
        setButton(BUTTON_NEGATIVE,context.getString(android.R.string.cancel),(DialogInterface.OnClickListener)null);
        setButton(BUTTON_NEUTRAL,context.getString(R.string.filter_clear),new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int which){mListener.onApply(null);}
        });
    }

    private Spinner spinner(Context context,LinearLayout fields,int label,int id,String[] items) {
        TextView title=new TextView(context);title.setText(label);fields.addView(title);
        Spinner result=new Spinner(context);result.setId(id);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(context,android.R.layout.simple_spinner_item,items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);result.setAdapter(adapter);
        fields.addView(result);return result;
    }

    private EditText dateField(Context context,LinearLayout fields,int label,int id) {
        TextView title=new TextView(context);title.setText(label);fields.addView(title);
        EditText result=new EditText(context);result.setId(id);result.setSingleLine(true);result.setHint("YYYY-MM-DD");
        result.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        fields.addView(result);return result;
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getButton(BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                try {
                    Long start=NoteFilter.parseDay(mStart.getText().toString(),false);
                    Long end=NoteFilter.parseDay(mEnd.getText().toString(),true);
                    int selected=mColor.getSelectedItemPosition();
                    NoteFilter filter=new NoteFilter(mFolderIds.get(mFolder.getSelectedItemPosition()),
                            selected==0?null:selected-1,start,end);
                    mListener.onApply(filter);dismiss();
                } catch(IllegalArgumentException ex){mEnd.setError(ex.getMessage());}
            }
        });
    }
}
