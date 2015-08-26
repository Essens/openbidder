@ECHO OFF

C:
cd /

echo ***********************************************
echo ***********************************************
echo "Building the mobitrans bidder jar"
echo ***********************************************
echo ***********************************************

call mvn -f "T:\Open-Bidder\google-open-bidder-trial-0.7.2\mobitrans-bidder\pom.xml" clean install -U

echo ***********************************************
echo ***********************************************
echo "Building the mobitrans bidder server tar"
echo ***********************************************
echo ***********************************************

call mvn -f "T:\Open-Bidder\google-open-bidder-trial-0.7.2\mobitrans-bidder-server\pom.xml" clean install -U

echo ***********************************************
echo ***********************************************
echo "Deploying the mobitrans bidder server"
echo ***********************************************
echo ***********************************************


call gsutil cp "T:\Open-Bidder\google-open-bidder-trial-0.7.2\mobitrans-bidder-server\target\mobitrans-bidder-server-0.7.2-bin.tar.gz" gs://mobitrans-bidder-bucket-nativertb


echo ***********************************************
echo *********************************************** 
echo "Rebooting all instances"
echo ***********************************************
echo ***********************************************

call gcloud compute instances reset  bidder-4-gce --zone europe-west1-b	
call gcloud compute instances reset  bidder-5-gce --zone europe-west1-b	
call gcloud compute instances reset  bidder-6-gce --zone europe-west1-b	

pause

