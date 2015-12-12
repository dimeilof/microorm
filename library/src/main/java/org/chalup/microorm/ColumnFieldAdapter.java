/*
 * Copyright (C) 2013 Jerzy Chalupski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.chalup.microorm;

import android.content.ContentValues;
import android.database.Cursor;

import java.lang.reflect.Field;

class ColumnFieldAdapter extends FieldAdapter {

  private final String mColumnName;
  private final String[] mColumnNames;
  private final TypeAdapter<?> mTypeAdapter;

  ColumnFieldAdapter(Field field, TypeAdapter<?> typeAdapter) {
    super(field);
    mTypeAdapter = typeAdapter;

    mColumnName = field.getName();
    mColumnNames = new String[] { mColumnName };
  }

  @Override
  public void setValueFromCursor(Cursor inCursor, Object outTarget) throws IllegalArgumentException, IllegalAccessException {
    mField.set(outTarget, mTypeAdapter.fromCursor(inCursor, mColumnName));
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void putValueToContentValues(Object fieldValue, ContentValues outValues) {
    ((TypeAdapter<Object>) mTypeAdapter).toContentValues(outValues, mColumnName, fieldValue);
  }

  @Override
  public String[] getColumnNames() {
    return mColumnNames;
  }

  @Override
  public String[] getWritableColumnNames() {
    return getColumnNames();
  }
}
