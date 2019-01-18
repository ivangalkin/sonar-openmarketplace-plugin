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

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;
import org.sonar.api.utils.HttpDownloader;
import org.sonar.plugins.openmarketplace.repository.RepositoryDescription;
import org.sonar.plugins.openmarketplace.repository.RepositoryMerger;
import org.sonar.plugins.openmarketplace.repository.RepositoryValidationException;

/**
 * Implement the logic of {@link OpenMarketplaceUpdatecenterHandler}, but create
 * a HTML step-by-step output
 *
 */
public class OpenMarketplaceSelftestHandler implements RequestHandler {

  private static final String HTML_BODY_TEMPLATE = "<!DOCTYPE html>\n" + //
      "<html>\n" + //
      "<head>\n" + //
      "<!-- inspired by https://www.w3schools.com/howto/howto_js_collapsible.asp -->\n" + //
      "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" + //
      "<style>\n" + //
      "code { white-space: pre; }\n" + //
      ".collapsible {\n" + //
      "  background-color: #777;\n" + //
      "  color: white;\n" + //
      "  cursor: pointer;\n" + //
      "  padding: 18px;\n" + //
      "  width: 100%%;\n" + //
      "  border: none;\n" + //
      "  text-align: left;\n" + //
      "  outline: none;\n" + //
      "  font-size: 15px;\n" + //
      "}\n" + //
      "\n" + //
      ".active, .collapsible:hover {\n" + //
      "  background-color: #555;\n" + //
      "}\n" + //
      "\n" + //
      ".content {\n" + //
      "  padding: 0 18px;\n" + //
      "  display: none;\n" + //
      "  overflow: hidden;\n" + //
      "  background-color: #f1f1f1;\n" + //
      "}\n" + //
      "</style>\n" + //
      "</head>\n" + //
      "<body>\n" + //
      "\n" + //
      "<h2>Open Marketplace Selftest</h2>" + //
      "%s\n" + //
      "<script>\n" + //
      "var coll = document.getElementsByClassName(\"collapsible\");\n" + //
      "var i;\n" + //
      "\n" + //
      "for (i = 0; i < coll.length; i++) {\n" + //
      "  coll[i].addEventListener(\"click\", function() {\n" + //
      "    this.classList.toggle(\"active\");\n" + //
      "    var content = this.nextElementSibling;\n" + //
      "    if (content.style.display === \"block\") {\n" + //
      "      content.style.display = \"none\";\n" + //
      "    } else {\n" + //
      "      content.style.display = \"block\";\n" + //
      "    }\n" + //
      "  });\n" + //
      "}\n" + "</script>" + //
      "</body>\n";

  private static final String HTML_COLLAPSIBLE_TEMPLATE = "<button class=\"collapsible\">%s</button>\n" + //
      "<div class=\"content\">\n" + //
      "<code>\n" + //
      "%s\n" + //
      "</code>\n" + //
      "</div>\n";

  private static String esc(String content) {
    return StringEscapeUtils.escapeHtml4(content);
  }

  private static String escAndQuote(String content) {
    final String quote = esc("\"");
    return quote + esc(content) + quote;
  }

  private static String createRepositoryContent(String rawContent, String url) {
    final String caption0 = "Content of " + esc(url);
    final String caption1 = "Content of " + esc(url) + " (sorted)";
    final String escapedContent = esc(rawContent);
    final String sortedContent = Arrays.stream(escapedContent.split("\\R")).sorted().collect(Collectors.joining("\n"));
    return String.format(HTML_COLLAPSIBLE_TEMPLATE, caption0, escapedContent)
        + String.format(HTML_COLLAPSIBLE_TEMPLATE, caption1, sortedContent);
  }

