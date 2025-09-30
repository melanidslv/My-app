pipeline {
  agent any

  environment {
    REGISTRY = 'ghcr.io/My-org'                // or docker.io/your-user
    IMAGE    = "${env.REGISTRY}/My-app"
    APP_PORT = '8081'
  }

  options {
    timestamps()
    ansiColor('xterm')
    buildDiscarder(logRotator(numToKeepStr: '20'))
  }

  tools {
    jdk 'jdk17'          // configure in Jenkins Global Tool Config
    maven 'maven3'       // configure in Jenkins Global Tool Config
  }

  triggers {
    pollSCM('@daily')    // or use GitHub webhook
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build') {
      steps {
        sh 'mvn -B -q -f app/pom.xml -DskipTests package'
        script {
          env.VERSION = sh(script: 'scripts/version.sh', returnStdout: true).trim()
          sh """
            docker build -t ${IMAGE}:${VERSION} -f Dockerfile .
            docker tag ${IMAGE}:${VERSION} ${IMAGE}:latest
          """
        }
        archiveArtifacts artifacts: 'app/target/*.jar', fingerprint: true
      }
    }

    stage('Test') {
      steps {
        sh 'mvn -B -q -f app/pom.xml test'
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: 'app/target/surefire-reports/*.xml'
        }
      }
    }

    stage('Code Quality (SonarQube)') {
      steps {
        withSonarQubeEnv('sonarqube') {
          sh 'mvn -B -q -f app/pom.xml sonar:sonar'
        }
      }
      post {
        success {
          script {
            timeout(time: 10, unit: 'MINUTES') {
              def qg = waitForQualityGate()
              if (qg.status != 'OK') {
                error "Pipeline aborted due to quality gate failure: ${qg.status}"
              }
            }
          }
        }
      }
    }

    stage('Security') {
      parallel {
        stage('Dependency Scan (OWASP)') {
          steps {
            sh 'mvn -B -q -f app/pom.xml org.owasp:dependency-check-maven:check -Dformat=ALL'
          }
          post {
            always {
              publishHTML(target: [
                reportDir: 'app/target/dependency-check-report',
                reportFiles: 'dependency-check-report.html',
                reportName: 'OWASP Dependency-Check'
              ])
              archiveArtifacts artifacts: 'app/target/dependency-check-report/*.*', allowEmptyArchive: true
            }
          }
        }
        stage('Image Scan (Trivy)') {
          steps {
            sh "scripts/trivy_scan.sh ${IMAGE} ${VERSION}"
          }
          post {
            always {
              archiveArtifacts artifacts: 'trivy-image-report.*', allowEmptyArchive: true
            }
            unsuccessful { unstable('High/Critical image vulnerabilities found (see Trivy report).') }
          }
        }
      }
    }

    stage('Push Image') {
      when { expression { return env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'master' } }
      steps {
        withCredentials([usernamePassword(credentialsId: 'docker-reg-cred', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh """
            echo "$DOCKER_PASS" | docker login ${env.REGISTRY} -u "$DOCKER_USER" --password-stdin
            docker push ${IMAGE}:${VERSION}
            docker push ${IMAGE}:latest
          """
        }
      }
    }

    stage('Deploy: Staging') {
      steps {
        sshagent(credentials: ['staging-ssh']) {
          sh "scripts/deploy.sh staging.example.com docker/docker-compose.staging.yml ${IMAGE} ${VERSION}"
        }
        sh "scripts/smoke_test.sh http://staging.example.com:${APP_PORT}/actuator/health"
      }
    }

    stage('Release & Promote') {
      when { expression { return env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'master' } }
      steps {
        input message: "Promote ${env.VERSION} to PRODUCTION?"
        sh """
          git config user.email "ci@example.com"
          git config user.name "Jenkins CI"
          git tag -a v${VERSION} -m "Release ${VERSION}" || true
          git push origin v${VERSION} || true
        """
      }
    }

    stage('Deploy: Production') {
      when { expression { return env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'master' } }
      steps {
        sshagent(credentials: ['prod-ssh']) {
          sh "scripts/deploy.sh prod.example.com docker/docker-compose.prod.yml ${IMAGE} ${VERSION}"
        }
        sh "scripts/smoke_test.sh http://prod.example.com:${APP_PORT}/actuator/health"
      }
    }

    stage('Monitoring Hook') {
      steps {
        echo "Monitoring is external (Prometheus/Grafana or Datadog/New Relic)."
        echo "Prometheus scrapes /actuator/prometheus. Alerts defined in monitoring/alerts.yml."
      }
    }
  }

  post {
    always {
      echo "Build: ${env.BUILD_TAG}, Version: ${env.VERSION}"
    }
    failure {
      echo 'Build failed — check stage logs and reports.'
    }
  }
}
