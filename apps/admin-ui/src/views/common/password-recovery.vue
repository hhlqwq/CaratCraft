<template>
    <main class="password-recovery">
        <section>
            <h1>找回密码</h1>
            <p>验证码会发到管理员为你登记的手机号或邮箱。收不到时，请联系管理员。</p>
            <el-form @submit.prevent="submit" aria-label="找回员工密码">
                <el-form-item label="用户名">
                    <el-input v-model.trim="userName" autocomplete="username" maxlength="50" />
                </el-form-item>
                <el-form-item label="接收方式">
                    <el-radio-group v-model="channel">
                        <el-radio value="email">邮箱</el-radio>
                        <el-radio value="mobile">手机号</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="图形验证码">
                    <div class="captcha-row">
                        <el-input v-model.trim="captcha" maxlength="8" />
                        <button class="captcha-refresh" type="button" @click="refreshCaptcha">
                            <img :src="captchaPath" alt="刷新图形验证码" />
                        </button>
                        <el-button :loading="sending" @click="send">发送验证码</el-button>
                    </div>
                </el-form-item>
                <el-form-item label="收到的验证码">
                    <el-input
                        v-model.trim="code"
                        inputmode="numeric"
                        autocomplete="one-time-code"
                        maxlength="6"
                    />
                </el-form-item>
                <el-form-item label="新密码">
                    <el-input
                        v-model="password"
                        type="password"
                        autocomplete="new-password"
                        maxlength="32"
                        placeholder="6到32位，不含空格"
                    />
                </el-form-item>
                <el-form-item label="确认新密码">
                    <el-input
                        v-model="confirmPassword"
                        type="password"
                        autocomplete="new-password"
                        maxlength="32"
                    />
                </el-form-item>
                <p v-if="message" role="status">{{ message }}</p>
                <div class="recovery-actions">
                    <el-button @click="$router.replace({name: 'login'})">返回登录</el-button>
                    <el-button type="primary" native-type="submit" :loading="saving"
                        >重置密码</el-button
                    >
                </div>
            </el-form>
        </section>
    </main>
</template>

<script>
import {getUUID} from '@/utils';

export default {
    /** 保存本次找回的输入, 离开页面后不保留. */
    data() {
        return {
            userName: '',
            channel: 'email',
            uuid: '',
            captcha: '',
            captchaPath: '',
            code: '',
            password: '',
            confirmPassword: '',
            message: '',
            sending: false,
            saving: false,
        };
    },
    /** 打开找回页时加载独立的图形验证码. */
    created() {
        this.refreshCaptcha();
    },
    methods: {
        /** 更换本次图形验证码, 防止复用已消费的挑战. */
        refreshCaptcha() {
            this.uuid = getUUID();
            this.captcha = '';
            this.captchaPath = this.$http.BASE_URL + '/captcha.jpg?uuid=' + this.uuid;
        },
        /** 通过图形验证码请求发送六位密码找回码. */
        async send() {
            if (this.sending || this.saving) {
                return;
            }
            if (!this.userName || !this.captcha) {
                this.message = '请填写用户名和图形验证码。';
                return;
            }
            this.sending = true;
            try {
                const {data} = await this.$http({
                    url: '/sys/password-recovery/send',
                    method: 'post',
                    showError: false,
                    data: {
                        userName: this.userName,
                        channel: this.channel,
                        uuid: this.uuid,
                        captcha: this.captcha,
                    },
                });
                this.message = data?.msg || '请稍后重试或联系管理员。';
            } catch {
                this.message = '暂时无法发送，请稍后重试或联系管理员。';
            } finally {
                this.refreshCaptcha();
                this.sending = false;
            }
        },
        /** 验证码消费和密码更新成功后返回登录. */
        async submit() {
            if (this.saving || this.sending) {
                return;
            }
            if (!this.userName || !/^[0-9]{6}$/.test(this.code)) {
                this.message = '请填写用户名和收到的六位验证码。';
                return;
            }
            if (!/^(?!123456$)[!-~]{6,32}$/.test(this.password)) {
                this.message = '请设置6到32位不含空格的新密码，且不能使用统一初始密码。';
                return;
            }
            if (this.password !== this.confirmPassword) {
                this.message = '两次填写的新密码不一致。';
                return;
            }
            this.saving = true;
            try {
                const {data} = await this.$http({
                    url: '/sys/password-recovery/reset',
                    method: 'post',
                    showError: false,
                    data: {
                        userName: this.userName,
                        channel: this.channel,
                        code: this.code,
                        newPassword: this.password,
                    },
                });
                if (data?.code === 0) {
                    this.$message.success('密码已更新，请重新登录');
                    await this.$router.replace({name: 'login'});
                } else {
                    this.message = data?.msg || '重置失败，请联系管理员。';
                }
            } catch {
                this.message = '暂时无法重置，请稍后再试。';
            } finally {
                this.saving = false;
            }
        },
    },
};
</script>

<style scoped>
.password-recovery {
    min-height: 100vh;
    background: #eeeae2;
    padding: 40px 16px;
}
.password-recovery section {
    max-width: 560px;
    margin: 0 auto;
    padding: 24px;
    border-left: 4px solid #b88d4e;
    background: white;
    color: #27332e;
}
.password-recovery p {
    line-height: 1.6;
}
.captcha-row {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
}
.captcha-row .el-input {
    width: 102px;
}
.captcha-refresh {
    padding: 0;
    border: none;
    background: transparent;
    cursor: pointer;
}
.captcha-refresh img {
    display: block;
    width: 96px;
    height: 32px;
}
.recovery-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
}
</style>
