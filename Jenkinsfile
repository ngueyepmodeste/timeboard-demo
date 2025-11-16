pipeline {
  agent any

  environment {
    NEXUS_URL = "http://CI_NEXUS_IP:8081/repository/java-app-releases"
    NEXUS_CRED = 'nexus-creds'
    APP_USER = "ubuntu"          // ou ton user sur app-server
    APP_HOST = "APP_SERVER_IP"   // IP publique de app-server
    APP_SERVICE_NAME = "simple-java-timesheet"
    JAR_NAME = "simple-java-timesheet-1.0.0.jar"
  }

  stages {
    stage('Checkout') {
      steps {
        git url: 'https://github.com/TON_USER/simple-java-timesheet.git', branch: 'main'
      }
    }

    stage('Build & Unit Tests') {
      steps {
        sh 'mvn clean test'
        junit 'target/surefire-reports/*.xml'
      }
    }

    stage('SAST (SpotBugs)') {
      steps {
        sh 'mvn spotbugs:spotbugs'
        archiveArtifacts artifacts: 'target/spotbugsXml.xml', allowEmptyArchive: true
      }
    }

    stage('SCA (OWASP Dependency Check)') {
      steps {
        sh 'mvn org.owasp:dependency-check-maven:check || true'
        archiveArtifacts artifacts: 'dependency-check-report.html', allowEmptyArchive: true
      }
    }

    stage('Package JAR') {
      steps {
        sh 'mvn package -DskipTests'
        archiveArtifacts artifacts: "target/${JAR_NAME}", allowEmptyArchive: false
      }
    }

    stage('Upload to Nexus') {
      steps {
        withCredentials([usernamePassword(credentialsId: NEXUS_CRED,
                                         usernameVariable: 'NEXUS_USER',
                                         passwordVariable: 'NEXUS_PASS')]) {
          sh """
            curl -u $NEXUS_USER:$NEXUS_PASS \\
                 --upload-file target/${JAR_NAME} \\
                 ${NEXUS_URL}/${JAR_NAME}
          """
        }
      }
    }

    stage('Deploy to App Server') {
      steps {
        sshagent (credentials: ['app-server-key']) {
          sh """
            scp -o StrictHostKeyChecking=no target/${JAR_NAME} ${APP_USER}@${APP_HOST}:/home/${APP_USER}/${JAR_NAME}
            ssh -o StrictHostKeyChecking=no ${APP_USER}@${APP_HOST} '
              sudo systemctl stop ${APP_SERVICE_NAME} || true
              sudo mv /home/${APP_USER}/${JAR_NAME} /opt/${APP_SERVICE_NAME}/${JAR_NAME}
              sudo systemctl start ${APP_SERVICE_NAME}
            '
          """
        }
      }
    }

    stage('Post-deploy Healthcheck') {
      steps {
        sh "curl -f http://${APP_HOST}/health"
      }
    }
  }
}
