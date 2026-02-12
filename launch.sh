#!/bin/bash

./pull.sh
./gradlew simpleWar
java -Xmx4G -jar build/libs/galio.war
