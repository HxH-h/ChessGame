<template>
    <div id="building">

        <el-container style="margin-top: 30vh;margin-left: 57vw;
                      ">
            <el-header style="height: 20vh;"></el-header>
            <el-card style="max-width: 360px; text-align: center;">
                <template #header>
                    <div class="card-header">
                        <span>玩家登录</span>
                    </div>
                </template>
                <el-form ref="ruleFormRef" :model="ruleForm" status-icon :rules="rules" label-width="auto"
                    class="demo-ruleForm">
                    <el-form-item label="用户名" prop="username" class="item">
                        <el-input v-model="ruleForm.username" />
                    </el-form-item>

                    <el-form-item label="密码" prop="password" class="item">
                        <el-input v-model="ruleForm.password" type="password" autocomplete="off" />
                    </el-form-item>


                    <el-form-item>
                        <el-button type="primary" @click="submitForm(ruleFormRef)"
                            style="position: relative; left: 6vw;background-color: transparent;">
                            Submit
                        </el-button>
                        <el-button type="primary" @click="toRegister()"
                            style="position: relative; left: 7vw; background-color: transparent;">
                            Register
                        </el-button>
                    </el-form-item>
                </el-form>
            </el-card>
        </el-container>
    </div>
</template>

<script lang="ts" setup>

import {onMounted, reactive, ref , onUnmounted } from 'vue'
import { useStore } from 'vuex';
import type { FormInstance, FormRules } from 'element-plus'
import route from '@/router/index';
import { post, get } from '@/ts/request';
const ruleFormRef = ref<FormInstance>()
import { ElMessage } from 'element-plus'
import { LOGIN_URL , PLAYER_INFO_URL } from '@/ts/url';


const store = useStore();

// 支持enter 提交表单
onMounted(() => {
    window.addEventListener('keydown' , (event: any) => {
        if(event.key === 'Enter'){
            submitForm(ruleFormRef.value)
        }
    })
})
onUnmounted(() => {
    window.removeEventListener('keydown' , (event: any) => {
        if(event.key === 'Enter'){
            submitForm(ruleFormRef.value)
        }
    })
})

const validatePass = (rule: any, value: any, callback: any) => {
    if (value === '' || !value) {
        callback(new Error('Please input the password'))
    } else {

        callback()
    }
}

const validateName = (rule: any, value: any, callback: any) => {
    if (value === '') {
        callback(new Error('Please input the user name'))
    } else {
        callback()
    }
}


const ruleForm = reactive({
    password: '',
    username: '',
})

const rules = reactive<FormRules<typeof ruleForm>>({
    username: [{ validator: validateName, trigger: 'blur' }],
    password: [{ validator: validatePass, trigger: 'blur' }],
})

const submitForm = (formEl: FormInstance | undefined) => {
    if (!formEl) return
    formEl.validate(async (valid) => {

        if (valid) {
            let response = await post(LOGIN_URL, '', ruleForm)
            if (response.code != 1001){
                ElMessage({
                    message: response.msg,
                    grouping: true,
                    offset: 50,
                    type: 'error',
                })
                return
            }
            let playerInfo = await get(PLAYER_INFO_URL, response.data.accessToken)  

            store.commit('setToken', response.data)
            store.commit('setUserInfo', playerInfo.data)
            route.push({ name: 'main' })
        } else {
            console.log('error submit!')
        }
    })
}

function toRegister() {
    route.push({ name: 'register' })
}


</script>

<style scoped>
html,
body {
    margin: 0;
    padding: 0;
}

#building {
    background: url("../../assets/login.png");
    width: 100%;
    height: 100%;
    position: fixed;
    background-size: 100% 100%;
}

.demo-ruleForm {
    max-width: 300px;
    background-color: transparent;

}

.el-card {
    background-color: transparent;
}
.item .el-form-item__label{
    font-family:LiSu;
    font-size: 2vh;
}
.el-button{
    border-color: transparent !important;
}
</style>