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

    stage('SonarQube Analysis') {
      when { expression { return params.RUN_SONAR } }
      steps {
        withSonarQubeEnv('sonarqube') {
          bat 'mvn -B -q sonar:sonar'
        }
      }
    }

    stage('Docker Build') {
      when { expression { return params.DOCKER_BUILD } }
      steps {
        bat """
          docker build -t %IMAGE%:%VERSION% -t %IMAGE%:latest -f Dockerfile .
        """
      }
    }

    stage('Trivy Scan') {
      when { expression { return params.DOCKER_BUILD && params.RUN_TRIVY } }
      steps {
        bat """
          docker run --rm -v %cd%:/workspace aquasec/trivy:latest image %IMAGE%:%VERSION%
        """
      }
    }

    stage('Push Image') {
      when { expression { return params.DOCKER_BUILD && params.PUSH_IMAGE } }
      steps {
        withCredentials([usernamePassword(credentialsId: 'docker-reg-cred', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          bat '''
            echo %DOCKER_PASS% | docker login %REGISTRY% -u %DOCKER_USER% --password-stdin
            docker push %IMAGE%:%VERSION%
            docker push %IMAGE%:latest
          '''
        }
      }
    }
  }

  post {
    success {
      echo "✅ Build & tests passed on Windows Jenkins"
    }
    failure {
      echo "❌ Build failed — check logs"
    }
  }
}
