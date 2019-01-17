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

public class OpenMarketplacePlugin implements Plugin {

  public static final String SONAR_OPENMARKETPLACE_URLS = "sonar.openmarketplace.urls";

  private static final String UPDATECENTER_RELATIVE_URL = "/" + OpenMarketplaceService.CONTROLLER_PATH + "/"
      + OpenMarketplaceService.ACTION_PATH;
  private static final String UPDATECENTER_ABSOLUTE_URL = "http\\\\://localhost[:port]" + UPDATECENTER_RELATIVE_URL;

  private static String href(String url, String text) {
    return String.format("<a href=\"%s\">%s</a>", url, text);
  }

  private static String href(String url) {
    return href(url, url);
  }

  @Override
  public void define(Context context) {
    context.addExtension(OpenMarketplaceService.class);

    final String subcateg = "(1) General";
    PropertyDefinition urlProperty = PropertyDefinition.builder(SONAR_OPENMARKETPLACE_URLS) //
        .defaultValue(OpenMarketplaceTrustedRepositories.OPENMARKETPLACE_REPOSITORY_URL) //
        .multiValues(true) //
        .name("URLs to the custom repositories") //
        .description(
            "URLs to the plain-text properties files. Each of them must represent a valid plugin repository (see online documentation for the format description). "
                + "The given files will be downloaded and appended to the "
                + href(OpenMarketplaceTrustedRepositories.ORIGINAL_REPOSITORY_URL, "original repository") + ". "
                + "The result will be provided under " + href(UPDATECENTER_RELATIVE_URL) + ". "
                + "Please add the line <code>sonar.updatecenter.url=" + UPDATECENTER_ABSOLUTE_URL + "</code> "
                + "to sonar.properties in order to enable the Open Marketplace.")//
        .subCategory(subcateg) //
        .index(1) //
        .build();

    context.addExtension(urlProperty);
  }

  @Override
  public String toString() {
    return "Open Marketplace";
  }

}
