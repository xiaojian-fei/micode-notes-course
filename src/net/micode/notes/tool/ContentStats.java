package net.micode.notes.tool;

import java.util.Collections;
import java.util.List;

/** Read-only content calculations. Characters are Unicode code points, not UTF-16 units. */
public final class ContentStats {
    private ContentStats() { }

    public static final class Result {
        public final int characters,lines,items,completed;
        private Result(int characters,int lines,int items,int completed) {
            this.characters=characters;this.lines=lines;this.items=items;this.completed=completed;
        }
        public double completionPercent() {return items==0 ? 0.0 : 100.0*completed/items;}
    }

    /** Empty text has zero logical lines; a terminal newline adds an empty line. */
    public static Result calculate(String content) {
        String text=content==null ? "" : content;
        int lines=text.isEmpty()?0:1;
        for(int i=0;i<text.length();i++) {
            char c=text.charAt(i);
            if(c=='\r') {
                lines++;
                if(i+1<text.length() && text.charAt(i+1)=='\n')i++;
            } else if(c=='\n')lines++;
        }
        return new Result(text.codePointCount(0,text.length()),lines,0,0);
    }

    /** Count visible nonblank checklist items; checkbox serialization markers are excluded. */
    public static Result calculateChecklist(List<String> contents,List<Boolean> checked) {
        if(contents==null)contents=Collections.emptyList();
        if(checked==null)checked=Collections.nCopies(contents.size(),Boolean.FALSE);
        if(contents.size()!=checked.size())throw new IllegalArgumentException("Checklist state size mismatch");
        StringBuilder body=new StringBuilder();int total=0,done=0;
        for(int i=0;i<contents.size();i++) {
            String text=contents.get(i);
            if(!hasContent(text))continue;
            if(total>0)body.append('\n');body.append(text);total++;
            if(Boolean.TRUE.equals(checked.get(i)))done++;
        }
        Result plain=calculate(body.toString());
        return new Result(plain.characters,plain.lines,total,done);
    }

    private static boolean hasContent(String text) {
        if(text==null)return false;
        for(int i=0;i<text.length();) {
            int point=text.codePointAt(i);
            if(!Character.isWhitespace(point) && !Character.isSpaceChar(point))return true;
            i+=Character.charCount(point);
        }
        return false;
    }
}
