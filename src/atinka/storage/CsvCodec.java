package atinka.storage;

final class CsvCodec {
    static final char SEP = '|';

    static String join(String[] parts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(SEP);
            String s = parts[i] == null ? "" : parts[i];
            // escape separators and newlines
            for (int j = 0; j < s.length(); j++) {
                char c = s.charAt(j);
                if (c == '\n' || c == '\r') continue;
                if (c == SEP || c == '\\') { sb.append('\\'); }
                sb.append(c);
            }
        }
        return sb.toString();
    }

    static String[] split(String line, int expectedCols) {
        if (line == null) return new String[expectedCols];
        String[] out = new String[expectedCols];
        int idx = 0; StringBuilder cur = new StringBuilder();
        boolean esc = false;
        for (int i=0;i<line.length();i++){
            char c=line.charAt(i);
            if (esc) { cur.append(c); esc=false; continue; }
            if (c=='\\'){ esc=true; continue; }
            if (c==SEP){
                if (idx<expectedCols) out[idx]=cur.toString(); // else drop overflow
                cur.setLength(0); idx++;
            } else if (c!='\n' && c!='\r'){
                cur.append(c);
            }
        }
        if (idx<expectedCols) out[idx]=cur.toString();
        // fill missing
        for (int i=0;i<expectedCols;i++) if (out[i]==null) out[i]="";
        return out;
    }
}
