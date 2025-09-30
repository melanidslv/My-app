pipeline {
  agent any

  parameters {
    booleanParam(name: 'RUN_SONAR',      defaultValue: true,  description: 'Run SonarQube Code Quality')
    booleanParam(name: 'RUN_DEP_CHECK',  defaultValue: true,  description: 'Run OWASP Dependency-Check')
    booleanParam(name: 'DOCKER_BUILD',   defaultValue: false, description: 'Build Docker image')
    booleanParam(name: 'DEPLOY_STAGING', defaultValue: false, description: 'Deploy to Staging (Docker Compose)')
    booleanParam(name: 'PROMOTE_PROD',   defaultValue: false, description: 'Manual promotion + deploy to Prod')
    booleanParam(name: 'RUN_MONITORING', defaultValue: false, description: 'Start Prometheus + Grafana stack')
  }

  environment {
    APP_PORT = '8081'
    REGISTRY = 'docker.io/melanidslv'     // change if using GHCR
    IMAGE    = "${env.REGISTRY}/My-app"
  }

  tools { jdk 'jdk17'; maven 'maven3' }

  options { timestamps(); buildDiscarder(logRotator(numToKeepStr: '20')) }

  stages {

    stage('Checkout') {
      steps { checkout scm }
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
      steps { bat 'mvn -B -q test' }
      post {
        always { junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml' }
      }
    }

    stage('Code Quality: SonarQube') {
      when { expression { return params.RUN_SONAR } }
      steps {
        withSonarQubeEnv('sonarcloud') {   // must match Jenkins config name
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

    stage('Security: OWASP Dependency-Check') {
      when { expression { return params.RUN_DEP_CHECK } }
      steps {
        bat 'mvn -B org.owasp:dependency-check-maven:check -Dformat=ALL'
      }
      post {
        always {
          publishHTML(target: [
            reportDir: 'target',
            reportFiles: 'dependency-check-report.html',
            reportName: 'OWASP Dependency-Check'
          ])
          archiveArtifacts artifacts: 'target/dependency-check-report.*', allowEmptyArchive: true
        }
      }
    }

    stage('Docker Build') {
      when { expression { return params.DOCKER_BUILD } }
      steps {
        bat "docker build -t %IMAGE%:%VERSION% -t %IMAGE%:latest -f Dockerfile ."
      }
    }

    stage('Deploy: Staging') {
      when { expression { return params.DOCKER_BUILD && params.DEPLOY_STAGING } }
      steps {
        bat """
          set IMAGE=%IMAGE%
          set TAG=%VERSION%
          docker compose -f docker\\docker-compose.staging.yml down || echo no_prev
          docker compose -f docker\\docker-compose.staging.yml up -d --force-recreate
        """
        bat """
          powershell -NoProfile -Command ^
            "for($i=0;$i -lt 30;$i++){try{$r=Invoke-WebRequest -UseBasicParsing http://localhost:%APP_PORT%/actuator/health; if($r.StatusCode -eq 200){exit 0}}catch{}; Start-Sleep -s 2}; exit 1"
        """
      }
    }

    stage('Release & Promote') {
      when { expression { return params.DOCKER_BUILD && params.PROMOTE_PROD } }
      steps {
        input message: "Promote ${env.VERSION} to PRODUCTION?"
        bat 'git config user.email "ci@example.com" && git config user.name "Jenkins CI"'
        bat "git tag -a v%VERSION% -m \"Release %VERSION%\" || echo tag_exists"
        bat "git push origin v%VERSION% || echo push_failed"
      }
    }

    stage('Deploy: Production') {
      when { expression { return params.DOCKER_BUILD && params.PROMOTE_PROD } }
      steps {
        bat """
          set IMAGE=%IMAGE%
          set TAG=%VERSION%
          docker compose -f docker\\docker-compose.prod.yml down || echo no_prev
          docker compose -f docker\\docker-compose.prod.yml up -d --force-recreate
        """
        bat """
          powershell -NoProfile -Command ^
            "for($i=0;$i -lt 30;$i++){try{$r=Invoke-WebRequest -UseBasicParsing http://localhost:%APP_PORT%/actuator/health; if($r.StatusCode -eq 200){exit 0}}catch{}; Start-Sleep -s 2}; exit 1"
        """
      }
    }

    stage('Monitoring & Alerts') {
      when { expression { return params.RUN_MONITORING } }
      steps {
        bat "docker compose -f monitoring\\docker-compose.monitoring.yml up -d"
        echo "Prometheus: http://localhost:9090  Grafana: http://localhost:3000 (admin/admin)"
      }
    }
  }

  post {
    always { echo "Build: ${env.BUILD_TAG}, Version: ${env.VERSION}" }
    failure { echo 'Build failed — check stage logs and reports.' }
  }
}
