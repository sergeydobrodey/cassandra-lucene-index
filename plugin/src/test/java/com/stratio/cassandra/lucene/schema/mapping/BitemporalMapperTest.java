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
package com.stratio.cassandra.lucene.schema.mapping;

import com.stratio.cassandra.lucene.IndexException;
import com.stratio.cassandra.lucene.column.Columns;
import com.stratio.cassandra.lucene.schema.mapping.BitemporalMapper.BitemporalDateTime;
import com.stratio.cassandra.lucene.schema.mapping.builder.BitemporalMapperBuilder;
import com.stratio.cassandra.lucene.util.DateParser;
import org.apache.cassandra.db.marshal.*;
import org.apache.cassandra.utils.UUIDGen;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexableField;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.stratio.cassandra.lucene.schema.SchemaBuilders.bitemporalMapper;
import static com.stratio.cassandra.lucene.util.DateParser.DEFAULT_PATTERN;
import static com.stratio.cassandra.lucene.util.DateParser.TIMESTAMP_PATTERN;
import static org.junit.Assert.*;

/**
 * @author eduardoalonso  {@literal <eduardoalonso@stratio.com>}
 */
public class BitemporalMapperTest extends AbstractMapperTest {
    @Test
    public void testConstructorWithDefaultArgs() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").build("f");
        assertEquals("Field is not set", "f", mapper.field);
        assertEquals("vtFrom is not set", "vtFrom", mapper.vtFrom);
        assertEquals("vtTo is not set", "vtTo", mapper.vtTo);
        assertEquals("ttFrom is not set", "ttFrom", mapper.ttFrom);
        assertEquals("ttTo is not set", "ttTo", mapper.ttTo);
        assertEquals("Now value is not set to default", Long.MAX_VALUE, mapper.nowValue, 0);
        assertEquals("Column date pattern is not set to default value", DEFAULT_PATTERN, mapper.parser.columnPattern);
        assertEquals("Field date pattern is not set to default value", DEFAULT_PATTERN, mapper.parser.lucenePattern);
    }

    @Test
    public void testConstructorWithAllArgs() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").validated(true)
                                                                                      .pattern(TIMESTAMP_PATTERN)
                                                                                      .columnPattern("yyyy-MM-dd")
                                                                                      .lucenePattern("yyyy/MM/dd")
                                                                                      .nowValue("2021/03/11")
                                                                                      .build("f");
        assertEquals("Field is not set", "f", mapper.field);
        assertEquals("vtFrom is not set", "vtFrom", mapper.vtFrom);
        assertEquals("vtTo is not set", "vtTo", mapper.vtTo);
        assertEquals("ttFrom is not set", "ttFrom", mapper.ttFrom);
        assertEquals("ttTo is not set", "ttTo", mapper.ttTo);
        assertEquals("Date pattern is wrong", mapper.parseBitemporalDate("2021/03/11"), BitemporalDateTime.MAX);
        assertEquals("Column date pattern is not set to default value", "yyyy-MM-dd", mapper.parser.columnPattern);
        assertEquals("Field date pattern is not set to default value", "yyyy/MM/dd", mapper.parser.lucenePattern);
    }

    @Test
    public void testConstructorWithDefaultPattern() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern(TIMESTAMP_PATTERN)
                                                                                      .build("f");
        assertEquals("Column date pattern is not set to default value", TIMESTAMP_PATTERN, mapper.parser.columnPattern);
        assertEquals("Field date pattern is not set to default value", TIMESTAMP_PATTERN, mapper.parser.lucenePattern);
    }

    @Test
    public void testMappedColumns() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").build("f");
        assertEquals("Mapped columns are not properly set", 4, mapper.mappedColumns.size());
        assertTrue("Mapped columns are not set", mapper.mappedColumns.contains("vtFrom"));
        assertTrue("Mapped columns are not set", mapper.mappedColumns.contains("vtTo"));
        assertTrue("Mapped columns are not set", mapper.mappedColumns.contains("ttFrom"));
        assertTrue("Mapped columns are not set", mapper.mappedColumns.contains("ttTo"));
    }

    @Test
    public void testParseJSONWithDefaultArgs() throws IOException {
        BitemporalMapperBuilder builder = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo");
        testJson(builder, "{type:\"bitemporal\",vt_from:\"vtFrom\",vt_to:\"vtTo\",tt_from:\"ttFrom\",tt_to:\"ttTo\"}");
    }

    @Test
    public void testParseJSONWithAllArgs() throws IOException {
        BitemporalMapperBuilder builder = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("yyyy/MM/dd")
                                                                                              .nowValue("2021/03/11");
        testJson(builder,
                 "{type:\"bitemporal\",vt_from:\"vtFrom\",vt_to:\"vtTo\",tt_from:\"ttFrom\",tt_to:\"ttTo\"," +
                 "pattern:\"yyyy/MM/dd\",now_value:\"2021/03/11\"}");
    }

    @Test(expected = IndexException.class)
    public void testConstructorWithNullVtFrom() {
        bitemporalMapper(null, "vtTo", "ttFrom", "ttTo").pattern("yyyy/MM/dd").nowValue("2021/03/11").build("f");
    }

    @Test(expected = IndexException.class)
    public void testConstructorWithEmptyVtFrom() {
        bitemporalMapper("", "vtTo", "ttFrom", "ttTo").pattern("yyyy/MM/dd").nowValue("2021/03/11").build("f");
    }

    @Test(expected = IndexException.class)
    public void testConstructorWithBlankVtFrom() {
        bitemporalMapper(" ", "vtTo", "ttFrom", "ttTo").pattern("yyyy/MM/dd").nowValue("2021/03/11").build("f");
    }

    @Test(expected = IndexException.class)
    public void testConstructorWithNullVtTo() {
        bitemporalMapper("vtFrom", null, "ttFrom", "ttTo").pattern("yyyy/MM/dd").nowValue("2021/03/11").build("f");
    }

    @Test(expected = IndexException.class)
    public void testConstructorWithEmptyVtTo() {
        bitemporalMapper("vtFrom", "", "ttFrom", "ttTo").pattern("yyyy/MM/dd").nowValue("2021/03/11").build("f");
    }

    @Test(expected = IndexException.class)
    public void testConstructorWithBlankVtTo() {
        bitemporalMapper("vtFrom", " ", "ttFrom", "ttTo").pattern("yyyy/MM/dd").nowValue("2021/03/11").build("f");
    }

    @Test(expected = IndexException.class)
    public void testConstructorWithNullTtFrom() {
        bitemporalMapper("vtFrom", "vtTo", null, "ttTo").pattern("yyyy/MM/dd").nowValue("2021/03/11").build("f");
    }

    @Test(expected = IndexException.class)
    public void testConstructorWithEmptyTtFrom() {
        bitemporalMapper("vtFrom", "vtTo", "", "ttTo").pattern("yyyy/MM/dd").nowValue("2021/03/11").build("f");
    }

    @Test(expected = IndexException.class)
    public void testConstructorWithBlankTtFrom() {
        bitemporalMapper("vtFrom", "vtTo", " ", "ttTo").pattern("yyyy/MM/dd").nowValue("2021/03/11").build("f");
    }

    @Test(expected = IndexException.class)
    public void testConstructorWithNullTtTo() {
        bitemporalMapper("vtFrom", "vtTo", "ttFrom", null).pattern("yyyy/MM/dd").nowValue("2021/03/11").build("f");
    }

    @Test(expected = IndexException.class)
    public void testConstructorWithEmptyTtTo() {
        bitemporalMapper("vtFrom", "vtTo", "ttFrom", "").pattern("yyyy/MM/dd").nowValue("2021/03/11").build("f");
    }

    @Test(expected = IndexException.class)
    public void testConstructorWithBlankTtTo() {
        bitemporalMapper("vtFrom", "vtTo", "ttFrom", " ").pattern("yyyy/MM/dd").nowValue("2021/03/11").build("f");
    }

    @Test(expected = IndexException.class)
    public void testConstructorWithEmptyNowValue() {
        bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("yyyy/MM/dd").nowValue("").build("f");
    }

    @Test(expected = IndexException.class)
    public void testConstructorWithBlankNowValue() {
        bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("yyyy/MM/dd").nowValue(" ").build("f");
    }

    @Test(expected = IndexException.class)
    public void testConstructorWithInvalidNowValue() {
        bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("yyyy/MM/dd")
                                                            .nowValue("2021-03-11 00:00:00.001")
                                                            .build("f");
    }

    @Test
    public void testReadVtFromFieldFromInt32Column() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", 5, Int32Type.instance)
                                       .addComposed("vtTo", 0, Int32Type.instance)
                                       .addComposed("ttFrom", 0, Int32Type.instance)
                                       .addComposed("ttTo", 0, Int32Type.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "vtFrom"));
    }

    @Test
    public void testReadVtFromFieldFromLongColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", 5L, LongType.instance)
                                       .addComposed("vtTo", 0L, LongType.instance)
                                       .addComposed("ttFrom", 0L, LongType.instance)
                                       .addComposed("ttTo", 0L, LongType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "vtFrom"));
    }

    @Test
    public void testReadVtFromFieldFromTimeUUIDColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", UUIDGen.getTimeUUID(5L), TimeUUIDType.instance)
                                       .addComposed("vtTo", UUIDGen.getTimeUUID(0L), TimeUUIDType.instance)
                                       .addComposed("ttFrom", UUIDGen.getTimeUUID(0L), TimeUUIDType.instance)
                                       .addComposed("ttTo", UUIDGen.getTimeUUID(0L), TimeUUIDType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "vtFrom"));
    }

    @Test
    public void testReadVtFromFieldFromIntegerColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", BigInteger.valueOf(5), IntegerType.instance)
                                       .addComposed("vtTo", BigInteger.valueOf(0), IntegerType.instance)
                                       .addComposed("ttFrom", BigInteger.valueOf(0), IntegerType.instance)
                                       .addComposed("ttTo", BigInteger.valueOf(0), IntegerType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "vtFrom"));
    }

    @Test
    public void testReadVtFromFieldFromSimpleDateColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addDecomposed("vtFrom",
                                                      SimpleDateType.instance.fromString("2015-10-10"),
                                                      SimpleDateType.instance);
        assertEquals("Wrong date parsing",
                     new BitemporalDateTime(1444435200000L),
                     mapper.readBitemporalDate(columns, "vtFrom"));
    }

    @Test
    public void testReadVtFromFieldFromFloatColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", 5.0f, FloatType.instance)
                                       .addComposed("vtTo", 0.0f, FloatType.instance)
                                       .addComposed("ttFrom", 0.0f, FloatType.instance)
                                       .addComposed("ttTo", 0.0f, FloatType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "vtFrom"));
    }

    @Test
    public void testReadVtFromFieldFromDoubleColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", 5.0, DoubleType.instance)
                                       .addComposed("vtTo", 0.0, DoubleType.instance)
                                       .addComposed("ttFrom", 0.0, DoubleType.instance)
                                       .addComposed("ttTo", 0.0, DoubleType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "vtFrom"));
    }

    @Test
    public void testReadVtFromFieldFromDecimalColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", BigDecimal.valueOf(5.0), DecimalType.instance)
                                       .addComposed("vtTo", BigDecimal.valueOf(0.0), DecimalType.instance)
                                       .addComposed("ttFrom", BigDecimal.valueOf(0.0), DecimalType.instance)
                                       .addComposed("ttTo", BigDecimal.valueOf(0.0), DecimalType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "vtFrom"));
    }

    @Test
    public void testReadVtFromFieldFromAsciiColumn() throws ParseException {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("yyyy/MM/dd HH:mm:ss")
                                                                                      .nowValue("2025/12/23 00:00:00")
                                                                                      .build("f");
        Columns columns = new Columns().addComposed("vtFrom", "2015/03/24 11:15:14", AsciiType.instance)
                                       .addComposed("vtTo", "2015/03/24 11:15:14", AsciiType.instance)
                                       .addComposed("ttFrom", "2015/03/24 11:15:14", AsciiType.instance)
                                       .addComposed("ttTo", "2015/03/24 11:15:14", AsciiType.instance);
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = format.parse("2015/03/24 11:15:14");
        assertEquals("Wrong date parsing", new BitemporalDateTime(date), mapper.readBitemporalDate(columns, "vtFrom"));
    }

    @Test
    public void testReadVtFromFieldFromUTF8Column() throws ParseException {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("yyyy/MM/dd HH:mm:ss")
                                                                                      .nowValue("2025/12/23 00:00:00")
                                                                                      .build("f");
        Columns columns = new Columns().addComposed("vtFrom", "2015/03/24 11:15:14", UTF8Type.instance)
                                       .addComposed("vtTo", "2015/03/24 11:15:14", UTF8Type.instance)
                                       .addComposed("ttFrom", "2015/03/24 11:15:14", UTF8Type.instance)
                                       .addComposed("ttTo", "2015/03/24 11:15:14", UTF8Type.instance);
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = format.parse("2015/03/24 11:15:14");
        assertEquals("Wrong date parsing",
                     new BitemporalDateTime(date),
                     mapper.readBitemporalDate(columns, "vtFrom"));
    }

    @Test
    public void testReadVtFromFieldFromTimeStampColumn() throws ParseException {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("yyyy/MM/dd HH:mm:ss")
                                                                                      .nowValue("2025/12/23 00:00:00")
                                                                                      .build("f");
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = format.parse("2015/03/24 11:15:14");
        Columns columns = new Columns().addComposed("vtFrom", date, TimestampType.instance)
                                       .addComposed("vtTo", date, TimestampType.instance)
                                       .addComposed("ttFrom", date, TimestampType.instance)
                                       .addComposed("ttTo", date, TimestampType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(date), mapper.readBitemporalDate(columns, "vtFrom"));
    }

    @Test
    public void testReadVtToFieldsFromInt32Column() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", 0, Int32Type.instance)
                                       .addComposed("vtTo", 5, Int32Type.instance)
                                       .addComposed("ttFrom", 0, Int32Type.instance)
                                       .addComposed("ttTo", 0, Int32Type.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "vtTo"));
    }

    @Test
    public void testReadVtToFieldsFromLongColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", 0L, LongType.instance)
                                       .addComposed("vtTo", 5L, LongType.instance)
                                       .addComposed("ttFrom", 0L, LongType.instance)
                                       .addComposed("ttTo", 0L, LongType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "vtTo"));
    }

    @Test
    public void testReadVtToFieldsFromTimeUUIDColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", UUIDGen.getTimeUUID(0L), TimeUUIDType.instance)
                                       .addComposed("vtTo", UUIDGen.getTimeUUID(5L), TimeUUIDType.instance)
                                       .addComposed("ttFrom", UUIDGen.getTimeUUID(0L), TimeUUIDType.instance)
                                       .addComposed("ttTo", UUIDGen.getTimeUUID(0L), TimeUUIDType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "vtTo"));
    }

    @Test
    public void testReadVtToFieldsFromIntegerColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", BigInteger.valueOf(0), IntegerType.instance)
                                       .addComposed("vtTo", BigInteger.valueOf(5), IntegerType.instance)
                                       .addComposed("ttFrom", BigInteger.valueOf(0), IntegerType.instance)
                                       .addComposed("ttTo", BigInteger.valueOf(0), IntegerType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "vtTo"));
    }

    @Test
    public void testReadVtToFieldFromSimpleDateColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns();
        ByteBuffer bb = SimpleDateType.instance.fromString("2015-10-10");
        columns.addDecomposed("vtTo", bb, SimpleDateType.instance);
        assertEquals("Wrong date parsing",
                     new BitemporalDateTime(1444435200000L),
                     mapper.readBitemporalDate(columns, "vtTo"));
    }

    @Test
    public void testReadVtToFieldsFromFloatColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", 0.0f, FloatType.instance)
                                       .addComposed("vtTo", 5.0f, FloatType.instance)
                                       .addComposed("ttFrom", 0.0f, FloatType.instance)
                                       .addComposed("ttTo", 0.0f, FloatType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "vtTo"));
    }

    @Test
    public void testReadVtToFieldsFromDoubleColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", 0.0, DoubleType.instance)
                                       .addComposed("vtTo", 5.0, DoubleType.instance)
                                       .addComposed("ttFrom", 0.0, DoubleType.instance)
                                       .addComposed("ttTo", 0.0, DoubleType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "vtTo"));
    }

    @Test
    public void testReadVtToFieldsFromDecimalColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", BigDecimal.valueOf(0.0), DecimalType.instance)
                                       .addComposed("vtTo", BigDecimal.valueOf(5.0), DecimalType.instance)
                                       .addComposed("ttFrom", BigDecimal.valueOf(0.0), DecimalType.instance)
                                       .addComposed("ttTo", BigDecimal.valueOf(0.0), DecimalType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "vtTo"));
    }

    @Test
    public void testReadVtToFieldsFromAsciiColumn() throws ParseException {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("yyyy/MM/dd HH:mm:ss")
                                                                                      .nowValue("2025/12/23 00:00:00")
                                                                                      .build("f");
        Columns columns = new Columns().addComposed("vtFrom", "2015/03/24 11:15:14", AsciiType.instance)
                                       .addComposed("vtTo", "2015/03/24 11:15:14", AsciiType.instance)
                                       .addComposed("ttFrom", "2015/03/24 11:15:14", AsciiType.instance)
                                       .addComposed("ttTo", "2015/03/24 11:15:14", AsciiType.instance);
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = format.parse("2015/03/24 11:15:14");
        assertEquals("Wrong date parsing", new BitemporalDateTime(date), mapper.readBitemporalDate(columns, "vtTo"));
    }

    @Test
    public void testReadVtToFieldsFromUTF8Column() throws ParseException {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("yyyy/MM/dd HH:mm:ss")
                                                                                      .nowValue("2025/12/23 00:00:00")
                                                                                      .build("f");
        Columns columns = new Columns().addComposed("vtFrom", "2015/03/24 11:15:14", UTF8Type.instance)
                                       .addComposed("vtTo", "2015/03/24 11:15:14", UTF8Type.instance)
                                       .addComposed("ttFrom", "2015/03/24 11:15:14", UTF8Type.instance)
                                       .addComposed("ttTo", "2015/03/24 11:15:14", UTF8Type.instance);
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = format.parse("2015/03/24 11:15:14");
        assertEquals("Wrong date parsing", new BitemporalDateTime(date), mapper.readBitemporalDate(columns, "vtTo"));
    }

    @Test
    public void testReadVtToFieldsFromTimeStampColumn() throws ParseException {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("yyyy/MM/dd HH:mm:ss")
                                                                                      .nowValue("2025/12/23 00:00:00")
                                                                                      .build("f");
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = format.parse("2015/03/24 11:15:14");
        Columns columns = new Columns().addComposed("vtFrom", date, TimestampType.instance)
                                       .addComposed("vtTo", date, TimestampType.instance)
                                       .addComposed("ttFrom", date, TimestampType.instance)
                                       .addComposed("ttTo", date, TimestampType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(date), mapper.readBitemporalDate(columns, "vtTo"));
    }

    @Test
    public void testReadTtFromFieldFromInt32Column() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", 0, Int32Type.instance)
                                       .addComposed("vtTo", 0, Int32Type.instance)
                                       .addComposed("ttFrom", 5, Int32Type.instance)
                                       .addComposed("ttTo", 0, Int32Type.instance);
        assertEquals("Wrong date parsing",
                     new BitemporalDateTime(5L),
                     mapper.readBitemporalDate(columns, "ttFrom"));
    }

    @Test
    public void testReadTtFromFieldFromLongColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", 0L, LongType.instance)
                                       .addComposed("vtTo", 0L, LongType.instance)
                                       .addComposed("ttFrom", 5L, LongType.instance)
                                       .addComposed("ttTo", 0L, LongType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "ttFrom"));
    }

    @Test
    public void testReadTtFromFieldFromTimeUUIDColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", UUIDGen.getTimeUUID(0L), TimeUUIDType.instance)
                                       .addComposed("vtTo", UUIDGen.getTimeUUID(0L), TimeUUIDType.instance)
                                       .addComposed("ttFrom", UUIDGen.getTimeUUID(5L), TimeUUIDType.instance)
                                       .addComposed("ttTo", UUIDGen.getTimeUUID(0L), TimeUUIDType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "ttFrom"));
    }

    @Test
    public void testReadTtFromFieldFromIntegerColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", BigInteger.valueOf(0), IntegerType.instance)
                                       .addComposed("vtTo", BigInteger.valueOf(0), IntegerType.instance)
                                       .addComposed("ttFrom", BigInteger.valueOf(5), IntegerType.instance)
                                       .addComposed("ttTo", BigInteger.valueOf(0), IntegerType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "ttFrom"));
    }

    @Test
    public void testReadTtFromFieldFromSimpleDateColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addDecomposed("ttFrom",
                                                      SimpleDateType.instance.fromString("2015-10-10"),
                                                      SimpleDateType.instance);
        assertEquals("Wrong date parsing",
                     new BitemporalDateTime(1444435200000L),
                     mapper.readBitemporalDate(columns, "ttFrom"));
    }

    @Test
    public void testReadTtFromFieldFromFloatColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", 0.0f, FloatType.instance)
                                       .addComposed("vtTo", 0.0f, FloatType.instance)
                                       .addComposed("ttFrom", 5.0f, FloatType.instance)
                                       .addComposed("ttTo", 0.0f, FloatType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "ttFrom"));
    }

    @Test
    public void testReadTtFromFieldFromDoubleColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", 0.0, DoubleType.instance)
                                       .addComposed("vtTo", 0.0, DoubleType.instance)
                                       .addComposed("ttFrom", 5.0, DoubleType.instance)
                                       .addComposed("ttTo", 0.0, DoubleType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "ttFrom"));
    }

    @Test
    public void testReadTtFromFieldFromDecimalColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", BigDecimal.valueOf(0.0), DecimalType.instance)
                                       .addComposed("vtTo", BigDecimal.valueOf(0.0), DecimalType.instance)
                                       .addComposed("ttFrom", BigDecimal.valueOf(5.0), DecimalType.instance)
                                       .addComposed("ttTo", BigDecimal.valueOf(0.0), DecimalType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "ttFrom"));
    }

    @Test
    public void testReadTtFromFieldFromAsciiColumn() throws ParseException {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("yyyy/MM/dd HH:mm:ss")
                                                                                      .nowValue("2025/12/23 00:00:00")
                                                                                      .build("f");
        Columns columns = new Columns().addComposed("vtFrom", "2015/03/24 11:15:14", AsciiType.instance)
                                       .addComposed("vtTo", "2015/03/24 11:15:14", AsciiType.instance)
                                       .addComposed("ttFrom", "2015/03/24 11:15:14", AsciiType.instance)
                                       .addComposed("ttTo", "2015/03/24 11:15:14", AsciiType.instance);
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = format.parse("2015/03/24 11:15:14");
        assertEquals("Wrong date parsing",
                     new BitemporalDateTime(date),
                     mapper.readBitemporalDate(columns, "ttFrom"));
    }

    @Test
    public void testReadTtFromFieldFromUTF8Column() throws ParseException {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("yyyy/MM/dd HH:mm:ss")
                                                                                      .nowValue("2025/12/23 00:00:00")
                                                                                      .build("f");
        Columns columns = new Columns().addComposed("vtFrom", "2015/03/24 11:15:14", UTF8Type.instance)
                                       .addComposed("vtTo", "2015/03/24 11:15:14", UTF8Type.instance)
                                       .addComposed("ttFrom", "2015/03/24 11:15:14", UTF8Type.instance)
                                       .addComposed("ttTo", "2015/03/24 11:15:14", UTF8Type.instance);
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = format.parse("2015/03/24 11:15:14");
        assertEquals("Wrong date parsing",
                     new BitemporalDateTime(date),
                     mapper.readBitemporalDate(columns, "ttFrom"));
    }

    @Test
    public void testReadTtFromFieldFromTimeStampColumn() throws ParseException {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("yyyy/MM/dd HH:mm:ss")
                                                                                      .nowValue("2025/12/23 00:00:00")
                                                                                      .build("f");
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = format.parse("2015/03/24 11:15:14");
        Columns columns = new Columns().addComposed("vtFrom", date, TimestampType.instance)
                                       .addComposed("vtTo", date, TimestampType.instance)
                                       .addComposed("ttFrom", date, TimestampType.instance)
                                       .addComposed("ttTo", date, TimestampType.instance);
        assertEquals("Wrong date parsing",
                     new BitemporalDateTime(date),
                     mapper.readBitemporalDate(columns, "ttFrom"));
    }

    @Test
    public void testReadTtToFieldFromInt32Column() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", 0, Int32Type.instance)
                                       .addComposed("vtTo", 0, Int32Type.instance)
                                       .addComposed("ttFrom", 0, Int32Type.instance)
                                       .addComposed("ttTo", 5, Int32Type.instance);
        assertEquals("Wrong date parsing",
                     new BitemporalDateTime(5L),
                     mapper.readBitemporalDate(columns, "ttTo"));
    }

    @Test
    public void testReadTtToFieldFromLongColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", 0L, LongType.instance)
                                       .addComposed("vtTo", 0L, LongType.instance)
                                       .addComposed("ttFrom", 0L, LongType.instance)
                                       .addComposed("ttTo", 5L, LongType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "ttTo"));
    }

    @Test
    public void testReadTtToFieldFromTimeUUIDColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", UUIDGen.getTimeUUID(0L), TimeUUIDType.instance)
                                       .addComposed("vtTo", UUIDGen.getTimeUUID(0L), TimeUUIDType.instance)
                                       .addComposed("ttFrom", UUIDGen.getTimeUUID(0L), TimeUUIDType.instance)
                                       .addComposed("ttTo", UUIDGen.getTimeUUID(5L), TimeUUIDType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "ttTo"));
    }

    @Test
    public void testReadTtToFieldFromIntegerColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", BigInteger.valueOf(0), IntegerType.instance)
                                       .addComposed("vtTo", BigInteger.valueOf(0), IntegerType.instance)
                                       .addComposed("ttFrom", BigInteger.valueOf(0), IntegerType.instance)
                                       .addComposed("ttTo", BigInteger.valueOf(5), IntegerType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "ttTo"));
    }

    @Test
    public void testReadTtToFieldFromSimpleDateColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns();
        ByteBuffer bb = SimpleDateType.instance.fromString("2015-10-10");
        columns.addDecomposed("ttTo", bb, SimpleDateType.instance);
        assertEquals("Wrong date parsing",
                     new BitemporalDateTime(1444435200000L),
                     mapper.readBitemporalDate(columns, "ttTo"));
    }

    @Test
    public void testReadTtToFieldFromFloatColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", 0.0f, FloatType.instance)
                                       .addComposed("vtTo", 0.0f, FloatType.instance)
                                       .addComposed("ttFrom", 0.0f, FloatType.instance)
                                       .addComposed("ttTo", 5.0f, FloatType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "ttTo"));
    }

    @Test
    public void testReadTtToFieldFromDoubleColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", 0.0, DoubleType.instance)
                                       .addComposed("vtTo", 0.0, DoubleType.instance)
                                       .addComposed("ttFrom", 0.0, DoubleType.instance)
                                       .addComposed("ttTo", 5.0, DoubleType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "ttTo"));
    }

    @Test
    public void testReadTtToFieldFromDecimalColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", BigDecimal.valueOf(0.0), DecimalType.instance)
                                       .addComposed("vtTo", BigDecimal.valueOf(0.0), DecimalType.instance)
                                       .addComposed("ttFrom", BigDecimal.valueOf(0.0), DecimalType.instance)
                                       .addComposed("ttTo", BigDecimal.valueOf(5.0), DecimalType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(5L), mapper.readBitemporalDate(columns, "ttTo"));
    }

    @Test
    public void testReadTtToFieldFromAsciiColumn() throws ParseException {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("yyyy/MM/dd HH:mm:ss")
                                                                                      .nowValue("2025/12/23 00:00:00")
                                                                                      .build("f");
        Columns columns = new Columns().addComposed("vtFrom", "2015/03/24 11:15:14", AsciiType.instance)
                                       .addComposed("vtTo", "2015/03/24 11:15:14", AsciiType.instance)
                                       .addComposed("ttFrom", "2015/03/24 11:15:14", AsciiType.instance)
                                       .addComposed("ttTo", "2015/03/24 11:15:14", AsciiType.instance);
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = format.parse("2015/03/24 11:15:14");
        assertEquals("Wrong date parsing", new BitemporalDateTime(date), mapper.readBitemporalDate(columns, "ttTo"));
    }

    @Test
    public void testReadTtToFieldFromUTF8Column() throws ParseException {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("yyyy/MM/dd HH:mm:ss")
                                                                                      .nowValue("2025/12/23 00:00:00")
                                                                                      .build("f");
        Columns columns = new Columns().addComposed("vtFrom", "2015/03/24 11:15:14", UTF8Type.instance)
                                       .addComposed("vtTo", "2015/03/24 11:15:14", UTF8Type.instance)
                                       .addComposed("ttFrom", "2015/03/24 11:15:14", UTF8Type.instance)
                                       .addComposed("ttTo", "2015/03/24 11:15:14", UTF8Type.instance);
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = format.parse("2015/03/24 11:15:14");
        assertEquals("Wrong date parsing", new BitemporalDateTime(date), mapper.readBitemporalDate(columns, "ttTo"));
    }

    @Test
    public void testReadTtToFieldFromTimeStampColumn() throws ParseException {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("yyyy/MM/dd HH:mm:ss")
                                                                                      .nowValue("2025/12/23 00:00:00")
                                                                                      .build("f");
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = format.parse("2015/03/24 11:15:14");
        Columns columns = new Columns().addComposed("vtFrom", date, TimestampType.instance)
                                       .addComposed("vtTo", date, TimestampType.instance)
                                       .addComposed("ttFrom", date, TimestampType.instance)
                                       .addComposed("ttTo", date, TimestampType.instance);
        assertEquals("Wrong date parsing", new BitemporalDateTime(date), mapper.readBitemporalDate(columns, "ttTo"));
    }

    @Test
    public void testGetVtFromStringColumnWithDefaultPattern() throws ParseException {
        String pattern = DateParser.DEFAULT_PATTERN;
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date expectedDate = sdf.parse("2015/02/28 01:02:03.004 GMT");
        BitemporalMapper mapper = bitemporalMapper("vt_from", "vt_to", "tt_from", "tt_to").build("f");
        Columns columns = new Columns().addComposed("vt_from", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("vt_to", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("tt_from", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("tt_to", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance);
        assertEquals("Wrong date parsing", expectedDate, mapper.readBitemporalDate(columns, "vt_from").toDate());
    }

    @Test
    public void testGetVtFromStringColumnWithCustomPattern() throws ParseException {
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date expectedDate = sdf.parse("2015-02-28");
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("yyyy-MM-dd")
                                                                                      .nowValue("2025-12-23 00:00:00")
                                                                                      .build("f");
        Columns columns = new Columns().addComposed("vtFrom", "2015-02-28", UTF8Type.instance)
                                       .addComposed("vtTo", "2015-02-28", UTF8Type.instance)
                                       .addComposed("ttFrom", "2015-02-28", UTF8Type.instance)
                                       .addComposed("ttTo", "2015-02-28", UTF8Type.instance);
        assertEquals("Wrong date parsing", expectedDate, mapper.readBitemporalDate(columns, "vtFrom").toDate());
    }

    @Test(expected = IndexException.class)
    public void testGetVtFromFromUnparseableStringColumn() {
        BitemporalMapper mapper = bitemporalMapper("vt_from", "vt_to", "tt_from", "tt_to").build("f");
        Columns columns = new Columns().addComposed("vt_from", "abc", UTF8Type.instance)
                                       .addComposed("vt_to", "0673679", UTF8Type.instance)
                                       .addComposed("tt_from", "abc", UTF8Type.instance)
                                       .addComposed("tt_to", "8947597", UTF8Type.instance);
        mapper.readBitemporalDate(columns, "vt_from");
    }

    @Test
    public void testGetVtFromWithNullColumn() {
        BitemporalMapper mapper = bitemporalMapper("vt_from", "vt_to", "tt_from", "tt_to").build("f");
        Columns columns = new Columns().addComposed("vt_to", -1, Int32Type.instance)
                                       .addComposed("tt_from", -1, Int32Type.instance)
                                       .addComposed("tt_to", -1, Int32Type.instance);
        assertNull("Wrong date parsing", mapper.readBitemporalDate(columns, "vt_from"));
    }

    @Test(expected = IndexException.class)
    public void testGetVtFromWithNegativeColumn() {
        BitemporalMapper mapper = bitemporalMapper("vt_from", "vt_to", "tt_from", "tt_to").build("f");
        Columns columns = new Columns().addComposed("vt_from", -1, Int32Type.instance)
                                       .addComposed("vt_to", -1, Int32Type.instance)
                                       .addComposed("tt_from", -1, Int32Type.instance)
                                       .addComposed("tt_to", -1, Int32Type.instance);
        mapper.readBitemporalDate(columns, "vt_from");
    }

    @Test
    public void testGetVtToStringColumnWithDefaultPattern() throws ParseException {
        String pattern = DateParser.DEFAULT_PATTERN;
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date expectedDate = sdf.parse("2015/02/28 01:02:03.004 GMT");
        BitemporalMapper mapper = bitemporalMapper("vt_from", "vt_to", "tt_from", "tt_to").build("f");
        Columns columns = new Columns().addComposed("vt_from", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("vt_to", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("tt_from", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("tt_to", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance);
        assertEquals("Wrong date parsing", expectedDate, mapper.readBitemporalDate(columns, "vt_to").toDate());
    }

    @Test
    public void testGetVtToStringColumnWithCustomPattern() throws ParseException {
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date expectedDate = sdf.parse("2015-02-28");
        BitemporalMapper mapper = bitemporalMapper("vt_from", "vt_to", "tt_from", "tt_to").pattern("yyyy-MM-dd")
                                                                                          .nowValue("2025-12-23")
                                                                                          .build("f");
        Columns columns = new Columns().addComposed("vt_from", "2015-02-28", UTF8Type.instance)
                                       .addComposed("vt_to", "2015-02-28", UTF8Type.instance)
                                       .addComposed("tt_from", "2015-02-28", UTF8Type.instance)
                                       .addComposed("tt_to", "2015-02-28", UTF8Type.instance);
        assertEquals("Wrong date parsing", expectedDate, mapper.readBitemporalDate(columns, "vt_to").toDate());
    }

    @Test(expected = IndexException.class)
    public void testGetVtToFromUnparseableStringColumn() {
        BitemporalMapper mapper = bitemporalMapper("vt_from", "vt_to", "tt_from", "tt_to").build("f");
        Columns columns = new Columns().addComposed("vt_from", "abc", UTF8Type.instance)
                                       .addComposed("vt_to", "0673679", UTF8Type.instance)
                                       .addComposed("tt_from", "abc", UTF8Type.instance)
                                       .addComposed("tt_to", "8947597", UTF8Type.instance);
        mapper.readBitemporalDate(columns, "vt_to");
    }

    @Test
    public void testGetVtToWithNullColumn() {
        BitemporalMapper mapper = bitemporalMapper("vt_from", "vt_to", "tt_from", "tt_to").build("f");
        Columns columns = new Columns().addComposed("vt_from", -1, Int32Type.instance)
                                       .addComposed("tt_from", -1, Int32Type.instance)
                                       .addComposed("tt_to", -1, Int32Type.instance);
        assertNull("Wrong date parsing", mapper.readBitemporalDate(new Columns(), "vt_to"));
    }

    @Test
    public void testGetTtFromStringColumnWithDefaultPattern() throws ParseException {
        String pattern = DateParser.DEFAULT_PATTERN;
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date expectedDate = sdf.parse("2015/02/28 01:02:03.004 GMT");
        BitemporalMapper mapper = bitemporalMapper("vt_from", "vt_to", "tt_from", "tt_to").build("f");
        Columns columns = new Columns().addComposed("vt_from", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("vt_to", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("tt_from", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("tt_to", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance);
        assertEquals("Wrong date parsing", expectedDate, mapper.readBitemporalDate(columns, "tt_from").toDate());
    }

    @Test
    public void testGetTtFromStringColumnWithCustomPattern() throws ParseException {
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date expectedDate = sdf.parse("2015-02-28");
        BitemporalMapper mapper = bitemporalMapper("vt_from", "vt_to", "tt_from", "tt_to").pattern("yyyy-MM-dd")
                                                                                          .nowValue("2025-12-23")
                                                                                          .build("f");
        Columns columns = new Columns().addComposed("vt_from", "2015-02-28", UTF8Type.instance)
                                       .addComposed("vt_to", "2015-02-28", UTF8Type.instance)
                                       .addComposed("tt_from", "2015-02-28", UTF8Type.instance)
                                       .addComposed("tt_to", "2015-02-28", UTF8Type.instance);
        assertEquals("Wrong date parsing", expectedDate, mapper.readBitemporalDate(columns, "tt_from").toDate());
    }

    @Test(expected = IndexException.class)
    public void testGetTtFromFromUnparseableStringColumn() {
        BitemporalMapper mapper = bitemporalMapper("vt_from", "vt_to", "tt_from", "tt_to").build("f");
        Columns columns = new Columns().addComposed("vt_from", "abc", UTF8Type.instance)
                                       .addComposed("vt_to", "0673679", UTF8Type.instance)
                                       .addComposed("tt_from", "abc", UTF8Type.instance)
                                       .addComposed("tt_to", "8947597", UTF8Type.instance);
        mapper.readBitemporalDate(columns, "tt_from");
    }

    @Test
    public void testGetTtFromWithNullColumn() {
        BitemporalMapper mapper = bitemporalMapper("vt_from", "vt_to", "tt_from", "tt_to").build("f");
        Columns columns = new Columns().addComposed("vt_from", -1, Int32Type.instance)
                                       .addComposed("vt_to", -1, Int32Type.instance)
                                       .addComposed("tt_to", -1, Int32Type.instance);
        assertNull("Wrong date parsing", mapper.readBitemporalDate(new Columns(), "tt_from"));
    }

    @Test
    public void testGetTtToStringColumnWithDefaultPattern() throws ParseException {
        String pattern = DateParser.DEFAULT_PATTERN;
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date expectedDate = sdf.parse("2015/02/28 01:02:03.004 GMT");
        BitemporalMapper mapper = bitemporalMapper("vt_from", "vt_to", "tt_from", "tt_to").build("f");
        Columns columns = new Columns().addComposed("vt_from", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("vt_to", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("tt_from", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("tt_to", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance);
        assertEquals("Wrong date parsing", expectedDate, mapper.readBitemporalDate(columns, "tt_to").toDate());
    }

    @Test
    public void testGetTtToStringColumnWithCustomPattern() throws ParseException {
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date expectedDate = sdf.parse("2015-02-28");
        BitemporalMapper mapper = bitemporalMapper("vt_from", "vt_to", "tt_from", "tt_to").pattern("yyyy-MM-dd")
                                                                                          .nowValue("2025-12-23")
                                                                                          .build("f");
        Columns columns = new Columns().addComposed("vt_from", "2015-02-28", UTF8Type.instance)
                                       .addComposed("vt_to", "2015-02-28", UTF8Type.instance)
                                       .addComposed("tt_from", "2015-02-28", UTF8Type.instance)
                                       .addComposed("tt_to", "2015-02-28", UTF8Type.instance);
        assertEquals("Wrong date parsing", expectedDate, mapper.readBitemporalDate(columns, "tt_to").toDate());
    }

    @Test(expected = IndexException.class)
    public void testGetTtToFromUnparseableStringColumn() {
        BitemporalMapper mapper = bitemporalMapper("vt_from", "vt_to", "tt_from", "tt_to").build("f");
        Columns columns = new Columns().addComposed("vt_from", "abc", UTF8Type.instance)
                                       .addComposed("vt_to", "0673679", UTF8Type.instance)
                                       .addComposed("tt_from", "abc", UTF8Type.instance)
                                       .addComposed("tt_to", "8947597", UTF8Type.instance);
        mapper.readBitemporalDate(columns, "tt_to");
    }

    @Test
    public void testGetTtToWithNullColumn() {
        BitemporalMapper mapper = bitemporalMapper("vt_from", "vt_to", "tt_from", "tt_to").build("f");
        Columns columns = new Columns().addComposed("vt_from", -1, Int32Type.instance)
                                       .addComposed("vt_to", -1, Int32Type.instance)
                                       .addComposed("tt_from", -1, Int32Type.instance);
        assertNull("Wrong date parsing", mapper.readBitemporalDate(new Columns(), "tt_to"));
    }

    @Test(expected = IndexException.class)
    public void testContructBitemporalVtToSmallerThanVtFromFromLongColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", 5L, LongType.instance)
                                       .addComposed("vtTo", 0L, LongType.instance)
                                       .addComposed("ttFrom", 0L, LongType.instance)
                                       .addComposed("ttTo", 0L, LongType.instance);
        Document document = new Document();
        mapper.addFields(document, columns);
    }

    @Test(expected = IndexException.class)
    public void testContructBitemporalTtToSmallerThanTtFromFromLongColumn() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("timestamp").build("f");
        Columns columns = new Columns().addComposed("vtFrom", 0L, LongType.instance)
                                       .addComposed("vtTo", 0L, LongType.instance)
                                       .addComposed("ttFrom", 5L, LongType.instance)
                                       .addComposed("ttTo", 0L, LongType.instance);
        Document document = new Document();
        mapper.addFields(document, columns);
    }

    @Test(expected = IndexException.class)
    public void testSortField() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").build("f");
        mapper.sortField("f", false);
    }

    private void testAddFieldsOnlyThese(Document doc,
                                        String[] wishedIndexedFieldNames,
                                        String[] nonWishedIndexedFieldNames) {
        for (String wishedIndexedFieldName : wishedIndexedFieldNames) {
            IndexableField[] indexableFields = doc.getFields(wishedIndexedFieldName);
            assertEquals("Add fields is wrong", 1, indexableFields.length);
            assertTrue("Add fields is wrong", indexableFields[0] instanceof Field);
            assertEquals("Add fields is wrong", wishedIndexedFieldName, indexableFields[0].name());
        }
        for (String nonWishedIndexedFieldName : nonWishedIndexedFieldNames) {
            IndexableField[] indexableFields = doc.getFields(nonWishedIndexedFieldName);
            assertEquals("Add fields is wrong", 0, indexableFields.length);
        }
    }

    @Test
    public void testAddFieldsT1() {
        String nowValue = "2100/01/01 00:00:00.001 GMT";
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").nowValue(nowValue).build("f");
        Columns columns = new Columns().addComposed("vtFrom", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("vtTo", nowValue, UTF8Type.instance)
                                       .addComposed("ttFrom", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("ttTo", nowValue, UTF8Type.instance);
        Document document = new Document();
        mapper.addFields(document, columns);
        testAddFieldsOnlyThese(document, new String[]{"f.ttFrom", "f.ttTo", "f.vtFrom", "f.vtTo"}, new String[0]);
    }

    @Test
    public void testAddFieldsT2() {
        String nowValue = "2100/01/01 00:00:00.000 GMT";
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").nowValue(nowValue).build("f");
        Columns columns = new Columns().addComposed("vtFrom", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("vtTo", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("ttFrom", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("ttTo", nowValue, UTF8Type.instance);
        Document document = new Document();
        mapper.addFields(document, columns);
        testAddFieldsOnlyThese(document, new String[]{"f.ttFrom", "f.ttTo", "f.vtFrom", "f.vtTo"}, new String[0]);
    }

    @Test
    public void testAddFieldsT3() {
        String nowValue = "2100/01/01 00:00:00.000 GMT";
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").nowValue(nowValue).build("f");
        Columns columns = new Columns().addComposed("vtFrom", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("vtTo", nowValue, UTF8Type.instance)
                                       .addComposed("ttFrom", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("ttTo", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance);
        Document document = new Document();
        mapper.addFields(document, columns);
        testAddFieldsOnlyThese(document, new String[]{"f.ttFrom", "f.ttTo", "f.vtFrom", "f.vtTo"}, new String[0]);
    }

    @Test
    public void testAddFieldsT4() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").build("f");
        Columns columns = new Columns().addComposed("vtFrom", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("vtTo", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("ttFrom", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("ttTo", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance);
        Document document = new Document();
        mapper.addFields(document, columns);
        testAddFieldsOnlyThese(document, new String[]{"f.ttFrom", "f.ttTo", "f.vtFrom", "f.vtTo"}, new String[0]);
    }

    @Test
    public void testAddFieldsAllNull() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").build("f");
        Columns columns = new Columns();
        Document document = new Document();
        mapper.addFields(document, columns);
        assertEquals("Null columns should produce no fields", 0, document.getFields().size());
    }

    @Test(expected = IndexException.class)
    public void testAddFieldsVtFromNull() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").build("f");
        Columns columns = new Columns().addComposed("vtTo", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("ttFrom", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("ttTo", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance);
        mapper.addFields(new Document(), columns);
    }

    @Test(expected = IndexException.class)
    public void testAddFieldsVtToNull() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").build("f");
        Columns columns = new Columns().addComposed("vtFrom", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("ttFrom", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("ttTo", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance);
        mapper.addFields(new Document(), columns);
    }

    @Test(expected = IndexException.class)
    public void testAddFieldsTtFromNull() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").build("f");
        Columns columns = new Columns().addComposed("vtFrom", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("vtTo", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("ttTo", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance);
        mapper.addFields(new Document(), columns);
    }

    @Test(expected = IndexException.class)
    public void testAddFieldsTtToNull() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").build("f");
        Columns columns = new Columns().addComposed("vtFrom", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("vtTo", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("ttFrom", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance);
        mapper.addFields(new Document(), columns);
    }

    @Test(expected = IndexException.class)
    public void testAddFieldsVtFromAfterVtToNull() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").build("f");
        Columns columns = new Columns().addComposed("vtFrom", "2015/02/28 01:02:03.005 GMT", UTF8Type.instance)
                                       .addComposed("vtTo", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("ttFrom", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("ttTo", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance);
        mapper.addFields(new Document(), columns);
    }

    @Test(expected = IndexException.class)
    public void testAddFieldsTtFromAfterTtToNull() {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").build("f");
        Columns columns = new Columns().addComposed("vtFrom", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("vtTo", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance)
                                       .addComposed("ttFrom", "2015/02/28 01:02:03.005 GMT", UTF8Type.instance)
                                       .addComposed("ttTo", "2015/02/28 01:02:03.004 GMT", UTF8Type.instance);
        mapper.addFields(new Document(), columns);
    }

    @Test
    public void testExtractAnalyzers() throws ParseException {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").build("f");
        assertNull("Analyzer should be null", mapper.analyzer);
    }

    @Test
    public void testToString() throws ParseException {
        BitemporalMapper mapper = bitemporalMapper("vtFrom", "vtTo", "ttFrom", "ttTo").pattern("yyyy/MM/dd")
                                                                                      .nowValue("2025/12/23")
                                                                                      .build("f");
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date date = format.parse("2025/12/23");
        String exp = "BitemporalMapper{field=f, validated=false, vtFrom=vtFrom, vtTo=vtTo, ttFrom=ttFrom, " +
                     "ttTo=ttTo, pattern=yyyy/MM/dd, nowValue=" + date.getTime() + "}";
        assertEquals("Method #toString is wrong", exp, mapper.toString());
    }
}
