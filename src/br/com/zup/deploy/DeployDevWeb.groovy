package br.com.zup.deploy

class DeployDevWeb {
    def call (jenkins) {
        jenkins.podTemplate(
            containers: [
                jenkins.containerTemplate(
                    name: 'flutter', 
                    image: 'cirrusci/flutter:2.0.1', 
                    ttyEnabled: true, 
                    command: 'cat'
                )
            ],
            yamlMergeStrategy: jenkins.merge(),
            workspaceVolume: jenkins.persistentVolumeClaimWorkspaceVolume(
                claimName: "pvc-${jenkins.env.JENKINS_AGENT_NAME}",
                readOnly: false
            )
        ) {
            jenkins.node(jenkins.POD_LABEL){
                jenkins.container('flutter'){
                    try{
                        jenkins.sh label: "Deploy flutter web", 
                                script: "s3Upload(bucket:'sample-app-flutter.s3-website-sa-east-1.amazonaws.com', includePathPattern:'**/*', workingDir:'/build/web')"
                    }
                    catch(Exception e){
                        jenkins.unstable("An error occured during deploy step. Please, verify the logs.")
                    }
                }
            }
        }
    }
}