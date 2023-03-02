#!/bin/bash
sudo yum update
sudo yum upgrade
sudo amazon-linux-extras install -y nginx1
echo Start Java Installation
sudo yum install java-17-amazon-corretto -y
echo "export JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto.x86_64" >>~/.bashrc
echo "export PATH=$PATH:$JAVA_HOME/bin" >>~/.bashrc
java --version
echo completed Java Installation
sudo yum install -y tomcat - y
sudo systemctl start tomcat
sudo systemctl enable tomcat
sudo chmod 770 /home/ec2-user/cloudapp-0.0.1-SNAPSHOT.jar
sudo cp /tmp/webservice.service /etc/systemd/system
sudo chmod 770 /etc/systemd/system/webservice.service
sudo systemctl start webservice.service
sudo systemctl enable webservice.service
sudo systemctl restart webservice.service
sudo systemctl status webservice.service
echo '****** Copied webservice! *******'