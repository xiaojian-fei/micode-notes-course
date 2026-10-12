package net.micode.notes;
import junit.framework.TestCase;
import java.util.Arrays;
import java.util.Collections;
import net.micode.notes.tool.ContentStats;

public class ContentStatsTests extends TestCase {
    public void testUnicodeAndLogicalLines() {
        String[] text={null,"","中A😀","a\n","a\r\nb","a\rb","\n"," \t ","中A😀\nline2\n","👨‍👩‍👧‍👦"};
        int[] chars={0,0,3,2,4,3,1,3,10,7},lines={0,0,1,2,2,2,2,1,3,1};
        for(int i=0;i<text.length;i++) {
            ContentStats.Result result=ContentStats.calculate(text[i]);
            assertEquals("characters case "+i,chars[i],result.characters);
            assertEquals("lines case "+i,lines[i],result.lines);
        }
    }

    public void testChecklistCountsAndPercent() {
        ContentStats.Result empty=ContentStats.calculateChecklist(null,null);
        assertEquals(0,empty.characters);assertEquals(0,empty.lines);assertEquals(0,empty.items);
        assertEquals(0.0,empty.completionPercent(),0.001);
        ContentStats.Result mixed=ContentStats.calculateChecklist(
                Arrays.asList(null,""," \t\u3000","课","Work😀"),Arrays.asList(true,true,true,true,false));
        assertEquals(7,mixed.characters);assertEquals(2,mixed.lines);
        assertEquals(2,mixed.items);assertEquals(1,mixed.completed);assertEquals(50.0,mixed.completionPercent(),0.001);
        ContentStats.Result done=ContentStats.calculateChecklist(Arrays.asList("A","中"),Arrays.asList(true,true));
        assertEquals(100.0,done.completionPercent(),0.001);
        ContentStats.Result zero=ContentStats.calculateChecklist(Arrays.asList("A","中"),null);
        assertEquals(0.0,zero.completionPercent(),0.001);
        try{ContentStats.calculateChecklist(Arrays.asList("A"),Collections.<Boolean>emptyList());fail();}
        catch(IllegalArgumentException expected){}
    }
}
