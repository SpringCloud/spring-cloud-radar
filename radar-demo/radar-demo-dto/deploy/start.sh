#!/bin/sh
. ./hostname.sh
APP_NAME=radar-demo-provider.jar
#nohup  java -jar $APP_NAME --spring.profiles.active=${spring_profiles_active} -Deureka.instance.hostname=${hostname}>>logs/start.log 2>>logs/startError.log &
# -server -Xms5000m -Xmx6000m
nohup  java -jar -server -Xms5000m -Xmx6000m -XX:+UseG1GC $APP_NAME >>logs/start.log 2>>logs/startError.log &

sleep 15

if test $(pgrep -f $APP_NAME|wc -l) -eq 0
then
   echo "start failed"
else
   echo "start successed"
fi