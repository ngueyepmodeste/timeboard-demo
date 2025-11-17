pipeline {
  agent any

  environment {
    REGISTRY          = "3.95.239.230:8083"
    IMAGE_NAME        = "ngueyepmodeste/timeboard"

    NEXUS_DOCKER_CRED = "nexus-docker-creds" // username/password pour Nexus (docker + raw)
    NEXUS_RAW_URL     = "http://3.95.239.230:8081/repository/timeboard-artifacts"

    APP_USER          = "ubuntu"
    APP_HOST          = "100.26.44.42"

    JAR_NAME          = "timeboard-demo-1.0.0.jar"
  }

  stages {

    stage('Checkout') {
      steps {
        // Si tu utilises "Pipeline from SCM", Jenkins a déjà fait le checkout.
        // Ici on log juste l’arborescence pour debug.
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
        sh '''
          docker build -t $REGISTRY/$IMAGE_NAME:$BUILD_NUMBER .
        '''
      }
    }

    stage('Image Scan (Trivy)') {
      steps {
        sh '''
          # Télécharger le template HTML de Trivy si absent
          if [ ! -f trivy-html.tpl ]; then
            curl -sSL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/html.tpl -o trivy-html.tpl
          fi

          # Génération du rapport HTML
          trivy image --exit-code 0 \
            --format template \
            --template "@trivy-html.tpl" \
            -o trivy-report.html \
            $REGISTRY/$IMAGE_NAME:$BUILD_NUMBER || true
        '''
        archiveArtifacts artifacts: 'trivy-report.html', allowEmptyArchive: true
      }
    }

    stage('Push Docker Image to Nexus') {
      steps {
        withCredentials([usernamePassword(credentialsId: NEXUS_DOCKER_CRED,
                                         usernameVariable: 'NEXUS_USER',
                                         passwordVariable: 'NEXUS_PASS')]) {
          sh '''
            echo "$NEXUS_PASS" | docker login "$REGISTRY" -u "$NEXUS_USER" --password-stdin
            docker push "$REGISTRY/$IMAGE_NAME:$BUILD_NUMBER"
            docker logout "$REGISTRY"
          '''
        }
      }
    }

    stage('Upload Reports & JAR to Nexus (raw)') {
      steps {
        withCredentials([usernamePassword(credentialsId: NEXUS_DOCKER_CRED,
                                         usernameVariable: 'NEXUS_USER',
                                         passwordVariable: 'NEXUS_PASS')]) {
          sh '''
            for f in gitleaks-report.json conftest-report.txt dependency-check-report.html trivy-report.html target/'"'"${JAR_NAME}"'"'; do
              if [ -f "$f" ]; then
                echo "Uploading $f to Nexus raw..."
                curl -u "$NEXUS_USER:$NEXUS_PASS" --upload-file "$f" "$NEXUS_RAW_URL/$f"
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
        sh '''
          # Arrêter et supprimer le conteneur de test s'il existe
          docker ps -q --filter "name=timeboard-ci-test" | xargs -r docker stop || true
          docker ps -aq --filter "name=timeboard-ci-test" | xargs -r docker rm || true

          # Lancer un nouveau conteneur de test
          docker run -d --rm --name timeboard-ci-test -p 3005:8080 $REGISTRY/$IMAGE_NAME:$BUILD_NUMBER
          sleep 15
          curl -f http://localhost:3005/health
          docker stop timeboard-ci-test
        '''
      }
    }

    stage('Deploy to App Server') {
      steps {
        sshagent (credentials: ['app-server-ssh']) {
          withCredentials([usernamePassword(credentialsId: NEXUS_DOCKER_CRED,
                                            usernameVariable: 'NEXUS_USER',
                                            passwordVariable: 'NEXUS_PASS')]) {
            sh '''
              # Stop & remove ancien conteneur
              ssh -o StrictHostKeyChecking=no "$APP_USER@$APP_HOST" "docker ps -q --filter 'name=timeboard-demo' | xargs -r docker stop || true"
              ssh -o StrictHostKeyChecking=no "$APP_USER@$APP_HOST" "docker ps -aq --filter 'name=timeboard-demo' | xargs -r docker rm || true"

              # Login au registry depuis app-server
              ssh -o StrictHostKeyChecking=no "$APP_USER@$APP_HOST" "echo '$NEXUS_PASS' | docker login $REGISTRY -u '$NEXUS_USER' --password-stdin"

              # Pull & run nouvelle image
              ssh -o StrictHostKeyChecking=no "$APP_USER@$APP_HOST" "docker pull $REGISTRY/$IMAGE_NAME:$BUILD_NUMBER"
              ssh -o StrictHostKeyChecking=no "$APP_USER@$APP_HOST" "docker run -d --name timeboard-demo -p 80:8080 $REGISTRY/$IMAGE_NAME:$BUILD_NUMBER"
            '''
          }
        }
      }
    }

  }  
}
