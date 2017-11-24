#!/usr/bin/env bash

#hlt auth

./gradlew build

mv ./build/libs/MyBot.jar .

zip -r MyBot.zip .

hlt bot -b MyBot.zip

rm MyBot.zip
