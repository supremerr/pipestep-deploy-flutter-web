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
                        jenkins.withCredentials([string(credentialsId: 'aws-credential', variable: 'aws-s3')]) {
                            jenkins.s3Upload(bucket:"http://sample-app-flutter.s3-website-sa-east-1.amazonaws.com", path:'', includePathPattern:'**/*', workingDir:'build/web', excludePathPattern:'**/*.svg,**/*.jpg')
                        }
                    }
                    catch(Exception e){
                        jenkins.echo e
                        jenkins.unstable("An error occured during deploy step. Please, verify the logs.")
                    }
                }
            }
        }
    }
}