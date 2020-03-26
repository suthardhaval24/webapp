#!/bin/bash
cd /home/ubuntu
source /etc/environment
sudo chown -R ubuntu:ubuntu /home/ubuntu/
sudo chmod +x WebApplication-0.0.1-SNAPSHOT.jar
printenv
nohup printenv > /home/ubuntu/environment.txt
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/home/ubuntu/CloudWatchConfiguration.json -s
kill -9 $(ps -ef|grep WebApplication | grep -v grep | awk '{print$2}')
echo Starting WebApplication on ec2
nohup java -Dserver.port=8081 -jar WebApplication-0.0.1-SNAPSHOT.jar > /home/ubuntu/output.txt 2> /home/ubuntu/output.txt &