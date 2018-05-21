println("currentBuild: ${currentBuild.class} / ${currentBuild}")
def build2 = currentBuild.rawBuild
println("Build2: ${build2.class} / ${build2}")

//        println("Build: ${build.class} / ${build}")
def mbuild2 = build2



GlobalVariable.ALL.each { var ->
    println("Var: ${var}")
}

println("currentBuild in dst: ${currentBuild.class} / ${currentBuild}")


//            def build = currentBuild.build
def cred0 = CredentialsProvider.findCredentialById(userRemoteConfigs.credentialsId
        , StringCredentials.class, mbuild2, [])

println("Found cred0: ${cred0}")

CredentialsProvider.lookupCredentials(
        com.cloudbees.plugins.credentials.Credentials.class
).each { cred ->
    println("Global creds: ${cred.class}, id: ${cred.id}: ${cred.description} -> ${cred}")
}

println("Found cred: ${cred0.class}, id: ${cred0.id}: ${cred0.description} -> ${cred0}")
def secret = cred0.secret
def token = secret.plainText
println("Token: ${token}")

com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
        com.cloudbees.plugins.credentials.Credentials.class
).each { cred ->
    println("Global creds: ${cred.class}, id: ${cred.id}: ${cred.description} -> ${cred}")
}
Jenkins.instance.getAllItems(com.cloudbees.hudson.plugins.folder.Folder.class).each{ f ->
    creds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
            com.cloudbees.plugins.credentials.Credentials.class, f)
    creds.each{cred ->
        println("Folder instance ${f} cred: ${cred.id}: ${cred.description} -> ${cred}")
    }
}

//CredentialsProvider.






def userRemoteConfigs = scm.userRemoteConfigs[0]
dstScm = [
        $class                           : 'GitSCM',
        branches                         : [[name: "*/${targetBranch}"], [name: "*/${sourceBranch}"]],
        doGenerateSubmoduleConfigurations: false,
        extensions                       : [],
//                    userRemoteConfigs                : scm.userRemoteConfigs
        userRemoteConfigs                : [[url: 'git@github.com:mpashka/test-jenkins-repo-dst.git', credentialsId: userRemoteConfigs.credentialsId]]
]

checkout(dstScm)


/*
        String script_root = "${workspace}/freebsd-ci"
        String build_script = "${script_root}/scripts/build/build1.sh"
        String build_ufs_script = "${script_root}/scripts/build/build-ufs-image.sh"

        dir ("freebsd-ci") {
            git changelog: false, poll: false, url: "${script_url}"
        }
*/

//        return deleteDir()

// Write out the new json config file, to be used by subsequent scripts
//        writeFile file: 'config.json', text: json_str


/*
        echo("scm. branches: ${scm.branches}, doGenerateSubmoduleConfigurations: ${scm.doGenerateSubmoduleConfigurations} , " +
                "extensions: ${scm.extensions}, userRemoteConfigs: ${scm.userRemoteConfigs}")

        userRemoteConfigs = scm.userRemoteConfigs
        echo("name: ${userRemoteConfigs.name}, refspec: ${userRemoteConfigs.refspec}, url: ${userRemoteConfigs.url}, " +
                "credentialsId: ${userRemoteConfigs.credentialsId}")

        sh 'ls'


        echo 'Hello world from my pipeline'

        echo '------------ env'
        sh 'env'
        echo '------------ set'
        sh 'set'
        echo '------------'
*/

//        String sh(returnStdout: true, script: "git log -n 1 --pretty=format:'%h'").trim()

//        git symbolic-ref HEAD
//        String branch = sh(returnStdout: true, script: "git symbolic-ref --short HEAD").trim()

//        GitSCM




import com.cloudbees.plugins.credentials.CredentialsProvider
import org.jenkinsci.plugins.plaincredentials.StringCredentials
import org.jenkinsci.plugins.workflow.cps.GlobalVariable

