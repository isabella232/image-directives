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

import co.cask.cdap.api.annotation.Description;
import co.cask.cdap.api.annotation.Name;
import co.cask.cdap.api.annotation.Plugin;
import co.cask.wrangler.api.Arguments;
import co.cask.wrangler.api.Directive;
import co.cask.wrangler.api.DirectiveExecutionException;
import co.cask.wrangler.api.DirectiveParseException;
import co.cask.wrangler.api.ErrorRowException;
import co.cask.wrangler.api.ExecutorContext;
import co.cask.wrangler.api.Row;
import co.cask.wrangler.api.parser.ColumnName;
import co.cask.wrangler.api.parser.TokenType;
import co.cask.wrangler.api.parser.UsageDefinition;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * This class <code>ExtractEXIF</code>implements a <code>Directive</code> interface
 * for reversing the text specified by the value of the <code>column</code>.
 */
@Plugin(type = Directive.Type)
@Name(ExtractEXIF.DIRECTIVE_NAME)
@Description(ExtractEXIF.DIRECTIVE_DESC)
public final class ExtractEXIF implements Directive {
  public static final String DIRECTIVE_NAME = "exif";
  public static final String DIRECTIVE_DESC = "Extracts EXIF data from binary image files.";

  private String sourceColumn;

  @Override
  public UsageDefinition define() {
    // Usage : exif :binary-image-column;
    UsageDefinition.Builder builder = UsageDefinition.builder(DIRECTIVE_NAME);
    builder.define("image-column", TokenType.COLUMN_NAME);
    return builder.build();
  }

  @Override
  public void initialize(Arguments args)
    throws DirectiveParseException {
    sourceColumn = ((ColumnName) args.value("image-column")).value();
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context)
    throws DirectiveExecutionException, ErrorRowException {
    for (Row row : rows) {
      int sourceColumnIndex = row.find(sourceColumn);
      if (sourceColumnIndex != -1) {
        try {
          Object imageData = row.getValue(sourceColumnIndex);
          if (imageData instanceof byte[]) {
            Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream((byte[]) imageData));
            for (Directory directory : metadata.getDirectories()) {
              for (Tag tag : directory.getTags()) {
                StringBuilder tagName = new StringBuilder();
                tagName.append(directory.getName().replaceAll(" ", "_"))
                  .append("_")
                  .append(tag.getTagName().replaceAll(" ", "_"));
                String description = tag.getDescription();
                if (description == null) {
                  description = directory.getString(tag.getTagType()) + " (unable to formulate description)";
                }
                row.addOrSet(tagName.toString(), description);
              }
            }
          }
        } catch (Exception e) {

        }
      }
    }
    return rows;
  }

  @Override
  public void destroy() {
    // no op
  }
}
