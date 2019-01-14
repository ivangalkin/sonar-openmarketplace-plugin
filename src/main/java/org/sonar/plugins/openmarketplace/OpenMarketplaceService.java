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

import org.sonar.api.config.Configuration;
import org.sonar.api.server.ws.WebService;

/**
 * {@link WebService} is available since SonarQube 4.2
 */
public class OpenMarketplaceService implements WebService {

  private static final String ENDPOINT_NAME = "update-center.properties";
  private static final String ENDPOINT_PATH = "api/update-center";

  final private Configuration configuration;

  public OpenMarketplaceService(Configuration c) {
    configuration = c;
  }

  @Override
  public void define(Context context) {
    NewController controller = context.createController(ENDPOINT_PATH);
    controller.createAction("properties").setDescription(ENDPOINT_NAME)
        .setHandler(new OpenMarketplaceRequestHandler(configuration));
    controller.done();
  }
}