ReadMe.txt


===================================================================================================
                       Read this guide to use Hotfix Maven Plugin effectively.
===================================================================================================

******************** This utility should have been delivered as a zip file. ************************

Contents:
1. Plugin Repository Artifact Zip - com.zip
2. Read Me - "readme.txt"
3. hotfix.properties

******************************** How to setup the plugin? ******************************************

1. Extract the Zip file contents in any directory.
2. Copy the com.zip inside the repository. Example REPO_LOCATION = C:\Users\<USERNAME>\.m2\repository\
3. Unzip the com.zip such that below artifact is created: <REPO_LOCATION>\com\rnd\hftool\hotfix-maven-plugin\1.1-SNAPSHOT\hotfix-maven-plugin-1.1-SNAPSHOT.jar

*************************** How to configure plugin for a project? **********************************

1. In project's pom.xml e.g. nec-src-integration-adapter_9.4.5.1_R7.2 > pom.xml, add plugin tag as below:

        <build>
            <plugins>
                <plugin>
                    <groupId>com.rnd.hftool</groupId>
                    <artifactId>hotfix-maven-plugin</artifactId>
                    <version>1.1-SNAPSHOT</version>
                </plugin>
            </plugins>
        </build>

2. Plugin has default hotfix.properties inside the plugin jar.
    However, to customize the plugin behavior, user can provide custom value of any property by defining hotfix.properties in project directory.
    User is free to declare only that property whose value is expected to be customized.

    Default hotfix.properties contents:

    classes.path=target/classes                                             --> Path where .class file will be looked for
    resources.path=src/main/resources                                       --> Path where resource files will be looked for
    other.paths=target/maven-status,target/generated-sources                --> Path where other files will be looked for
    single.zip.prefix.path=applications/NetCracker.ear/APP-INF/classes      --> Single zip file path prefix
    patch.record.prefix=Index:                                              --> Patch file record prefix
    debug.mode=false                                                        --> Debug Mode


*************************** How to use plugin for a project? **********************************

1. Create a patch file containing the changed artifacts and keep it in project directory.
2. Latest .patch file will be processed by Hotfix-Maven-Plugin.

******************************* Resulting artifacts *******************************************

1. Module specific jars - based on the changed artifacts.
2. Aggregated Jar - Zip of the module specific jars
3. Single Zip - Deliverable Hotfix Zip file of artifacts with prefix of single.zip.prefix.path property value.

All these articats will be stored in project directory > hotfix directory.

************************************ How it works? *********************************************

1. Hotfix Maven Plugin identifies the project path (component path) as the path of pom.xml where the plugin is configured.
2. Hotfix Maven Plugin considers the hotfix.properties in project directory to contain custom values for the hotfix properties.
    Default property values are read from plugin jar, and custom values will be read from custom hotfix.properties.
    Custom properties will have higher precedence.
3. Hotfix Maven Plugin considers the latest .patch file in project directory as the input for the plugin.
4. Hotfix Maven Plugin identifies the artifacts by prefix of the line, example "Index:". This prefix can be configured in hotfix.properties.
5. Hotfix Maven Plugin identifies if the artifact corresponds to a class file or is a resource/regular file.
6. Hotfix Maven Plugin identifies the modules within the project (component) by searching all the pom.xml inside the project directory.
7. Hotfix Maven Plugin searches for the artifacts based on artifact type in classes.path or in resources.path and others.path.
8. Hotfix Maven Plugin packs the found artifacts in a module specific jar, with qualified path having package immediately inside classes.path, resources.path or others.path, wherever it is found.
9. Hotfix Maven Plugin zips all module specific jars in Aggregated Zip.
10. Hotfix Maven Plugin zips all the artifacts in Single Zip with prefix of a specific path as defined in single.zip.prefix.path property.

************************************ Additional Customization *********************************************

1. By default, the utility is running with INFO logging level.
   To enable debug logs, set the property debug.mode=true

********************************************** Contact *********************************************

 For any assistance using this utility, contact Nirav Khandhedia @ nirav.khandhedia@netcracker.com

********************************************* Happy HotFixing! *************************************