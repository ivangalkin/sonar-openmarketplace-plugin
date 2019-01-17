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
package org.sonar.plugins.openmarketplace;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;

import org.sonar.api.config.Configuration;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;
import org.sonar.api.utils.HttpDownloader;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.openmarketplace.repository.RepositoryDescription;
import org.sonar.plugins.openmarketplace.repository.RepositoryMerger;
import org.sonar.plugins.openmarketplace.repository.RepositoryValidationException;

public class OpenMarketplaceRequestHandler implements RequestHandler {

  private static final Logger LOG = Loggers.get(OpenMarketplaceRequestHandler.class);
  private static final int SC_MOVED_TEMPORARILY = 302;

  private final Configuration configuration;
  private final HttpDownloader downloader;

  public OpenMarketplaceRequestHandler(Configuration c, HttpDownloader d) {
    configuration = c;
    downloader = d;
  }

  private RepositoryDescription download(String url) throws IOException, URISyntaxException {
    final URI uri = new URI(url);
    final String encoding = StandardCharsets.UTF_8.name();
    String content = null;
    try {
      content = downloader.downloadPlainText(uri, encoding);
    } catch (Exception e) {
      throw new IOException(e);
    }
    return new RepositoryDescription(content, url);
  }

  private Optional<String> mergeAll() throws IOException {
    LOG.debug("Download original repository: {}", OpenMarketplaceTrustedRepositories.ORIGINAL_REPOSITORY_URL);
    RepositoryDescription original = null;
    try {
      original = download(OpenMarketplaceTrustedRepositories.ORIGINAL_REPOSITORY_URL);
    } catch (IOException | URISyntaxException ioe) {
      LOG.error("Unable to download original repository {}, exception {}",
          OpenMarketplaceTrustedRepositories.ORIGINAL_REPOSITORY_URL, ioe.getMessage());
      return Optional.empty();
    }

    final RepositoryMerger merger = new RepositoryMerger(original);

    for (String urlProperty : configuration.getStringArray(OpenMarketplacePlugin.SONAR_OPENMARKETPLACE_URLS)) {
      final String customURL = urlProperty.trim();
      if (customURL.isEmpty()) {
        continue;
      }

      try {
        LOG.debug("Append custom repository {}", customURL);

        final RepositoryDescription custom = download(customURL);
        custom.validate();
        merger.addCustomRepository(custom);

        final String customPlugins = custom.getPluginIDs().stream().collect(Collectors.joining(","));
        LOG.info("Custom repository was added url='{}' plugins=[{}] ", customURL, customPlugins);
      } catch (IOException | URISyntaxException ioe) {
        LOG.warn("Unable to download custom repository {}, exception {}", customURL, ioe.getMessage());
      } catch (RepositoryValidationException rve) {
        LOG.warn("Unable to validate custom repository {}, exception {}", customURL, rve.getMessage());
      }
    }

    return Optional.of(merger.merge());
  }

  @Override
  public void handle(Request request, Response response) throws Exception {

    Optional<String> mergedRepositories = Optional.empty();
    try {
      mergedRepositories = mergeAll();
    } catch (Exception e) {
      LOG.error("Unexpected exception while downloading and merging {}", e);
    }

    if (mergedRepositories.isPresent()) {
      try (OutputStream stream = response.stream().output()) {
        response.setHeader("Content-Type", "text/plain; charset=ISO-8859-1");
        stream.write(mergedRepositories.get().getBytes("ISO-8859-1"));
      }
    } else {
      response.stream().setStatus(SC_MOVED_TEMPORARILY);
      response.setHeader("Location", OpenMarketplaceTrustedRepositories.ORIGINAL_REPOSITORY_URL);
    }

  }

}