  private static String createRepositoryPlugins(RepositoryDescription repository) {
    final String plugins = repository.getPluginIDs().stream().sorted().map(id -> "<li>" + esc(id) + "</li>")
        .collect(Collectors.joining("\n"));
    final String caption = "List of plugins (" + repository.getPluginIDs().size() + ") from " + esc(repository.getUrl())
        + " (sorted)";
    final String content = "<ul>\n" + plugins + "</ul>\n";
    return String.format(HTML_COLLAPSIBLE_TEMPLATE, caption, content);
  }

  private Object createSettingsContent() {
    String html = createINFO(String.format("Following repositories will be merged"));
    html = html + "<ul>\n<li>" + esc(OpenMarketplaceTrustedRepositories.ORIGINAL_REPOSITORY_URL) + "</li>\n";
    html = html + Arrays.stream(configuration.getStringArray(OpenMarketplacePlugin.SONAR_OPENMARKETPLACE_URLS))
        .map(String::trim).filter(s -> !s.isEmpty()).map(url -> "<li>" + esc(url) + "</li>")
        .collect(Collectors.joining("\n"));
    return html + "</ul>\n";
  }

  private static String createINFO(String message) {
    return String.format("<p><font color=\"green\">[INFO]</font> %s</p>\n", message);
  }

  private static String createERROR(String message) {
    return String.format("<p><font color=\"red\">[ERROR]</font> %s</p>\n", message);
  }

  private static String createWARN(String message) {
    return String.format("<p><font color=\"orange\">[WARN]</font> %s</p>\n", message);
  }

  private final Configuration configuration;
  private final HttpDownloader downloader;

  public OpenMarketplaceSelftestHandler(Configuration c, HttpDownloader d) {
    configuration = c;
    downloader = d;
  }

