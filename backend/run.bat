@echo off
set JAVA_HOME=C:\Users\imcha\.jdks\corretto-20.0.2.1
set PATH=%JAVA_HOME%\bin;%PATH%
mvn clean spring-boot:run
