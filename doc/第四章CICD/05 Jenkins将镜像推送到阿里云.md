# 远程保存镜像，将镜像推送到阿里云

阿里云创建镜像仓库

![image-20240417095012211](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404171423084.png)

点击访问凭证 访问密码

![image-20240417095221843](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404171423699.png)

回到Jenkins添加凭证（阿里云）

![image-20240417103853317](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404171423429.png)

将镜像推送到阿里云需要3步

![image-20240417110508930](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404171423648.png)

1.登录

https://www.jenkins.io/zh/doc/book/pipeline/jenkinsfile/#for-secret-text-usernames-and-passwords-and-secret-files

![image-20240417104852213](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404171423245.png)





ALIYUN_SECRET = credentials('aliyun-docker-repo')就可以获取到设置的阿里云凭证

然后使用ALIYUN_SECRET_USR获取账户

ALIYUN_SECRET_PSW获取密码



```shell
docker login -u $ALIYUN_SECRET_USR -p $ALIYUN_SECRET_PSW registry.cn-hangzhou.aliyuncs.com
```



打标签和推送镜像需要tag，想要使tag不是写死的，由我们来输入，使用input等待交互式输入

https://www.jenkins.io/zh/doc/book/pipeline/syntax/#input

![image-20240417113927438](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404171423199.png)

使用片段生成器生成

![image-20240417115016031](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404171424596.png)

注意生成的片段经过测试无法直接使用

```
input message: '是否推送进项？', ok: '推送', parameters: [text(defaultValue: 'v1.0', description: '镜像标签版本', name: 'IMAGE_TAG')]
```

修改为官方提供的样式

```shell
            input {
                message "是否推送镜像？"
                ok "推送"
                parameters {
                    string(name: 'IMAGE_TAG', defaultValue: 'v1.0', description: '镜像标签版本?')
                }
            }
```

最终Jenkinsfile变为

