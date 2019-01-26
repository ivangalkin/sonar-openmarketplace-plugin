# sonar-openmarketplace-plugin
# Open Marketplace for SonarQube

Add custom repositories to the SonarQube marketplace (SonarQube update center). Allow arbitrary plugins to be installed/updated by means of the marketplace WebUI.

* [For SonarQube admins](#for_admins)
* [For plugin developers](#for_developers)
* [How it works](#how_it_works)
* [Disclosure](#disclosure)

<a name="for_admins"/>

# Are you SonarQube admin?

1. Download the JAR file into `<sonar installation home>/extensions/plugins/` (e.g. `/opt/sonar/extensions/plugins/`)
2. Put the line `sonar.updatecenter.url=http\://localhost\:9000/setup/openmarketplace` at the bottom of the file  `<sonar installation home>/conf/sonar.properties` (you might want to adjust the port 9000 according to your customization of `sonar.web.port` if there is any)
3. Restart your SonarQube server
4. Now you can add custom plugin repositories additionally to the original one
   * Go to `http://<sonarserver>/admin/settings?category=open+marketplace`
   * Parametrize your custom repositories by means of the property `sonar.openmarketplace.urls`
   * Validate your configuration by means of the selftest: `http://<sonarserver>/api/openmarketplace/selftest`
5. SonarQube updates its Marketplace only once an hour. So it might be faster to restart the SonarQube server in order to force the refresh.

**BENEFITS:** From now on, you'll be automatically informed about the new versions of your custom plugins. Their installation and update will be possible through the standard Marketplace UI. No more SSH logins, files copying etc.

**ATTENTION:** SonarQube plugins might be harmful. Be careful, which URLs you use as your additional repositories! Use only trusted plugins and repositories! You use the Open Marketplace at your own risk.

<a name="for_developers"/>

# Are you plugin developer?

1. Publish a plain text description of your plugin.
2. It must be a [.properties](https://en.wikipedia.org/wiki/.properties) file, which contains

a) a list of exported plugins
```properties
plugins=yourplugin
```
b) general description of your plugin, incl. the list of available versions
```properties
plugins=yourplugin
yourplugin.category=
yourplugin.description=
yourplugin.developers=
yourplugin.homepageUrl=
yourplugin.issueTrackerUrl=
yourplugin.license=GNU LGPL 3
yourplugin.name=
yourplugin.organization=
yourplugin.organizationUrl=
yourplugin.scm=
yourplugin.publicVersions=0.1
yourplugin.versions=0.1
```

c) simple description for each available version. e.g.
```properties
yourplugin.0.1.description=
yourplugin.0.1.mavenArtifactId=
yourplugin.0.1.mavenGroupId=
yourplugin.0.1.requiredSonarVersions=6.7,6.7.1,6.7.2,6.7.3,6.7.4,6.7.5,6.7.6,7.0,7.1,7.2,7.2.1,7.3,7.4,7.5
yourplugin.0.1.sqVersions=6.7,6.7.1,6.7.2,6.7.3,6.7.4,6.7.5,6.7.6,7.0,7.1,7.2,7.2.1,7.3,7.4,7.5
yourplugin.0.1.downloadUrl=
yourplugin.0.1.date=
```

You might want to use the [original repository](https://update.sonarsource.org/update-center.properties) or [our repository](https://raw.githubusercontent.com/ivangalkin/sonar-openmarketplace-plugin/master/update-center.properties) as an example. Please pay your attention to the key `publicVersions` from the original repository. It will give you the full list of available SonarQube version, which is...
```properties
publicVersions=6.7,6.7.1,6.7.2,6.7.3,6.7.4,6.7.5,6.7.6,7.0,7.1,7.2,7.2.1,7.3,7.4,7.5
```
... at the moment.

**BENEFITS**:  

1. There is an [official way](https://docs.sonarqube.org/display/DEV/Deploying+to+the+Marketplace) to deploy your plugin into the Marketplace. Unfortunately the rules are very restrictive. If your plugin gets rejected, there will be no other way than to install you plugin through the file system. This is inconvenient. So your plugin will be installed/updated less often. Open Marketplace being installed once, integrates 3rd party plugins as the 1st class citizens.

2. SonarQube Marketplace ensures the compatibility of installed SonarQube version with the available plugins. If your plugin is not deployed officially, you must keep track of the compatibility by yourself. This is error-prone, since the file-system-based installation and update are not validated automatically. Open Marketplace allows you and your users to benefit from the consistency checks.

<a name="how_it_works"/>

# How it works

## SonarQube Marketplace

By default all plugins which appear in the SonarQube marketplace (`http://<sonarserver>/admin/marketplace`) are fetched from the internet. The official repository (also known as "update center") is located under the URL https://update.sonarsource.org/update-center.properties. It provides a list of all ["deployed"](https://docs.sonarqube.org/display/DEV/Deploying+to+the+Marketplace) plugins, their available releases, information about the compatibility with different SonarQube versions etc. Each time you visit the Marketplace Web UI, SonarQube downloads the mentioned list of plugins, parses and analyses it. As result you'll see

1. which plugins are already installed
2. which installed plugins can be updated
3. which plugins can be also installed

For 2) and 3) update center is required. In order to provide all available options for installation and update, SonarQube server compares the list of installed plugins and their versions with the downloaded list. Compatibility with your particular SonarQube version is taken into account.

The default URL for the update center can be changed by means of the property `sonar.updatecenter.url`. That means, that one can use the standard Marketplace Web UI and all its great features with the altered list of plugins.

## SonarQube Open Marketplace

SonarQube plugin API allows creation of web pages and web services. We use this technique in order to implement a (kind of) servlet and bind it to the URL "/setup/openmarketplace". Now what happes, when the URL `http://<sonarserver>/setup/openmarketplace` is called:

1. we download the [original update center](https://update.sonarsource.org/update-center.properties).
2. we read the value of `sonar.openmarketplace.urls`. This is our own plugin property. It allows you to specify additional update centers / repositories. You can edit this list from the Settings Web UI (see `http://<sonarserver>/admin/settings?category=open+marketplace`). Our plugin suggests a list of trustworthy repositories by default. Among others there is our own repository https://raw.githubusercontent.com/ivangalkin/sonar-openmarketplace-plugin/master/update-center.properties. Each admin can add more custom repositories to this list and/or change the existing ones.
3. we download each custom repository and merge its plugins with the original list (see 1)
4. the resulting list is available under the URL `http://<sonarserver>/setup/openmarketplace`.

Also we provide an URL `http://<sonarserver>/api/openmarketplace/selftest`. Open this URL in order to retrace all steps from above.

Now we are able to extend the orignal update center with the custom repositories. Our extended list of plugins becomes effective if you change the default update center URL with `http://localhost:9000/setup/openmarketplace` (you might want to adapt the port if necessary). In order to do so, please put the line...

```PROPERTIES
sonar.updatecenter.url=http\://localhost\:9000/setup/openmarketplace
```
... at the bottom of the file `<sonar installation home>/conf/sonar.properties`. At the moment this property is hidden, so you can not change it through the Settings Web UI.

Please follow the [installation manual](#for_admins) and you'll extend your **original** Marketplace and apply all its useful features to custom plugins.

**IMPORTANT**: we don't aim to hide or fake the original plugins. From that reason we always download the original update center. Also we don't allow custom repositories to provide plugin IDs, which are already listed in the original update center. Nevertheless we are not able to protect you from malicious plugins. Please be careful, which custom repositories you add to your Marketplace. Safety of your SonarQube installation is your own responsibility.


<a name="disclosure"/>

# Disclosure

*THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.*
