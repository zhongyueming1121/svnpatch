@echo off
::will set java_home only in this window.
::you can uncomment to set JAVA_HOME
::set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_271
set PATH=%JAVA_HOME%/bin;%JAVA_HOME%/jre/bin
set CLASSPATH=.;%JAVA_HOME%\lib\dt.jar;%JAVA_HOME%\lib\tools.jar
::run jar.
java -Xmx512m -Xms1024m -jar svnpatch.jar
pause