<template>
    <CardItem :width=80 :height=90>
        <template #body>
            <div class="layout-container">
                <el-row>
                    <el-col :span="4" class="left-column">
                        <div class="left-area">
                            <!-- 左上区域 -->
                            <CardItem :width=12 :height=33>
                                <template #head>
                                    <img :src=store.state.photo style="width: 100%" />
                                </template>
                                <template #body>
                                    <div class="username">{{ store.state.username }}</div>
                                    <div class="rank">{{ store.state.rank }}</div>
                                </template>

                            </CardItem>
                            <!-- 间隔 -->
                            <div class="left-gap">
                                <el-button type="danger" @click="giveup()">认输</el-button>
                            </div>
                            <!-- 左下区域 -->
                            <div class="left-bottom">
                                <CardItem :width=12 :height=33>
                                    <template #head>
                                        <img :src=store.state.gamer.photo style="width: 100%" />
                                    </template>
                                    <template #body>
                                        <div class="username">{{ store.state.gamer.opponent_name }}</div>
                                        <div class="rank">{{ store.state.gamer.opponent_score }}</div>
                                    </template>
                                </CardItem>
                            </div>
                        </div>
                    </el-col>
                    <el-col :span="14" class="center-column">
                        <el-countdown title="倒计时" :value="countDown" />
                        <canvas id="can" ref="canvas" :width="canvasSize" :height="canvasSize"></canvas>
                    </el-col>
                    <el-col :span="6" class="right-column">

                    </el-col>
                </el-row>
            </div>
        </template>
    </CardItem>

</template>
<script setup>

import router from '@/router';
import { onMounted, onUnmounted, ref, watch } from 'vue';
import CardItem from '@/components/CardItem.vue'
import { ChessBoard } from '@/componetjs/ChessBoard.js'
import { Chess } from '@/componetjs/Chess.js'
import { useStore } from "vuex"
import route from '@/router/index'
import useWsStore from '@/store/WsSocket';
import { ElMessage, ElLoading } from 'element-plus'
import { transform } from '@/ts/utils'



components: {
    CardItem
}

//棋盘参数
const canvasSize = 600;
const size = 19;
const margin = 30;
const gridSize = (canvasSize - 2 * margin) / (size - 1);
const canvas = ref(null);
const button = ref("开始匹配")
const timer = ref()
const countDown = ref(Date.now() + 1000 * 60 * 60 * 7)
//获取棋盘原点
var rect = null;
//获取棋子对象
var Chesses = null;

const store = useStore()

var pagedata = history.state.data
console.log(pagedata.event)

// 获取websocket
const wsstore = useWsStore();

// 判断当前页面 匹配中 false 还是 游戏中 true
var gamestatus = false

// 如果是匹配 则匹配时需要展示匹配动画
var loadingInstance


onMounted(() => {
    if (!pagedata || pagedata.event != "start-matching" && pagedata.event != "startAI" && pagedata.event != "reconnect") {
        // 没有参数 跳转首页
        console.log("no data")
        router.push({ name: 'gamemode' })
        return
    }
    // 初始化渲染
    render();
    // 判断是 断线重连 还是 新游戏
    if (pagedata.event == 'reconnect') {
        if (loadingInstance) loadingInstance.close()
        recovery(pagedata.reconnect)
    }



    // 开启监听
    watch(() => wsstore.msg, (newValue, oldValue) => {
        const data = JSON.parse(newValue)
        //判断消息类型
        if (data.event === "match_success") {
            // 游戏开始
            if(loadingInstance){
                loadingInstance.close()                
            }
            gamestatus = true
            let msg = JSON.parse(data.match_success)
            store.state.gamer.turn = msg.turn
            store.state.gamer.opponent_name = msg.opponent_name
            store.state.gamer.opponent_score = msg.opponent_score
            store.state.gamer.opponent_photo = msg.opponent_photo
        } else if (data.event === "InitChess") {
            console.log(data)
            Chesses.InitChessBoard(data.InitChess)
        } else if (data.event === "start_turn") {
            if (data.start_turn == store.state.gamer.turn) {
                ElMessage({
                    message: 'It`s your turn',
                    type: 'success',
                })
                document.getElementById("can").onclick = ClickHandle
            } else {
                document.getElementById("can").onclick = DoNothing
            }
        } else if (data.event === "synchronization") {
            Chesses.InitChessBoard(data.synchronization)
        } else if (data.event === "end") {
            console.log("end")
            var ret = ''
            window.removeEventListener('mousemove', MoveHandle)
            if (data.end == store.state.gamer.turn) {
                ret = 'victory'
            } else {
                ret = 'defeat'
            }
            timer.value = setTimeout(() => {
                alert(ret)
                gameover()
                route.push({ name: 'gamemode' })
            }, 150)

        } else if (data.event === "countDown") {
            countDown.value = data.countDown
        } else if (data.event === "timeout") {
            var ret = ''
            window.removeEventListener('mousemove', MoveHandle)
            if (data.timeout == store.state.gamer.turn) {
                ret = 'your time out ,your defeat'
            } else {
                ret = 'your opponter is time out ,you victory'

            }
            timer.value = setTimeout(() => {
                alert(ret)
                gameover()
                route.push({ name: 'gamemode' })
            }, 150)

        } else if (data.event === "giveUp") {
            var ret = ''
            window.removeEventListener('mousemove', MoveHandle)
            if (data.giveUp == store.state.gamer.turn) {
                ret = 'you give up ,you defeat'
            } else {
                ret = 'your opponter give up ,you victory'
            }
            timer.value = setTimeout(() => {
                alert(ret)
                gameover()
                route.push({ name: 'gamemode' })
            }, 150)
        } else if (data.event === "reconnect") {
            if (loadingInstance) loadingInstance.close()
            recovery(data.reconnect)
        }
    })

    if (pagedata.event == 'start-matching') {
        const options = {
            text: '匹配中',
            background: 'rgba(255,255,255,0.9)'
        }
        // 如果是匹配 则匹配时需要展示匹配动画
        loadingInstance = ElLoading.service(options)
    }

    // 发送开始请求
    if (pagedata.event == 'startAI'){
        wsstore.sendMsg({ event: pagedata.event , level: pagedata.level })
    }else{
        wsstore.sendMsg({ event: pagedata.event })
    }

})



