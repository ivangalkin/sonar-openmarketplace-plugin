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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.Test;

class RepositoryMergerTest {

  @Test
  void testMergeOk() throws IOException {
    // original repo: 2 plugins x 2 properties + plugins list
    final String originalFile = //
        "plugins=plugin0,plugin1\n" + //
            "plugin0.versions=1.3\n" + //
            "plugin0.homepageUrl=https\\://url.url\n" + //
            "plugin1.1.3.mavenGroupId=org.group\n" + //
            "plugin1.name=Name\n";

    // custom repo: 2 plugins x 2 properties + plugins list
    final String customFile = //
        "plugins=plugin2,plugin3\n" + //
            "plugin2.issueTrackerUrl=http\\://url.url\n" + //
            "plugin2.scm=https\\://url.url\n" + //
            "plugin3.description=Description\n" + //
            "plugin3.license=GNU LGPL 3\n";

    final RepositoryDescription originalDesc = new RepositoryDescription(
        new ByteArrayInputStream(originalFile.getBytes()), "http://original.com");
    final RepositoryDescription customDesc = new RepositoryDescription(new ByteArrayInputStream(customFile.getBytes()),
        "http://custom.com");

    final RepositoryMerger merger = new RepositoryMerger(originalDesc);
    merger.addCustomRepository(customDesc);
    final String result = merger.merge();
    final Properties prop = new Properties();
    prop.load(new ByteArrayInputStream(result.getBytes()));

    // expected total: 4 plugins x 2 properties + 1 plugins list
    assertThat(prop.keySet()).hasSize(9);

    assertThat(prop).containsOnlyKeys("plugins", "plugin0.versions", "plugin0.homepageUrl", "plugin1.1.3.mavenGroupId",
        "plugin1.name", "plugin2.issueTrackerUrl", "plugin2.scm", "plugin3.description", "plugin3.license");

    assertThat(prop.getProperty("plugins").split(",")).containsOnly("plugin0", "plugin1", "plugin2", "plugin3");

    assertThat(prop).containsEntry("plugin0.versions", "1.3");
    assertThat(prop).containsEntry("plugin0.homepageUrl", "https://url.url");
    assertThat(prop).containsEntry("plugin1.1.3.mavenGroupId", "org.group");
    assertThat(prop).containsEntry("plugin1.name", "Name");
    assertThat(prop).containsEntry("plugin2.issueTrackerUrl", "http://url.url");
    assertThat(prop).containsEntry("plugin2.scm", "https://url.url");
    assertThat(prop).containsEntry("plugin3.description", "Description");
    assertThat(prop).containsEntry("plugin3.license", "GNU LGPL 3");

  }

  @Test
  void testIntersectionIDs() {
    final String originalFile = //
        "plugins=plugin0,plugin1\n" + //
            "plugin0.versions=1.3\n" + //
            "plugin0.homepageUrl=https\\://url.url\n" + //
            "plugin1.1.3.mavenGroupId=org.group\n" + //
            "plugin1.name=Name\n";

    final String customFile = //
        "plugins=plugin2,plugin1\n" + //
            "plugin2.issueTrackerUrl=http\\://url.url\n" + //
            "plugin2.scm=https\\://url.url\n" + //
            "plugin1.description=Description\n" + //
            "plugin1.license=GNU LGPL 3\n";

    final RepositoryDescription originalDesc = new RepositoryDescription(
        new ByteArrayInputStream(originalFile.getBytes()), "http://original.com");
    final RepositoryDescription customDesc = new RepositoryDescription(new ByteArrayInputStream(customFile.getBytes()),
        "http://custom.com");

    final RepositoryMerger merger = new RepositoryMerger(originalDesc);

    final RepositoryValidationException e = assertThrows(RepositoryValidationException.class, () -> {
      merger.addCustomRepository(customDesc);
    });

    assertThat(e).hasMessage(
        "Custom repository 'http://custom.com' exports plugins [plugin1] which are already exported by the original repository");

  }

}
