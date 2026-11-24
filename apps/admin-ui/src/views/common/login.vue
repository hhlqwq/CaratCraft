<template>
    <div class="site-wrapper site-page--login min300">
        <div class="site-content__wrapper">
            <div class="site-content">
                <div class="login-main">
                    <p class="login-brand">CaratCraft</p>
                    <h1 class="login-title">管理员登录</h1>
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
                                show-password
                                placeholder="密码"
                                aria-label="管理员密码"
                                autocomplete="current-password"
                            ></el-input>
                        </el-form-item>
                        <div class="login-password-actions">
                            <button type="button" @click="showPasswordRecovery" @keyup.enter.stop>
                                忘记密码
                            </button>
                            <button type="button" @click="showRegistrationHelp" @keyup.enter.stop>
                                注册用户
                            </button>
                        </div>
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
                                class="login-submit"
                                type="primary"
                                :loading="loginSubmitting"
                                :disabled="loginSubmitting"
                                @click="dataFormSubmit()"
                                style="width: 100%"
                                >登录</el-button
                            >
                        </el-form-item>
                    </el-form>
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
        };
    },
    /** 创建组件时准备所需的依赖数据. */
    created() {
        this.getCaptcha();
    },
    methods: {
        /** 打开员工验证码找回页面. */
        showPasswordRecovery() {
            this.$router.push({name: 'password-recovery'});
        },
        /** 提示新员工通过有开户权限的管理员获取账号. */
        showRegistrationHelp() {
            this.$alert(
                '请联系有开户权限的管理员创建员工账号。取得账号和初始密码后即可登录。',
                '注册用户'
            );
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
                                return this.$router.replace({path: '/home'}).catch((error) => {
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
.site-wrapper.site-page--login {
    min-width: 0;

    .site-content__wrapper {
        margin: 0;
        padding: 20px;
        display: grid;
        place-items: center;
    }

    .site-content {
        width: min(100%, 424px);
        padding: 0;
    }

    .login-main {
        width: 100%;
        padding: clamp(22px, 6vw, 36px);
        background: var(--studio-surface);
        border: 1px solid var(--studio-edge);
        border-radius: 8px;
    }

    .login-brand {
        margin: 0 0 5px;
        color: var(--studio-brand);
        font-size: 21px;
        font-weight: 650;
    }

    .login-title {
        margin: 0 0 25px;
        font-size: 17px;
        font-weight: 550;
    }

    .el-input__wrapper,
    .login-submit {
        min-height: 40px;
    }

    .login-password-actions {
        display: flex;
        gap: 4px 16px;
        justify-content: flex-end;
        margin: -9px 0 14px;

        button {
            border: 0;
            background: none;
            color: var(--studio-brand);
            font: inherit;
            cursor: pointer;
            min-height: 28px;

            &:hover,
            &:focus-visible {
                text-decoration: underline;
            }
        }
    }

    .login-captcha {
        display: flex;
        align-items: center;

        img {
            cursor: pointer;
            object-fit: cover;
            width: 100% !important;
        }
    }

    .el-form-item:last-child {
        margin-bottom: 0;
    }
}
</style>
