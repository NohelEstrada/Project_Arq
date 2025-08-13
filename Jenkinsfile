pipeline {
    agent any
    
    environment {
        SONAR_SCANNER_HOME = tool 'SonarQube Scanner'
        SONAR_PROJECT_KEY = 'pharmacy-project'
        SONAR_PROJECT_NAME = 'Pharmacy Project'
        PROJECT_DIR = 'ensurancePharmacy'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMIT_HASH = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                    env.BRANCH_NAME = env.BRANCH_NAME ?: sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
                    echo "Building branch: ${env.BRANCH_NAME}"
                    echo "Commit: ${env.GIT_COMMIT_HASH}"
                }
            }
        }
        
        stage('Determine Environment') {
            steps {
                script {
                    if (env.BRANCH_NAME == 'dev') {
                        env.ENVIRONMENT = 'development'
                        env.COMPOSE_FILE = 'docker-compose.dev.yml'
                        env.FRONTEND_PORT = '8083'
                        env.BACKEND_PORT = '8084'
                    } else if (env.BRANCH_NAME == 'uat') {
                        env.ENVIRONMENT = 'uat'
                        env.COMPOSE_FILE = 'docker-compose.uat.yml'
                        env.FRONTEND_PORT = '8090'
                        env.BACKEND_PORT = '8091'
                    } else if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'main') {
                        env.ENVIRONMENT = 'production'
                        env.COMPOSE_FILE = 'docker-compose.prod.yml'
                        env.FRONTEND_PORT = '8100'
                        env.BACKEND_PORT = '8101'
                    } else {
                        error "Branch ${env.BRANCH_NAME} is not configured for deployment"
                    }
                    echo "Deploying to ${env.ENVIRONMENT} environment using ${env.COMPOSE_FILE}"
                }
            }
        }
        
        stage('Unit Tests') {
            parallel {
                stage('Backend Tests') {
                    steps {
                        dir("${env.PROJECT_DIR}/backv5") {
                            sh 'mvn clean test -DskipTests=true'
                        }
                    }
                }
                stage('Frontend Tests') {
                    steps {
                        dir("${env.PROJECT_DIR}/pharmacy") {
                            sh 'npm install'
                            sh 'echo "Frontend tests - skipping for now"'
                        }
                    }
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                dir(env.PROJECT_DIR) {
                    withSonarQubeEnv('SonarQube') {
                        sh "${SONAR_SCANNER_HOME}/bin/sonar-scanner"
                    }
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        
        stage('Build and Deploy') {
            steps {
                script {
                    dir(env.PROJECT_DIR) {
                        echo "Deploying to ${env.ENVIRONMENT} environment..."
                        
                        // Stop existing containers for this environment
                        sh "docker-compose -f ${env.COMPOSE_FILE} down || true"
                        
                        // Build and start new containers
                        sh "docker-compose -f ${env.COMPOSE_FILE} up -d --build"
                        
                        // Wait for services to be ready
                        sh "sleep 30"
                        
                        echo "Deployment completed!"
                        echo "Frontend: http://localhost:${env.FRONTEND_PORT}"
                        echo "Backend: http://localhost:${env.BACKEND_PORT}"
                    }
                }
            }
        }
    }
    
    post {
        success {
            echo "Pipeline completed successfully for ${env.ENVIRONMENT} environment!"
        }
        failure {
            echo "Pipeline failed for ${env.ENVIRONMENT} environment!"
        }
    }
} 