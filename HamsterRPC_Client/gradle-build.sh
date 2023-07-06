#!/bin/bash

if [ -n "$1" ] ; then
    httpHost=$(sed -rn "s|http://(.*):.*|http\.proxyHost=\1|p" <<< $1)
    httpPort=$(sed -rn "s|http://.*:(.*)|http\.proxyPort=\1|p" <<< $1)
    httpsHost=$(sed -rn "s|http://(.*):.*|https\.proxyHost=\1|p" <<< $1)
    httpsPort=$(sed -rn "s|http://.*:(.*)|https\.proxyPort=\1|p" <<< $1)

    ./gradlew -D"$httpHost" -D"$httpPort" -D"$httpsHost" -D"$httpsPort" $2
else
    ./gradlew $2
fi
