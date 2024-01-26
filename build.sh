#!/bin/bash
#!/bin/sh
# Build script for a light Java project
# version 5.2 - integrate Cucumber
# author Frédéric Delorme fredericDOTdelormeTgmailDOTcom
# MIT License
#
# Copyright (c) 2024 Frederic Delorme
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
cd ./

ENV=build

# Reading of ${ENV}.properties file
function prop {
  #grep "${1}" env/${ENV}.properties|cut -d'=' -f2
  grep "${1}" ${ENV}.properties | cut -d'=' -f2
}

export PROGRAM_NAME=$(prop project.name)
export PROGRAM_VERSION=$(prop project.version)
export PROGRAM_TITLE=$(prop project.title)
export MAIN_CLASS=$(prop project.main.class)
export JAVADOC_CLASSPATH=$(prop project.javadoc.classpath)
export JAVADOC_GROUPS=$(prop project.javadoc.packages)
export VENDOR_NAME=$(prop project.author.name)
export AUTHOR_NAME=$(prop project.author.email)
export JAVA_VERSION=$(prop project.build.jdk.version)
export EXTERNAL_JARS=$(prop project.build.jars)
# ---- to enforce preview compatibility use the --enable-preview mode,
# ---- for more information, see https://docs.oracle.com/en/java/javase/18/language/preview-language-and-vm-features.html
# ---- define the checkstyle rule set file
export COMPILATION_OPTS=$(prop project.compilation.opts)
export JAR_OPTS=${COMPILATION_OPTS} $(prop project.jar.opts)
# A dirty list of package to be build (TODO add automation on package detection)
#export JAVADOC_CLASSPATH="$PACKAGES_LIST"

# paths
export SRC=./src
export LIBS=./lib
export TARGET=./target
export BUILD=${TARGET}/build
export CLASSES=${TARGET}/classes
export RESOURCES=${SRC}/main/resources

# ---- Java JDK version and file encoding
export SOURCE_VERSION=${JAVA_VERSION}
export SOURCE_ENCODING=UTF-8

# ---- CheckStyle Rules
#
# Uncomment the one to be used
export CHECK_RULES=sun
#export CHECK_RULES=google
#
# Uncomment the required HTML report template from the following ones
export CHECKSTYLE_REPORT_XSL=checkstyle-author.xsl
#export CHECKSTYLE_REPORT_XSL=checkstyle-simple.xsl
#export CHECKSTYLE_REPORT_XSL=checkstyle-noframes-sorted.xsl

# ---- JDK and sources versions (mainly for manifest generator)
export JAVA_BUILD=$(java --version | head -1 | cut -f2 -d' ')
export GIT_COMMIT_ID=$(git rev-parse HEAD)

if [[ "$OSTYPE" == "linux"* ]]; then
  FS=":"
else
  FS=";"
fi
# define colors
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'
#
# Paths
export SRC=src
export LIBS=lib
export LIB_TEST=$LIBS/test/junit-platform-console-standalone-1.10.1.jar
export LIB_CUCUMBER_TEST=$LIBS/test/cucumber-core-7.15.0.jar${FS}$LIBS/test/cucumber-java8-7.9.0.jar${FS}$LIBS/test/cucumber-junit-platform-engine-7.15.0.jar${FS}$LIBS/test/cucumber-expressions-9.0.0.jar${FS}$LIBS/test/datatable-7.9.0.jar${FS}$LIBS/test/datatable-dependencies-3.0.0.jar${FS}$LIBS/test/junit-platform-suite-1.9.3.jar
export LIB_CHECKSTYLES=$LIBS/tools/checkstyle-10.12.3-all.jar
export TARGET=target
export BUILD=$TARGET/build
export CLASSES=$TARGET/classes
export TEST_CLASSES=$TARGET/test-classes
export RESOURCES=$SRC/main/resources
export TEST_RESOURCES=$SRC/test/resources
export JAR_NAME=$PROGRAM_NAME-$PROGRAM_VERSION.jar
export JAR_JAVADOC_NAME=$PROGRAM_NAME-$PROGRAM_VERSION-javadoc.jar
export CHECK_RULES_FILE=$LIBS/tools/rules/${CHECK_RULES}_checks.xml
#
echo "> Java version : $JAVA_BUILD"
echo "> Git   commit : $GIT_COMMIT_ID"
echo "> Encoding     : $SOURCE_ENCODING"
#
echo -e "> ${BLUE}Prepare environment (if ~/.sdkmanrc' file exists)${NC}"
if [ -f .sdkmanrc ]; then
  echo " |_ file sdkmanrc detected"
  source "$HOME/.sdkman/bin/sdkman-init.sh"
  sdk env install
  sdk env use
