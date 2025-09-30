pipeline {
  agent any

  parameters {
    booleanParam(name: 'DEPLOY_LOCAL', defaultValue: true, description: 'Deploy to local Docker Desktop?')
    booleanParam(name: 'DOCKER_BUILD', defaultValue: true, description: 'Build Docker image?')
    string(name: 'APP_PORT', defaultValue: '8081', description: 'Application port for health check')
    string(name: 'IMAGE', defaultValue: 'my-app', description: 'Docker image name')
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

    stage('Docker Build') {
      when { expression { return params.DOCKER_BUILD } }
      steps {
        sh """
          docker build -t ${IMAGE}:${VERSION} \
                       -t ${IMAGE}:latest \
                       -f Dockerfile .
        """
      }
    }

    stage('Deploy: Local (Docker Desktop)') {
      when { expression { return params.DEPLOY_LOCAL && params.DOCKER_BUILD } }
      steps {
        // Redeploy locally with compose
        sh """
          export IMAGE='${IMAGE}'
          export TAG='${VERSION}'
          docker compose -f docker-compose.local.yml down || true
          docker compose -f docker-compose.local.yml up -d
        """

        // Quick health check
        sh """
          for i in {1..30}; do
            code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:${APP_PORT}/actuator/health || true)
            [ "$code" = "200" ] && exit 0
            sleep 2
          done
          echo "Health check failed"
          exit 1
        """
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
