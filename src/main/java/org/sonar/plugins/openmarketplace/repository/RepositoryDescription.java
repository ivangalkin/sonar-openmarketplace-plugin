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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class RepositoryDescription {

  private final String url;
  private final Set<String> pluginIDs;
  private final Properties pluginProperties;

  /**
   * @param input
   *          properties file with the same structure as <a href=
   *          "https://update.sonarsource.org/update-center.properties">https://update.sonarsource.org/update-center.properties</a>
   *          We expect each repository to have a list of exported plugins
   *          ("plugins=pluginA,pluginB,...,pluginZ") and key-value pairs for
   *          each exported plugin.
   * @throws IOException
   */
  public RepositoryDescription(InputStream input, String url) {

    this.url = url;
    this.pluginProperties = new Properties();
    try {
      pluginProperties.load(input);
    } catch (IOException e) {
      throw new RepositoryValidationException("Unable to read repository '" + url + "': " + e.getMessage());
    }

    final String pluginIDsCSV = pluginProperties.getProperty("plugins");
    if (pluginIDsCSV == null) {
      throw new RepositoryValidationException(
          "Repository '" + url + "' doesn't contain a list of plugins (key = plugins)");
    }

    pluginProperties.remove("plugins");
    pluginIDs = Arrays.stream(pluginIDsCSV.split(",")).map(String::trim).filter(s -> !s.isEmpty())
        .collect(Collectors.toSet());
  }

  public void validate() {
    // make sure list of plugins is not empty
    if (pluginIDs.isEmpty()) {
      throw new RepositoryValidationException(
          "Repository '" + url + "'doesn't export any plugins, 'plugins=...' is empty");
    }

    // make sure, the repository provides properties only for its own plugins
    // all keys have the following format: <pluginID>.property0[.property1 ...
    // .propertyN]
    //
    // this check is important, because we don't want, that some malicious
    // repository sneak a fake plugin
    // see RepositoryMerger
    final Set<String> invalidKeys = new HashSet<>();
    for (Object rawKey : pluginProperties.keySet()) {
      final String key = (String) rawKey;
      final String pluginID = key.split("\\.")[0];
      if (!pluginIDs.contains(pluginID)) {
        invalidKeys.add(pluginID);
      }
    }
    if (!invalidKeys.isEmpty()) {
      final String invalidKeysStr = invalidKeys.stream().collect(Collectors.joining(", "));
      final String pluginIDsStr = pluginIDs.stream().collect(Collectors.joining(", "));

      throw new RepositoryValidationException("Repository '" + url + "' contains keys [" + invalidKeysStr
          + "], which doesn't match the list of exported plugins [" + pluginIDsStr + "]");
    }
  }

  public String getUrl() {
    return url;
  }

  public Set<String> getPluginIDs() {
    return Collections.unmodifiableSet(pluginIDs);
  }

  public String getPluginProperties() {
    try {
      final StringWriter writer = new StringWriter();
      pluginProperties.store(writer, "source: " + url);
      return writer.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
