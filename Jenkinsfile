pipeline {
  agent any

  environment {
    REGISTRY          = "50.19.74.203:8083"
    IMAGE_NAME        = "ngueyepmodeste/timeboard"

    NEXUS_DOCKER_CRED = "nexus-docker-creds" // username/password pour le registry
    NEXUS_RAW_URL     = "http://50.19.74.203:8081/repository/timeboard-artifacts"

    APP_USER          = "ubuntu"
    APP_HOST          = "34.233.121.71"

    JAR_NAME          = "timeboard-demo-1.0.0.jar"
  }

  stages {

    stage('Checkout') {
      steps {
    // Jenkins a déjà fait le checkout grâce à "Pipeline from SCM"
    sh 'pwd'
    sh 'ls -R'
  }
}

    stage('Secret Scan (Gitleaks)') {
      steps {
        sh 'gitleaks detect --source . --no-git -v --report-path gitleaks-report.json || true'
        archiveArtifacts artifacts: 'gitleaks-report.json', allowEmptyArchive: true
      }
    }

    stage('Policy Check (Conftest)') {
      steps {
        sh '''
          if [ -f deploy/config.yaml ]; then
            conftest test deploy/config.yaml -p policy > conftest-report.txt || true
          else
            echo "deploy/config.yaml missing; skipping conftest" > conftest-report.txt
          fi
        '''
        archiveArtifacts artifacts: 'conftest-report.txt', allowEmptyArchive: true
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
        sh 'mvn spotbugs:spotbugs || true'
        archiveArtifacts artifacts: 'target/spotbugsXml.xml', allowEmptyArchive: true
      }
    }

    stage('SCA (Dependency-Check)') {
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

    stage('Build Docker Image') {
      steps {
        sh """
          docker build -t ${REGISTRY}/${IMAGE_NAME}:${BUILD_NUMBER} .
        """
      }
    }

    stage('Image Scan (Trivy)') {
      steps {
        sh """
          trivy image --exit-code 0 --format table \
            --output trivy-report.txt ${REGISTRY}/${IMAGE_NAME}:${BUILD_NUMBER} || true
        """
        archiveArtifacts artifacts: 'trivy-report.txt', allowEmptyArchive: true
      }
    }

    stage('Push Docker Image to Nexus') {
      steps {
        withCredentials([usernamePassword(credentialsId: NEXUS_DOCKER_CRED,
                                         usernameVariable: 'NEXUS_USER',
                                         passwordVariable: 'NEXUS_PASS')]) {
          sh """
            echo \$NEXUS_PASS | docker login ${REGISTRY} -u \$NEXUS_USER --password-stdin
            docker push ${REGISTRY}/${IMAGE_NAME}:${BUILD_NUMBER}
            docker logout ${REGISTRY}
          """
        }
      }
    }

    stage('Upload Reports & JAR to Nexus (raw)') {
      steps {
        withCredentials([usernamePassword(credentialsId: NEXUS_DOCKER_CRED,
                                         usernameVariable: 'NEXUS_USER',
                                         passwordVariable: 'NEXUS_PASS')]) {
          sh '''
            for f in gitleaks-report.json conftest-report.txt dependency-check-report.html trivy-report.txt target/'"${JAR_NAME}"'; do
              if [ -f "$f" ]; then
                echo "Uploading $f to Nexus raw..."
                curl -u ${NEXUS_USER}:${NEXUS_PASS} --upload-file "$f" "${NEXUS_RAW_URL}/$f"
              else
                echo "File $f not found, skipping."
              fi
            done
          '''
        }
      }
    }

    stage('DAST light (local smoke test)') {
      steps {
        sh """
          docker stop  timeboard-ci-test
          docker rm  timeboard-ci-test
          docker run -d --rm --name timeboard-ci-test -p 8080:3005 ${REGISTRY}/${IMAGE_NAME}:${BUILD_NUMBER}
          sleep 15
          curl -f http://localhost:3005/health
          docker stop timeboard-ci-test
        """
      }
    }

    stage('Deploy to App Server') {
      steps {
        sshagent (credentials: ['app-server-ssh']) {
          withCredentials([usernamePassword(credentialsId: NEXUS_DOCKER_CRED,
                                            usernameVariable: 'NEXUS_USER',
                                            passwordVariable: 'NEXUS_PASS')]) {
            sh """
              ssh -o StrictHostKeyChecking=no ${APP_USER}@${APP_HOST} '
                docker ps -q --filter "name=timeboard-demo" | xargs -r docker stop &&
                docker ps -aq --filter "name=timeboard-demo" | xargs -r docker rm || true &&
                echo ${NEXUS_PASS} | docker login ${REGISTRY} -u ${NEXUS_USER} --password-stdin &&
                docker pull ${REGISTRY}/${IMAGE_NAME}:${BUILD_NUMBER} &&
                docker run -d --name timeboard-demo -p 80:80 ${REGISTRY}/${IMAGE_NAME}:${BUILD_NUMBER}
              '
            """
          }
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
