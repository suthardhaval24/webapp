#!/bin/bash
cd /home/ubuntu
sudo chown -R ubuntu:ubuntu /home/ubuntu/
sudo chmod +x WebApplication-0.0.1-SNAPSHOT.jar
source /etc/environment
sudo kill $(lsof -t -i:8000)
echo Starting WebApplication
nohup java -jar WebApplication-0.0.1-SNAPSHOT.jar > /home/ubuntu/output.txt