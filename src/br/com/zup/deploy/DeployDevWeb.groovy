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
                        jenkins.withAWS(credentials: 'aws-credential') {
                            jenkins.s3Upload(bucket:"sample-app-flutter", path:'', includePathPattern:'**/*', workingDir:'build/web', excludePathPattern:'**/*.svg,**/*.jpg')
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