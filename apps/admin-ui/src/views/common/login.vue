<template>
    <div class="site-wrapper site-page--login min300">
        <div class="site-content__wrapper">
            <div class="site-content">
                <div class="brand-info brand-info--intro">
                    <div class="brand-info__kicker">CARAT CRAFT · MERCHANT</div>
                    <h2 class="brand-info__text">克拉匠造</h2>
                    <p class="brand-info__intro">
                        珠宝商品、定制组件与订单生产的一站式商家工作台。
                    </p>
                </div>
                <div class="login-main">
                    <h3 class="login-title">管理员登录</h3>
                    <el-form
                        :model="dataForm"
                        :rules="dataRule"
                        ref="dataForm"
                        @keyup.enter="dataFormSubmit()"
                        status-icon
                    >
                        <el-form-item prop="userName">
                            <el-input
                                v-model="dataForm.userName"
                                placeholder="帐号"
                                aria-label="管理员账号"
                                autocomplete="username"
                                autofocus
                            ></el-input>
                        </el-form-item>
                        <el-form-item prop="password">
                            <el-input
                                v-model="dataForm.password"
                                type="password"
                                placeholder="密码"
                                aria-label="管理员密码"
                                autocomplete="current-password"
                            ></el-input>
                        </el-form-item>
                        <el-form-item prop="captcha">
                            <el-row :gutter="20">
                                <el-col :span="14">
                                    <el-input
                                        v-model="dataForm.captcha"
                                        placeholder="验证码"
                                        aria-label="图形验证码"
                                    >
                                    </el-input>
                                </el-col>
                                <el-col :span="10" class="login-captcha">
                                    <img
                                        :src="captchaPath"
                                        @click="getCaptcha()"
                                        @keydown.enter="getCaptcha()"
                                        @keydown.space.prevent="getCaptcha()"
                                        role="button"
                                        tabindex="0"
                                        alt="点击刷新图形验证码"
                                        style="height: 32px; width: 96px; border-radius: 4px"
                                    />
                                </el-col>
                            </el-row>
                        </el-form-item>
                        <el-form-item>
                            <el-button
                                type="primary"
                                :loading="loginSubmitting"
                                :disabled="loginSubmitting"
                                @click="dataFormSubmit()"
                                style="width: 100%"
                                >登录</el-button
                            >
                        </el-form-item>
                        <el-form-item>
                            <el-button link type="primary" @click="showPasswordRecovery"
                                >忘记密码</el-button
                            >
                            <el-button link type="primary" @click="preparePasswordChange"
                                >修改密码</el-button
                            >
                        </el-form-item>
                        <p v-if="changeAfterLogin">
                            请先使用当前密码登录，系统将打开修改密码窗口。
                        </p>
                    </el-form>
                </div>

                <div class="brand-info brand-info--footer">
                    <p class="brand-info__intro">
                        <span>CaratCraft · 商家管理平台</span>
                    </p>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import {getUUID} from '@/utils';
import {Encrypt} from '@/utils/AESUtils';
import {isNavigationFailure} from 'vue-router';

export default {
    /** 初始化当前组件的响应式界面和表单状态. */
    data() {
        return {
            dataForm: {
                userName: '',
                password: '',
                uuid: '',
                captcha: '',
            },
            dataRule: {
                userName: [
                    {
                        required: true,
                        message: '帐号不能为空',
                        trigger: 'blur',
                    },
                ],
                password: [
                    {
                        required: true,
                        message: '密码不能为空',
                        trigger: 'blur',
                    },
                ],
                captcha: [
                    {
                        required: true,
                        message: '验证码不能为空',
                        trigger: 'blur',
                    },
                ],
            },
            captchaPath: '',
            loginSubmitting: false,
            changeAfterLogin: false,
        };
    },
    /** 创建组件时准备所需的依赖数据. */
    created() {
        this.getCaptcha();
    },
    methods: {
        /** 告知员工如何通过已授权管理员安全重置忘记的密码. */
        showPasswordRecovery() {
            this.$alert(
                '请联系单位的后台管理员核验个人身份并为你重置密码。拿到新密码后可在此登录并修改。',
                '忘记密码'
            );
        },
        /** 先通过既有图形验证码和当前密码登录, 再打开改密对话框. */
        preparePasswordChange() {
            this.changeAfterLogin = true;
        },
        // 提交表单
        dataFormSubmit() {
            if (this.loginSubmitting) {
                return;
            }
            this.$refs['dataForm'].validate((valid) => {
                if (valid) {
                    this.loginSubmitting = true;
                    Promise.resolve()
                        .then(async () =>
                            this.$http({
                                url: '/sys/login',
                                method: 'post',
                                loading: false,
                                showError: false,
                                data: {
                                    userName: this.dataForm.userName,
                                    password: await Encrypt(this.dataForm.password),
                                    uuid: this.dataForm.uuid,
                                    captcha: this.dataForm.captcha,
                                },
                            })
                        )
                        .then(({data}) => {
                            if (data && data.code === 0) {
                                const token = String(data.data || '').trim();
                                if (!token) {
                                    this.showLoginError('服务端未返回登录凭证');
                                    this.dataForm.captcha = '';
                                    this.getCaptcha();
                                    return;
                                }
                                this.$cookie.set('token', token);
                                if (this.$cookie.get('token') !== token) {
                                    this.showLoginError(
                                        '浏览器未能保存登录凭证，请检查 Cookie 设置'
                                    );
                                    this.dataForm.captcha = '';
                                    this.getCaptcha();
                                    return;
                                }
                                return this.$router
                                    .replace(
                                        this.changeAfterLogin
                                            ? {path: '/home', query: {changePassword: '1'}}
                                            : {path: '/home'}
                                    )
                                    .catch((error) => {
                                        if (!isNavigationFailure(error)) {
                                            throw error;
                                        }
                                    });
                            } else {
                                this.showLoginError(
                                    (data && data.msg) || '账号、密码或验证码不正确'
                                );
                                this.dataForm.captcha = '';
                                this.getCaptcha();
                            }
                        })
                        .catch((error) => {
                            const status = error.response && error.response.status;
                            const message =
                                error.code === 'SECURE_CONTEXT_REQUIRED'
                                    ? error.message
                                    : status
                                      ? `后台服务请求失败（HTTP ${status}）`
                                      : '无法连接后台服务，请确认服务已启动并使用端口 8888 访问';
                            this.showLoginError(message);
                            this.dataForm.captcha = '';
                            this.getCaptcha();
                        })
                        .finally(() => {
                            this.loginSubmitting = false;
                        });
                }
            });
        },
        // 显示唯一的登录错误提示 / Shows one concise login error message.
        showLoginError(message) {
            this.$message.closeAll();
            this.$message.error({
                message: `登录失败：${String(message).substring(0, 120)}`,
                showClose: true,
                duration: 5000,
            });
        },
        // 获取验证码
        getCaptcha() {
            this.dataForm.uuid = getUUID();
            this.captchaPath = this.$http.BASE_URL + `/captcha.jpg?uuid=${this.dataForm.uuid}`;
        },
    },
};
</script>

