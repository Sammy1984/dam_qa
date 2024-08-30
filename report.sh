#!/bin/bash

# Директория где хранятся результаты аллюра
ALLURE_RESULTS_DIRECTORY="target/allure-results"
# Проект в который будет записываться отчет
PROJECT_ID=$1

# Очистить директорию с результатами
clearResult=$(curl -u $ALLURE_SERVER_USERNAME:$ALLURE_SERVER_PASSWORD -X GET $ALLURE_SERVER"/allure-docker-service/clean-results?project_id=$PROJECT_ID" -H  "accept: */*")
echo "$clearResult"
# Получаем путь до баш крипта
SCRIPT_PATH="${BASH_SOURCE[0]}"
# Строим полный путь
DIR="$(cd "$(dirname "$SCRIPT_PATH")" >/dev/null 2>&1 && pwd)"
# Собираем файлы для отправки
FILES_TO_SEND=$(find $DIR/$ALLURE_RESULTS_DIRECTORY/ -type f | grep -v /$)

# Если нет файлов прерываем процесс
if [ -z "$FILES_TO_SEND" ]; then
  echo "Nothing to send"
  exit 0
fi

# Строим url с файлами
FILES=''
for FILE in $FILES_TO_SEND; do
  FILES+="-F files[]=@$FILE "
done

# Отправляем через curl запрос на заливку отчета
#set -o xtrace
echo "------------------SEND-RESULTS------------------"
sendResultsResponse=$(curl -u $ALLURE_SERVER_USERNAME:$ALLURE_SERVER_PASSWORD -X POST $ALLURE_SERVER"/allure-docker-service/send-results?project_id=$PROJECT_ID" -H 'Content-Type: multipart/form-data' $FILES -ik)
echo "$sendResultsResponse"
# Если нужно сгенерировать отчет, нужно отправить запрос на эндпоинт GET /generate-report и выставить >> CHECK_RESULTS_EVERY_SECONDS: NONE в контейнере с отчетами
#curl -X GET 'http://localhost:5050/allure-docker-service/generate-report?project_id=default&execution_name=test_exec&execution_from=http://local.com&execution_type=bobobob'
echo "------------------GENERATE-REPORT------------------"
EXECUTION_NAME='Gitlab-CI'
EXECUTION_FROM=$CI_PIPELINE_URL
EXECUTION_TYPE='Gitlab-Gradle'
# Отправляем через curl запрос на генерацию отчета
generateResponse=$(curl -u $ALLURE_SERVER_USERNAME:$ALLURE_SERVER_PASSWORD -X GET $ALLURE_SERVER"/allure-docker-service/generate-report?project_id=$PROJECT_ID&execution_name=$EXECUTION_NAME&execution_from=$EXECUTION_FROM&execution_type=$EXECUTION_TYPE" $FILES)
echo "$generateResponse"

allureReport=$(sed -E 's/.*"report_url":"?([^,"]*)"?.*/\1/' <<<"$generateResponse")

echo "------------------allure------------------"
export ALLURE_REPORT_URL="${allureReport}"
echo "ALLURE_REPORT_URL=$ALLURE_REPORT_URL" >> .env
echo "ALLURE_REPORT_URL=$ALLURE_REPORT_URL"

echo "-------------allure docker----------------"
echo "ALLURE_DOCKER_URL=https://allure.k-stage.sbmt.io/allure-docker-service-ui/projects/$PROJECT_ID"

rm -rf /tmp/results.zip
