package br.com.zup.deploy

class DeployDevWeb {
    def call (jenkins) {
        def bucketName = jenkins.env.JOB_NAME.toLowerCase()
        
        jenkins.podTemplate(
            containers: [
                jenkins.containerTemplate(
                    name: 'aws-cli', 
                    image: 'amazon/aws-cli:2.1.30', 
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
                jenkins.container('aws-cli'){
                def bucketPolicy = jenkins.readJSON text:"""{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "PublicReadGetObject",
            "Effect": "Allow",
            "Principal": "*",
            "Action": [
                "s3:GetObject"
            ],
            "Resource": [
                "arn:aws:s3:::${bucketName}.dev.iupp.io/*"
            ]
        }
    ]
}"""
                    def bucketPolicyFile = jenkins.writeJSON file: 'policy.json', json: bucketPolicy
                    try{
                        try{
                            jenkins.sh label: "S3 Create Bucket", 
                            script: "aws s3api create-bucket --bucket ${bucketName}.dev.iupp.io --region sa-east-1 --create-bucket-configuration LocationConstraint=sa-east-1 --acl public-read"
                            jenkins.sh label: "S3 Create Website", 
                            script: "aws s3 website s3://${bucketName}.dev.iupp.io --index-document index.html"
                            jenkins.sh label: "S3 Create Bucket Policy", 
                            script: "aws s3api put-bucket-policy --bucket ${bucketName}.dev.iupp.io --policy file://policy.json"

                        } catch(Exception e){
                            jenkins.echo "Não foi necessária a ciração do bucket"
                        }
                        jenkins.withAWS(credentials: 'aws-credential') {
                            jenkins.s3Upload(bucket:"${bucketName}.dev.iupp.io", path:'', includePathPattern:'**/*', workingDir:'build/web', excludePathPattern:'**/*.svg,**/*.jpg')
                        }
                    } catch(Exception e){
                        jenkins.echo e
                        jenkins.unstable("An error occured during deploy step. Please, verify the logs.")
                     }
                }
            }
        }
    }
}