<style lang="scss">
body {
    background: #16231f;
    height: auto;
}

.site-wrapper.site-page--login {
    bottom: 0;
    left: 0;
    overflow: hidden;
    position: absolute;
    right: 0;
    top: 0;

    .site-content__wrapper {
        bottom: 0;
        left: 0;
        margin: 0;
        overflow-x: hidden;
        overflow-y: auto;
        padding: 0;
        position: absolute;
        right: 0;
        top: 0;
        background: linear-gradient(90deg, #16231f 0, #16231f 52%, #eeeae2 52%, #eeeae2 100%);
    }

    .site-content {
        align-items: center;
        display: grid;
        grid-template-columns: 52% 48%;
        grid-template-rows: 1fr auto;
        min-height: 100vh;
        padding: 0;
    }

    .brand-info {
        color: #fff;
        margin: 0;
    }

    .brand-info--intro {
        grid-column: 1;
        padding: 0 10vw;
    }

    .brand-info__kicker {
        color: #c8a96e;
        font-size: 11px;
        font-weight: 600;
        letter-spacing: 5px;
    }

    .brand-info__text {
        color: #fff;
        font-family: Georgia, 'Times New Roman', 'Microsoft YaHei', serif;
        font-size: 52px;
        font-weight: 500;
        letter-spacing: 8px;
        margin: 22px 0 18px;
    }

    .brand-info__intro {
        color: rgba(255, 255, 255, 0.58);
        font-size: 14px;
        line-height: 1.8;
        margin: 0;
        max-width: 430px;
    }

    .login-main {
        background: #fff;
        border-left: 4px solid #b88d4e;
        box-shadow: 0 24px 70px rgba(26, 33, 29, 0.18);
        grid-column: 2;
        margin: 0 auto;
        max-width: 430px;
        min-width: 340px;
        padding: 48px 52px 40px;
        width: 60%;

        .el-form {
            border: 0;
            padding: 0;
        }

        .el-input__inner {
            background: #f7f5f0;
            border: 0;
            border-bottom: 1px solid #cfc8bd;
            border-radius: 0;
            height: 46px;
        }

        .el-input__inner:focus {
            border-bottom-color: #a77b3f;
        }

        .el-button--primary {
            background: #17231f;
            border-color: #17231f;
            border-radius: 0;
            height: 46px;
            letter-spacing: 6px;
            margin-top: 10px;
        }
    }

    .login-title {
        color: #27332e;
        font-family: Georgia, 'Times New Roman', 'Microsoft YaHei', serif;
        font-size: 24px;
        font-weight: 500;
        letter-spacing: 3px;
        margin: 0 0 34px;
        text-align: left;
    }

    .login-captcha {
        align-items: center;
        display: flex;
        height: 46px;
        overflow: hidden;

        > img {
            cursor: pointer;
            height: 38px !important;
            width: 100% !important;
        }
    }

    .brand-info--footer {
        bottom: 22px;
        grid-column: 1 / 3;
        position: fixed;
        text-align: center;

        .brand-info__intro {
            max-width: none;
        }
    }

    @media (max-width: 900px) {
        .site-content__wrapper {
            background: #eeeae2;
        }

        .site-content {
            display: flex;
            justify-content: center;
        }

        .brand-info--intro {
            display: none;
        }

        .login-main {
            width: calc(100% - 48px);
        }
    }
}
</style>
