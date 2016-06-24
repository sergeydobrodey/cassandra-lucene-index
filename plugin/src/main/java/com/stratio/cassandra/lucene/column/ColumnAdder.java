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

/**
 * Class for building and adding a {@link Column} to a {@link Columns}.
 *
 * @author Andres de la Pena {@literal <adelapena@stratio.com>}
 */
class ColumnAdder {

    private final Columns columns;
    private final ColumnBuilder builder;

    /**
     * Constructor taking the columns where the built {@link Column} is going to be added and a {@link Column} builder.
     *
     * @param columns the columns where the built {@link Column} is going to be added
     * @param builder a {@link Column} builder
     */
    ColumnAdder(Columns columns, ColumnBuilder builder) {
        this.columns = columns;
        this.builder = builder;
    }

    /**
     * Returns a new {@link Column}s with a {@link Column} using the specified composed value and its type.
     *
     * @param composedValue the decomposed value
     * @param type the value type
     * @param <T> the marshaller's base type
     * @return the columns with the specified column
     */
    <T> Columns addComposed(T composedValue, AbstractType<T> type) {
        return columns.add(builder.buildComposed(composedValue, type));
    }

    /**
     * Returns a new {@link Column}s with a {@link Column} using the specified decomposed value and its type.
     *
     * @param decomposedValue the decomposed value
     * @param type the value type
     * @param <T> the marshaller's base type
     * @return the columns with the specified column
     */
    <T> Columns addDecomposed(ByteBuffer decomposedValue, AbstractType<T> type) {
        return columns.add(builder.buildDecomposed(decomposedValue, type));
    }

    /**
     * Returns a new {@link Column}s with a {@link Column} with {@code null} value and the specified type .
     *
     * @param type the value type
     * @param <T> the marshaller's base type
     * @return the columns with the specified column
     */
    <T> Columns addNull(AbstractType<T> type) {
        return columns.add(builder.buildNull(type));
    }

    /**
     * Returns a new adder with the specified UDT name component.
     *
     * @param name the UDT name component
     * @return a new adder with the specified UDT name component
     */
    ColumnAdder withUDTName(String name) {
        return new ColumnAdder(columns, builder.withUDTName(name));
    }

    /**
     * Returns a new adder with the specified map name component.
     *
     * @param name the map key name component
     * @return a new adder with the specified map name component
     */
    ColumnAdder withMapName(String name) {
        return new ColumnAdder(columns, builder.withMapName(name));
    }
}
