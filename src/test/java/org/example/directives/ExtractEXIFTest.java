/*
 *  Copyright © 2017 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package org.example.directives;

import co.cask.directives.ExtractEXIF;
import co.cask.wrangler.api.RecipePipeline;
import co.cask.wrangler.api.Row;
import co.cask.wrangler.test.TestingRig;
import co.cask.wrangler.test.api.TestRecipe;
import co.cask.wrangler.test.api.TestRows;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Tests {@link ExtractEXIF}
 */
public class ExtractEXIFTest {

  @Ignore
  @Test
  public void testBasicReverse() throws Exception {
    TestRecipe recipe = new TestRecipe();
    recipe.add("exif :body;");
    String fileName = "10x12x16bit-CMYK.psd";
    ClassLoader classLoader = getClass().getClassLoader();
    URL imageFile = classLoader.getResource(fileName);
    Path path = Paths.get(imageFile.getPath());

    TestRows rows = new TestRows();
    rows.add(new Row("body", Files.readAllBytes(path)));

    RecipePipeline pipeline = TestingRig.pipeline(ExtractEXIF.class, recipe);
    List<Row> actual = pipeline.execute(rows.toList());

    Assert.assertEquals(1, actual.size());
    Assert.assertEquals(73, actual.get(0).getFields().size());
  }
}