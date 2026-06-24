pipeline {
    agent any

    tools {
        maven 'Maven3'   // must match name in Jenkins Global Tool Configuration
        jdk 'JDK17'
    }

    options {
        disableConcurrentBuilds()   // avoids container name/port clashes if nightly cron
                                     // and a push-triggered run overlap
    }

    triggers {
        cron('H 2 * * *')   // nightly run, independent of push triggers
    }

    parameters {
        // Set by Job A on success; defaults to 'latest' for manual/local-triggered runs
        string(name: 'APP_TAG', defaultValue: 'latest', description: 'Tag of the shopping-cart image to test')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Cleanup Previous Run') {
            steps {
                // Guards against leftover containers from a crashed/aborted prior run
                // blocking this run's 'docker-compose up' on name/port conflicts.
                sh 'docker-compose down -v || true'
            }
        }

        stage('Start App + Grid') {
            environment {
                APP_TAG = "${params.APP_TAG}"
            }
            steps {
                sh 'docker-compose up -d'

                sh '''
                  echo "Waiting for Selenium Grid..."
                  retries=0
                  until curl -sf http://localhost:4444/wd/hub/status | grep '"ready":true' > /dev/null; do
                    retries=$((retries+1))
                    if [ $retries -eq 20 ]; then
                      echo "Grid failed to become ready in time"
                      exit 1
                    fi
                    sleep 3
                  done
                  echo "Grid is ready."
                '''

                sh '''
                  echo "Waiting for shopping-cart app..."
                  retries=0
                  until curl -sf http://localhost:8080 > /dev/null; do
                    retries=$((retries+1))
                    if [ $retries -eq 20 ]; then
                      echo "App failed to start in time"
                      exit 1
                    fi
                    sleep 5
                  done
                  echo "App is ready."
                '''
            }
        }

        stage('Run Tests') {
            steps {
                sh 'mvn clean test -Dgrid.url=http://localhost:4444/wd/hub -Dapp.url=http://shopping-cart-app:8080'
            }
        }

        stage('Publish Report') {
            steps {
                junit '**/target/surefire-reports/*.xml'
                archiveArtifacts artifacts: 'target/screenshots/**', allowEmptyArchive: true
            }
        }
    }

    post {
        always {
            sh 'docker-compose down -v || true'
        }
        failure {
            // Placeholder - wire up to your actual notification channel
            // e.g. mail to: 'you@example.com', subject: "QA Tests Failed: #${env.BUILD_NUMBER}", body: "${env.BUILD_URL}"
            echo "QA tests failed - notify here (email/Slack) - ${env.BUILD_URL}"
        }
    }
}