  private String download(String url) throws IOException, URISyntaxException {
    final URI uri = new URI(url);
    final String encoding = StandardCharsets.UTF_8.name();
    try {
      return downloader.downloadPlainText(uri, encoding);
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  private String mergeAll() throws IOException {
    final String originalURLescaped = escAndQuote(OpenMarketplaceTrustedRepositories.ORIGINAL_REPOSITORY_URL);
    final StringBuilder html = new StringBuilder();

    final Optional<String> updatecenterURL = configuration.get("sonar.updatecenter.url");
    boolean isOpenMarketplaceActive = false;
    if (updatecenterURL.isPresent()) {
      html.append(createINFO("sonar.updatecenter.url is set to " + escAndQuote(updatecenterURL.get())));
      isOpenMarketplaceActive = !OpenMarketplaceTrustedRepositories.ORIGINAL_REPOSITORY_URL
          .equals(updatecenterURL.get().trim());
    } else {
      html.append(createWARN("sonar.updatecenter.url is not set, default URL " + originalURLescaped + " is used"));
    }

    if (!isOpenMarketplaceActive) {
      html.append(createWARN("Open Marketplace is not activated as update center"));
    }

    final Integer webPort = configuration.getInt("sonar.web.port").orElse(9000);
    final String suggestedURL = String.format("http://localhost:%d/%s/%s", webPort,
        OpenMarketplaceService.CONTROLLER_PATH, OpenMarketplaceService.UPDATECENTER_ACTION_PATH);

    html.append(createINFO("Suggested settings for sonar.properties are <code>sonar.updatecenter.url="
        + suggestedURL.replaceAll(Pattern.quote(":"), Matcher.quoteReplacement("\\:")) + "</code>"));

    html.append(createSettingsContent());

    RepositoryDescription original = null;
    try {
      final String originalContent = download(OpenMarketplaceTrustedRepositories.ORIGINAL_REPOSITORY_URL);
      html.append(createINFO("Original repository " + originalURLescaped + " downloaded sucessfully"));
      html.append(createRepositoryContent(originalContent, OpenMarketplaceTrustedRepositories.ORIGINAL_REPOSITORY_URL));
      original = new RepositoryDescription(originalContent, OpenMarketplaceTrustedRepositories.ORIGINAL_REPOSITORY_URL);
      html.append(createINFO("Original repository parsed successfully"));
      html.append(createRepositoryPlugins(original));

    } catch (IOException | URISyntaxException ioe) {
      html.append(createERROR("Unable to download original repository " + originalURLescaped + ": exception "
          + escAndQuote(ioe.getMessage())));
      html.append(createERROR(
          "Open Marketplace will stop merging repositories and redirect (status code SC_MOVED_TEMPORARILY) to the original repository"));
      return html.toString();
    } catch (Exception e) {
      html.append(createERROR("Unexpected exception while processing of original repository" + originalURLescaped
          + ": exception " + escAndQuote(e.getMessage())));
      html.append(createERROR(
          "Open Marketplace will stop merging repositories and redirect (status code SC_MOVED_TEMPORARILY) to the original repository"));
      return html.toString();
    }

    final RepositoryMerger merger = new RepositoryMerger(original);

    for (String urlProperty : configuration.getStringArray(OpenMarketplacePlugin.SONAR_OPENMARKETPLACE_URLS)) {
      final String customURL = urlProperty.trim();
      if (customURL.isEmpty()) {
        continue;
      }
      final String customURLescaped = escAndQuote(customURL);

      try {
        final String customContent = download(customURL);
        html.append(createINFO("Custom repository " + customURLescaped + " downloaded sucessfully"));
        html.append(createRepositoryContent(customContent, customURL));
        final RepositoryDescription custom = new RepositoryDescription(customContent, customURL);
        html.append(createINFO("Custom repository " + customURLescaped + " parsed successfully"));
        html.append(createRepositoryPlugins(custom));

        custom.validate();
        merger.addCustomRepository(custom);

        html.append(createINFO("Custom repository " + customURLescaped + " validated sucessfully"));
      } catch (IOException | URISyntaxException ioe) {
        html.append(createWARN("Unable to download custom repository " + customURLescaped + ": exception "
            + escAndQuote(ioe.getMessage())));
        html.append(createWARN("Ignore custom repository " + customURLescaped));
      } catch (RepositoryValidationException rve) {
        html.append(createWARN("Unable to validate custom repository " + customURLescaped + ": exception "
            + escAndQuote(rve.getMessage())));
        html.append(createWARN("Ignore custom repository " + customURLescaped));
      } catch (Exception e) {
        html.append(createERROR("Unexpected exception while processing of custom repository " + customURLescaped
            + ": exception " + escAndQuote(e.getMessage())));
        html.append(createWARN("Ignore custom repository " + customURLescaped));
      }
    }

    final String mergedContent = merger.merge();
    html.append(createINFO("Original and custom repositories were merged sucessfully"));
    html.append(createRepositoryContent(mergedContent, suggestedURL));
    RepositoryDescription mergedRepository = null;
    try {
      mergedRepository = new RepositoryDescription(mergedContent, suggestedURL);
    } catch (Exception e) {
      html.append(createERROR("Unable to parse merged repository: exception " + escAndQuote(e.getMessage())));
    }
    if (mergedRepository != null) {
      html.append(createRepositoryPlugins(mergedRepository));
      html.append(createINFO("Properties merged by means of java.util.Properties:"));
      html.append(createRepositoryContent(mergedRepository.getPluginProperties(),
          suggestedURL + " after parsing by means of java.util.Properties"));
    }

    html.append(createINFO("Self test finished"));

    return html.toString();
  }

  @Override
  public void handle(Request request, Response response) throws Exception {

    String report = "";
    try {
      report = mergeAll();
    } catch (Exception e) {
      report = createERROR("Unexpected exception while selftest: exception " + e.getMessage());
    }

    report = String.format(HTML_BODY_TEMPLATE, report);

    try (OutputStream stream = response.stream().output()) {
      response.setHeader("Content-Type", "text/html;charset=UTF-8");
      stream.write(report.getBytes(StandardCharsets.UTF_8));
    }

  }

}