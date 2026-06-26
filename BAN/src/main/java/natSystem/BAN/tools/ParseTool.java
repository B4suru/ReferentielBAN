package natSystem.BAN.tools;

public class ParseTool {
    public Integer parseIntSafe(String v) {
        if (v == null || v.isBlank()) return null;
        return Integer.parseInt(v);
    }

    public Double parseDoubleSafe(String v) {
        if (v == null || v.isBlank()) return 0.0;
        return Double.parseDouble(v);
    }
}
