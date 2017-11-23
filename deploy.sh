#!/usr/bin/env bash

#hlt auth

./gradlew build

zip -r bot.zip .

hlt bot -b bot.zip

rm bot.zip
