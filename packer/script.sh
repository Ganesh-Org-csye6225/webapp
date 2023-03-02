#!/bin/bash
sudo yum update
sudo yum upgrade
sudo amazon-linux-extras install -y nginx1
echo Start Java Installation
sudo yum install java-17-amazon-corretto -y
echo "export JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto.x86_64" >>~/.bashrc
echo "export PATH=$PATH:$JAVA_HOME/bin" >>~/.bashrc
java --version
sudo yum install maven -y
echo completed Java Installation
sudo yum install -y tomcat - y
sudo systemctl start tomcat
sudo systemctl enable tomcat
# sudo amazon-linux-extras install -y epel
# sudo amazon-linux-extras install postgresql11 -y
# sudo amazon-linux-extras enable postgresql11
# sudo yum install postgresql-server -y
# sudo postgresql-setup initdb
# sudo sed -i 's/ident/md5/g' /var/lib/pgsql/data/pg_hba.conf
# sudo systemctl start postgresql
# sudo systemctl enable postgresql
# sudo -i -u postgres psql -c  "ALTER USER postgres PASSWORD '1234';"
# sudo -i -u postgres psql -c "CREATE DATABASE pawan;"
sudo chmod 770 /home/ec2-user/cloudapp-0.0.1-SNAPSHOT.jar
sudo cp /tmp/webservice.service /etc/systemd/system
sudo chmod 770 /etc/systemd/system/webservice.service
cd home/ec2-user/
sudo systemctl start webservice.service
sudo systemctl enable webservice.service
sudo systemctl restart webservice.service
sudo systemctl status webservice.service
echo '****** Copied webservice! *******'