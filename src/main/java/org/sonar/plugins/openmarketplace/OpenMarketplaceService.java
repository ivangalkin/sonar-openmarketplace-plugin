/*
 * Open Marketplace plugin for SonarQube
 * Copyright (C) 2018-2019 ivangalkin
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

public class OpenMarketplaceService implements WebService {

  /**
   * <code>/setup/*</code> doesn't require a user session (see
   * {@link org.sonar.server.authentication.UserSessionInitializer}). It's
   * important to use this controller path, otherwise the resource will be not
   * available if <code>sonar.forceAuthentication</code> was enabled.
   */
  public static final String UPDATECENTER_CONTROLLER_PATH = "setup";
  public static final String UPDATECENTER_ACTION_PATH = "openmarketplace";

  /**
   * <code>/api/openmarketplace/selftest</code> is protected by regular
   * authentication rules.
   */
  public static final String SELFTEST_CONTROLLER_PATH = "api/openmarketplace";
  public static final String SELFTEST_ACTION_PATH = "selftest";

  private final Configuration configuration;
  private final HttpDownloader downloader;

  public OpenMarketplaceService(Configuration c, HttpDownloader d) {
    configuration = c;
    downloader = d;
  }

  @Override
  public void define(Context context) {
    final NewController updateCenterController = context.createController(UPDATECENTER_CONTROLLER_PATH);
    updateCenterController.createAction(UPDATECENTER_ACTION_PATH) //
        .setDescription("Merge multiple repositories into one property file") //
        .setHandler(new OpenMarketplaceUpdatecenterHandler(configuration, downloader))//
        .setSince("6.7") //
        .setInternal(false); //
    updateCenterController.done();

    final NewController selfTestController = context.createController(SELFTEST_CONTROLLER_PATH);
    selfTestController.createAction(SELFTEST_ACTION_PATH) //
        .setDescription("Perform a self test of Open Marketplace settings") //
        .setHandler(new OpenMarketplaceSelftestHandler(configuration, downloader))//
        .setSince("6.7") //
        .setInternal(false); //
    selfTestController.done();
  }
}