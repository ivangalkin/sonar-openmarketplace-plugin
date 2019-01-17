/*
 * Sonar Open Marketplace
 * Copyright (C) 2018-2018 ivangalkin
 * http://github.com/ivangalkin
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.openmarketplace.repository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class RepositoryDownloader {

  public static final String ORIGINAL_REPOSITORY_URL = "https://update.sonarsource.org/update-center.properties";

  private RepositoryDownloader() {
  }

  public static RepositoryDescription download(String url) throws IOException {

    try (final CloseableHttpClient client = HttpClients.createDefault();
        final CloseableHttpResponse response = client.execute(new HttpGet(url));
        final ByteArrayOutputStream rawContent = new ByteArrayOutputStream()) {

      final HttpEntity entity = response.getEntity();
      entity.writeTo(rawContent);

      final ContentType contentType = ContentType.getOrDefault(entity);
      final Charset contentCharset = (contentType.getCharset() == null) ? ContentType.TEXT_PLAIN.getCharset()
          : contentType.getCharset();
      final String content = rawContent.toString(contentCharset.name());

      return new RepositoryDescription(content, url);
    }

  }

  public static RepositoryDescription downloadOriginal() throws IOException {
    return download(ORIGINAL_REPOSITORY_URL);
  }
}