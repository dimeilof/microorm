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
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ReflectiveDaoAdapter<T> implements DaoAdapter<T> {

    private final Class<T> mKlass;
    private final List<FieldAdapter> mFieldAdapters;
    private final List<EmbeddedFieldInitializer> mFieldInitializers;
    private final String[] mProjection;
    private final String[] mWritableColumns;
    private final Set<String> mWritableDuplicates;

    ReflectiveDaoAdapter(Class<T> klass, List<FieldAdapter> fieldAdapters, List<EmbeddedFieldInitializer> fieldInitializers) {
        mKlass = klass;
        mFieldAdapters = fieldAdapters;
        mFieldInitializers = fieldInitializers;

        List<String> projectionBuilder = new ArrayList<>();
        List<String> writableColumnsBuilder = new ArrayList<>();

        for (FieldAdapter fieldAdapter : fieldAdapters) {
            projectionBuilder.addAll(Arrays.asList(fieldAdapter.getColumnNames()));
            writableColumnsBuilder.addAll(Arrays.asList(fieldAdapter.getWritableColumnNames()));
        }
        mProjection = array(projectionBuilder);
        mWritableColumns = array(writableColumnsBuilder);
        mWritableDuplicates = findDuplicates(mWritableColumns);
    }

    private static String[] array(Collection<String> collection) {
        return collection.toArray(new String[collection.size()]);
    }

    private static <T> Set<T> findDuplicates(T[] array) {
        final Set<T> result = new HashSet<>();
        final Set<T> uniques = new HashSet<>();

        for (T element : array) {
            if (!uniques.add(element)) {
                result.add(element);
            }
        }

        return result;
    }

    @Override
    public T createInstance() {
        return createInstance(mKlass);
    }

    private T createInstance(Class<T> klass) {
        try {
            T instance = klass.newInstance();
            for (EmbeddedFieldInitializer fieldInitializer : mFieldInitializers) {
                fieldInitializer.initEmbeddedField(instance);
            }
            return instance;
        } catch (InstantiationException e) {
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public T fromCursor(Cursor c, T object) {
        try {
            for (FieldAdapter fieldAdapter : mFieldAdapters) {
                fieldAdapter.setValueFromCursor(c, object);
            }
            return object;
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public ContentValues toContentValues(ContentValues values, T object) {
        if (!mWritableDuplicates.isEmpty()) {
            throw new IllegalArgumentException("Duplicate columns definitions: " + TextUtils.join(", ", mWritableDuplicates));
        }
        try {
            for (FieldAdapter fieldAdapter : mFieldAdapters) {
                fieldAdapter.putToContentValues(object, values);
            }
        } catch (IllegalArgumentException e) {
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }

        return values;
    }

    @Override
    public ContentValues createContentValues() {
        return new ContentValues(mWritableColumns.length);
    }

    @Override
    public String[] getProjection() {
        return mProjection.clone();
    }

    @Override
    public String[] getWritableColumns() {
        return mWritableColumns.clone();
    }
}
