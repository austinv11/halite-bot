#!/usr/bin/env bash

rm -f *.log && rm -f ./build/libs/MyBot.jar && ./gradlew build && ./halite -d '160 160' -t 'java -jar ./build/libs/MyBot.jar' 'java -jar ./build/libs/MyBot.jar'
