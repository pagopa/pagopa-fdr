echo "Run test ..."
rm -rf report reports

echo "Command to execute ..."
echo "behave --junit-directory=./report-nodo --junit ./features$FEATURES_PATH --tags=runnable,midRunnable --summary --show-timings -v"

#behave --junit-directory=./report --junit ./features$FEATURES_PATH --tags=runnable,midRunnable --summary --show-timings -v
behave --format html -o reports/index.html --junit-directory=./reports --junit --tags=runnable,midRunnable --summary --show-timings -v
