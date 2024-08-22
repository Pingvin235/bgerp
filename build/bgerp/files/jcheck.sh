MINIMAL_MAJOR_VERSION="21"

# search java executable in JAVA_HOME
if [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]] && [[ -x "$JAVA_HOME/bin/javac" ]];  then
    _java="$JAVA_HOME/bin/java"
else
    echo "The java and javac binaries are not found in JAVA_HOME='$JAVA_HOME'"
    exit 1
fi

if [[ "$_java" ]]; then
    version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo "Using Java version '$version'"

    major_version="${version%%.*}"
    if [[ "$major_version" -lt "$MINIMAL_MAJOR_VERSION" ]]; then
        echo "This version of Java ($major_version) is below the minimum required version of $MINIMAL_MAJOR_VERSION"
        exit 1
    fi
fi