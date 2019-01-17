@Library('jenkins-shared-library')_
pipeline {
  agent any
  stages {
    stage ('Start') {
      steps {
        sendNotifications 'STARTED'
      }
    }
    stage('Make Environment') {
      parallel {
        stage('Touch local.properties') {
          steps {
            sh 'echo "sdk.dir=/opt/android-sdk-linux" >> local.properties'
          }
        }
        stage('Touch google-services.json') {
                  steps {
                    sh 'echo $GOOGLE_SERVICES_JSON | base64 --decode --ignore-garbage > app/google-services.json'
                  }
                }
        stage('Display directory') {
          steps {
            sh 'ls -la'
          }
        }
      }
    }
    stage('permission') {
      steps {
        sh 'chmod 777 gradlew'
      }
    }
    stage('assembleDebug') {
      steps {
        sh './gradlew  assembleDebug --stacktrace'
      }
    }
  }
  post {
    always {
      sendNotifications currentBuild.result
    }
  }
  environment {
  }
}