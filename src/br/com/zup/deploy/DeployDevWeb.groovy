package br.com.zup.deploy

class DeployDevWeb {
    def call (jenkins) {
        jenkins.podTemplate(
            yamlMergeStrategy: jenkins.merge(),
            workspaceVolume: jenkins.persistentVolumeClaimWorkspaceVolume(
                claimName: "pvc-${jenkins.env.JENKINS_AGENT_NAME}",
                readOnly: false
            )
        ) {
            jenkins.node(jenkins.POD_LABEL){
                jenkins.container('jnlp'){
                    try{
                        jenkins.sh label: "Deploy flutter web", 
                                script: "s3Upload(bucket:\"http://sample-app-flutter.s3-website-sa-east-1.amazonaws.com\", path:'/build/web', includePathPattern:'**/*', workingDir:'/build/web', excludePathPattern:'**/*.svg,**/*.jpg')"
                    }
                    catch(Exception e){
                        jenkins.unstable("An error occured during deploy step. Please, verify the logs.")
                    }
                }
            }
        }
    }
}