pipeline {
    agent any
    
    environment {
        SONAR_SCANNER_HOME = tool 'SonarQube Scanner'
        SONAR_PROJECT_KEY = 'pharmacy-project'
        SONAR_PROJECT_NAME = 'Pharmacy Project'
        LEAD_DEVELOPER_EMAIL = 'lead@company.com'
        PRODUCT_OWNER_EMAIL = 'productowner@company.com'
        PROJECT_DIR = 'ensurancePharmacy'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMIT_HASH = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                    env.BRANCH_NAME = env.BRANCH_NAME ?: sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
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
                            sh 'mvn clean test'
                        }
                    }
                    post {
                        always {
                            publishTestResults(
                                testResultsPattern: "${env.PROJECT_DIR}/backv5/target/surefire-reports/*.xml",
                                allowEmptyResults: true
                            )
                        }
                        failure {
                            script {
                                sendFailureEmail('Unit Tests', 'Backend unit tests failed')
                            }
                        }
                    }
                }
                stage('Frontend Tests') {
                    steps {
                        dir("${env.PROJECT_DIR}/pharmacy") {
                            sh 'npm install'
                            sh 'npm test -- --watchAll=false --coverage'
                        }
                    }
                    post {
                        failure {
                            script {
                                sendFailureEmail('Unit Tests', 'Frontend unit tests failed')
                            }
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
            post {
                failure {
                    script {
                        sendFailureEmail('SonarQube Analysis', 'SonarQube analysis failed')
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
            post {
                failure {
                    script {
                        sendFailureEmail('Quality Gate', 'SonarQube Quality Gate failed - Technical debt not allowed')
                    }
                }
            }
        }
        
        stage('Build and Deploy') {
            steps {
                script {
                    dir(env.PROJECT_DIR) {
                        // Stop existing containers for this environment
                        sh "docker-compose -f ${env.COMPOSE_FILE} down || true"
                        
                        // Build and start new containers
                        sh "docker-compose -f ${env.COMPOSE_FILE} up -d --build"
                        
                        // Wait for services to be ready
                        sh "sleep 30"
                        
                        // Health check
                        sh """
                            curl -f http://localhost:${env.BACKEND_PORT}/health || exit 1
                            curl -f http://localhost:${env.FRONTEND_PORT} || exit 1
                        """
                    }
                }
            }
            post {
                failure {
                    script {
                        sendFailureEmail('Deployment', "Deployment to ${env.ENVIRONMENT} failed")
                    }
                }
                success {
                    script {
                        sendSuccessEmail()
                    }
                }
            }
        }
        
        stage('Smoke Tests') {
            steps {
                dir(env.PROJECT_DIR) {
                    sh """
                        python3 smoke_test.sh ${env.FRONTEND_PORT} ${env.BACKEND_PORT}
                    """
                }
            }
            post {
                failure {
                    script {
                        sendFailureEmail('Smoke Tests', 'Smoke tests failed after deployment')
                    }
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        failure {
            script {
                sendFailureEmail('Pipeline', 'Overall pipeline failed')
            }
        }
    }
}

def sendFailureEmail(stageName, message) {
    emailext (
        subject: "Pipeline Failed: ${env.JOB_NAME} - ${stageName}",
        body: """
            Pipeline failed at stage: ${stageName}
            
            Branch: ${env.BRANCH_NAME}
            Environment: ${env.ENVIRONMENT}
            Commit: ${env.GIT_COMMIT_HASH}
            
            Error: ${message}
            
            Build URL: ${env.BUILD_URL}
        """,
        to: "${env.LEAD_DEVELOPER_EMAIL}, ${env.PRODUCT_OWNER_EMAIL}"
    )
}

def sendSuccessEmail() {
    emailext (
        subject: "Deployment Success: ${env.JOB_NAME} - ${env.ENVIRONMENT}",
        body: """
            Successful deployment to ${env.ENVIRONMENT}
            
            Branch: ${env.BRANCH_NAME}
            Commit: ${env.GIT_COMMIT_HASH}
            
            Frontend: http://localhost:${env.FRONTEND_PORT}
            Backend: http://localhost:${env.BACKEND_PORT}
            
            Build URL: ${env.BUILD_URL}
        """,
        to: "${env.LEAD_DEVELOPER_EMAIL}, ${env.PRODUCT_OWNER_EMAIL}"
    )
} 