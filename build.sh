#!/bin/bash
#!/bin/sh
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
# Uncomment the one to be used
export CHECK_RULES=sun
#export CHECK_RULES=google

# ---- JDK and sources versions (mainly for manifest generator)
export JAVA_BUILD=$(java --version | head -1 | cut -f2 -d' ')
export GIT_COMMIT_ID=$(git rev-parse HEAD)
#
# Paths
export SRC=src
export LIBS=lib
export LIB_TEST=$LIBS/test/junit-platform-console-standalone-1.10.0.jar
export LIB_CHECKSTYLES=$LIBS/tools/checkstyle-10.12.3-all.jar
export TARGET=target
export BUILD=$TARGET/build
export CLASSES=$TARGET/classes
export TESTCLASSES=$TARGET/test-classes
export RESOURCES=$SRC/main/resources
export TESTRESOURCES=$SRC/test/resources
export JAR_NAME=$PROGRAM_NAME-$PROGRAM_VERSION.jar
export JAR_JAVADOC_NAME=$PROGRAM_NAME-$PROGRAM_VERSION-javadoc.jar
# ---- to enforce preview compatibility use the --enable-preview mode,
# ---- for more information, see https://docs.oracle.com/en/java/javase/18/language/preview-language-and-vm-features.html
export COMPILATION_OPTS="--enable-preview"
#export COMPILATION_OPTS="--enable-preview -Xlint:preview"
#export COMPILATION_OPTS="--enable-preview -Xlint:unchecked -Xlint:preview"
# ---- to execute JAR one JDK preview, add the same attribute on JAR execution command line
export JAR_OPTS=--enable-preview
# ---- define the checkstyle rule set file
export CHECK_RULES_FILE=$LIBS/tools/rules/${CHECK_RULES}_checks.xml
#
echo "> Java version : $JAVA_BUILD"
echo "> Git   commit : $GIT_COMMIT_ID"
echo "> Encoding     : $SOURCE_ENCODING"
#
echo "> Prepare environement (if .sdkmanrc file exists)"
if [ -f .sdkmanrc ]; then
  echo " |_ file sdkmanrc detected"
  source "$HOME/.sdkman/bin/sdkman-init.sh"
  sdk env install
  sdk env use
