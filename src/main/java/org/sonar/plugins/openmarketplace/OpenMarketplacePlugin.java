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

import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

public class OpenMarketplacePlugin implements Plugin {

  public static final String SONAR_OPENMARKETPLACE_URLS = "sonar.openmarketplace.urls";

  @Override
  public void define(Context context) {
    context.addExtension(OpenMarketplaceService.class);

    final String subcateg = "(1) General";
    PropertyDefinition urlProperty = PropertyDefinition.builder("sonar.customupdatecenter.urls")
        .defaultValue(
            "https://raw.githubusercontent.com/ivangalkin/sonar-cxx.updatecenter.url/master/update-center.properties")
        .multiValues(true).name("URLs to the custom repositories")
        .description("URLs to the property files, which represents a custom plugin repository. "
            + "These files will be appended to the original repository "
            + "(see https://update.sonarsource.org/update-center.properties).")
        .subCategory(subcateg).onQualifiers(Qualifiers.APP).index(1).build();

    context.addExtension(urlProperty);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
