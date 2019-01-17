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
import org.sonar.api.utils.HttpDownloader;

/**
 * {@link WebService} is available since SonarQube 4.2
 */
public class OpenMarketplaceService implements WebService {

  public static final String CONTROLLER_PATH = "api/openmarketplace";
  public static final String ACTION_PATH = "updatecenter";

  private final Configuration configuration;
  private final HttpDownloader downloader;

  public OpenMarketplaceService(Configuration c, HttpDownloader d) {
    configuration = c;
    downloader = d;
  }

  @Override
  public void define(Context context) {
    NewController controller = context.createController(CONTROLLER_PATH);
    controller.createAction(ACTION_PATH) //
        .setDescription("Merge multiple repositories into one property file") //
        .setHandler(new OpenMarketplaceRequestHandler(configuration, downloader))//
        .setSince("6.7") //
        .setInternal(false); //
    controller.done();
  }
}