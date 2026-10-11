package net.micode.notes;

import android.app.Activity;
import android.content.Intent;
import android.test.InstrumentationTestCase;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import net.micode.notes.ui.NoteEditActivity;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Calendar;
import android.view.ContextThemeWrapper;
import android.widget.NumberPicker;
import net.micode.notes.ui.DateTimePicker;

/** Actual device component tests complement, and never replace, natural UI tests. */
public class MaintenanceTests extends InstrumentationTestCase {
    public void testReminderCalendarBoundaries() throws Throwable {
        final Throwable[] error = new Throwable[1];
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() { try {
                DateTimePicker picker = new DateTimePicker(new ContextThemeWrapper(
                        getInstrumentation().getTargetContext(), R.style.NoteTheme),
                        System.currentTimeMillis(), true);
                Field field = DateTimePicker.class.getDeclaredField("mOnHourChangedListener");
                field.setAccessible(true);
                NumberPicker.OnValueChangeListener listener =
                        (NumberPicker.OnValueChangeListener)field.get(picker);
                NumberPicker hour = (NumberPicker)picker.findViewById(R.id.hour);
                int[][] dates={{2026,2,31,2026,3,1},{2024,1,28,2024,1,29},
                    {2024,1,29,2024,2,1},{2026,11,31,2027,0,1}};
                for(int[] v:dates) {
                    picker.setCurrentDate(v[0],v[1],v[2],23,15);
                    hour.setValue(0); listener.onValueChange(hour,23,0);
                    assertEquals(v[3],picker.getCurrentYear());
                    assertEquals(v[4],picker.getCurrentMonth());
                    assertEquals(v[5],picker.getCurrentDay());
                    assertEquals(0,picker.getCurrentHourOfDay());
                    hour.setValue(23);listener.onValueChange(hour,0,23);
                    assertEquals(v[0],picker.getCurrentYear());
                    assertEquals(v[1],picker.getCurrentMonth());
                    assertEquals(v[2],picker.getCurrentDay());
                }
                picker.setCurrentDate(2026,2,31,8,15);
                picker.setCurrentDate(2026,3,1,8,15);
                assertEquals(3,picker.getCurrentMonth());assertEquals(1,picker.getCurrentDay());
                picker.set24HourView(false);
                assertEquals(8,hour.getValue());
                picker.setCurrentDate(2024,1,29,0,15);
                assertEquals(12,hour.getValue());
                picker.setCurrentDate(2024,1,29,12,15);
                assertEquals(12,hour.getValue());assertEquals(12,picker.getCurrentHourOfDay());
                picker.set24HourView(true);assertEquals(12,hour.getValue());
            } catch(Throwable t){error[0]=t;} }
        });
        if(error[0]!=null)throw error[0];
    }
    public void testSearchHighlightEdges() throws Throwable {
        final Activity activity = getInstrumentation().startActivitySync(
                new Intent(Intent.ACTION_INSERT_OR_EDIT)
                .setClass(getInstrumentation().getTargetContext(), NoteEditActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        try {
            final Method method = NoteEditActivity.class.getDeclaredMethod(
                    "getHighlightQueryResult", String.class, String.class);
            method.setAccessible(true);
            final String[][] inputs = {{null, null}, {null, "abc"}, {"", "abc"},
                {"abc", ""}, {"abc", null}, {"abc abc abc", "abc"},
                {"测试测试", "测试"}, {"[ ( \\ a* ^ $", "["},
                {"[ ( \\ a* ^ $", "("}, {"[ ( \\ a* ^ $", "\\"},
                {"[ ( \\ a* ^ $", "a*"}, {"[ ( \\ a* ^ $", "^"},
                {"[ ( \\ a* ^ $", "$"}, {"😀😀", "😀"}};
            final int[] expected = {0,0,0,0,0,3,2,1,1,1,1,1,1,2};
            final Throwable[] error = new Throwable[1];
            getInstrumentation().runOnMainSync(new Runnable() {
                public void run() {
                    try {
                        for (int i=0; i<inputs.length; i++) {
                            Spannable text = (Spannable)method.invoke(activity,
                                    inputs[i][0], inputs[i][1]);
                            assertEquals("case " + i, expected[i], text.getSpans(
                                    0,text.length(),BackgroundColorSpan.class).length);
                        }
                    } catch (Throwable t) { error[0]=t; }
                }
            });
            if (error[0]!=null) throw error[0];
        } finally { getInstrumentation().runOnMainSync(new Runnable() {
            public void run() { activity.finish(); }
        }); }
    }
}
