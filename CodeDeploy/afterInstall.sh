#!/bin/bash
cd /home/ubuntu
sudo chown -R ubuntu:ubuntu /home/ubuntu/
sudo chmod +x WebApplication-0.0.1-SNAPSHOT.jar
source /etc/environment
kill -9 $(ps -ef|grep webApp | grep -v grep | awk '{print$2}')
echo Starting WebApplication
nohup java -jar WebApplication-0.0.1-SNAPSHOT.jar > /home/ubuntu/output.txt