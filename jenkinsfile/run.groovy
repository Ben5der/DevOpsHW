pipeline {
    agent{node('master')}
    stages {
        stage('Clean workspace & dowload dist') {
            steps {
                script {
                    cleanWs()
                    withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {
                        try {
                            sh "echo '${password}' | sudo -S docker stop nginx_alexander_k"
                            sh "echo '${password}' | sudo -S docker container rm nginx_alexander_k"
                        } catch (Exception e) {
                            print 'container not exist, skip clean'
                        }
                    }
                }
                script {
                    echo 'Update from repository'
                    checkout([$class                           : 'GitSCM',
                              branches                         : [[name: '*/master']],
                              doGenerateSubmoduleConfigurations: false,
                              extensions                       : [[$class           : 'RelativeTargetDirectory',
                                                                   relativeTargetDir: 'auto']],
                              submoduleCfg                     : [],
                              userRemoteConfigs                : [[credentialsId: '	1d1b2435-82a7-4e3b-bbe6-ed18aa0d1d86', url: 'https://github.com/Ben5der/DevOpsHW.git']]])
                }
            }
        }
        stage ('Build & run docker image'){
            steps{
                script{
                     withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {

                        sh "echo '${password}' | sudo -S docker build ${WORKSPACE}/auto -t docker_image_alexander_k"
                        sh "echo '${password}' | sudo -S docker run -d -p 9090:80 --name nginx_Alexander_K -v /home/adminci/Alexander_K_V2:/ docker_image_alexander_k"
                    }
                }
            }
        }
        stage ('Get stats & write to file'){
            steps{
                script{
                    withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {
                        try {
                            sh "truncate -s 0 ${WORKSPACE}/log.txt"
                        } catch (Exception e) {
                            print 'file exist'
                        }
                        sh "echo '${password}' | sudo -S docker exec -t nginx_alexander_k bash -c 'df -h > /log.txt'"
                        sh "echo '${password}' | sudo -S docker exec -t nginx_alexander_k bash -c 'top -n 1 -b >> /log.txt'"
                    }
                }
            }
        }
        
    }

    
}
