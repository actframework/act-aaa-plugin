# act-aaa CHANGE LOG

1.3.1
* Adding `-act.apidoc` to `aaa.authenticate.list` file does not stop authentication requirement of ApiDoc services #17
* Allow system endpoints to skip authentication check at dev mode #18

1.3.0
* Catch up to act-1.6.x

1.2.4
* Ebean dao can't inject into ActAAAService.Base extends class without @Lazy loading #13 

1.2.3
* Cannot create AAA context. AAA plugin disabled #12 

1.2.2
* Cannot instantiate beetlsql mapper interface when inject #11 
* It can load yaml content before AAA service is fully initialized #10 
* Raise event when principal resolved #09 
* Including aaa without adding anything else to new ACT project will cause an NPE #08 

1.2.1
* Update to osgl-aaa 1.2.1 to fix show stopper issue https://github.com/osglworks/java-aaa/issues/3

1.2.0
* Update ActFramework to 1.3.0
* It waived authentication for a controller method when before handler has no requirement on authentication #4 
* New AAAObject name matching logic #5 
* Allow profile specified `acl.yaml` configuration #6 
* updated acl.yaml not reloaded in dev mode #7 

1.1.1
* It waived authentication for a controller method when before handler has no requirement on authentication #4 

1.1.0
* update to osgl-aaa 1.2.0 #2 
* Add FastJsonRoleCodec #3 

1.0.2
* register ExceptionHandler to convert NoAccessException exception into 403 Forbidden response
* take out version range from pom.xml. See https://issues.apache.org/jira/browse/MNG-3092

1.0.1
* It shall not throw out NPE when AAA service cannot find the user in the system #1 

1.0.0
* first release

0.7.0-SNAPSHOT
* update to act-0.7.0

0.6.0-SNAPSHOT
* update to act-0.6.0

0.5.0-SNAPSHOT
* update to act-0.5.0

0.4.0-SNAPSHOT
* update to act-0.4.0

0.3.0-SNAPSHOT
* Simplified application integration

0.2.0-SNAPSHOT
* update to ActFramework 0.2.0

0.1.2-SNAPSHOT
* register default AAAContext to AAA facade before App start
