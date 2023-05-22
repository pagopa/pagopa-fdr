#!/usr/bin/env sh
mkdir -p .cache
cd .cache
if [ ! -f google-java-format-1.17.0-all-deps.jar ]
then
    curl -LJO "https://github.com/google/google-java-format/releases/download/v1.17.0/google-java-format-1.17.0-all-deps.jar"
    chmod 755 google-java-format-1.17.0-all-deps.jar
fi
cd ..

#changed_java_files=$(git diff --cached --name-only --diff-filter=ACMR | grep ".*java$" )
#echo $changed_java_files
#java -jar .cache/google-java-format-1.17.0-all-deps.jar --replace $changed_java_files

## escludo le classi con il String text block perchè non ancora supportate da google java format
java -jar .cache/google-java-format-1.17.0-all-deps.jar \
--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED \
--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED \
--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED \
--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED \
--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED \
--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED --replace $(find . -type f -name "*.java" ! -name "PspResourceTest.java" ! -name "App.java")

