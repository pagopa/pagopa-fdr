echo "Run test ..."
#rm -rf results

echo "Command to execute ..."
echo "behave --format html -o reports/index.html --junit-directory=./reports --junit --tags=$TAGS --summary --show-timings -v"


#behave --format html -o reports/index.html --junit-directory=./results --junit --tags=$TAGS --summary --show-timings -v
behave --format allure_behave.formatter:AllureFormatter -o results --tags=$TAGS --summary --show-timings -v

rm -rf results/history && cp -R reports/history results/history
allure generate results -o reports --clean
