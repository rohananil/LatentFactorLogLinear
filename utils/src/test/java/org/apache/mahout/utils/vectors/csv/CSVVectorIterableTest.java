package org.apache.mahout.utils.vectors.csv;
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.mahout.math.Vector;
import org.apache.mahout.utils.MahoutTestCase;
import org.apache.mahout.utils.vectors.RandomVectorIterable;
import org.apache.mahout.utils.vectors.VectorHelper;
import org.apache.mahout.utils.vectors.io.JWriterVectorWriter;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class CSVVectorIterableTest extends MahoutTestCase {

  @Test
  public void test() throws Exception {

    StringWriter sWriter = new StringWriter();
    JWriterVectorWriter jwvw = new JWriterVectorWriter(sWriter) {

      @Override
      protected void formatVector(Vector vector) throws IOException {
        String vecStr = VectorHelper.vectorToCSVString(vector, false);
        writer.write(vecStr);
      }
    };
    Iterable<Vector> iter = new RandomVectorIterable(50);
    jwvw.write(iter);
    jwvw.close();
    Iterable<Vector> csvIter = new CSVVectorIterable(new StringReader(sWriter.getBuffer().toString()));
    int count = 0;
    for (Vector vector : csvIter) {
      //System.out.println("Vec: " + vector);
      count++;
    }
    assertEquals(50, count);
  }
}