// 渲染棋盘
function render() {
    const ctx = canvas.value.getContext('2d');
    rect = canvas.value.getBoundingClientRect();
    //棋盘
    new ChessBoard(ctx, canvasSize, size, margin, gridSize)
    Chesses = new Chess(ctx, margin, gridSize)
    window.addEventListener('mousemove', MoveHandle);
}
//检测鼠标移动，并选择四角中的一角
function MoveHandle(e) {
    var x = e.clientX;
    var y = e.clientY;
    x -= rect.left;
    y -= rect.top;
    x = (x - margin) / gridSize
    y = (y - margin) / gridSize

    var dx = x - parseInt(x)
    var dy = y - parseInt(y)
    if (dx > 0.5 && dy > 0.5) {
        x = parseInt(x) + 1;
        y = parseInt(y) + 1;

    } else if (dx < 0.5 && dy > 0.5) {
        x = parseInt(x);
        y = parseInt(y) + 1;

    } else if (dx > 0.5 && dy < 0.5) {
        x = parseInt(x) + 1;
        y = parseInt(y);

    } else {
        x = parseInt(x);
        y = parseInt(y);
    }
    if (x < 0 || y < 0 || x > 18 || y > 18) {
        return;
    }
    Chesses.ChessPossible(x, y)
}

function ClickHandle(e) {

    //下棋
    var pos = Chesses.setChess(1);

    if (!pos) {
        ElMessage.error('this position already has chess')
        return
    }
    wsstore.sendMsg({
        event: "down",
        position: pos
    })

}
function DoNothing() {
    ElMessage.error('It\'s not your turn')
}

function recovery(reconnect) {
    // 更新为游戏状态
    gamestatus = true
    // 断线重连需要同步棋盘信息
    store.state.gamer.turn = reconnect.playerturn
    var curTurn = reconnect.curturn
    timer.value = setTimeout(() => {
        Chesses.InitChessBoard(transform(reconnect.chess))
    }, 150)
    // 判断是否为落子回合
    if (curTurn == store.state.gamer.turn) {
        ElMessage({
            message: 'It`s your turn',
            type: 'success',
        })
        document.getElementById("can").onclick = ClickHandle
    } else {
        document.getElementById("can").onclick = DoNothing
    }
}
function gameover() {
    store.state.gamer.turn = ''
    store.state.gamer.opponent_name = ''
    store.state.gamer.opponent_level = ''
    store.state.gamer.opponent_rank = ''
    store.state.gamer.status = ''
}

function giveup() {
    wsstore.sendMsg({
        event: "giveUp"
    })
}



onUnmounted(() => {
    if (gamestatus) {
        giveup()
    } else {
        wsstore.sendMsg({ event: "stop-matching" })
        if (loadingInstance) {
            loadingInstance.close()
        }
    }
    // 页面卸载时，取消对鼠标的监听
    window.removeEventListener('mousemove', MoveHandle)

})
</script>
<style scoped>
.layout-container {
    height: 100%;

    display: flex;
    flex-direction: column;
}

.left-column {
    display: flex;
    flex-direction: column;
    height: 100%;
    background-color: #f5f7fa;
}

.left-area {
    display: flex;
    flex-direction: column;
    height: 100%;
}

.left-top,
.left-bottom {
    flex: 1;
    padding: 10px;
    text-align: center;
}

.left-top {
    background-color: #e6f7ff;
    margin-bottom: 10px;
    /* 添加底部边距 */
}

.left-bottom {
    background-color: #d9e8ff;
    margin-top: 10px;
    /* 添加顶部边距 */
}

.large-gap {
    flex: 2;
    /* 较大的间隔，占据剩余空间的两倍 */
    background-color: transparent;
}

.center-column {
    height: 100%;
    background-color: #eef1f6;
    padding: 10px;
    text-align: center;
}

.right-column {
    height: 100%;
    background-color: #f0f9eb;
    padding: 10px;
    text-align: center;
}
</style>