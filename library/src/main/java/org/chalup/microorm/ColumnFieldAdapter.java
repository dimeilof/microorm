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

        mColumnName = toSQLNameDefault(field.getName());
        mColumnNames = new String[]{mColumnName};
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
    
    public static String toSQLNameDefault(String camelCased) {
        StringBuilder sb = new StringBuilder();
        char[] buf = camelCased.toCharArray();

        for (int i = 0; i < buf.length; i++) {
            char prevChar = (i > 0) ? buf[i - 1] : 0;
            char c = buf[i];
            char nextChar = (i < buf.length - 1) ? buf[i + 1] : 0;
            boolean isFirstChar = (i == 0);

            if (isFirstChar || Character.isLowerCase(c) || Character.isDigit(c)) {
                sb.append(Character.toUpperCase(c));
            } else if (Character.isUpperCase(c)) {
                if (Character.isLetterOrDigit(prevChar)) {
                    if (Character.isLowerCase(prevChar)) {
                        sb.append('_').append(c);
                    } else if (nextChar > 0 && Character.isLowerCase(nextChar)) {
                        sb.append('_').append(c);
                    } else {
                        sb.append(c);
                    }
                } else {
                    sb.append(c);
                }
            }
        }

        return sb.toString();
    }
}
