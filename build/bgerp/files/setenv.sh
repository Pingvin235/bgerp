JAVA_HOME=

if [ -z "$JAVA_HOME" ]; then
  echo "The JAVA_HOME environment variable is not defined"
  echo "This environment variable is needed to run this program"
  exit 1
fi