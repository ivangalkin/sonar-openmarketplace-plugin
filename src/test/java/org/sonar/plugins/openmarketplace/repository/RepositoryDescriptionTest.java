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

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.openmarketplace.repository.RepositoryDescription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RepositoryDescriptionTest {

  @Test
  void testProperProperties() {
    final String file = //
        "key0=value0\n" + //
            "key1=value1\n" + //
            "key2=value2\n" + //
            "plugins=a,b,c,d,e,,,,,,, , f, g ,h , i   ,,,,\n" + //
            "key3=value3\n" + //
            "key4=\n";

    final RepositoryDescription desc = new RepositoryDescription(new ByteArrayInputStream(file.getBytes()),
        "http://repo.com");
    assertThat(desc.getUrl()).isEqualTo("http://repo.com");

    final Set<String> pluginIDs = desc.getPluginIDs();
    assertThat(pluginIDs).containsOnly("a", "b", "c", "d", "e", "f", "g", "h", "i");

    final String properties = desc.getPluginProperties();
    List<String> propertiesLines = Arrays.asList(properties.split("\\R"));
    assertThat(propertiesLines.stream().filter(s -> !s.startsWith("#"))).containsOnly("key0=value0", "key1=value1",
        "key2=value2", "key3=value3", "key4=");
    assertThat(propertiesLines).contains("#source: http://repo.com");
  }

  @Test
  void testMissingPlugin() {
    final String file = //
        "key0=value0\n" + //
            "key1=value1\n" + //
            "key2=value2\n" + //
            "key3=value3\n";

    final RepositoryValidationException e = assertThrows(RepositoryValidationException.class, () -> {
      new RepositoryDescription(new ByteArrayInputStream(file.getBytes()), "http://rep0.com");
    });

    assertThat(e).hasMessage("Repository 'http://rep0.com' doesn't contain a list of plugins (key = plugins)");
  }

  void testValidateOk0() {
    final String file = //
        "plugins=plugin0\n" + //
            "plugin0.versions=1.3\n" + //
            "plugin0.1.3.requiredSonarVersions=6.7.6,7.0,7.1,7.2,7.2.1,7.3,7.4,7.5\n" + //
            "plugin0.homepageUrl=https\\://url.url\n" + //
            "plugin0.issueTrackerUrl=http\\://url.url\n" + //
            "plugin0.scm=https\\://url.url\n" + //
            "plugin0.1.3.mavenGroupId=org.group\n" + //
            "plugin0.description=Description\n" + //
            "plugin0.license=GNU LGPL 3\n" + //
            "plugin0.1.3.description=Description\n" + //
            "plugin0.1.3.mavenArtifactId=sonar-plugin0-plugin\n" + //
            "plugin0.name=Name\n";
    new RepositoryDescription(new ByteArrayInputStream(file.getBytes()), "http://rep0.com").validate();
  }

  void testValidateOk1() {
    final String file = //
        "plugins=plugin0,plugin1\n" + //
            "plugin0.versions=1.3\n" + //
            "plugin0.1.3.requiredSonarVersions=6.7.6,7.0,7.1,7.2,7.2.1,7.3,7.4,7.5\n" + //
            "plugin0.homepageUrl=https\\://url.url\n" + //
            "plugin0.issueTrackerUrl=http\\://url.url\n" + //
            "plugin0.scm=https\\://url.url\n" + //
            "plugin1.1.3.mavenGroupId=org.group\n" + //
            "plugin1.description=Description\n" + //
            "plugin1.license=GNU LGPL 3\n" + //
            "plugin1.1.3.description=Description\n" + //
            "plugin1.1.3.mavenArtifactId=sonar-plugin0-plugin\n" + //
            "plugin1.name=Name\n";
    new RepositoryDescription(new ByteArrayInputStream(file.getBytes()), "http://rep0.com").validate();
  }

  @Test
  void testValidateEmptyPlugin0() {
    final String file = //
        "key0=value0\n" + //
            "key1=value1\n" + //
            "key2=value2\n" + //
            "key3=value3\n" + //
            "plugins=\n";

    RepositoryDescription desc = new RepositoryDescription(new ByteArrayInputStream(file.getBytes()),
        "http://rep0.com");

    final RepositoryValidationException e = assertThrows(RepositoryValidationException.class, () -> {
      desc.validate();
    });

    assertThat(e).hasMessage("Repository 'http://rep0.com'doesn't export any plugins, 'plugins=...' is empty");
  }

  @Test
  void testValidateEmptyPlugin1() {
    final String file = //
        "key0=value0\n" + //
            "key1=value1\n" + //
            "key2=value2\n" + //
            "key3=value3\n" + //
            "plugins=,,, , ,, , ,, \n";

    RepositoryDescription desc = new RepositoryDescription(new ByteArrayInputStream(file.getBytes()),
        "http://rep1.com");

    final RepositoryValidationException e = assertThrows(RepositoryValidationException.class, () -> {
      desc.validate();
    });

    assertThat(e).hasMessage("Repository 'http://rep1.com'doesn't export any plugins, 'plugins=...' is empty");
  }

  @Test
  void testValidateUnregisteredPlugin() {
    final String file = //
        "plugins=plugin0,plugin1\n" + //
            "plugin0.versions=1.3\n" + //
            "plugin0.1.3.requiredSonarVersions=6.7.6,7.0,7.1,7.2,7.2.1,7.3,7.4,7.5\n" + //
            "plugin0.homepageUrl=https\\://url.url\n" + //
            "plugin0.issueTrackerUrl=http\\://url.url\n" + //
            "plugin0.scm=https\\://url.url\n" + //
            "plugin1.1.3.mavenGroupId=org.group\n" + //
            "plugin1.description=Description\n" + //
            "plugin1.license=GNU LGPL 3\n" + //
            "plugin1.1.3.description=Description\n" + //
            "plugin1.1.3.mavenArtifactId=sonar-plugin0-plugin\n" + //
            "plugin2.name=Name\n"; // <<= plugin2 is not registered

    RepositoryDescription desc = new RepositoryDescription(new ByteArrayInputStream(file.getBytes()),
        "http://rep2.com");

    final RepositoryValidationException e = assertThrows(RepositoryValidationException.class, () -> {
      desc.validate();
    });

    assertThat(e).hasMessage(
        "Repository 'http://rep2.com' contains keys [plugin2], which doesn't match the list of exported plugins [plugin1, plugin0]");
  }

}
