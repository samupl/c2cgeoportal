@Library('c2c-pipeline-library')
import static com.camptocamp.utils.*

final MAIN_BRANCH = 'master'
env.MAIN_BRANCH = MAIN_BRANCH
final MAJOR_VERSION = '2.4.0'
env.MAJOR_VERSION = MAJOR_VERSION
env.CI = 'true'

dockerBuild {
    timeout(time: 2, unit: 'HOURS') {
        stage('Says hello'){
            if ( env.BRANCH_NAME ==~ /release\/.*/ ){
                echo 'We are going to release now'
            }
            echo 'Hello'
        }
    }
}
