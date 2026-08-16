```bash
sudo apt-get update
sudo apt-get install apt-transport-https ca-certificates gnupg curl

curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo gpg --dearmor -o /usr/share/keyrings/cloud.google.gpg

sudo apt-get update && sudo apt-get install google-cloud-cli

gcloud auth login 
gcloud auth application-default login 


adc_file=/home/ts/.config/gcloud/application_default_credentials.json


```