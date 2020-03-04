#!/bin/bash
cd /home/ubuntu
sudo chown -R ubuntu:ubuntu /home/ubuntu/
sudo chmod +x WebApplication-0.0.1-SNAPSHOT.jar
source /etc/environment
echo Starting WebApplication on ec2
nohup java -Dserver.port=8081 -jar WebApplication-0.0.1-SNAPSHOT.jar > /home/ubuntu/output.txt