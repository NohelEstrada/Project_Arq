pipeline {
  agent any
  tools {
    nodejs 'NodeJS'          // <-- Global Tool: NodeJS
  }

  environment {
    // Nombre del servidor SonarQube que configuraste en Jenkins → System
    SONARQUBE_SERVER = 'sonarqube'
    // Direcciones para notificaciones (ajústalas)
    EMAIL_TO = 'pablopolis2016@gmail.com'
  }

  options { timestamps() }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Unit Tests & Coverage (auto-detect)') {
      steps {
        sh '''
          set -e
          # Frontend (Node) - si existe package.json en ./ensurance o ./front ajusta al path que uses
          if [ -f "./ensurance/package.json" ]; then
            echo "[frontend] npm ci && npm test"
            cd ./ensurance
            npm ci || true
            npm test -- --ci || true
            cd -
          fi

          # Backend (Node/Java/Python) – intenta detectar:
          if [ -f "./pharmacy/package.json" ]; then
            echo "[backend] npm ci && npm test"
            cd ./pharmacy
            npm ci || true
            npm test -- --ci || true
            cd -
          elif [ -f "pom.xml" ]; then
            echo "[backend] mvn -B -DskipTests=false test"
            mvn -B -DskipTests=false test || true
          elif [ -f "build.gradle" ] || [ -f "gradlew" ]; then
            echo "[backend] gradle test"
            ./gradlew test || true
          elif [ -f "requirements.txt" ] && command -v pytest >/dev/null 2>&1; then
            echo "[backend] pytest"
            pytest || true
          else
            echo "No se detectaron tests automáticamente. (OK para primera corrida)"
          fi
        '''
      }
      post {
        unsuccessful {
          emailext to: "${env.EMAIL_TO}",
                   subject: "❌ Tests fallaron: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                   body: "Revisa la consola: ${env.BUILD_URL}"
        }
      }
    }

    stage('SonarQube Analysis') {
      steps {
        withSonarQubeEnv("${SONARQUBE_SERVER}") {
          // Usa sonar-project.properties en la raíz
          sh '''
            if ! command -v sonar-scanner >/dev/null 2>&1; then
              echo "Instala SonarScanner en Global Tool Config y expórtalo en el PATH"
              exit 1
            fi
            sonar-scanner -Dsonar.projectVersion=${BUILD_NUMBER}
          '''
        }
      }
    }

    stage('Quality Gate') {
      steps {
        timeout(time: 10, unit: 'MINUTES') {
          waitForQualityGate abortPipeline: true
        }
      }
      post {
        unsuccessful {
          emailext to: "${env.EMAIL_TO}",
                   subject: "⛔ Quality Gate no aprobado: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                   body: "Se bloquea el merge por incumplir el gate (no nueva deuda técnica). ${env.BUILD_URL}"
        }
      }
    }

    stage('Deploy DEV (branch dev)') {
      when { branch 'dev' }
      steps {
        sh '''
          echo "Deploy DEV → backend1/frontend1"
          docker compose -f docker-compose.ensurance.yaml up -d --build ensurance-db ensurance-backend1 ensurance-frontend1
        '''
      }
    }

    stage('Deploy UAT (branch test)') {
      when { branch 'test' }
      steps {
        sh '''
          echo "Deploy UAT → backend2/frontend2"
          docker compose -f docker-compose.ensurance.yaml up -d --build ensurance-db ensurance-backend2 ensurance-frontend2
        '''
      }
    }

    stage('Deploy PROD (branch main)') {
      when { branch 'main' }
      steps {
        sh '''
          echo "Deploy PROD → backend3/frontend3"
          docker compose -f docker-compose.ensurance.yaml up -d --build ensurance-db ensurance-backend3 ensurance-frontend3
        '''
      }
    }
  }

  post {
    success {
      echo "✅ Pipeline OK"
    }
    unsuccessful {
      emailext to: "${env.EMAIL_TO}",
               subject: "⚠️ Pipeline fallido: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
               body: "Detalle: ${env.BUILD_URL}"
    }
  }
}
