#!/bin/sh
. ./hostname.sh
APP_NAME=radar-ui.jar
#nohup  java -jar $APP_NAME --spring.profiles.active=${spring_profiles_active} -Deureka.instance.hostname=${hostname}>>logs/start.log 2>>logs/startError.log &
# -server -Xms5000m -Xmx6000m
nohup  java -jar -server -Xms2000m -XX:+UseG1GC $APP_NAME  --server.port=8082>>logs/start.log 2>>logs/startError.log &

sleep 15

if test $(pgrep -f $APP_NAME|wc -l) -eq 0
then
   echo "start failed"
else
   echo "start successed"
fi