package br.com.zup.deploy

class DeployDevWeb {
    def call (jenkins) {
        def bucketName = jenkins.env.JOB_NAME.toLowerCase()
        
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
                            jenkins.s3Upload(bucket:"${bucketName}.dev.iupp.io", path:'', includePathPattern:'**/*', workingDir:'build/web', excludePathPattern:'**/*.svg,**/*.jpg')
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