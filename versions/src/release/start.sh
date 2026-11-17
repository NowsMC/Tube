#!/bin/sh
cd "$(dirname "$0")" || exit 1

JAVA="java"
if [ -n "$JAVA_HOME" ]; then
    JAVA="$JAVA_HOME/bin/java"
fi

MAJOR=$("$JAVA" -version 2>&1 | head -n 1 | sed -E 's/.*version "([0-9]+).*/\1/')
if [ -z "$MAJOR" ] || [ "$MAJOR" -lt 25 ] 2>/dev/null; then
    echo "Tube needs Java 25 or newer. Install it or set JAVA_HOME."
    exit 1
fi

exec "$JAVA" -Xms1G -Xmx2G -jar "@{tube.server.jar}" "$@"
