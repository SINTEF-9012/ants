package no.sintef.giot.bhp.fitbit.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.google.gson.Gson;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * DB helper for handling HRV entries in the SQLite database
 * TODO: consider renaming it towards HRV and have separate handlers for each table?
 */
public class DBHelper extends SQLiteOpenHelper {

  private static final String TAG = "DBHelper";
  // creating a constant variables for our database.
  // below variable is for our database name.
  private static final String DB_NAME = "fitbit_db";
  // below int is our database version
  private static final int DB_VERSION = 1;
  // Table Names
  private static final String TABLE_NAME = "hrv_table";
  // Column names
  private static final String ID_COL = "id";
  private static final String TIMESTAMP_COL = "timestamp";
  private static final String RMSSD_COL = "rmssd";
  private static final String COVERAGE_COL = "coverage";
  private static final String LOW_FREQUENCY_COL = "low_frequency";
  private static final String HIGH_FREQUENCY_COL = "high_frequency";
  private static DBHelper sInstance;

  // creating a constructor for our database handler.
  private DBHelper(Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }

  public static synchronized DBHelper getInstance(Context context) {
    // Use the application context, which will ensure that you
    // don't accidentally leak an Activity's context.
    if (sInstance == null) {
      sInstance = new DBHelper(context.getApplicationContext());
    }
    return sInstance;
  }

  // Called when the database connection is being configured.
  // Configure database settings for things like foreign key support, write-ahead logging, etc.
  @Override
  public void onConfigure(SQLiteDatabase db) {
    super.onConfigure(db);
    db.setForeignKeyConstraintsEnabled(true);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    // on below line we are creating
    // an sqlite query and we are
    // setting our column names
    // along with their data types.
    String query = "CREATE TABLE " + TABLE_NAME + " ("
        + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + TIMESTAMP_COL + " TEXT,"
        + RMSSD_COL + " TEXT,"
        + COVERAGE_COL + " TEXT,"
        + LOW_FREQUENCY_COL + " TEXT,"
        + HIGH_FREQUENCY_COL + " TEXT)";

    // at last we are calling a exec sql
    // method to execute above sql query
    db.execSQL(query);
  }

  /**
   * Add single entry to the SQLite database
   *
   * @param aHrv HRV entry
   */
  public void addNewEntry(HeartRateVariability aHrv) {

    // on below line we are creating a variable for
    // our sqlite database and calling writable method
    // as we are writing data in our database.
    SQLiteDatabase db = this.getWritableDatabase();

    // on below line we are creating a
    // variable for content values.
    ContentValues values = new ContentValues();

    // on below line we are passing all values
    // along with its key and value pair.
    values.put(TIMESTAMP_COL, aHrv.getTimestamp());
    values.put(RMSSD_COL, aHrv.getRmssd());
    values.put(COVERAGE_COL, aHrv.getCoverage());
    values.put(LOW_FREQUENCY_COL, aHrv.getLowFrequency());
    values.put(HIGH_FREQUENCY_COL, aHrv.getHighFrequency());

    // after adding all values we are passing
    // content values to our table.
    db.insert(TABLE_NAME, null, values);

    // at last we are closing our
    // database after adding database.
    db.close();
  }

  /**
   * Adds new entries to the SQLite database
   *
   * @param aHrvList list of HRV entries
   */
  public void addNewEntries(List<HeartRateVariability> aHrvList) {

    // on below line we are creating a variable for
    // our sqlite database and calling writable method
    // as we are writing data in our database.
    SQLiteDatabase db = this.getWritableDatabase();
    db.execSQL("delete from " + TABLE_NAME);

    for (HeartRateVariability hrv : aHrvList) {
      // on below line we are creating a
      // variable for content values.
      ContentValues values = new ContentValues();

      // on below line we are passing all values
      // along with its key and value pair.
      values.put(TIMESTAMP_COL, hrv.getTimestamp());
      values.put(RMSSD_COL, hrv.getRmssd());
      values.put(COVERAGE_COL, hrv.getCoverage());
      values.put(LOW_FREQUENCY_COL, hrv.getLowFrequency());
      values.put(HIGH_FREQUENCY_COL, hrv.getHighFrequency());

      // after adding all values we are passing
      // content values to our table.
      db.insert(TABLE_NAME, null, values);
    }

    // at last we are closing our
    // database after adding database.
    db.close();
  }

