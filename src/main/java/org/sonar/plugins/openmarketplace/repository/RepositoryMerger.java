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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RepositoryMerger {

  private final List<RepositoryDescription> repositories = new ArrayList<>();

  public RepositoryMerger(RepositoryDescription originalRepository) {
    repositories.add(originalRepository);
  }

  public void addCustomRepository(RepositoryDescription customRepository) {
    final RepositoryDescription originalRepository = repositories.get(0);
    final Set<String> intersectedIDs = new HashSet<>(originalRepository.getPluginIDs());
    intersectedIDs.retainAll(customRepository.getPluginIDs());

    if (!intersectedIDs.isEmpty()) {
      final String intersectedIDsStr = intersectedIDs.stream().collect(Collectors.joining(", "));
      throw new RepositoryValidationException("Custom repository '" + customRepository.getUrl() + "' exports plugins ["
          + intersectedIDsStr + "] which are already exported by the original repository");
    }
    repositories.add(customRepository);
  }

  public String merge() {
    final String idsCSV = repositories.stream().map(RepositoryDescription::getPluginIDs).flatMap(Collection::stream)
        .collect(Collectors.joining(","));
    final String properties = repositories.stream().map(RepositoryDescription::getPluginProperties)
        .collect(Collectors.joining("\n"));

    final StringBuilder builder = new StringBuilder();
    builder.append("plugins=");
    builder.append(idsCSV);
    builder.append('\n');
    builder.append(properties);
    return builder.toString();
  }

}
