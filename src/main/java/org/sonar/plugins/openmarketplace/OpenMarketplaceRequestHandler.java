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
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.openmarketplace.repository.RepositoryDescription;
import org.sonar.plugins.openmarketplace.repository.RepositoryDownloader;
import org.sonar.plugins.openmarketplace.repository.RepositoryMerger;
import org.sonar.plugins.openmarketplace.repository.RepositoryValidationException;

public class OpenMarketplaceRequestHandler implements RequestHandler {

  private static final Logger LOG = Loggers.get(OpenMarketplaceRequestHandler.class);
  private Configuration configuration;

  public OpenMarketplaceRequestHandler(Configuration c) {
    configuration = c;
  }

  private Optional<String> downloadAndMerge() throws IOException {
    LOG.debug("Download original repository: {}", RepositoryDownloader.ORIGINAL_REPOSITORY_URL);
    RepositoryDescription original = null;
    try {
      original = RepositoryDownloader.downloadOriginal();
    } catch (IOException ioe) {
      LOG.error("Unable to download original repository {}, exception {}", RepositoryDownloader.ORIGINAL_REPOSITORY_URL,
          ioe);
      return Optional.empty();
    }

    final RepositoryMerger merger = new RepositoryMerger(original);

    for (String customURL : configuration.getStringArray(OpenMarketplacePlugin.SONAR_OPENMARKETPLACE_URLS)) {
      try {
        LOG.debug("Append custom repository {}", customURL);

        final RepositoryDescription custom = RepositoryDownloader.download(customURL);
        custom.validate();
        merger.addCustomRepository(custom);

        if (LOG.isDebugEnabled()) {
          final String customPlugins = custom.getPluginIDs().stream().collect(Collectors.joining(","));
          LOG.debug("Custom plugins were added: [{}] ", customPlugins);
        }
      } catch (IOException ioe) {
        LOG.warn("Unable to download custom repository {}, exception {}", customURL, ioe);
      } catch (RepositoryValidationException rve) {
        LOG.warn("Unable to validate custom repository {}, exception {}", customURL, rve);
      }
    }

    return Optional.of(merger.merge());
  }

  @Override
  public void handle(Request request, Response response) throws Exception {

    Optional<String> mergedRepositories = Optional.empty();
    try {
      mergedRepositories = downloadAndMerge();
    } catch (Exception e) {
      LOG.error("Unexpected exception while downloading and merging {}", e);
    }

    if (mergedRepositories.isPresent()) {
      try (OutputStream stream = response.stream().output()) {
        response.setHeader("Content-Type", "text/plain; charset=ISO-8859-1");
        stream.write(mergedRepositories.get().getBytes("ISO-8859-1"));
      }
    } else {
      response.stream().setStatus(HttpStatus.SC_MOVED_TEMPORARILY);
      response.setHeader("Location", RepositoryDownloader.ORIGINAL_REPOSITORY_URL);
    }

  }

}