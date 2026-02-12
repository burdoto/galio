#!/bin/bash

./pull.sh
./gradlew clean bootWar
java -Xmx4G -jar build/libs/galio.war
