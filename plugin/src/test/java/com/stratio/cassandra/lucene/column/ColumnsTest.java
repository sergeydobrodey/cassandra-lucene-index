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

import org.apache.cassandra.db.marshal.UTF8Type;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link Columns}.
 *
 * @author Andres de la Pena {@literal <adelapena@stratio.com>}
 */
public class ColumnsTest {

    @Test
    public void testBuild() {
        Columns columns = new Columns();
        assertEquals(0, columns.size());
    }

    @Test
    public void testAdd() {
        Columns columns = new Columns().addComposed("f1", "v1", UTF8Type.instance)
                                       .addComposed("f2", "v2", UTF8Type.instance);
        assertEquals("Columns size is wrong", 2, columns.size());
    }

    @Test
    public void testAddAll() {
        Columns columns = new Columns().addComposed("f1", "v1", UTF8Type.instance)
                                       .addComposed("f2", "v2", UTF8Type.instance);
        assertEquals("Columns size is wrong", 2, columns.size());
    }

    @Test
    public void testIterator() {
        Columns columns = new Columns().addComposed("f1", "v1", UTF8Type.instance)
                                       .addComposed("f2", "v2", UTF8Type.instance);
        List<Column<?>> list = new ArrayList<>();
        for (Column<?> column : columns) {
            list.add(column);
        }
        assertEquals("Columns size is wrong", 2, list.size());
        assertEquals("Columns order is wrong", "f1", list.get(0).getCellName());
        assertEquals("Columns order is wrong", "f2", list.get(1).getCellName());
    }

    @Test
    public void testStream() {
        List<Column<?>> list = new Columns().addComposed("f1", "v1", UTF8Type.instance)
                                            .addComposed("f2", "v2", UTF8Type.instance)
                                            .stream().collect(Collectors.toList());
        assertEquals("Columns size is wrong", 2, list.size());
        assertEquals("Columns order is wrong", "f1", list.get(0).getCellName());
        assertEquals("Columns order is wrong", "f2", list.get(1).getCellName());
    }

    @Test
    public void testGetColumnsByCellName() {
        Columns columns = new Columns();
        columns.adder("f1").addComposed("v", UTF8Type.instance)
               .adder("f1").withUDTName("1").addComposed("v", UTF8Type.instance)
               .adder("f1").withMapName("1").addComposed("v", UTF8Type.instance)
               .adder("f1").withUDTName("1").withMapName("1").addComposed("v", UTF8Type.instance);
        assertEquals("Columns size is wrong", 4, columns.getByCellName("f1").size());
        assertEquals("Columns size is wrong", 4, columns.getByCellName("f1.1").size());
        assertEquals("Columns size is wrong", 4, columns.getByCellName("f1$1").size());
        assertEquals("Columns size is wrong", 4, columns.getByCellName("f1.1$1$2").size());
        assertEquals("Columns size is wrong", 4, columns.getByCellName("f1.2").size());
        assertEquals("Columns size is wrong", 0, columns.getByCellName("f2").size());
    }

    @Test
    public void testGetColumnsByFullName() {
        Columns columns = new Columns();
        columns.adder("f1").addComposed("v", UTF8Type.instance)
               .adder("f1").withUDTName("1").addComposed("v", UTF8Type.instance)
               .adder("f1").withMapName("1").addComposed("v", UTF8Type.instance)
               .adder("f1").withUDTName("1").withMapName("1").addComposed("v", UTF8Type.instance)
               .adder("f1").withUDTName("1").withUDTName("1").addComposed("v", UTF8Type.instance)
               .adder("f1").withMapName("1").withMapName("1").addComposed("v", UTF8Type.instance);
        assertEquals("Columns size is wrong", 1, columns.getByFullName("f1").size());
        assertEquals("Columns size is wrong", 1, columns.getByFullName("f1.1").size());
        assertEquals("Columns size is wrong", 1, columns.getByFullName("f1.1.1").size());
        assertEquals("Columns size is wrong", 1, columns.getByFullName("f1$1").size());
        assertEquals("Columns size is wrong", 1, columns.getByFullName("f1.1$1").size());
        assertEquals("Columns size is wrong", 1, columns.getByFullName("f1$1$1").size());
        assertEquals("Columns size is wrong", 0, columns.getByFullName("f2").size());
    }

    @Test
    public void testGetColumnsByMapperName() {
        Columns columns = new Columns();
        columns.addComposed("f1", "v", UTF8Type.instance)
               .adder("f1").withUDTName("1").addComposed("v", UTF8Type.instance)
               .adder("f1").withMapName("1").addComposed("v", UTF8Type.instance)
               .adder("f1").withUDTName("1").withMapName("1").addComposed("v", UTF8Type.instance)
               .adder("f1").withUDTName("1").withUDTName("1").addComposed("v", UTF8Type.instance)
               .adder("f1").withMapName("1").withMapName("1").addComposed("v", UTF8Type.instance);
        assertEquals("Columns size is wrong", 3, columns.getByMapperName("f1").size());
        assertEquals("Columns size is wrong", 2, columns.getByMapperName("f1.1").size());
        assertEquals("Columns size is wrong", 3, columns.getByMapperName("f1$1").size());
        assertEquals("Columns size is wrong", 2, columns.getByMapperName("f1.1$2").size());
        assertEquals("Columns size is wrong", 1, columns.getByMapperName("f1.1.1").size());
        assertEquals("Columns size is wrong", 0, columns.getByMapperName("f2").size());
    }

    @Test
    public void testToStringEmpty() {
        assertEquals("Method #toString is wrong", "Columns{}", new Columns().toString());
    }

    @Test
    public void testToStringWithColumns() {
        Columns columns = new Columns();
        columns.adder("f1").addComposed("v", UTF8Type.instance)
               .adder("f1").withUDTName("1").addComposed("v", UTF8Type.instance)
               .adder("f1").withMapName("1").addComposed("v", UTF8Type.instance)
               .adder("f1").withUDTName("1").withMapName("1").addComposed("v", UTF8Type.instance)
               .adder("f1").withUDTName("1").withUDTName("1").addComposed("v", UTF8Type.instance)
               .adder("f1").withMapName("1").withMapName("1").addComposed("v", UTF8Type.instance);
        assertEquals("Method #toString is wrong",
                     "Columns{f1=v, f1.1=v, f1$1=v, f1.1$1=v, f1.1.1=v, f1$1$1=v}",
                     columns.toString());
    }

    @Test
    public void testCleanDeleted() {
        Columns columns = new Columns().adder("f1", 1).addComposed("v1", UTF8Type.instance)
                                       .adder("f2", 2).addComposed("v2", UTF8Type.instance)
                                       .adder("f3", 3).addComposed("v3", UTF8Type.instance)
                                       .adder("f4", 4).addComposed("v4", UTF8Type.instance);
        columns = columns.cleanDeleted(3);
        assertEquals("Columns clean deleted is wrong", 1, columns.size());
        assertEquals("Columns clean deleted is wrong", 1, columns.getByMapperName("f4").size());
    }
}
