package net.micode.notes;

import android.app.Activity;
import android.content.Intent;
import android.test.InstrumentationTestCase;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import net.micode.notes.ui.NoteEditActivity;
import java.lang.reflect.Method;

/** Actual device component tests complement, and never replace, natural UI tests. */
public class MaintenanceTests extends InstrumentationTestCase {
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