```shell
pipeline {
    // 全部的CICD流程都定义在这里
    agent any // 任何代理都可以被执行

    environment {
        username = "dongguo"
        WORK_SPACE = "$WORKSPACE" // 全局环境中的WORKSPACE是/var/jenkins_home/workspace/devops-demo
        ALIYUN_SECRET = credentials('aliyun-docker-repo')  //Jenkins中保存的凭证（阿里云容器仓库）
    }

    // 定义流水线的加工流程
    stages {
        // 阶段0检查
        stage('环境检查') {
            // 要做的事情
            steps {
                echo "正在检查基本信息"
                sh 'java -version'
                sh 'git --version'
                sh 'docker version'
            }
        }
        // 阶段1编译
        stage('编译') {
            // 要做的事情
            agent {
                docker {
                    image 'maven:3-alpine'
                    args '-v /var/jenkins_home/appconfig/maven/repository:/repository' // 将Docker容器内的路径映射到Jenkins主机上的路径
                }
            }
            steps {
                echo "编译。。。"
                sh 'printenv'
                sh 'mvn --version'
                sh 'cd $WORK_SPACE && mvn clean package -s "/var/jenkins_home/appconfig/maven/settings.xml" -Dmaven.test.skip=true' // 打包
            }
        }
        // 阶段2测试
        stage('测试') {
            steps {
                echo "测试。。。"
                echo "$username"
                echo "${username}"
                sh 'pwd && ls -alh' // 打印当前工作目录（使用 pwd 命令），然后列出当前目录下所有文件和目录的详细信息（使用 ls -alh 命令）。
                sh 'printenv' // 打印当前 Shell 环境中的所有环境变量及其取值。
            }
        }

        // 阶段3生成镜像
        stage('生成镜像') {
            steps {
                echo "生成镜像。。。"
                sh 'pwd && ls -alh'
                sh 'docker version'
                sh 'docker build -t devops-demo .'
            }
        }
        // 阶段推送镜像
        stage('推送镜像') {
            input {
                message "是否推送镜像？"
                ok "推送"
                parameters {
                    string(name: 'IMAGE_TAG', defaultValue: 'v1.0', description: '镜像标签版本?')
                }
            }
            steps {
                echo "推送镜像。。。"
                sh 'docker login -u $ALIYUN_SECRET_USR -p $ALIYUN_SECRET_PSW registry.cn-hangzhou.aliyuncs.com'
                sh 'docker tag devops-demo registry.cn-hangzhou.aliyuncs.com/dongguo/devops-demo:$IMAGE_TAG'
                sh 'docker push registry.cn-hangzhou.aliyuncs.com/dongguo/devops-demo:$IMAGE_TAG'
            }
        }

        // 阶段4部署
        stage('部署') {
            steps {
                echo "部署。。。"
                sh 'docker rm -f devops-demo'
                sh 'docker run -d -p 8081:8081 --name=devops-demo devops-demo'
            }
//             post {
//                 failure {
//                     // One or more steps need to be included within each condition's block.
//                     echo "执行失败。。。"
//                 }
//                 success {
//                     // One or more steps need to be included within each condition's block.
//                     echo "执行成功。。。"
//                 }
//             }
        }

        // 阶段5发送报告
        stage('发送报告') {
            steps {
                echo "发送报告。。。"
                emailext body: '''<!DOCTYPE html>
                <html>
                <head>
                <meta charset="UTF-8">
                <title>${ENV, var="JOB_NAME"}-第${BUILD_NUMBER}次构建日志</title>
                </head>

                <body leftmargin="8" marginwidth="0" topmargin="8" marginheight="4"
                    offset="0">
                    <table width="95%" cellpadding="0" cellspacing="0"
                        style="font-size: 11pt; font-family: Tahoma, Arial, Helvetica, sans-serif">
                        <tr>
                            <td>本邮件是Jenkins自动发送，请勿回复！</td>
                        </tr>
                        <tr>
                            <td><h3>
                                    <font color="#e53935">&nbsp&nbsp&nbsp&nbsp构建结果 - ${BUILD_STATUS}!</font>
                                </h3></td>
                        </tr>
                        <tr>
                            <td><br />
                            <b><font color="#3f51b5">构建信息:</font></b>
                            <hr size="2" width="100%" align="center" /></td>
                        </tr>
                        <tr>
                            <td>
                                <ul>
                                    <li>项目名称：${PROJECT_NAME}</li>
                                    <li>构建编号：第${BUILD_NUMBER}次构建</li>
                                    <li>构建状态：${BUILD_STATUS}</li>
                                    <li>触发原因：${CAUSE}</li>
                                    <li>项目Url：
                                        <a href="${PROJECT_URL}">${PROJECT_URL}</a></li>
                                           <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                                       </a>
                                    </li>
                                    <li>测试报告： <a href="${PROJECT_URL}HTML_20Report">${PROJECT_URL}HTML_20Report</a></li>
                                    <li>所有用例： ${TEST_COUNTS,var="total"}</li>
                                    <li>成功用例： ${TEST_COUNTS,var="pass"}</li>
                                    <li>失败用例： ${TEST_COUNTS,var="fail"}</li>
                                    <li>跳过用例： ${TEST_COUNTS,var="skip"}</li>
                                    <li>构建日志：<a href="${BUILD_URL}console">${BUILD_URL}console</a></li>
                                </ul>
                            </td>
                         <tr>
                            <td><b><font color="#3f51b5">构建日志:</font></b>
                            <hr size="2" width="100%" align="center" /></td>
                        </tr>
                        <tr>
                            <td><textarea cols="160" rows="80" readonly="readonly"
                                    style="font-family: Microsoft YaHei">${BUILD_LOG,maxLines=1000}</textarea>
                            </td>
                        </tr>
                </html>''', subject: '${ENV, var="JOB_NAME"}-第${BUILD_NUMBER}次构建日志', to: '291320608@qq.com'
            }
        }
    }

    post {
        failure {
            // One or more steps need to be included within each condition's block.
            echo "执行失败。。。"
        }
        success {
            // One or more steps need to be included within each condition's block.
            echo "执行成功。。。"
        }
    }
}

```

提交

在推送镜像阶段会询问是否推送镜像，第一次就是用默认值v1.0，点击推送

点击终止阶段就不在往下进行了，直接到end

![image-20240417112344083](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404171424880.png)

构建完毕

![image-20240417114429146](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404171424701.png)



查看阿里云镜像仓库

![image-20240417114547023](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404171424691.png)

修改为v1.1

![image-20240417114710799](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404171424484.png)

查看阿里云

![image-20240417114730294](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404171424282.png)

查看终止情况，直接跳到end，不会继续执行接下来的阶段

![image-20240417114807061](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404171424796.png)

Jenkinsfile修改

增加选择部署的大区，比如项目部署在不同的地区，根据选择的大区发布到指定地区的镜像仓库

