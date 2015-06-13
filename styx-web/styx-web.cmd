@echo off
java -Djava.util.logging.config.file=logging.properties -Dstyx.web.http.port=8082 -Dstyx.web.session.factory=sqlite_local -jar target/styx-web-0.1-SNAPSHOT.war %1 %2 %3 %4 %5 %6 %7 %8 %9
