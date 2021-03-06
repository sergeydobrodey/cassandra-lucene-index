/*
 * Copyright (C) 2014 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.cassandra.lucene.column;

import org.apache.cassandra.db.marshal.AbstractType;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for building a new {@link Column}.
 *
 * @author Andres de la Pena {@literal <adelapena@stratio.com>}
 */
class ColumnBuilder {

    private final String cellName;
    private final List<String> udtNames;
    private final List<String> mapNames;
    private final int deletionTime;

    /**
     * Constructor taking the cell name.
     *
     * @param cellName the cell name
     * @param deletionTime the deletion time in seconds
     */
    ColumnBuilder(String cellName, int deletionTime) {
        this.cellName = cellName;
        this.deletionTime = deletionTime;
        udtNames = new ArrayList<>();
        mapNames = new ArrayList<>();
    }

    /**
     * Returns a new {@link Column} using the specified composed value and its type.
     *
     * @param composedValue the decomposed value
     * @param type the value type
     * @param <T> the marshaller's base type
     * @return the built column
     */
    <T> Column<T> buildComposed(T composedValue, AbstractType<T> type) {
        ByteBuffer decomposedValue = type.decompose(composedValue);
        return new Column<>(cellName, udtNames, mapNames, decomposedValue, composedValue, type, deletionTime);
    }

    /**
     * Returns a new {@link Column} using the specified decomposed value and its type.
     *
     * @param decomposedValue the decomposed value
     * @param type the value type
     * @param <T> the marshaller's base type
     * @return the built column
     */
    <T> Column<T> buildDecomposed(ByteBuffer decomposedValue, AbstractType<T> type) {
        T composedValue = type.compose(decomposedValue);
        return new Column<>(cellName, udtNames, mapNames, decomposedValue, composedValue, type, deletionTime);
    }

    /**
     * Returns a new {@link Column} with {@code null} value and the specified type .
     *
     * @param type the value type
     * @param <T> the marshaller's base type
     * @return the built column
     */
    <T> Column<T> buildNull(AbstractType<T> type) {
        return new Column<>(cellName, udtNames, mapNames, null, null, type, deletionTime);
    }

    /**
     * Returns this builder with the specified UDT name component.
     *
     * @param name the UDT name component
     * @return this
     */
    ColumnBuilder withUDTName(String name) {
        ColumnBuilder clone = copy();
        clone.udtNames.add(name);
        return clone;
    }

    /**
     * Returns this builder with the specified map name component.
     *
     * @param name the map key name component
     * @return this
     */
    ColumnBuilder withMapName(String name) {
        ColumnBuilder clone = copy();
        clone.mapNames.add(name);
        return clone;
    }

    /**
     * Returns a new copy of this.
     *
     * @return the copy
     */
    private ColumnBuilder copy() {
        ColumnBuilder clone = new ColumnBuilder(cellName, deletionTime);
        clone.udtNames.addAll(udtNames);
        clone.mapNames.addAll(mapNames);
        return clone;
    }
}
