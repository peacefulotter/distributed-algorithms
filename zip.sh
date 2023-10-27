echo "Removing submission.zip"
rm submission.zip
echo "Creating submission.zip"
zip -r submission.zip src bin build.sh cleanup.sh run.sh pom.xml