fi
# prepare target
function clearTarget() {
  echo "> Clear build workspace"
  rm -rf ${TARGET}
  mkdir -p ${CLASSES}
}
#
function manifest() {
  # build manifest
  echo -e "|_ ${BLUE}1. Create Manifest file '${TARGET}/MANIFEST.MF'${NC}..."
  echo """Manifest-Version: ${PROGRAM_NAME}
Main-Class: ${MAIN_CLASS}
Class-Path: ${EXTERNAL_JARS}
Created-By: ${JAVA_BUILD}
Implementation-Title: ${PROGRAM_NAME}
Implementation-Version: ${PROGRAM_VERSION}-build_${GIT_COMMIT_ID:0:12}
Implementation-Vendor: ${VENDOR_NAME}
Implementation-Author: ${AUTHOR_NAME}
""" >>${TARGET}/MANIFEST.MF
  echo -e "   |_ ${GREEN}done$NC"
  echo "- generate MANIFEST file for ${PROGRAM_NAME} version ${PROGRAM_VERSION} on git id ${GIT_COMMIT_ID:0:12}" >>target/build.log
}
#
function compile() {
  echo -e "|_ ${BLUE}2. Compile sources from '$SRC/main'${NC}..."
  echo "> from : ${SRC}"
  echo "> to   : ${CLASSES}"
  echo "> with : ${EXTERNAL_JARS}"
  # prepare target
  mkdir -p $CLASSES
  # Compile class files
  rm -Rf $CLASSES/*
  find $SRC/main -name '*.java' >$TARGET/sources.lst
  # Compilation via JavaC with some debug options to add source, lines and vars in the compiled classes.
  javac $COMPILATION_OPTS \
    -d $CLASSES \
    -g:source,lines,vars \
    -source $SOURCE_VERSION \
    -target $SOURCE_VERSION \
    -cp ".${FS}${EXTERNAL_JARS}${FS}${CLASSES}" @$TARGET/sources.lst
  echo -e "   |_ ${GREEN}done$NC"
  echo "- Compile project from ${SRC} to ${CLASSES} with ${EXTERNAL_JARS}" >>target/build.log
}
function checkCodeStyleQA() {
  echo -e "|_ ${BLUE}3. Check code quality against rules $CHECK_RULES${NC}..."
  echo "> explore sources at : $SRC"
  mkdir -p $TARGET
  find $SRC/main -name '*.java' >$TARGET/sources.lst
  java $JAR_OPTS -cp "$LIB_CHECKSTYLES${FS}$EXTERNAL_JARS${FS}$CLASSES:." \
    -jar $LIB_CHECKSTYLES \
    -c $CHECK_RULES_FILE \
    -f xml \
    -o $TARGET/checkstyle_errors.xml \
    @$TARGET/sources.lst
  xsltproc -o $TARGET/checkstyle_report.html $LIBS/tools/rules/$CHECKSTYLE_REPORT_XSL $TARGET/checkstyle_errors.xml
  echo -e "   |_ ${GREEN}done$NC"
  echo "- Check all code with  $CHECK_RULES" >>target/build.log
}
function generateJavadoc() {
  echo -e "|_ ${BLUE}4. Generate Javadoc ${NC}..."
  echo "> from : $SRC"
  echo "> to   : $TARGET/javadoc"
  # prepare target
  mkdir -p $TARGET/javadoc
  mkdir -p $SRC/main/javadoc
  # Compile class files
  rm -Rf $TARGET/javadoc/*
  java -jar ./lib/tools/markdown2html-0.3.1.jar <README.md >$SRC/main/javadoc/overview.html
  javadoc -source $SOURCE_VERSION \
    -author -use -version \
    -doctitle \"$PROGRAM_NAME\" \
    -d $TARGET/javadoc \
    -overview $SRC/main/javadoc/overview.html \
    $JAVADOC_GROUPS \
    -sourcepath "${SRC}/main/java${FS}${SRC}/main/javadoc" \
    -subpackages "${JAVADOC_CLASSPATH}" \
    -cp ".;$EXTERNAL_JARS"
  cd $TARGET/javadoc
  jar cvf ../$JAR_JAVADOC_NAME *
  cd ../../
  echo -e "   |_ ${GREEN}done$NC"
  echo "- build javadoc $JAR_JAVADOC_NAME" >>target/build.log
}
#
function generateSourceJar() {
  echo -e "|_ ${BLUE}5. Generate JAR sources $TARGET/${PROGRAM_NAME}-sources-${PROGRAM_VERSION}.jar${NC}..."
  echo "> from : $SRC"
  echo "> to   : $TARGET/"
  jar cvf ${TARGET}/${PROGRAM_NAME}-${PROGRAM_VERSION}-sources.jar -C src .
  echo -e "   |_ ${GREEN}done$NC"
  echo "- create JAR sources ${PROGRAM_NAME}-${PROGRAM_VERSION}-sources.jar" >>target/build.log
}
#
function executeTests() {
  echo -e "|_ ${BLUE}6. Execute tests${NC}..."
  echo "> from : $SRC/test"
  echo "> to   : $TARGET/test-classes"
  mkdir -p $TARGET/test-classes
  echo "copy test resources"
  cp -r ./$RESOURCES/* $TEST_CLASSES
  cp -r ./$TEST_RESOURCES/* $TEST_CLASSES
  echo "compile test classes"
  #list test sources
  find $SRC/main -name '*.java' >$TARGET/sources.lst
  find $SRC/test -name '*.java' >$TARGET/test-sources.lst
  javac -source ${SOURCE_VERSION} -encoding ${SOURCE_ENCODING} ${COMPILATION_OPTS} \
   -cp ".${FS}$LIB_TEST${FS}$LIB_CUCUMBER_TEST${FS}${EXTERNAL_JARS}" \
   -d $TEST_CLASSES @$TARGET/sources.lst @$TARGET/test-sources.lst
  echo "execute tests through JUnit"
  echo "--"
  echo "java $JAR_OPTS -jar \"${LIB_TEST}\" -cp \"${LIB_CUCUMBER_TEST}${FS}${EXTERNAL_JARS}${FS}${CLASSES}${FS}${TEST_CLASSES}${FS}${FS}.\" --scan-class-path"
  echo "--"
  java $JAR_OPTS -jar "${LIB_TEST}" \
   -cp "${LIB_CUCUMBER_TEST}${FS}${EXTERNAL_JARS}${FS}${CLASSES}${FS}${TEST_CLASSES}${FS}${FS}." \
   --scan-class-path
  echo -e "   |_ ${GREEN}done$NC"
  echo "execute Gherkin tests through Cucumber"
  echo "--"
  echo "java $JAR_OPTS -jar \"${LIB_CUCUMBER_TEST}\" -cp \"${LIB_CUCUMBER_TEST}${FS}${EXTERNAL_JARS}${FS}${CLASSES}${FS}${TEST_CLASSES}${FS}${TEST_RESOURCES}${FS}.\" cucumber.api.cli.Main --glue com.karma.test.features src/test/resources/features/"
  echo "--"
  java $JAR_OPTS -jar "${LIB_CUCUMBER_TEST}" --cp "${LIB_CUCUMBER_TEST}${FS}${EXTERNAL_JARS}${FS}${CLASSES}${FS}${TEST_CLASSES}${FS}${TEST_RESOURCES}${FS}." cucumber.api.cli.Main --glue com.karma.test.features src/test/resources/features/
  echo -e "   |_ ${GREEN}done$NC"
  echo "- execute tests through JUnit/Cucumber $SRC/test." >>target/build.log
}
#
function createJar() {
  echo -e "|_ ${BLUE}7. package jar file '$TARGET/$JAR_NAME'${NC}..."
  if ([ "$(ls $CLASSES | wc -l | grep -w "0")" ]); then
    echo '   |_ ${RED}ERROR: No compiled class files${NC}'
  else
    # Build JAR
    jar -cfmv $TARGET/$JAR_NAME $TARGET/MANIFEST.MF -C $CLASSES . -C $RESOURCES .
  fi
  echo -e "   |_ ${GREEN}done$NC"
  echo "- create JAR file '$TARGET/$JAR_NAME'." >>target/build.log
}
#
function wrapJar() {
  # create runnable program
  echo -e "|_ ${BLUE}8. create run file '$BUILD/$PROGRAM_NAME-$PROGRAM_VERSION.run'${NC}..."
  mkdir -p $BUILD/lib/dep
  cat $LIBS/stub.sh $TARGET/$PROGRAM_NAME-$PROGRAM_VERSION.jar >$BUILD/$PROGRAM_NAME-$PROGRAM_VERSION.run
  chmod +x $BUILD/$PROGRAM_NAME-$PROGRAM_VERSION.run
  cp -r $LIBS/dep $BUILD/lib
  echo -e "   |_ ${GREEN}done$NC"
  echo "- wrap jar to a stub script '$BUILD/$PROGRAM_NAME-$PROGRAM_VERSION.run'." >>target/build.log
}
#
function executeJar() {
  manifest
  compile
  createJar
  echo -e "|_ ${BLUE}9. Execute just created JAR $TARGET/$PROGRAM_NAME-$PROGRAM_VERSION.jar${NC}..."
  echo "$JAR_OPTS -cp \".${FS}${EXTERNAL_JARS}\" -jar $TARGET/$PROGRAM_NAME-$PROGRAM_VERSION.jar \"$@\""
  java $JAR_OPTS -cp ".${FS}$EXTERNAL_JARS" -jar $TARGET/$PROGRAM_NAME-$PROGRAM_VERSION.jar "$@"
  echo "- execute jar '$TARGET/$PROGRAM_NAME-$PROGRAM_VERSION.jar'." >>target/build.log
}
#
function generateEpub() {
  echo -e "|_ ${BLUE}10.1 Generate documentation as E-PUB from '/docs' to '$TARGET/book/book-$PROGRAM_NAME-$PROGRAM_VERSION.epub'${NC}..."
  if ! [ -x "$(command -v pandoc)" ]; then
    echo -e "   |_ ${RED}ERROR: pandoc not available.${NC}"
  else
    rm -Rf $TARGET/book
    mkdir $TARGET/book
    cat docs/*.yml >$TARGET/book/book.mdo
    cat docs/preface.md >>$TARGET/book/book.mdo
    cat docs/chapter-*.md >>$TARGET/book/book.mdo
    mv $TARGET/book/book.mdo $TARGET/book/book.md
    pandoc $TARGET/book/book.md --resource-path=./docs -t epub3 -o $TARGET/book/book-$PROGRAM_NAME-$PROGRAM_VERSION.epub
    echo "   |_ done."
  fi
  echo "- generate an EPUB file '$TARGET/book/book-$PROGRAM_NAME-$PROGRAM_VERSION.epub'." >>target/build.log
}
# TODO https://www.toptal.com/docker/pandoc-docker-publication-chain
function generatePDF() {
  echo -e "|_ ${BLUE}10.2 Generate documentation as PDF from '/docs' to '$TARGET/book/book-$PROGRAM_NAME-$PROGRAM_VERSION.pdf'${NC}..."
  if ! [ -x "$(command -v pandoc)" ]; then
    echo -e "   |_ ${RED}ERROR: pandoc not available.${NC}"
  else
    rm -Rf $TARGET/book/
    mkdir $TARGET/book
    cat docs/*.yml >$TARGET/book/book.mdo
    cat docs/chapter-*.md >>$TARGET/book/book.mdo
    mv $TARGET/book/book.mdo $TARGET/book/book.md
    # see https://stackoverflow.com/questions/29240290/pandoc-for-windows-pdflatex-not-found
    pandoc $TARGET/book/book.md --resource-path=./docs --pdf-engine=xelatex -o $TARGET/book/book-$PROGRAM_NAME-$PROGRAM_VERSION.pdf
    echo -e "   |_ ${GREEN}done$NC"
  fi
  echo "- generate a PDF file '$TARGET/book/book-$PROGRAM_NAME-$PROGRAM_VERSION.pdf'." >>target/build.log
}
#
function sign() {
  # must see here: https://docs.oracle.com/javase/tutorial/security/toolsign/signer.html
  echo "not already implemented... sorry"
}
#
function createZIP() {
  echo -e "|_ ${BLUE}11. Generate Zip distribution archive to $TARGET/$PROGRAM_NAME-$PROGRAM_VERSION.zip${NC}..."
  if ! [ -x "$(command -v zip)" ]; then
    echo -e "   |_ ${RED}ERROR: zip command not available.${NC}"
  else
    cd $BUILD
    zip ../$PROGRAM_NAME-$PROGRAM_VERSION.zip -r * -x "${BUILD}/lib/dep/.idea/*"
    echo -e "   |_ ${GREEN}done$NC"
  fi
  echo "- generate ZIP file '$TARGET/$PROGRAM_NAME-$PROGRAM_VERSION.zip' from '$BUILD' path." >>../build.log
}
#
function help() {
  echo -e "${BLUE}$0${NC} command line usage :"
  echo "---------------------------"
  echo " - a|A|all     : perform all following operations"
  echo " - c|C|compile : compile all sources project"
  echo " - d|D|doc     : generate javadoc for project"
  echo " - e|E|epub    : generate *.epub file as docs for project (require pandoc : https://pandoc.org )"
  echo " - k|K|check   : check code source quality againt rules set (sun or google: see in build.sh for details)"
  echo " - t|T|test    : execute JUnit tests"
  echo " - j|J|jar     : build JAR with all resources"
  echo " - w|W|wrap    : Build and wrap jar as a shell script"
  echo " - z|Z|zip     : create a delivery zip for the full application"
  echo " - p|P|pdf     : generate *.pdf file as docs for project (require pandoc: https://pandoc.org and miktex: https://miktex.org/download)"
  echo " - s|S|sign    : Build and wrap signed jar as a shell script"
  echo " - r|R|run     : execute (and build if needed) the created JAR"
  echo ""
  echo " (c)2023 MIT License Frederic Delorme (@McGivrer) fredericDOTdelormeATgmailDOTcom"
  echo " --"
}
#
function run() {
  echo "Build of program '$PROGRAM_NAME-$PROGRAM_VERSION' ..."
  echo "-----------"
  case $1 in
  a | A | all)
    clearTarget
    manifest
    compile
    checkCodeStyleQA
    executeTests
    generateJavadoc
    generateSourceJar
    createJar
    wrapJar
    createZIP
    ;;
  c | C | compile)
    clearTarget
    manifest
    compile
    ;;
  d | D | doc)
    manifest
    compile
    generateJavadoc
    ;;
  e | E | epub)
    generateEpub
    ;;
  k | K | check)
    checkCodeStyleQA
    ;;
  j | J | jar)
    createJar
    ;;
  t | T | test)
    clearTarget
    manifest
    compile
    executeTests
    ;;
  w | W | wrap)
    wrapJar
    ;;
  p | P | pdf)
    generatePDF
    ;;
  r | R | run)
    executeJar
    ;;
  s | S | sources)
    generateSourceJar $2
    ;;
  z | Z | zip)
    createJar
    wrapJar
    createZIP
    ;;
  h | H | ? | *)
    help
    ;;
  esac
  echo "-----------"
  echo -e "... ${GREEN}done${NC}".
}
#
run "$1"