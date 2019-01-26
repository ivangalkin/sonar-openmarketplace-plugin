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
   * Parametrize your custom repositories
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

<a name="for_admins"/>
# How it works

<a name="disclosure"/>

# Disclosure

*THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.*
