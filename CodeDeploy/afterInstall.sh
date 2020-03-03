#!/bin/bash
cd /home/ubuntu
sudo chown -R ubntu:ubntu /home/ubntu/
sudo chmod +x WebApplication-0.0.1-SNAPSHOT.jar
source /etc/environment
echo Starting WebApplication
java -jar WebApplication-0.0.1-SNAPSHOT.jar