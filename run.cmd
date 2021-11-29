@echo off
::set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_271
::set CLASSPATH=.;%JAVA_HOME%\lib\dt.jar;%JAVA_HOME%\lib\tools.jar
::set PATH=%JAVA_HOME%/bin;
java -Xms512m -Xmx1024m -jar svnpatch.jar
pause