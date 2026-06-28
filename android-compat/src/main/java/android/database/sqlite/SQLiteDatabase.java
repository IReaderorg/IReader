package android.database.sqlite;
public class SQLiteDatabase {
    public interface CursorFactory {}
    public static SQLiteDatabase openOrCreateDatabase(String path, CursorFactory factory) { return new SQLiteDatabase(); }
}
