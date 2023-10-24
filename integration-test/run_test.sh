echo "Run test ..."
rm -rf reports

echo "Command to execute ..."
echo "behave --format html -o reports/index.html --junit-directory=./reports --junit --tags=$TAGS --summary --show-timings -v"

behave --format html -o reports/index.html --junit-directory=./reports --junit --tags=$TAGS --summary --show-timings -v