```shell
pipeline {
    // 全部的CICD流程都定义在这里
    agent any // 任何代理都可以被执行

    environment {
        username = "dongguo"
        WORK_SPACE = "$WORKSPACE" // 全局环境中的WORKSPACE是/var/jenkins_home/workspace/devops-demo
        ALIYUN_SECRET = credentials('aliyun-docker-repo')  //Jenkins中保存的凭证（阿里云容器仓库）
    }

    // 定义流水线的加工流程
    stages {
        // 阶段0检查
        stage('环境检查') {
            // 要做的事情
            steps {
                echo "正在检查基本信息"
                sh 'java -version'
                sh 'git --version'
                sh 'docker version'
            }
        }
        // 阶段1编译
        stage('编译') {
            // 要做的事情
            agent {
                docker {
                    image 'maven:3-alpine'
                    args '-v /var/jenkins_home/appconfig/maven/repository:/repository' // 将Docker容器内的路径映射到Jenkins主机上的路径
                }
            }
            steps {
                echo "编译。。。"
                sh 'printenv'
                sh 'mvn --version'
                sh 'cd $WORK_SPACE && mvn clean package -s "/var/jenkins_home/appconfig/maven/settings.xml" -Dmaven.test.skip=true' // 打包
            }
        }
        // 阶段2测试
        stage('测试') {
            steps {
                echo "测试。。。"
                echo "$username"
                echo "${username}"
                sh 'pwd && ls -alh' // 打印当前工作目录（使用 pwd 命令），然后列出当前目录下所有文件和目录的详细信息（使用 ls -alh 命令）。
                sh 'printenv' // 打印当前 Shell 环境中的所有环境变量及其取值。
            }
        }

        // 阶段3生成镜像
        stage('生成镜像') {
            steps {
                echo "生成镜像。。。"
                sh 'pwd && ls -alh'
                sh 'docker version'
                sh 'docker build -t devops-demo .'
            }
        }
        // 阶段推送镜像
        stage('推送镜像') {
            input {
                message "是否推送镜像？"
                ok "推送"
                parameters {
                    string(name: 'IMAGE_TAG', defaultValue: 'v1.0', description: '镜像标签版本')
                    choice choices: ['hangzhou','shanghai','beijing'], description: '部署的大区', name: 'DEPLOY_WHERE'
                }
            }
            steps {
                echo "推送镜像。。。"
                sh 'docker login -u $ALIYUN_SECRET_USR -p $ALIYUN_SECRET_PSW registry.cn-$DEPLOY_WHERE.aliyuncs.com'
                sh 'docker tag devops-demo registry.cn-$DEPLOY_WHERE.aliyuncs.com/dongguo/devops-demo:$IMAGE_TAG'
                sh 'docker push registry.cn-$DEPLOY_WHERE.aliyuncs.com/dongguo/devops-demo:$IMAGE_TAG'
            }
        }

        // 阶段4部署
        stage('部署') {
            steps {
                echo "部署。。。"
                sh 'docker rm -f devops-demo'
                sh 'docker run -d -p 8081:8081 --name=devops-demo devops-demo'
            }
//             post {
//                 failure {
//                     // One or more steps need to be included within each condition's block.
//                     echo "执行失败。。。"
//                 }
//                 success {
//                     // One or more steps need to be included within each condition's block.
//                     echo "执行成功。。。"
//                 }
//             }
        }

        // 阶段5发送报告
        stage('发送报告') {
            steps {
                echo "发送报告。。。"
                emailext body: '''<!DOCTYPE html>
                <html>
                <head>
                <meta charset="UTF-8">
                <title>${ENV, var="JOB_NAME"}-第${BUILD_NUMBER}次构建日志</title>
                </head>

                <body leftmargin="8" marginwidth="0" topmargin="8" marginheight="4"
                    offset="0">
                    <table width="95%" cellpadding="0" cellspacing="0"
                        style="font-size: 11pt; font-family: Tahoma, Arial, Helvetica, sans-serif">
                        <tr>
                            <td>本邮件是Jenkins自动发送，请勿回复！</td>
                        </tr>
                        <tr>
                            <td><h3>
                                    <font color="#e53935">&nbsp&nbsp&nbsp&nbsp构建结果 - ${BUILD_STATUS}!</font>
                                </h3></td>
                        </tr>
                        <tr>
                            <td><br />
                            <b><font color="#3f51b5">构建信息:</font></b>
                            <hr size="2" width="100%" align="center" /></td>
                        </tr>
                        <tr>
                            <td>
                                <ul>
                                    <li>项目名称：${PROJECT_NAME}</li>
                                    <li>构建编号：第${BUILD_NUMBER}次构建</li>
                                    <li>构建状态：${BUILD_STATUS}</li>
                                    <li>触发原因：${CAUSE}</li>
                                    <li>项目Url：
                                        <a href="${PROJECT_URL}">${PROJECT_URL}</a></li>
                                           <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                                       </a>
                                    </li>
                                    <li>测试报告： <a href="${PROJECT_URL}HTML_20Report">${PROJECT_URL}HTML_20Report</a></li>
                                    <li>所有用例： ${TEST_COUNTS,var="total"}</li>
                                    <li>成功用例： ${TEST_COUNTS,var="pass"}</li>
                                    <li>失败用例： ${TEST_COUNTS,var="fail"}</li>
                                    <li>跳过用例： ${TEST_COUNTS,var="skip"}</li>
                                    <li>构建日志：<a href="${BUILD_URL}console">${BUILD_URL}console</a></li>
                                </ul>
                            </td>
                         <tr>
                            <td><b><font color="#3f51b5">构建日志:</font></b>
                            <hr size="2" width="100%" align="center" /></td>
                        </tr>
                        <tr>
                            <td><textarea cols="160" rows="80" readonly="readonly"
                                    style="font-family: Microsoft YaHei">${BUILD_LOG,maxLines=1000}</textarea>
                            </td>
                        </tr>
                </html>''', subject: '${ENV, var="JOB_NAME"}-第${BUILD_NUMBER}次构建日志', to: '291320608@qq.com'
            }
        }
    }

    post {
        failure {
            // One or more steps need to be included within each condition's block.
            echo "执行失败。。。"
        }
        success {
            // One or more steps need to be included within each condition's block.
            echo "执行成功。。。"
        }
    }
}

```



