/*
 * 
 */

package com.coinwtb.coinassistant.common;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class CommonDatabaseHelper extends SQLiteOpenHelper {
	private static final int DB_VERSION = 1;

	protected SQLiteDatabase mDatabase;

	public enum FieldType {
		FieldIntegerIndex, FieldStringIndex, FieldInteger, FieldString, FieldTime, FieldDouble,
	}

	public class Field {
		String mName;
		FieldType mType;
		int mSize;

		public Field(String name, FieldType type) {
			mName = name;
			mType = type;
			mSize = 0;
		}

		public Field(String name, FieldType type, int size) {
			mName = name;
			mType = type;
			mSize = size;
		}
	}

	public CommonDatabaseHelper(Context context, String databaseName,
			CursorFactory cursorFactory) {
		super(context, databaseName, cursorFactory, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		mDatabase = db;
		onCreateDatabase();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int aOldVersion, int aNewVersion) {
		mDatabase = db;
		onUpgradeDatabase();
		onCreate(db);
	}

	public abstract void onCreateDatabase();

	public abstract void onUpgradeDatabase();

	/**
	 * deleteTable
	 */
	public void deleteTable(String tableName) {
		try {
			mDatabase.beginTransaction();
			mDatabase.execSQL("DROP TABLE IF EXISTS " + tableName + ";");
			mDatabase.setTransactionSuccessful();
		} catch (SQLiteException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			mDatabase.endTransaction();
		}
	}

	/**
	 * createTable
	 */
	public void createTable(String tableName, List<Field> fieldList) {
		try {
			mDatabase.beginTransaction();
			StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");

			for (int i = 0; i < fieldList.size(); i++) {
				sql.append(fieldList.get(i).mName + " ");

				String strSize = (fieldList.get(i).mSize > 0) ? String.format(
						"(%d)", fieldList.get(i).mSize) : "";
				switch (fieldList.get(i).mType) {
				case FieldIntegerIndex:
					sql.append("INTEGER PRIMARY KEY ");
					break;

				case FieldStringIndex:
					sql.append("VARCHAR PRIMARY KEY ");
					break;
				case FieldInteger:
					sql.append("INTEGER");
					break;
          
				case FieldString:
					sql.append("VARCHAR");
					break;
				case FieldDouble :
					sql.append("DOUBLE");
					break;
				default:
					break;
				}

				sql.append(strSize);
				
				if (i < fieldList.size() - 1) {
					sql.append(",");
				} else {
					sql.append(");");
				}

			}
			mDatabase.execSQL(sql.toString());
			mDatabase.setTransactionSuccessful();
		} catch (SQLiteException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			mDatabase.endTransaction();
		}
	}

	protected void updateData(String tableName, ArrayList<Field> setFields,
			ArrayList<String> data, String condition) {
		SQLiteDatabase db = getWritableDatabase();

		try {
			db.beginTransaction();

			String sql = "UPDATE " + tableName + " SET ";
			for (int i = 0; i < setFields.size(); i++) {
				sql += setFields.get(i).mName + " = " + data.get(i);
				if (i == setFields.size() - 1) {
				} else {
					sql += ", ";
				}
			}

			if (condition.length() > 0) {
				sql += " where ";
			}
			sql += condition + ";";

			db.execSQL(sql);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}
}
