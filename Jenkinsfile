pipeline {
  agent any

  tools {
    jdk 'jdk17'
    maven 'maven3'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build') {
      steps {
        bat 'mvn -B -q -DskipTests package'
      }
    }

    stage('Test') {
      steps {
        bat 'mvn -B -q test'
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
        }
      }
    }

    stage('Code Quality: SonarCloud') {
      steps {
        withSonarQubeEnv('sonarqube') {
          withCredentials([string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')]) {
            bat """
              mvn clean verify sonar:sonar ^
                -Dsonar.projectKey=melanidslv_My-app ^
                -Dsonar.organization=melanidslv ^
                -Dsonar.host.url=https://sonarcloud.io ^
                -Dsonar.token=%SONAR_TOKEN%
            """
          }
        }
      }
    }
  }

  post {
    always {
      echo "Build completed: ${env.BUILD_TAG}"
    }
    failure {
      echo "Build failed — check logs."
    }
  }
}
