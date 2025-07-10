<template>
    <CardItem :background=backgroundImg>
        <template #head>
            <img />
        </template>
        <template #body>

            <el-row :gutter="20">
                <el-col :span="6" :offset="1">
                    <div class="grid-content ep-bg-purple">
                        <el-select v-model="value" value-key="id" placeholder="人机对战" @change="selectChange" style="width: 240px">
                            <el-option v-for="item in options" :key="item.id" :label="item.label" :value="item" />
                        </el-select>
                    </div>
                </el-col>
                <el-col :span="6" :offset="10">
                    <div class="grid-content ep-bg-purple">
                        <el-button @click="jump('chessgame', 'start-matching')">多人对战</el-button>
                    </div>
                </el-col>
            </el-row>

        </template>
    </CardItem>

</template>

<script lang="ts">
import CardItem from '@/components/CardItem.vue'
import route from '@/router/index'
import router from '@/router';
import { ref } from 'vue'
export default {
    components: {
        CardItem,
    },
    setup() {
        type Option = {
            id: number
            label: string
        }
        const value = ref < Option > ()
        const options = ref([
            { id: 1, label: '简单'},
            { id: 2, label: '困难'},
        ])
        function jumpPage(page: any) {
            route.push({ name: page })
        }
        function jump(page:any, type:any) {
            var data = { 'event': type }
            router.push({ name: page, state: { data } })
        }
        function selectChange(value: any) {
            console.log(value)
            var data = { 'event': 'startAI' , 'level': value.id }
            router.push({ name: 'chessgame', state: { data } })
        }
        return {
            jumpPage,
            jump,
            value,
            options,
            selectChange,
            backgroundImg: require('../../assets/gamemode.png')
        }
    }

}

</script>
<style scoped>
.el-row {
    margin-bottom: 20px;
}

.el-row:last-child {
    margin-bottom: 0;
}

.el-col {
    border-radius: 4px;
}

.grid-content {
    border-radius: 4px;
    min-height: 36px;
}

.el-button:hover {
    background: radial-gradient(circle at center, white 0%, black 60%, black 70%) !important;
    color: white !important;
    font-weight: bold;
    border-color: black !important;
}
</style>