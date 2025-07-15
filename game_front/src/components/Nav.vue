<template>
    <div style=" background-size:100% 100%;">
        <el-menu :default-active="route.currentRoute.name" class="el-menu-demo" mode="horizontal" :ellipsis="false"
            style="background-color: transparent;" @select="">
            <el-menu-item style="width: 5vw;margin-left: 10vw">
                <img src="../assets/logo.png" style="width: 5vw; background-color: transparent ;" alt="Element logo" />
            </el-menu-item>

            <el-menu-item index="0" style="margin-left: 10vw;color: #A9A9A9"
                @click="jumpPage('gamemode')">对战模式</el-menu-item>
            <el-menu-item index="1" style="margin-left: 3svw;color: #A9A9A9"
                @click="jumpPage('GameList')">对战列表</el-menu-item>
            <el-menu-item index="2" style="margin-left: 3svw;color: #A9A9A9"
                @click="jumpPage('RankList')">排行榜</el-menu-item>
            <el-menu-item index="3" style="margin-left: 3svw;color: #A9A9A9"
                @click="jumpPage('friendmodule')">好友</el-menu-item>

            <div class="flex-grow" />
            <el-sub-menu index="4" v-if="true">
                <template #title>{{ store.state.username }}</template>
                <el-menu-item index="4-1" @click="jumpPage('gamerinfo')">个人信息</el-menu-item>
                <el-menu-item index="4-2" @click="jumpPage('dataAnalysis')">数据分析</el-menu-item>
                <el-menu-item index="4-3" @click="logout">登出</el-menu-item>
            </el-sub-menu>
            <el-menu-item index="4" @click="jumpPage('login')" v-else>登录</el-menu-item>
        </el-menu>
    </div>
</template>
<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useStore } from "vuex"
import route from '../router/index'
import useWsStore from '@/store/WsSocket';

const activeIndex = ref('1')
const store = useStore()

const wsstore = useWsStore();


function logout() {
    store.commit("logout")
    localStorage.removeItem("jwt_token")
    wsstore.close()
    route.push({ name: 'login' })
}
function jumpPage(page) {
    route.push({ path: '/' + page })
}

</script>
<style scoped>
.flex-grow {
    flex-grow: 1;
}

.el-menu-demo>.el-menu-item {
    border-bottom: none;
    text-decoration: none;
}

.el-submenu__title:focus,
.el-submenu__title:hover {
    outline: 0 !important;
    color: black !important;
    background: transparent !important;
}

.el-menu-item:hover {
    outline: 0 !important;
    color: black !important;
    background: transparent !important;
}

.el-menu-item.is-active {
    color: black !important;
    background: transparent !important;
}
</style>