pipeline {
  agent any

  parameters {
    booleanParam(name: 'DEPLOY_LOCAL', defaultValue: true, description: 'Deploy to local Docker Desktop?')
    booleanParam(name: 'DOCKER_BUILD', defaultValue: true, description: 'Build Docker image?')
    string(name: 'APP_PORT', defaultValue: '8081', description: 'Application port for health check')
    string(name: 'IMAGE', defaultValue: 'My-app', description: 'Docker image name')
    string(name: 'VERSION', defaultValue: 'latest', description: 'Docker image tag')
  }

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

    stage('Quality Gate') {
      steps {
        timeout(time: 3, unit: 'MINUTES') {
          waitForQualityGate abortPipeline: true
        }
      }
    }

    stage('Package Artifact') {
      steps {
        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
      }
    }

    stage('Security Scan') {
      steps {
        echo "Running security scan (placeholder stage)..."
      }
    }

    stage('Deploy: Docker') {
      when {
        expression { return params.DEPLOY_LOCAL }
      }
      steps {
        bat '''
          echo Building Docker image...
          docker build -t %IMAGE%:%VERSION% -t %IMAGE%:latest -f Dockerfile .

          echo Stopping old container (if running)...
          docker stop my-app || exit 0
          docker rm my-app || exit 0

          echo Starting new container...
          docker run -d --name my-app -p %APP_PORT%:8081 %IMAGE%:%VERSION%
        '''
      }
    }

    stage('Release') {
      steps {
        echo "Promoting build to Production (placeholder stage)..."
      }
    }

    stage('Monitoring') {
      steps {
        echo "Monitoring application health (placeholder stage)..."
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