fi
# prepare target
echo "> Clear build workspace"
rm -rf ${TARGET}
mkdir -p ${CLASSES}
#
function manifest() {
  # build manifest
  echo "|_ 1. Create Manifest file '${TARGET}/MANIFEST.MF'"
  echo """Manifest-Version: ${PROGRAM_NAME}
Main-Class: ${MAIN_CLASS}
Created-By: ${JAVA_BUILD}
Implementation-Title: ${PROGRAM_NAME}
Implementation-Version: ${PROGRAM_VERSION}-build_${GIT_COMMIT_ID:0:12}
Implementation-Vendor: ${VENDOR_NAME}
Implementation-Author: ${AUTHOR_NAME}""" >>${TARGET}/MANIFEST.MF
  echo "   |_ done"
}
#
function compile() {
  echo "|_ 2. Compile sources from '$SRC/main' ..."
  echo "> from : ${SRC}"
  echo "> to   : ${CLASSES}"
  echo "> with : ${EXTERNALJARS}"
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
    -cp ".;${EXTERNAL_JARS};${CLASSES}" @$TARGET/sources.lst


  echo "   done."
}
function checkCodeStyleQA() {
  echo "|_ 3. Check code quality against rules $CHECK_RULES"
  echo "> explore sources at : $SRC"
  find $SRC/main -name '*.java' >$TARGET/sources.lst
  java $JAR_OPTS -cp "$LIB_CHECKSTYLES:$EXTERNAL_JARS:$CLASSES:." \
    -jar $LIB_CHECKSTYLES \
    -c $CHECK_RULES_FILE \
    -f xml \
    -o $TARGET/checkstyle_errors.xml \
    @$TARGET/sources.lst
  echo "   done."
}
function generatedoc() {
  echo "|_ 4. Generate Javadoc "
  echo "> from : $SRC"
  echo "> to   : $TARGET/javadoc"
  # prepare target
  mkdir -p $TARGET/javadoc
  mkdir -p $SRC/main/javadoc
  # Compile class files
  rm -Rf $TARGET/javadoc/*
  java -jar ./lib/tools/markdown2html-0.3.1.jar <README.md >$SRC/javadoc/overview.html
  javadoc $JAR_OPTS -source $SOURCE_VERSION \
    -author -use -version \
    -doctitle \"$PROGRAM_NAME\" \
    -d $TARGET/javadoc \
    -overview $TARGET/javadoc/overview.html \
    $JAVADOC_GROUPS \
    -sourcepath $SRC/main/java:$SRC/main/javadoc \
    -subpackages $JAVADOC_CLASSPATH
  cd $TARGET/javadoc
  jar cvf ../$JAR_JAVADOC_NAME *
  cd ../../
  echo "   done." >>target/build.log
}
#
function generateSourceJar() {
  echo "|_ 5. Generate JAR sources $TARGET/${PROGRAM_NAME}-sources-${PROGRAM_VERSION}.jar"
  echo "> from : $SRC"
  echo "> to   : $TARGET/"
  jar cvf ${TARGET}/${PROGRAM_NAME}-${PROGRAM_VERSION}-sources.jar -C src .
  echo "   JAR containing sources generation done." >>target/build.log
}
#
function executeTests() {
  echo "|_ 6. Execute tests"
  echo "> from : $SRC/test"
  echo "> to   : $TARGET/test-classes"
  mkdir -p $TARGET/test-classes
  echo "copy test resources"
  cp -r ./$RESOURCES/* $TESTCLASSES
  cp -r ./$TESTRESOURCES/* $TESTCLASSES
  echo "compile test classes"
  #list test sources
  find $SRC/main -name '*.java' >$TARGET/sources.lst
  find $SRC/test -name '*.java' >$TARGET/test-sources.lst
  javac -source $SOURCE_VERSION -encoding $SOURCE_ENCODING $COMPILATION_OPTS -cp ".;$LIB_TEST;${EXTERNAL_JARS}" -d $TESTCLASSES @$TARGET/sources.lst @$TARGET/test-sources.lst
  echo "execute tests through JUnit"
  java $JAR_OPTS -jar $LIB_TEST --cp "$CLASSES;$TESTCLASSES;." --scan-class-path
  echo "done."
}
#
function createJar() {
  echo "|_ 7. package jar file '$TARGET/$JAR_NAME'..."
  if ([ $(ls $CLASSES | wc -l | grep -w "0") ]); then
    echo 'No compiled class files'
  else
    # Build JAR
    jar -cfmv $TARGET/$JAR_NAME $TARGET/MANIFEST.MF -C $CLASSES . -C $RESOURCES .
  fi

  echo "   |_ done."
}
#
function wrapJar() {
  # create runnable program
  echo "|_ 8. create run file '$BUILD/$PROGRAM_NAME-$PROGRAM_VERSION.run'..."
  mkdir -p $BUILD
  cat $LIBS/stub.sh $TARGET/$PROGRAM_NAME-$PROGRAM_VERSION.jar >$BUILD/$PROGRAM_NAME-$PROGRAM_VERSION.run
  chmod +x $BUILD/$PROGRAM_NAME-$PROGRAM_VERSION.run
  echo "   |_ done."
}
#
function executeJar() {
  manifest
  compile
  createJar
  echo "|_ 99. Execute just created JAR $TARGET/$PROGRAM_NAME-$PROGRAM_VERSION.jar"
  java $JAR_OPTS -cp ".;$ETERNAL_JAR" -jar $TARGET/$PROGRAM_NAME-$PROGRAM_VERSION.jar "$@"
}
#
function generateEpub() {
  echo "|_ 9. Generate documentation as E-PUB from '/docs'"
  rm -Rf $TARGET/book
  mkdir $TARGET/book
  cat docs/*.yml >$TARGET/book/book.mdo
  cat docs/preface.md >>$TARGET/book/book.mdo
  cat docs/chapter-*.md >>$TARGET/book/book.mdo
  mv $TARGET/book/book.mdo $TARGET/book/book.md
  pandoc $TARGET/book/book.md --resource-path=./docs -t epub3 -o $TARGET/book/book-$PROGRAM_NAME-$PROGRAM_VERSION.epub
  echo "|_ 6. generate ebook to $TARGET/book/book-$PROGRAM_NAME-$PROGRAM_VERSION.epub"
}
# TODO https://www.toptal.com/docker/pandoc-docker-publication-chain
function generatePDF() {
  echo "|_ 10. Generate documentation as PDF from '/docs'"
  rm -Rf $TARGET/book/
  mkdir $TARGET/book
  cat docs/*.yml >$TARGET/book/book.mdo
  cat docs/chapter-*.md >>$TARGET/book/book.mdo
  mv $TARGET/book/book.mdo $TARGET/book/book.md
  # see https://stackoverflow.com/questions/29240290/pandoc-for-windows-pdflatex-not-found
  pandoc $TARGET/book/book.md --resource-path=./docs --pdf-engine=xelatex -o $TARGET/book/book-$PROGRAM_NAME-$PROGRAM_VERSION.pdf
  echo "|_ 6. generate pdf book to $TARGET/book/book-$PROGRAM_NAME-$PROGRAM_VERSION.pdf"
}
#
function sign() {
  # must see here: https://docs.oracle.com/javase/tutorial/security/toolsign/signer.html
  echo "not already implemented... sorry"
}
#
function help() {
  echo "$0 command line usage :"
  echo "---------------------------"
  echo " - a|A|all     : perform all following operations"
  echo " - c|C|compile : compile all sources project"
  echo " - d|D|doc     : generate javadoc for project"
  echo " - e|E|epub    : generate *.epub file as docs for project (require pandoc : https://pandoc.org )"
  echo " - k|K|check   : check code source quality againt rules set (sun or google: see in build.sh for details)"
  echo " - t|T|test    : execute JUnit tests"
  echo " - j|J|jar     : build JAR with all resources"
  echo " - w|W|wrap    : Build and wrap jar as a shell script"
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
    manifest
    compile
    checkCodeStyleQA
    executeTests
    generatedoc
    generateSourceJar
    createJar
    wrapJar
    ;;
  c | C | compile)
    manifest
    compile
    ;;
  d | D | doc)
    manifest
    compile
    generatedoc
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
  h | H | ? | *)
    help
    ;;
  esac
  echo "-----------"
  echo "... done".
}
#
run "$1"