选择发布的tag标签，部署的大区

![image-20240417124532011](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404171424213.png)

解析成功

![image-20240417124743424](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404171424233.png)

阿里云杭州地域的仓库中推送了v1.3

![image-20240417124803031](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404171424659.png)

# **密钥管理**

## 第一种方式credentials()

环境变量

```shell
    environment {
        ALIYUN_SECRET = credentials('aliyun-docker-repo')  //Jenkins中保存的凭证（阿里云容器仓库）
    }
    
    
        // 阶段推送镜像
        stage('推送镜像') {
            input {
                message "是否推送镜像？"
                ok "推送"
                parameters {
                    string(name: 'IMAGE_TAG', defaultValue: 'v1.0', description: '镜像标签版本')
                    choice choices: ['hangzhou','shanghai','beijing'], description: '部署的大区', name: 'DEPLOY_WHERE'
                }
            }
            steps {
                echo "推送镜像。。。"
                sh 'docker login -u $ALIYUN_SECRET_USR -p $ALIYUN_SECRET_PSW registry.cn-$DEPLOY_WHERE.aliyuncs.com'
                sh 'docker tag devops-demo registry.cn-$DEPLOY_WHERE.aliyuncs.com/dongguo/devops-demo:$IMAGE_TAG'
                sh 'docker push registry.cn-$DEPLOY_WHERE.aliyuncs.com/dongguo/devops-demo:$IMAGE_TAG'
            }
        }
```

## 第二种方式withCredentials()

脚本方式：

新增**Username and password**  ，选择已经配置的凭证

![image-20240417140024512](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404171424347.png)

```shell
withCredentials([usernamePassword(credentialsId: 'aliyun-docker-repo', passwordVariable: 'aliyun-repo-psw', usernameVariable: 'aliyun-repo-usr')]) {
    // some block
}
```

使用脚本，注意Variable不要带-命名，识别解析不了，可以带_命名

```shell
        // 阶段推送镜像
        stage('推送镜像') {
            input {
                message "是否推送镜像？"
                ok "推送"
                parameters {
                    string(name: 'IMAGE_TAG', defaultValue: 'v1.0', description: '镜像标签版本')
                    choice choices: ['hangzhou','shanghai','beijing'], description: '部署的大区', name: 'DEPLOY_WHERE'
                }
            }
            steps {
                echo "推送镜像。。。"
                script{
                    withCredentials([usernamePassword(credentialsId: 'aliyun-docker-repo', passwordVariable: 'aliyun_psw', usernameVariable: 'aliyun_usr')]) {
                        // some block
                        sh 'docker login -u $aliyun_usr -p $aliyun_psw registry.cn-$DEPLOY_WHERE.aliyuncs.com'
                    }
                }
//                 sh 'docker login -u $ALIYUN_SECRET_USR -p $ALIYUN_SECRET_PSW registry.cn-$DEPLOY_WHERE.aliyuncs.com'
                sh 'docker tag devops-demo registry.cn-$DEPLOY_WHERE.aliyuncs.com/dongguo/devops-demo:$IMAGE_TAG'
                sh 'docker push registry.cn-$DEPLOY_WHERE.aliyuncs.com/dongguo/devops-demo:$IMAGE_TAG'
            }
        }
```

构建通过

![image-20240417141659686](https://gitee.com/dongguo4812_admin/image/raw/master/image/202404171424388.png)