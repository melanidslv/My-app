pipeline {
  agent any

  tools {
    jdk 'jdk17'
    maven 'maven3'
  }

  options {
    timestamps()
    buildDiscarder(logRotator(numToKeepStr: '20'))
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
  }

  post {
    success {
      echo "Build & tests passed on Windows Jenkins"
    }
    failure {
      echo "Build failed-check logs"
    }
  }
}
