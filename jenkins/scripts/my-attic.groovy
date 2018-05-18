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