  /**
   * Returns database entries as a CSV string
   *
   * @return CSV string
   * @throws IOException exception
   */
  public String getAllEntriesAsCsv()
      throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
    List<HeartRateVariability> data = getAllEntries();
    // create a string writer
    StringWriter writer = new StringWriter();
    ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
    // Correct order of columns
    String[] columns = new String[] {TIMESTAMP_COL, RMSSD_COL, COVERAGE_COL, LOW_FREQUENCY_COL,
        HIGH_FREQUENCY_COL};
    mappingStrategy.setColumnMapping(columns);
    mappingStrategy.setType(HeartRateVariability.class);
    // create a csv writer
    StatefulBeanToCsv<HeartRateVariability> csvWriter =
        new StatefulBeanToCsvBuilder<HeartRateVariability>(writer)
            .withMappingStrategy(mappingStrategy)
            .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
            .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
            .withEscapechar(CSVWriter.DEFAULT_ESCAPE_CHARACTER)
            .withLineEnd(CSVWriter.DEFAULT_LINE_END)
            .withOrderedResults(true)
            .build();

    // write list of objects
    csvWriter.write(data);
    String result = writer.toString();
    // close the writer
    writer.close();

    return result;
  }

  /**
   * Returns database entries in JSON format
   *
   * @return JSON string
   */
  public String getAllEntriesAsJson() {
    List<HeartRateVariability> data = getAllEntries();

    Gson gson = new Gson();
    // Serialization
    String json = gson.toJson(data);
    return json;
  }

  /**
   * Gets all entries in the database
   *
   * @return list of HRV entries
   */
  public List<HeartRateVariability> getAllEntries() {
    List<HeartRateVariability> hrvEntries = new ArrayList<>();

    // SELECT ALL FROM hrv_table
    String HRV_SELECT_QUERY =
        String.format("SELECT * FROM %s",
            TABLE_NAME);

    // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
    // disk space scenarios)
    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(HRV_SELECT_QUERY, null);
    try {
      if (cursor.moveToFirst()) {
        do {
          HeartRateVariability hrv = new HeartRateVariability();
          hrv.setTimestamp(cursor.getString(cursor.getColumnIndex(TIMESTAMP_COL)));
          hrv.setCoverage(cursor.getString(cursor.getColumnIndex(COVERAGE_COL)));
          hrv.setRmssd(cursor.getString(cursor.getColumnIndex(RMSSD_COL)));
          hrv.setLowFrequency(cursor.getString(cursor.getColumnIndex(LOW_FREQUENCY_COL)));
          hrv.setHighFrequency(cursor.getString(cursor.getColumnIndex(HIGH_FREQUENCY_COL)));
          hrvEntries.add(hrv);
        } while (cursor.moveToNext());
      }
    } catch (Exception e) {
      Log.d(TAG, "Error while trying to get HRV entries from database");
    } finally {
      if (cursor != null && !cursor.isClosed()) {
        cursor.close();
      }
    }
    return hrvEntries;
  }

  /**
   * Gets all entries within a time range
   *
   * @param aFrom starting date
   * @param aTo   closing date
   * @return list of HRV entries
   */
  public List<HeartRateVariability> getAllEntriesFromTo(String aFrom, String aTo) {
    List<HeartRateVariability> hrvEntries = new ArrayList<>();

    // SELECT ALL FROM hrv_table
    String HRV_SELECT_QUERY =
        String.format(
            "SELECT * FROM %s WHERE DATETIME(timestamp) BETWEEN DATETIME('%s') AND DATETIME('%s')",
            TABLE_NAME, aFrom, aTo);

    // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
    // disk space scenarios)
    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.rawQuery(HRV_SELECT_QUERY, null);
    try {
      if (cursor.moveToFirst()) {
        do {
          HeartRateVariability hrv = new HeartRateVariability();
          hrv.setTimestamp(cursor.getString(cursor.getColumnIndex(TIMESTAMP_COL)));
          hrv.setCoverage(cursor.getString(cursor.getColumnIndex(COVERAGE_COL)));
          hrv.setRmssd(cursor.getString(cursor.getColumnIndex(RMSSD_COL)));
          hrv.setLowFrequency(cursor.getString(cursor.getColumnIndex(LOW_FREQUENCY_COL)));
          hrv.setHighFrequency(cursor.getString(cursor.getColumnIndex(HIGH_FREQUENCY_COL)));
          hrvEntries.add(hrv);
        } while (cursor.moveToNext());
      }
    } catch (Exception e) {
      Log.d(TAG, "Error while trying to get HRV entries from database");
    } finally {
      if (cursor != null && !cursor.isClosed()) {
        cursor.close();
      }
    }
    Log.d(TAG, String.valueOf(hrvEntries.size()) + " entries found!");
    return hrvEntries;
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // this method is called to check if the table exists already.
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    onCreate(db);
  }
}
