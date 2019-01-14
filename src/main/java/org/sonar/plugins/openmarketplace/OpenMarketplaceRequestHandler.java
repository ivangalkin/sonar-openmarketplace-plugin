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

import java.io.OutputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.sonar.api.config.Configuration;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;

public class OpenMarketplaceRequestHandler implements RequestHandler {

  private Configuration configuration;

  public OpenMarketplaceRequestHandler(Configuration c) {
    configuration = c;
  }

  /**
   * TODO
   */
  @Override
  public void handle(Request request, Response response) throws Exception {
    response.setHeader("Content-Type", "text/plain; charset=ISO-8859-1");
    OutputStream stream = response.stream().output();
    String comments = Arrays.stream(configuration.getStringArray(OpenMarketplacePlugin.SONAR_OPENMARKETPLACE_URLS))
        .map(s -> "# " + s).collect(Collectors.joining("\n"));
    String out = comments + "\n" + "plugins=cxx\n" + "cxx.category=Languages\n"
        + "cxx.organization=SonarOpenCommunity\n" + "cxx.scm=https\\://github.com/SonarOpenCommunity/sonar-cxx\n"
        + "cxx.versions=1.2.0\n" + "cxx.publicVersions=1.2.0\n" + "cxx.description=Code Analyzer for C/C++\n"
        + "cxx.homepageUrl=https\\://github.com/SonarOpenCommunity/sonar-cxx/wiki\n" + "cxx.developers=\n"
        + "cxx.issueTrackerUrl=https\\://github.com/SonarOpenCommunity/sonar-cxx/issues\\?state\\=open\n"
        + "cxx.archivedVersions=\n" + "cxx.name=C++ (Community)\n" + "cxx.license=GNU LGPL 3\n"
        + "cxx.1.2.0.sqVersions=6.7,6.7.1,6.7.2,6.7.3,6.7.4,6.7.5,6.7.6,7.0,7.1,7.2,7.2.1,7.3,7.4\n"
        + "cxx.1.2.0.description=Release sonar-cxx version 1.2.0\n"
        + "cxx.1.2.0.downloadUrl=https\\://github.com/SonarOpenCommunity/sonar-cxx/releases/download/cxx-1.2.0/sonar-cxx-plugin-1.2.0.jar\n"
        + "cxx.1.2.0.changelogUrl=https\\://github.com/SonarOpenCommunity/sonar-cxx/releases/tag/cxx-1.2.0\n"
        + "cxx.1.2.0.mavenGroupId=org.sonarsource.sonarqube-plugins.cxx\n" + "cxx.1.2.0.mavenArtifactId=cxx\n"
        + "cxx.1.2.0.displayVersion=1.2.0\n" + "cxx.1.2.0.date=2018-11-09\n"
        + "cxx.1.2.0.requiredSonarVersions=6.7,6.7.1,6.7.2,6.7.3,6.7.4,6.7.5,6.7.6,7.0,7.1,7.2,7.2.1,7.3,7.4";

    stream.write(out.getBytes("ISO-8859-1"));
  }

}