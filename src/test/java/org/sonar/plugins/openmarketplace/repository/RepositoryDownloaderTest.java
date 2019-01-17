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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class RepositoryDownloaderTest {

  @Test
  void testDownloadOriginalOk() throws IOException {
    final RepositoryDescription originalRepo = RepositoryDownloader.downloadOriginal();

    assertThat(originalRepo.getPluginIDs()).contains("java");
    assertThat(originalRepo.getPluginProperties()).isNotEmpty();
  }

  @Test
  void testDownloadWrongUrl() throws IOException {

    assertThrows(IOException.class, () -> {
      RepositoryDownloader.download("https://upd473.50n4r50urc3.0r6/update-center.properties");
    });

  }

}
