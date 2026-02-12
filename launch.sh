#!/bin/bash

./pull.sh
./gradlew bootWar
java -Xmx4G -jar build/libs/galio.war
