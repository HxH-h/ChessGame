import {Game_Obj} from './Game_Obj.js'


export class Chess extends Game_Obj{
    constructor(ctx,margin,gridSize){
        super()
        this.ctx=ctx
        this.margin=margin
        this.gridSize=gridSize
        this.lastX=0
        this.lastY=0
        this.curX=0
        this.curY=0
        
        this.chessAry=[
            [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
            [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
            [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
            [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
            [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
            [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
            [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
            [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
            [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
            [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
            [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
            [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
            [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
            [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
            [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
            [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
            [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
            [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
            [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
        ]
      
    }
    InitChessBoard(chessAry){
        this.chessAry=chessAry
    }
    update(){
        this.render()

    }
    //渲染
    render(){
        let x=0;
        let y=0;
        for(let i=0;i<19;i++){
            for(let j=0;j<19;j++){ 
                if(this.chessAry[i][j]%2===1){ 
                    x=this.margin+this.gridSize*j;
                    y=this.margin+this.gridSize*i;
                    this.drawChess(x,y,'black','white',this.chessAry[i][j])
                }else if(this.chessAry[i][j]%2===0 && this.chessAry[i][j]!==0){
                    x=this.margin+this.gridSize*j;
                    y=this.margin+this.gridSize*i;
                    this.drawChess(x,y,'white','black',this.chessAry[i][j])
                }else if(this.chessAry[i][j]=== -1){
                    x=this.margin+this.gridSize*j;
                    y=this.margin+this.gridSize*i;

                    this.drawChessPossible(x,y)
                }
            }
          
        }
    }
    //获取当前位置并落子
    setChess(role){
        if(role<1||role>2){
            return
        }
    
        if(this.chessAry[this.curY][this.curX]!=0 &&
            this.chessAry[this.curY][this.curX]!= -1
        ){
            return
        }
        var pos={
            x: this.curX,
            y: this.curY
        }

        return pos
    }
    drawChess(x,y,color,fontcolor,num){
        this.ctx.beginPath();
        this.ctx.arc(x, y, 6, 0, Math.PI * 2);
        this.ctx.fillStyle = color;
        this.ctx.fill();
        
        // 在棋子中央写入数字
        this.ctx.font = "10px Arial";
        this.ctx.fillStyle = fontcolor;
        this.ctx.textAlign = 'center';
        this.ctx.fillText(num, x, y + 5);
        
    }
    ChessPossible(x,y){
        if(x<0||y<0||x>18||y>18){
            return ;
        }
        //该位置没有子则推荐
        if(this.chessAry[y][x]===0&&(x!=this.lastX||y!=this.lastY)){
            //若在推荐位置落子则不撤销
            if(this.chessAry[this.lastY][this.lastX]=== -1){
                this.chessAry[this.lastY][this.lastX]=0
            }
            //更新当前位置
            this.curX=x
            this.curY=y
            this.chessAry[y][x]= -1
            this.lastX=x
            this.lastY=y
        }
    }
    drawChessPossible(x,y){
        this.ctx.beginPath();//开始绘制
        this.ctx.arc(x,y,5,0,2*Math.PI);
        this.ctx.strokeStyle="white";
        this.ctx.stroke()
    }
}