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
package com.stratio.cassandra.lucene.schema.mapping.builder;

import com.stratio.cassandra.lucene.schema.mapping.DateMapper;
import com.stratio.cassandra.lucene.util.DateParser;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * {@link SingleColumnMapperBuilder} to build a new {@link DateMapper}.
 *
 * @author Andres de la Pena {@literal <adelapena@stratio.com>}
 */
public class DateMapperBuilder extends SingleColumnMapperBuilder<DateMapper, DateMapperBuilder> {

    /** The default date pattern */
    @JsonProperty("pattern")
    private String pattern;

    /** The date pattern for columns */
    @JsonProperty("column_pattern")
    private String columnPattern;

    /** The date pattern for fields */
    @JsonProperty("lucene_pattern")
    private String lucenePattern;

    /**
     * Sets the default date format pattern.
     *
     * @param pattern a {@link java.text.SimpleDateFormat} date pattern, or "timestamp" for UNIX time milliseconds
     * @return This.
     */
    public DateMapperBuilder pattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    /**
     * Sets the date pattern for columns.
     *
     * @param pattern a {@link java.text.SimpleDateFormat} date pattern, or "timestamp" for UNIX time milliseconds
     * @return this
     */
    public DateMapperBuilder columnPattern(String pattern) {
        columnPattern = pattern;
        return this;
    }

    /**
     * Sets the date pattern for fields.
     *
     * @param pattern a {@link java.text.SimpleDateFormat} date pattern, or "timestamp" for UNIX time milliseconds
     * @return this
     */
    public DateMapperBuilder lucenePattern(String pattern) {
        lucenePattern = pattern;
        return this;
    }

    /**
     * Returns the {@link DateMapper} represented by this {@link MapperBuilder}.
     *
     * @param field the name of the field to be built
     * @return the {@link DateMapper} represented by this
     */
    @Override
    public DateMapper build(String field) {
        DateParser dateParser = new DateParser(pattern, columnPattern, lucenePattern);
        return new DateMapper(field, column, validated, dateParser);
    }
}
