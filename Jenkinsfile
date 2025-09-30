pipeline {
  agent any

  tools {
    jdk 'jdk17'
    maven 'maven3'
  }

  options { timestamps() }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build') {
      steps {
        script {
          def sha = env.GIT_COMMIT.take(7)
          env.VERSION = "1.0.${env.BUILD_NUMBER}-${sha}"
        }
        bat 'mvn -B -q -DskipTests package'
        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
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
        withSonarQubeEnv('sonarqube') {  // must match the name in Jenkins config
          bat '''
            mvn -B -q sonar:sonar ^
              -Dsonar.projectKey=d6d1ee10b16fe72188f1a51add9fda12a940a06b ^
              -Dsonar.organization=My-app ^
              -Dsonar.login=%SONAR_AUTH_TOKEN%
          '''
        }
      }
      post {
        success {
          script {
            timeout(time: 10, unit: 'MINUTES') {
              def qg = waitForQualityGate()
              if (qg.status != 'OK') {
                error "Quality gate failed: ${qg.status}"
              }
            }
          }
        }
      }
    }
  }

  post {
    always { echo "Build: ${env.BUILD_TAG}, Version: ${env.VERSION}" }
    failure { echo 'Build failed — check stage logs and reports.' }
  }
}
